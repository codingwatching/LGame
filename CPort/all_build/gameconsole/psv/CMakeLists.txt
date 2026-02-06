cmake_minimum_required(VERSION 3.15)
project(MySDLApp C)

set(CMAKE_C_STANDARD 11)
set(CMAKE_C_STANDARD_REQUIRED ON)
add_compile_options(-O3)

set(CMAKE_SYSTEM_NAME Vita)
set(CMAKE_SYSTEM_PROCESSOR arm)

if(NOT DEFINED ENV{VITASDK})
    message(FATAL_ERROR "未检测到 VITASDK 环境变量，请设置 VITASDK 指向 Vita SDK 根目录")
endif()

set(VITASDK $ENV{VITASDK})
set(CMAKE_TOOLCHAIN_FILE "${VITASDK}/share/vita.toolchain.cmake")

if(NOT DEFINED SRC_DIR)
    set(SRC_DIR ${CMAKE_SOURCE_DIR}/src)
endif()

file(GLOB_RECURSE SOURCES ${SRC_DIR}/*.c)

set(MAIN_FILE ${SRC_DIR}/all.c)
if(NOT EXISTS ${MAIN_FILE})
    message(FATAL_ERROR "未找到入口文件 all.c")
endif()

include(FetchContent)
set(FETCHCONTENT_BASE_DIR ${CMAKE_BINARY_DIR}/third_party)

# 限定搜索路径到 VITASDK，避免误用主机 SDL2
set(CMAKE_PREFIX_PATH "${VITASDK}/arm-vita-eabi" ${CMAKE_PREFIX_PATH})

# SDL2 (Vita 移植版)
find_package(SDL2 QUIET)
if(SDL2_FOUND AND SDL2_INCLUDE_DIRS MATCHES "${VITASDK}")
    message(STATUS "使用 VITASDK 中的 SDL2 库")
    add_library(SDL2_Vita::SDL2 ALIAS SDL2)
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
    add_library(SDL2_Vita::SDL2 ALIAS SDL2)
endif()

# SDL2_image
find_package(SDL2_image QUIET)
if(SDL2_IMAGE_FOUND AND SDL2_IMAGE_INCLUDE_DIRS MATCHES "${VITASDK}")
    message(STATUS "使用 VITASDK 中的 SDL2_image 库")
    add_library(SDL2_Vita::SDL2_image ALIAS SDL2_image)
else()
    message(STATUS "未找到 VITASDK SDL2_image，开始下载")
    FetchContent_Declare(
      SDL2_image_Vita
      GIT_REPOSITORY https://github.com/libsdl-org/SDL_image.git
      GIT_TAG release-2.8.8
      SOURCE_DIR ${FETCHCONTENT_BASE_DIR}/sdl2_image_vita-src
      BINARY_DIR ${FETCHCONTENT_BASE_DIR}/sdl2_image_vita-build
    )
    FetchContent_MakeAvailable(SDL2_image_Vita)
    add_library(SDL2_Vita::SDL2_image ALIAS SDL2_image)
endif()

# SDL2_mixer
find_package(SDL2_mixer QUIET)
if(SDL2_MIXER_FOUND AND SDL2_MIXER_INCLUDE_DIRS MATCHES "${VITASDK}")
    message(STATUS "使用 VITASDK 中的 SDL2_mixer 库")
    add_library(SDL2_Vita::SDL2_mixer ALIAS SDL2_mixer)
else()
    message(STATUS "未找到 VITASDK SDL2_mixer，开始下载")
    FetchContent_Declare(
      SDL2_mixer_Vita
      GIT_REPOSITORY https://github.com/libsdl-org/SDL_mixer.git
      GIT_TAG release-2.8.1
      SOURCE_DIR ${FETCHCONTENT_BASE_DIR}/sdl2_mixer_vita-src
      BINARY_DIR ${FETCHCONTENT_BASE_DIR}/sdl2_mixer_vita-build
    )
    FetchContent_MakeAvailable(SDL2_mixer_Vita)
    add_library(SDL2_Vita::SDL2_mixer ALIAS SDL2_mixer)
endif()

add_executable(MySDLApp ${SOURCES} ${MAIN_FILE})

target_link_libraries(MySDLApp
    SDL2_Vita::SDL2
    SDL2_Vita::SDL2_image
    SDL2_Vita::SDL2_mixer
    SceDisplay_stub
    SceCtrl_stub
    SceAudio_stub
    SceSysmodule_stub
    SceGxm_stub
)
