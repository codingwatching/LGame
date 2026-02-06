cmake_minimum_required(VERSION 3.15)
project(MySDLApp C)

set(CMAKE_C_STANDARD 11)
set(CMAKE_C_STANDARD_REQUIRED ON)
add_compile_options(-O3)

set(CMAKE_SYSTEM_NAME Generic)
set(CMAKE_SYSTEM_PROCESSOR x86_64)

if(NOT DEFINED ENV{PS5SDK})
    message(FATAL_ERROR "未检测到 PS5SDK 环境变量，请设置 PS5SDK 指向 PS5 工具链目录")
endif()

set(PS5SDK $ENV{PS5SDK})
set(CMAKE_TOOLCHAIN_FILE "${PS5SDK}/share/ps5.toolchain.cmake")

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

# 限定搜索路径到 PS5SDK，避免误用主机 SDL2
set(CMAKE_PREFIX_PATH "${PS5SDK}/arm-ps5-eabi" ${CMAKE_PREFIX_PATH})

find_package(SDL2 QUIET)
if(SDL2_FOUND AND SDL2_INCLUDE_DIRS MATCHES "${PS5SDK}")
    message(STATUS "使用 PS5SDK 中的 SDL2 库")
    add_library(SDL2_PS5::SDL2 ALIAS SDL2)
else()
    message(STATUS "未找到 PS5SDK SDL2，开始下载 SDL-PS5")
    FetchContent_Declare(
      SDL2_PS5
      GIT_REPOSITORY https://github.com/ps5-payload-dev/SDL.git
      GIT_TAG master
      SOURCE_DIR ${FETCHCONTENT_BASE_DIR}/sdl2_ps5-src
      BINARY_DIR ${FETCHCONTENT_BASE_DIR}/sdl2_ps5-build
    )
    FetchContent_MakeAvailable(SDL2_PS5)
    add_library(SDL2_PS5::SDL2 ALIAS SDL2)
endif()

find_package(SDL2_image QUIET)
if(SDL2_IMAGE_FOUND AND SDL2_IMAGE_INCLUDE_DIRS MATCHES "${PS5SDK}")
    message(STATUS "使用 PS5SDK 中的 SDL2_image 库")
    add_library(SDL2_PS5::SDL2_image ALIAS SDL2_image)
else()
    message(STATUS "未找到 PS5SDK SDL2_image，开始下载")
    FetchContent_Declare(
      SDL2_image_PS5
      GIT_REPOSITORY https://github.com/libsdl-org/SDL_image.git
      GIT_TAG release-2.8.8
      SOURCE_DIR ${FETCHCONTENT_BASE_DIR}/sdl2_image_ps5-src
      BINARY_DIR ${FETCHCONTENT_BASE_DIR}/sdl2_image_ps5-build
    )
    FetchContent_MakeAvailable(SDL2_image_PS5)
    add_library(SDL2_PS5::SDL2_image ALIAS SDL2_image)
endif()

find_package(SDL2_mixer QUIET)
if(SDL2_MIXER_FOUND AND SDL2_MIXER_INCLUDE_DIRS MATCHES "${PS5SDK}")
    message(STATUS "使用 PS5SDK 中的 SDL2_mixer 库")
    add_library(SDL2_PS5::SDL2_mixer ALIAS SDL2_mixer)
else()
    message(STATUS "未找到 PS5SDK SDL2_mixer，开始下载")
    FetchContent_Declare(
      SDL2_mixer_PS5
      GIT_REPOSITORY https://github.com/libsdl-org/SDL_mixer.git
      GIT_TAG release-2.8.1
      SOURCE_DIR ${FETCHCONTENT_BASE_DIR}/sdl2_mixer_ps5-src
      BINARY_DIR ${FETCHCONTENT_BASE_DIR}/sdl2_mixer_ps5-build
    )
    FetchContent_MakeAvailable(SDL2_mixer_PS5)
    add_library(SDL2_PS5::SDL2_mixer ALIAS SDL2_mixer)
endif()

add_executable(MySDLApp ${SOURCES} ${MAIN_FILE})

target_link_libraries(MySDLApp
    SDL2_PS5::SDL2
    SDL2_PS5::SDL2_image
    SDL2_PS5::SDL2_mixer
    ScePad_stub
    SceAudio_stub
    SceVideoOut_stub
    SceSystemService_stub
)