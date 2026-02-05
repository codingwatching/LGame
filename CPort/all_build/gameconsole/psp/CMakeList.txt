cmake_minimum_required(VERSION 3.15)
project(MySDLApp C)

set(CMAKE_C_STANDARD 11)
set(CMAKE_C_STANDARD_REQUIRED ON)
add_compile_options(-O3)

set(CMAKE_SYSTEM_NAME Generic)
set(CMAKE_SYSTEM_PROCESSOR mips)

if(NOT DEFINED ENV{PSPDEV})
    message(FATAL_ERROR "未检测到 PSPDEV 环境变量")
endif()

set(PSPDEV $ENV{PSPDEV})
set(CMAKE_TOOLCHAIN_FILE "${PSPDEV}/psp.cmake")

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

# SDL2 (PSP)
find_package(SDL2 QUIET)
if(NOT SDL2_FOUND)
  FetchContent_Declare(
    SDL2_PSP
    GIT_REPOSITORY https://github.com/pspdev/SDL.git
    GIT_TAG psp-2.30.x
    SOURCE_DIR ${FETCHCONTENT_BASE_DIR}/sdl2_psp-src
    BINARY_DIR ${FETCHCONTENT_BASE_DIR}/sdl2_psp-build
  )
  FetchContent_MakeAvailable(SDL2_PSP)
  add_library(SDL2_PSP::SDL2 ALIAS SDL2)
endif()

# SDL2_image
find_package(SDL2_image QUIET)
if(NOT SDL2_IMAGE_FOUND)
  FetchContent_Declare(
    SDL2_image_PSP
    GIT_REPOSITORY https://github.com/libsdl-org/SDL_image.git
    GIT_TAG release-2.8.8
    SOURCE_DIR ${FETCHCONTENT_BASE_DIR}/sdl2_image_psp-src
    BINARY_DIR ${FETCHCONTENT_BASE_DIR}/sdl2_image_psp-build
  )
  FetchContent_MakeAvailable(SDL2_image_PSP)
  add_library(SDL2_PSP::SDL2_image ALIAS SDL2_image)
endif()

# SDL2_mixer
find_package(SDL2_mixer QUIET)
if(NOT SDL2_MIXER_FOUND)
  FetchContent_Declare(
    SDL2_mixer_PSP
    GIT_REPOSITORY https://github.com/libsdl-org/SDL_mixer.git
    GIT_TAG release-2.8.1
    SOURCE_DIR ${FETCHCONTENT_BASE_DIR}/sdl2_mixer_psp-src
    BINARY_DIR ${FETCHCONTENT_BASE_DIR}/sdl2_mixer_psp-build
  )
  FetchContent_MakeAvailable(SDL2_mixer_PSP)
  add_library(SDL2_PSP::SDL2_mixer ALIAS SDL2_mixer)
endif()

# 构建 PSP 可执行文件（EBOOT.PBP）
add_executable(MySDLApp ${SOURCES} ${MAIN_FILE})

target_link_libraries(MySDLApp
    SDL2_PSP::SDL2
    SDL2_PSP::SDL2_image
    SDL2_PSP::SDL2_mixer
    pspdisplay
    pspgu
    pspge
    pspctrl
    pspaudio
    pspuser
)
