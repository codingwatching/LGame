cmake_minimum_required(VERSION 3.15)
project(MySDLApp C)

set(CMAKE_C_STANDARD 11)
set(CMAKE_C_STANDARD_REQUIRED ON)
set(CMAKE_SYSTEM_PROCESSOR x86_64)

if(MSVC)
    add_compile_options(/Ox)
endif()

if(DEFINED ENV{GDKXSDK})
    message(STATUS "检测到 GDKX SDK，使用 GDKX 工具链")
    set(CMAKE_SYSTEM_NAME WindowsStore)
    set(CMAKE_SYSTEM_VERSION 10.0)
    set(CMAKE_GENERATOR_PLATFORM Gaming.Desktop.x64)
    set(GDKXSDK $ENV{GDKXSDK})
    set(CMAKE_TOOLCHAIN_FILE "${GDKXSDK}/share/gdkx.toolchain.cmake")
elseif(DEFINED ENV{XDKSDK})
    message(STATUS "检测到 XDK SDK，使用 XDK 工具链")
    set(CMAKE_SYSTEM_NAME Generic)
    set(CMAKE_SYSTEM_VERSION 10.0)
    set(CMAKE_GENERATOR_PLATFORM Xbox360)
    set(XDKSDK $ENV{XDKSDK})
    set(CMAKE_TOOLCHAIN_FILE "${XDKSDK}/share/xdk.toolchain.cmake")
else()
    message(FATAL_ERROR "未检测到 GDKX 或 XDK SDK，请设置环境变量 GDKXSDK 或 XDKSDK")
endif()

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

# SDL2 (Xbox)
find_package(SDL2 QUIET)
if(NOT SDL2_FOUND)
  FetchContent_Declare(
    SDL2_Xbox
    GIT_REPOSITORY https://github.com/xboxdev/SDL.git
    GIT_TAG xbox-2.30.x
    SOURCE_DIR ${FETCHCONTENT_BASE_DIR}/sdl2_xbox-src
    BINARY_DIR ${FETCHCONTENT_BASE_DIR}/sdl2_xbox-build
  )
  FetchContent_MakeAvailable(SDL2_Xbox)
  add_library(SDL2_Xbox::SDL2 ALIAS SDL2)
endif()

# SDL2_image
find_package(SDL2_image QUIET)
if(NOT SDL2_IMAGE_FOUND)
  FetchContent_Declare(
    SDL2_image_Xbox
    GIT_REPOSITORY https://github.com/libsdl-org/SDL_image.git
    GIT_TAG release-2.8.8
    SOURCE_DIR ${FETCHCONTENT_BASE_DIR}/sdl2_image_xbox-src
    BINARY_DIR ${FETCHCONTENT_BASE_DIR}/sdl2_image_xbox-build
  )
  FetchContent_MakeAvailable(SDL2_image_Xbox)
  add_library(SDL2_Xbox::SDL2_image ALIAS SDL2_image)
endif()

# SDL2_mixer
find_package(SDL2_mixer QUIET)
if(NOT SDL2_MIXER_FOUND)
  FetchContent_Declare(
    SDL2_mixer_Xbox
    GIT_REPOSITORY https://github.com/libsdl-org/SDL_mixer.git
    GIT_TAG release-2.8.1
    SOURCE_DIR ${FETCHCONTENT_BASE_DIR}/sdl2_mixer_xbox-src
    BINARY_DIR ${FETCHCONTENT_BASE_DIR}/sdl2_mixer_xbox-build
  )
  FetchContent_MakeAvailable(SDL2_mixer_Xbox)
  add_library(SDL2_Xbox::SDL2_mixer ALIAS SDL2_mixer)
endif()

add_executable(MySDLApp ${SOURCES} ${MAIN_FILE})

target_link_libraries(MySDLApp
    SDL2_Xbox::SDL2
    SDL2_Xbox::SDL2_image
    SDL2_Xbox::SDL2_mixer
)

# 平台特定库
if(DEFINED ENV{GDKXSDK})
  target_link_libraries(MySDLApp d3d11 xinput xaudio2)
elseif(DEFINED ENV{XDKSDK})
  target_link_libraries(MySDLApp xboxkrnl)
endif()
