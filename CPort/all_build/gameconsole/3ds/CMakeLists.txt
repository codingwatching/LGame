cmake_minimum_required(VERSION 3.15)
project(MySDLApp C)

set(CMAKE_C_STANDARD 11)
set(CMAKE_C_STANDARD_REQUIRED ON)
add_compile_options(-O3)

set(CMAKE_SYSTEM_NAME Generic)
set(CMAKE_SYSTEM_PROCESSOR arm)

# 检查 DEVKITPRO 环境变量
if(NOT DEFINED ENV{DEVKITPRO})
    message(FATAL_ERROR "未检测到 DEVKITPRO 环境变量，请设置 DEVKITPRO 指向 devkitPro 根目录")
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

# 限定搜索路径到 3DS devkitPro，避免误用主机 SDL2
set(CMAKE_PREFIX_PATH "${DEVKITPRO}/portlibs/3ds" ${CMAKE_PREFIX_PATH})

# SDL2 for 3DS
find_package(SDL2 QUIET)
if(SDL2_FOUND AND SDL2_INCLUDE_DIRS MATCHES "${DEVKITPRO}")
    message(STATUS "使用 DEVKITPRO 中的 SDL2 库")
    add_library(SDL2_3DS::SDL2 ALIAS SDL2)
else()
    message(STATUS "未找到 DEVKITPRO SDL2，开始下载 SDL-3DS")
    FetchContent_Declare(
      SDL2_3DS
      GIT_REPOSITORY https://github.com/devkitPro/SDL.git
      GIT_TAG master
      SOURCE_DIR ${FETCHCONTENT_BASE_DIR}/sdl2_3ds-src
      BINARY_DIR ${FETCHCONTENT_BASE_DIR}/sdl2_3ds-build
    )
    FetchContent_MakeAvailable(SDL2_3DS)
    add_library(SDL2_3DS::SDL2 ALIAS SDL2)
endif()

# SDL2_image
find_package(SDL2_image QUIET)
if(SDL2_IMAGE_FOUND AND SDL2_IMAGE_INCLUDE_DIRS MATCHES "${DEVKITPRO}")
    message(STATUS "使用 DEVKITPRO 中的 SDL2_image 库")
    add_library(SDL2_3DS::SDL2_image ALIAS SDL2_image)
else()
    message(STATUS "未找到 DEVKITPRO SDL2_image，开始下载")
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
if(SDL2_MIXER_FOUND AND SDL2_MIXER_INCLUDE_DIRS MATCHES "${DEVKITPRO}")
    message(STATUS "使用 DEVKITPRO 中的 SDL2_mixer 库")
    add_library(SDL2_3DS::SDL2_mixer ALIAS SDL2_mixer)
else()
    message(STATUS "未找到 DEVKITPRO SDL2_mixer，开始下载")
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

# 构建 3DS 可执行文件 (.3dsx 格式)
add_executable(MySDLApp ${SOURCES} ${MAIN_FILE})

target_link_libraries(MySDLApp
    SDL2_3DS::SDL2
    SDL2_3DS::SDL2_image
    SDL2_3DS::SDL2_mixer
    citro2d
    citro3d
    ctru
	m
)