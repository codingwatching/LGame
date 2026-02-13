cmake_minimum_required(VERSION 3.15)
project(MyPSVApp C)

set(CMAKE_C_STANDARD 11)
set(CMAKE_C_STANDARD_REQUIRED ON)
add_compile_options(-O3)

# 检查 VITASDK 环境变量
if(NOT DEFINED ENV{VITASDK})
    message(FATAL_ERROR "未检测到 VITASDK 环境变量，请设置 VITASDK 指向 Vita SDK 根目录")
endif()

set(VITASDK $ENV{VITASDK})

# 使用官方工具链文件
set(VITA_TOOLCHAIN_FILE "${VITASDK}/share/vita.toolchain.cmake")
if(EXISTS "${VITA_TOOLCHAIN_FILE}")
    set(CMAKE_TOOLCHAIN_FILE "${VITA_TOOLCHAIN_FILE}" CACHE FILEPATH "Vita toolchain file")
    message(STATUS "使用的 Vita toolchain 路径: ${VITA_TOOLCHAIN_FILE}")
else()
    message(FATAL_ERROR "未找到 vita.toolchain.cmake，请检查 VITASDK 路径是否正确")
endif()

# 源码目录
if(NOT DEFINED SRC_DIR)
    set(SRC_DIR ${CMAKE_SOURCE_DIR}/src)
endif()

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

# 限定搜索路径到 VITASDK
list(APPEND CMAKE_PREFIX_PATH "${VITASDK}/arm-vita-eabi")

# SDL2 (Vita 移植版)
find_package(SDL2 QUIET)
if(SDL2_FOUND AND SDL2_INCLUDE_DIRS MATCHES "${VITASDK}")
    message(STATUS "使用 VITASDK 中的 SDL2 库")
    add_library(SDL2_Vita::SDL2 ALIAS SDL2::SDL2)
else()
    message(STATUS "未找到 VITASDK SDL2，开始下载 SDL-Vita")
    FetchContent_Declare(
      SDL2_Vita
      GIT_REPOSITORY https://github.com/Rinnegatamante/SDL-Vita.git
      GIT_TAG master
      SOURCE_DIR ${FETCHCONTENT_BASE_DIR}/sdl2_vita-src
      BINARY_DIR ${FETCHCONTENT_BASE_DIR}/sdl2_vita-build
    )
    FetchContent_MakeAvailable(SDL2_Vita)
    add_library(SDL2_Vita::SDL2 ALIAS SDL2::SDL2)
endif()

# SDL2_image
set(SDL2IMAGE_AVIF OFF CACHE BOOL "" FORCE)
set(SDL2IMAGE_WEBP OFF CACHE BOOL "" FORCE)
set(SDL2IMAGE_TIF OFF CACHE BOOL "" FORCE)
set(SDL2IMAGE_VENDORED OFF CACHE BOOL "" FORCE)
find_package(SDL2_image QUIET)
if(SDL2_IMAGE_FOUND)
    message(STATUS "使用本地 SDL2_image 库")
    add_library(SDL2_Vita::SDL2_image ALIAS SDL2_image::SDL2_image)
else()
    message(STATUS "未找到本地 SDL2_image，开始下载官方 SDL2_image")
    FetchContent_Declare(
      SDL2_image_Vita
      GIT_REPOSITORY https://github.com/libsdl-org/SDL_image.git
      GIT_TAG release-2.8.8
      SOURCE_DIR ${FETCHCONTENT_BASE_DIR}/sdl2_image_vita-src
      BINARY_DIR ${FETCHCONTENT_BASE_DIR}/sdl2_image_vita-build
    )
    FetchContent_MakeAvailable(SDL2_image_Vita)
    add_library(SDL2_Vita::SDL2_image ALIAS SDL2_image::SDL2_image)
endif()

# SDL2_mixer
set(SDL2MIXER_WAVPACK OFF CACHE BOOL "" FORCE)
find_package(SDL2_mixer QUIET)
if(SDL2_MIXER_FOUND)
    message(STATUS "使用本地 SDL2_mixer 库")
    add_library(SDL2_Vita::SDL2_mixer ALIAS SDL2_mixer::SDL2_mixer)
else()
    message(STATUS "未找到本地 SDL2_mixer，开始下载官方 SDL2_mixer")
    FetchContent_Declare(
      SDL2_mixer_Vita
      GIT_REPOSITORY https://github.com/libsdl-org/SDL_mixer.git
      GIT_TAG release-2.8.1
      SOURCE_DIR ${FETCHCONTENT_BASE_DIR}/sdl2_mixer_vita-src
      BINARY_DIR ${FETCHCONTENT_BASE_DIR}/sdl2_mixer_vita-build
    )
    FetchContent_MakeAvailable(SDL2_mixer_Vita)
    add_library(SDL2_Vita::SDL2_mixer ALIAS SDL2_mixer::SDL2_mixer)
endif()

# 第三方依赖库 (Vita portlibs)
find_library(PNG_LIB NAMES png libpng REQUIRED PATHS "${VITASDK}/arm-vita-eabi/lib")
find_library(JPEG_LIB NAMES jpeg libjpeg REQUIRED PATHS "${VITASDK}/arm-vita-eabi/lib")
find_library(ZLIB_LIB NAMES z REQUIRED PATHS "${VITASDK}/arm-vita-eabi/lib")
find_library(OGG_LIB NAMES ogg REQUIRED PATHS "${VITASDK}/arm-vita-eabi/lib")
find_library(VORBIS_LIB NAMES vorbis REQUIRED PATHS "${VITASDK}/arm-vita-eabi/lib")
find_library(MPG123_LIB NAMES mpg123 REQUIRED PATHS "${VITASDK}/arm-vita-eabi/lib")

add_compile_definitions(PURE_SDL=1)

# 构建 PS Vita 可执行文件
add_executable(MyPSVApp ${MAIN_FILE})

target_link_libraries(MyPSVApp
    SDL2_Vita::SDL2
    SDL2_Vita::SDL2_image
    SDL2_Vita::SDL2_mixer
    ${PNG_LIB}
    ${JPEG_LIB}
    ${ZLIB_LIB}
    ${OGG_LIB}
    ${VORBIS_LIB}
    ${MPG123_LIB}
    SceDisplay_stub
    SceCtrl_stub
    SceAudio_stub
    SceSysmodule_stub
    SceGxm_stub
)
