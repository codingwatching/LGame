cmake_minimum_required(VERSION 3.15)
project(MySDLApp C)

set(CMAKE_C_STANDARD 11)
set(CMAKE_C_STANDARD_REQUIRED ON)
add_compile_options(-O3)

set(CMAKE_SYSTEM_NAME Generic)
set(CMAKE_SYSTEM_PROCESSOR x86_64)

if(NOT DEFINED ENV{PS4SDK})
    message(FATAL_ERROR "未检测到 PS4SDK 环境变量")
endif()

set(PS4SDK $ENV{PS4SDK})
set(CMAKE_TOOLCHAIN_FILE "${PS4SDK}/share/ps4.toolchain.cmake")

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
    SDL2_PS4
    GIT_REPOSITORY https://github.com/ps4dev/SDL.git
    GIT_TAG release-2.30.x-ps4
    SOURCE_DIR ${FETCHCONTENT_BASE_DIR}/sdl2_ps4-src
    BINARY_DIR ${FETCHCONTENT_BASE_DIR}/sdl2_ps4-build
  )
  FetchContent_MakeAvailable(SDL2_PS4)
  add_library(SDL2_PS4::SDL2 ALIAS SDL2)
endif()

# SDL2_image
find_package(SDL2_image QUIET)
if(NOT SDL2_IMAGE_FOUND)
  FetchContent_Declare(
    SDL2_image_PS4
    GIT_REPOSITORY https://github.com/libsdl-org/SDL_image.git
    GIT_TAG release-2.8.8
    SOURCE_DIR ${FETCHCONTENT_BASE_DIR}/sdl2_image_ps4-src
    BINARY_DIR ${FETCHCONTENT_BASE_DIR}/sdl2_image_ps4-build
  )
  FetchContent_MakeAvailable(SDL2_image_PS4)
  add_library(SDL2_PS4::SDL2_image ALIAS SDL2_image)
endif()

# SDL2_mixer
find_package(SDL2_mixer QUIET)
if(NOT SDL2_MIXER_FOUND)
  FetchContent_Declare(
    SDL2_mixer_PS4
    GIT_REPOSITORY https://github.com/libsdl-org/SDL_mixer.git
    GIT_TAG release-2.8.1
    SOURCE_DIR ${FETCHCONTENT_BASE_DIR}/sdl2_mixer_ps4-src
    BINARY_DIR ${FETCHCONTENT_BASE_DIR}/sdl2_mixer_ps4-build
  )
  FetchContent_MakeAvailable(SDL2_mixer_PS4)
  add_library(SDL2_PS4::SDL2_mixer ALIAS SDL2_mixer)
endif()

add_executable(MySDLApp ${SOURCES} ${MAIN_FILE})

target_link_libraries(MySDLApp
    SDL2_PS4::SDL2
    SDL2_PS4::SDL2_image
    SDL2_PS4::SDL2_mixer
    ScePad_stub
    SceAudio_stub
    SceVideoOut_stub
    SceSystemService_stub
)
