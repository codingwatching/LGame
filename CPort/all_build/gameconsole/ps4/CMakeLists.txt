cmake_minimum_required(VERSION 3.15)
project(MyPS4App C)

set(CMAKE_C_STANDARD 11)
set(CMAKE_C_STANDARD_REQUIRED ON)
add_compile_options(-O3)

# 指定交叉编译目标为 PS4 (OpenOrbis)
set(CMAKE_SYSTEM_NAME Orbis)
set(CMAKE_SYSTEM_PROCESSOR x86_64)

# 检查 PS4SDK 环境变量
if(NOT DEFINED ENV{PS4SDK})
    message(FATAL_ERROR "未检测到 PS4SDK 环境变量，请设置 PS4SDK 指向 OpenOrbis 工具链目录")
endif()

set(PS4SDK $ENV{PS4SDK})

# 工具链文件
set(PS4_TOOLCHAIN_FILE "${PS4SDK}/share/toolchain/ps4.toolchain.cmake")
if(EXISTS "${PS4_TOOLCHAIN_FILE}")
    set(CMAKE_TOOLCHAIN_FILE "${PS4_TOOLCHAIN_FILE}" CACHE FILEPATH "PS4 toolchain file")
    message(STATUS "使用的 PS4 toolchain 路径: ${PS4_TOOLCHAIN_FILE}")
else()
    set(PS4_TOOLCHAIN_FILE_ALT "${PS4SDK}/share/ps4.toolchain.cmake")
    if(EXISTS "${PS4_TOOLCHAIN_FILE_ALT}")
        set(CMAKE_TOOLCHAIN_FILE "${PS4_TOOLCHAIN_FILE_ALT}" CACHE FILEPATH "PS4 toolchain file (alt)")
        message(STATUS "使用的 PS4 toolchain 路径: ${PS4_TOOLCHAIN_FILE_ALT}")
    else()
        message(FATAL_ERROR "未找到 ps4.toolchain.cmake，请检查 PS4SDK 路径是否正确")
    endif()
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

# 禁止SDL2编译test文件夹 
set(SDL_TEST OFF CACHE BOOL "" FORCE)

set(FETCHCONTENT_BASE_DIR ${CMAKE_BINARY_DIR}/third_party)

# 限定搜索路径到 PS4SDK，避免误用主机 SDL2
set(CMAKE_PREFIX_PATH "${PS4SDK}/arm-orbis-eabi" ${CMAKE_PREFIX_PATH})

# SDL2 for PS4
find_package(SDL2 QUIET)
if(SDL2_FOUND AND SDL2_INCLUDE_DIRS MATCHES "${PS4SDK}")
    message(STATUS "使用 PS4SDK 中的 SDL2 库")
    add_library(SDL2_PS4::SDL2 ALIAS SDL2::SDL2-static)
else()
    message(STATUS "未找到 PS4SDK SDL2，开始下载 SDL-PS4")
    FetchContent_Declare(
      SDL2_PS4
      GIT_REPOSITORY https://github.com/OpenOrbis/SDL-PS4.git
      GIT_TAG master
      SOURCE_DIR ${FETCHCONTENT_BASE_DIR}/sdl2_ps4-src
      BINARY_DIR ${FETCHCONTENT_BASE_DIR}/sdl2_ps4-build
    )
    FetchContent_MakeAvailable(SDL2_PS4)
    add_library(SDL2_PS4::SDL2 ALIAS SDL2::SDL2-static)
endif()

# SDL2_image
set(SDL2IMAGE_AVIF OFF CACHE BOOL "" FORCE)
set(SDL2IMAGE_WEBP OFF CACHE BOOL "" FORCE)
set(SDL2IMAGE_TIF OFF CACHE BOOL "" FORCE)
set(SDL2IMAGE_VENDORED OFF CACHE BOOL "" FORCE)
find_package(SDL2_image QUIET)
if(SDL2_IMAGE_FOUND)
    message(STATUS "使用本地 SDL2_image 库")
    add_library(SDL2_PS4::SDL2_image ALIAS SDL2_image::SDL2_image-static)
else()
    message(STATUS "未找到本地 SDL2_image，开始下载")
    FetchContent_Declare(
      SDL2_image_PS4
      GIT_REPOSITORY https://github.com/libsdl-org/SDL_image.git
      GIT_TAG release-2.8.8
      SOURCE_DIR ${FETCHCONTENT_BASE_DIR}/sdl2_image_ps4-src
      BINARY_DIR ${FETCHCONTENT_BASE_DIR}/sdl2_image_ps4-build
    )
    FetchContent_MakeAvailable(SDL2_image_PS4)
    add_library(SDL2_PS4::SDL2_image ALIAS SDL2_image::SDL2_image-static)
endif()

# SDL2_mixer
set(SDL2MIXER_WAVPACK OFF CACHE BOOL "" FORCE)
find_package(SDL2_mixer QUIET)
if(SDL2_MIXER_FOUND)
    message(STATUS "使用本地 SDL2_mixer 库")
    add_library(SDL2_PS4::SDL2_mixer ALIAS SDL2_mixer::SDL2_mixer-static)
else()
    message(STATUS "未找到本地 SDL2_mixer，开始下载")
    FetchContent_Declare(
      SDL2_mixer_PS4
      GIT_REPOSITORY https://github.com/libsdl-org/SDL_mixer.git
      GIT_TAG release-2.8.1
      SOURCE_DIR ${FETCHCONTENT_BASE_DIR}/sdl2_mixer_ps4-src
      BINARY_DIR ${FETCHCONTENT_BASE_DIR}/sdl2_mixer_ps4-build
    )
    FetchContent_MakeAvailable(SDL2_mixer_PS4)
    add_library(SDL2_PS4::SDL2_mixer ALIAS SDL2_mixer::SDL2_mixer-static)
endif()

# 第三方依赖库 (PS4 portlibs)
find_library(PNG_LIB png REQUIRED PATHS "${PS4SDK}/arm-orbis-eabi/lib")
find_library(JPEG_LIB jpeg REQUIRED PATHS "${PS4SDK}/arm-orbis-eabi/lib")
find_library(ZLIB_LIB z REQUIRED PATHS "${PS4SDK}/arm-orbis-eabi/lib")
find_library(OGG_LIB ogg REQUIRED PATHS "${PS4SDK}/arm-orbis-eabi/lib")
find_library(VORBIS_LIB vorbis REQUIRED PATHS "${PS4SDK}/arm-orbis-eabi/lib")
find_library(MPG123_LIB mpg123 REQUIRED PATHS "${PS4SDK}/arm-orbis-eabi/lib")

# 全局定义 pure_sdl 宏
add_compile_definitions(PURE_SDL=1)

# 构建 PS4 可执行文件，只编译 all.c
add_executable(MyPS4App ${MAIN_FILE})

# 在 PS4 下使用静态链接，避免运行时依赖动态库
target_link_libraries(MyPS4App
    SDL2_PS4::SDL2
    SDL2_PS4::SDL2_image
    SDL2_PS4::SDL2_mixer
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
