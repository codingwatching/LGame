cmake_minimum_required(VERSION 3.15)
project(MySDLApp C)

set(CMAKE_C_STANDARD 11)
set(CMAKE_C_STANDARD_REQUIRED ON)
add_compile_options(-O3)

set(CMAKE_SYSTEM_NAME Generic)
set(CMAKE_SYSTEM_PROCESSOR arm)

if(NOT DEFINED ENV{DEVKITPRO})
    message(FATAL_ERROR "未检测到 DEVKITPRO 环境变量，请先安装 devkitPro 并设置环境变量。")
endif()

set(DEVKITPRO $ENV{DEVKITPRO})
set(CMAKE_TOOLCHAIN_FILE "${DEVKITPRO}/cmake/3DS.cmake")

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

# SDL2
find_package(SDL2 QUIET)
if(NOT SDL2_FOUND)
  FetchContent_Declare(
    SDL2_3DS
    GIT_REPOSITORY https://github.com/devkitPro/SDL.git
    GIT_TAG 3ds-2.30.x
    SOURCE_DIR ${FETCHCONTENT_BASE_DIR}/sdl2_3ds-src
    BINARY_DIR ${FETCHCONTENT_BASE_DIR}/sdl2_3ds-build
  )
  FetchContent_MakeAvailable(SDL2_3DS)
  add_library(SDL2_3DS::SDL2 ALIAS SDL2)
endif()

# SDL2_image
find_package(SDL2_image QUIET)
if(NOT SDL2_IMAGE_FOUND)
  FetchContent_Declare(
    SDL2_image_3DS
    GIT_REPOSITORY https://github.com/libsdl-org/SDL_image.git
    GIT_TAG release-2.8.8
    SOURCE_DIR ${FETCHCONTENT_BASE_DIR}/sdl2_image_3ds-src
    BINARY_DIR ${FETCHCONTENT_BASE_DIR}/sdl2_image_3ds-build
  )
  FetchContent_MakeAvailable(SDL2_image_3DS)
  add_library(SDL2_3DS::SDL2_image ALIAS SDL2_image)
endif()

# SDL2_mixer
find_package(SDL2_mixer QUIET)
if(NOT SDL2_MIXER_FOUND)
  FetchContent_Declare(
    SDL2_mixer_3DS
    GIT_REPOSITORY https://github.com/libsdl-org/SDL_mixer.git
    GIT_TAG release-2.8.1
    SOURCE_DIR ${FETCHCONTENT_BASE_DIR}/sdl2_mixer_3ds-src
    BINARY_DIR ${FETCHCONTENT_BASE_DIR}/sdl2_mixer_3ds-build
  )
  FetchContent_MakeAvailable(SDL2_mixer_3DS)
  add_library(SDL2_3DS::SDL2_mixer ALIAS SDL2_mixer)
endif()

add_executable(MySDLApp ${SOURCES} ${MAIN_FILE})

target_link_libraries(MySDLApp
    SDL2_3DS::SDL2
    SDL2_3DS::SDL2_image
    SDL2_3DS::SDL2_mixer
    citro2d
    citro3d
    m
)
