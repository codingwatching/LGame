cmake_minimum_required(VERSION 3.15)
project(MyPS5App C)

set(CMAKE_C_STANDARD 11)
set(CMAKE_C_STANDARD_REQUIRED ON)
add_compile_options(-O3)

# 检查 PS5_PAYLOAD_SDK 环境变量
if(NOT DEFINED ENV{PS5_PAYLOAD_SDK})
    message(FATAL_ERROR "未检测到 PS5_PAYLOAD_SDK 环境变量，请设置 PS5_PAYLOAD_SDK 指向 PS5 工具链目录")
endif()

set(PS5_PAYLOAD_SDK $ENV{PS5_PAYLOAD_SDK})

# 工具链文件
set(PS5_TOOLCHAIN_FILE "${PS5_PAYLOAD_SDK}/toolchain-ps5.cmake")
if(EXISTS "${PS5_TOOLCHAIN_FILE}")
    set(CMAKE_TOOLCHAIN_FILE "${PS5_TOOLCHAIN_FILE}" CACHE FILEPATH "PS5 toolchain file")
    message(STATUS "使用的 PS5 toolchain 路径: ${PS5_TOOLCHAIN_FILE}")
else()
    message(FATAL_ERROR "未找到 toolchain-ps5.cmake，请检查 PS5_PAYLOAD_SDK 路径是否正确")
endif()

# 源码目录
if(NOT DEFINED SRC_DIR)
    set(SRC_DIR ${CMAKE_SOURCE_DIR}/src)
endif()

# 入口文件 all.c
set(MAIN_FILE ${SRC_DIR}/all.c)
if(NOT EXISTS ${MAIN_FILE})
    message(FATAL_ERROR "未找到入口文件 all.c")
endif()

include(FetchContent)

# 强制 SDL 系列库静态编译
set(SDL_SHARED OFF CACHE BOOL "" FORCE)
set(SDL_STATIC ON CACHE BOOL "" FORCE)
set(SDL2IMAGE_SHARED OFF CACHE BOOL "" FORCE)
set(SDL2IMAGE_STATIC ON CACHE BOOL "" FORCE)
set(SDL2MIXER_SHARED OFF CACHE BOOL "" FORCE)
set(SDL2MIXER_STATIC ON CACHE BOOL "" FORCE)

set(SDL_TEST OFF CACHE BOOL "" FORCE)
set(FETCHCONTENT_BASE_DIR ${CMAKE_BINARY_DIR}/third_party)

# 限定搜索路径到 PS5_PAYLOAD_SDK 根目录和 portlibs
list(APPEND CMAKE_PREFIX_PATH "${PS5_PAYLOAD_SDK}")
list(APPEND CMAKE_PREFIX_PATH "${PS5_PAYLOAD_SDK}/portlibs")

# SDL2 for PS5
find_package(SDL2 QUIET)
if(SDL2_FOUND AND SDL2_INCLUDE_DIRS MATCHES "${PS5_PAYLOAD_SDK}")
    message(STATUS "使用 PS5_PAYLOAD_SDK 中的 SDL2 库")
    add_library(SDL2_PS5::SDL2 ALIAS SDL2::SDL2)
else()
    message(STATUS "未找到 PS5_PAYLOAD_SDK SDL2，开始下载 SDL-PS5")
    FetchContent_Declare(
      SDL2_PS5
      GIT_REPOSITORY https://github.com/ps5-payload-dev/SDL.git
      GIT_TAG master
      SOURCE_DIR ${FETCHCONTENT_BASE_DIR}/sdl2_ps5-src
      BINARY_DIR ${FETCHCONTENT_BASE_DIR}/sdl2_ps5-build
    )
    FetchContent_MakeAvailable(SDL2_PS5)
    add_library(SDL2_PS5::SDL2 ALIAS SDL2::SDL2)
endif()

# SDL2_image
set(SDL2IMAGE_AVIF OFF CACHE BOOL "" FORCE)
set(SDL2IMAGE_WEBP OFF CACHE BOOL "" FORCE)
set(SDL2IMAGE_TIF OFF CACHE BOOL "" FORCE)
set(SDL2IMAGE_VENDORED OFF CACHE BOOL "" FORCE)
find_package(SDL2_image QUIET)
if(SDL2_IMAGE_FOUND)
    message(STATUS "使用本地 SDL2_image 库")
    add_library(SDL2_PS5::SDL2_image ALIAS SDL2_image::SDL2_image)
else()
    message(STATUS "未找到本地 SDL2_image，开始下载官方 SDL2_image")
    FetchContent_Declare(
      SDL2_image_PS5
      GIT_REPOSITORY https://github.com/libsdl-org/SDL_image.git
      GIT_TAG release-2.8.8
      SOURCE_DIR ${FETCHCONTENT_BASE_DIR}/sdl2_image_ps5-src
      BINARY_DIR ${FETCHCONTENT_BASE_DIR}/sdl2_image_ps5-build
    )
    FetchContent_MakeAvailable(SDL2_image_PS5)
    add_library(SDL2_PS5::SDL2_image ALIAS SDL2_image::SDL2_image)
endif()

# SDL2_mixer
set(SDL2MIXER_WAVPACK OFF CACHE BOOL "" FORCE)
find_package(SDL2_mixer QUIET)
if(SDL2_MIXER_FOUND)
    message(STATUS "使用本地 SDL2_mixer 库")
    add_library(SDL2_PS5::SDL2_mixer ALIAS SDL2_mixer::SDL2_mixer)
else()
    message(STATUS "未找到本地 SDL2_mixer，开始下载官方 SDL2_mixer")
    FetchContent_Declare(
      SDL2_mixer_PS5
      GIT_REPOSITORY https://github.com/libsdl-org/SDL_mixer.git
      GIT_TAG release-2.8.1
      SOURCE_DIR ${FETCHCONTENT_BASE_DIR}/sdl2_mixer_ps5-src
      BINARY_DIR ${FETCHCONTENT_BASE_DIR}/sdl2_mixer_ps5-build
    )
    FetchContent_MakeAvailable(SDL2_mixer_PS5)
    add_library(SDL2_PS5::SDL2_mixer ALIAS SDL2_mixer::SDL2_mixer)
endif()

# 第三方依赖库 (PS5 portlibs 或 lib)
find_library(PNG_LIB NAMES png libpng REQUIRED PATHS "${PS5_PAYLOAD_SDK}/lib" "${PS5_PAYLOAD_SDK}/portlibs/lib")
find_library(JPEG_LIB NAMES jpeg libjpeg REQUIRED PATHS "${PS5_PAYLOAD_SDK}/lib" "${PS5_PAYLOAD_SDK}/portlibs/lib")
find_library(ZLIB_LIB NAMES z REQUIRED PATHS "${PS5_PAYLOAD_SDK}/lib" "${PS5_PAYLOAD_SDK}/portlibs/lib")
find_library(OGG_LIB NAMES ogg REQUIRED PATHS "${PS5_PAYLOAD_SDK}/lib" "${PS5_PAYLOAD_SDK}/portlibs/lib")
find_library(VORBIS_LIB NAMES vorbis REQUIRED PATHS "${PS5_PAYLOAD_SDK}/lib" "${PS5_PAYLOAD_SDK}/portlibs/lib")
find_library(MPG123_LIB NAMES mpg123 REQUIRED PATHS "${PS5_PAYLOAD_SDK}/lib" "${PS5_PAYLOAD_SDK}/portlibs/lib")

# 全局定义 pure_sdl 宏
add_compile_definitions(PURE_SDL=1)

# 构建 PS5 可执行文件，只编译 all.c
add_executable(MyPS5App ${MAIN_FILE})

# 静态链接 SDL 和依赖库
target_link_libraries(MyPS5App
    SDL2_PS5::SDL2
    SDL2_PS5::SDL2_image
    SDL2_PS5::SDL2_mixer
    ${PNG_LIB}
    ${JPEG_LIB}
    ${ZLIB_LIB}
    ${OGG_LIB}
    ${VORBIS_LIB}
    ${MPG123_LIB}
    ScePad_stub
    SceAudio_stub
    SceVideoOut_stub
    SceSystemService_stub
)
