cmake_minimum_required(VERSION 3.15)
project(MySDLApp C)

set(CMAKE_C_STANDARD 11)
set(CMAKE_C_STANDARD_REQUIRED ON)
add_compile_options(-O3)

# 指定交叉编译目标为 PSP
set(CMAKE_SYSTEM_NAME Generic)
set(CMAKE_SYSTEM_PROCESSOR mips)

# 检查 PSPDEV 环境变量
if(NOT DEFINED ENV{PSPDEV})
    message(FATAL_ERROR "未检测到 PSPDEV 环境变量，请设置 PSPDEV 指向 PSP 工具链目录")
endif()

set(PSPDEV $ENV{PSPDEV})
set(CMAKE_TOOLCHAIN_FILE "${PSPDEV}/psp.cmake")

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
set(FETCHCONTENT_BASE_DIR ${CMAKE_BINARY_DIR}/third_party)

# 限定搜索路径到 PSPDEV，避免误用主机 SDL2
set(CMAKE_PREFIX_PATH "${PSPDEV}/psp" ${CMAKE_PREFIX_PATH})

# SDL2 (PSP)
find_package(SDL2 QUIET)
if(SDL2_FOUND AND SDL2_INCLUDE_DIRS MATCHES "${PSPDEV}")
    message(STATUS "使用 PSPDEV 中的 SDL2 库")
    add_library(SDL2_PSP::SDL2 ALIAS SDL2)
else()
    message(STATUS "未找到 PSPDEV SDL2，开始下载 SDL-PSP")
    FetchContent_Declare(
      SDL2_PSP
      GIT_REPOSITORY https://github.com/pspdev/SDL.git
      GIT_TAG master
      SOURCE_DIR ${FETCHCONTENT_BASE_DIR}/sdl2_psp-src
      BINARY_DIR ${FETCHCONTENT_BASE_DIR}/sdl2_psp-build
    )
    FetchContent_MakeAvailable(SDL2_PSP)
    add_library(SDL2_PSP::SDL2 ALIAS SDL2)
endif()

# SDL2_image
set(SDL2IMAGE_AVIF OFF CACHE BOOL "" FORCE)
set(SDL2IMAGE_WEBP OFF CACHE BOOL "" FORCE)
set(SDL2IMAGE_TIF OFF CACHE BOOL "" FORCE)
set(SDL2IMAGE_VENDORED OFF CACHE BOOL "" FORCE)
find_package(SDL2_image QUIET)
if(SDL2_IMAGE_FOUND AND SDL2_IMAGE_INCLUDE_DIRS MATCHES "${PSPDEV}")
    message(STATUS "使用 PSPDEV 中的 SDL2_image 库")
    add_library(SDL2_PSP::SDL2_image ALIAS SDL2_image)
else()
    message(STATUS "未找到 PSPDEV SDL2_image，开始下载")
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
set(SDL2MIXER_WAVPACK OFF CACHE BOOL "" FORCE)
find_package(SDL2_mixer QUIET)
if(SDL2_MIXER_FOUND AND SDL2_MIXER_INCLUDE_DIRS MATCHES "${PSPDEV}")
    message(STATUS "使用 PSPDEV 中的 SDL2_mixer 库")
    add_library(SDL2_PSP::SDL2_mixer ALIAS SDL2_mixer)
else()
    message(STATUS "未找到 PSPDEV SDL2_mixer，开始下载")
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

# 构建 PSP 可执行文件，只编译 all.c
add_executable(MySDLApp ${MAIN_FILE})

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
