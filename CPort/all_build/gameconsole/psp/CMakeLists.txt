cmake_minimum_required(VERSION 3.15)
project(MyPSPApp C)

set(CMAKE_C_STANDARD 11)
set(CMAKE_C_STANDARD_REQUIRED ON)
add_compile_options(-O3)

# 检查 PSPDEV 环境变量
if(NOT DEFINED ENV{PSPDEV})
    message(FATAL_ERROR "未检测到 PSPDEV 环境变量，请设置 PSPDEV 指向 PSP 工具链目录")
endif()

set(PSPDEV $ENV{PSPDEV})

# 使用官方推荐的工具链文件
set(PSP_TOOLCHAIN_FILE "${PSPDEV}/psp/share/pspdev.cmake")
if(EXISTS "${PSP_TOOLCHAIN_FILE}")
    set(CMAKE_TOOLCHAIN_FILE "${PSP_TOOLCHAIN_FILE}" CACHE FILEPATH "PSP toolchain file")
    message(STATUS "使用的 PSP toolchain 路径: ${PSP_TOOLCHAIN_FILE}")
else()
    message(FATAL_ERROR "未找到 pspdev.cmake，请检查 PSPDEV 路径是否正确")
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

# 限定搜索路径到 PSPDEV 根目录和 portlibs
list(APPEND CMAKE_PREFIX_PATH "${PSPDEV}/psp")
list(APPEND CMAKE_PREFIX_PATH "${PSPDEV}/portlibs/psp")

# SDL2 (PSP)
find_package(SDL2 QUIET)
if(SDL2_FOUND AND SDL2_INCLUDE_DIRS MATCHES "${PSPDEV}")
    message(STATUS "使用 PSPDEV 中的 SDL2 库")
    add_library(SDL2_PSP::SDL2 ALIAS SDL2::SDL2)
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
    add_library(SDL2_PSP::SDL2 ALIAS SDL2::SDL2)
endif()

# SDL2_image
set(SDL2IMAGE_AVIF OFF CACHE BOOL "" FORCE)
set(SDL2IMAGE_WEBP OFF CACHE BOOL "" FORCE)
set(SDL2IMAGE_TIF OFF CACHE BOOL "" FORCE)
set(SDL2IMAGE_VENDORED OFF CACHE BOOL "" FORCE)
find_package(SDL2_image QUIET)
if(SDL2_IMAGE_FOUND)
    message(STATUS "使用本地 SDL2_image 库")
    add_library(SDL2_PSP::SDL2_image ALIAS SDL2_image::SDL2_image)
else()
    message(STATUS "未找到本地 SDL2_image，开始下载官方 SDL2_image")
    FetchContent_Declare(
      SDL2_image_PSP
      GIT_REPOSITORY https://github.com/libsdl-org/SDL_image.git
      GIT_TAG release-2.8.8
      SOURCE_DIR ${FETCHCONTENT_BASE_DIR}/sdl2_image_psp-src
      BINARY_DIR ${FETCHCONTENT_BASE_DIR}/sdl2_image_psp-build
    )
    FetchContent_MakeAvailable(SDL2_image_PSP)
    add_library(SDL2_PSP::SDL2_image ALIAS SDL2_image::SDL2_image)
endif()

# SDL2_mixer
set(SDL2MIXER_WAVPACK OFF CACHE BOOL "" FORCE)
find_package(SDL2_mixer QUIET)
if(SDL2_MIXER_FOUND)
    message(STATUS "使用本地 SDL2_mixer 库")
    add_library(SDL2_PSP::SDL2_mixer ALIAS SDL2_mixer::SDL2_mixer)
else()
    message(STATUS "未找到本地 SDL2_mixer，开始下载官方 SDL2_mixer")
    FetchContent_Declare(
      SDL2_mixer_PSP
      GIT_REPOSITORY https://github.com/libsdl-org/SDL_mixer.git
      GIT_TAG release-2.8.1
      SOURCE_DIR ${FETCHCONTENT_BASE_DIR}/sdl2_mixer_psp-src
      BINARY_DIR ${FETCHCONTENT_BASE_DIR}/sdl2_mixer_psp-build
    )
    FetchContent_MakeAvailable(SDL2_mixer_PSP)
    add_library(SDL2_PSP::SDL2_mixer ALIAS SDL2_mixer::SDL2_mixer)
endif()

# 第三方依赖库 (PSP portlibs)
find_library(PNG_LIB NAMES png libpng REQUIRED PATHS "${PSPDEV}/psp/lib" "${PSPDEV}/portlibs/psp/lib")
find_library(JPEG_LIB NAMES jpeg libjpeg REQUIRED PATHS "${PSPDEV}/psp/lib" "${PSPDEV}/portlibs/psp/lib")
find_library(ZLIB_LIB NAMES z REQUIRED PATHS "${PSPDEV}/psp/lib" "${PSPDEV}/portlibs/psp/lib")
find_library(OGG_LIB NAMES ogg REQUIRED PATHS "${PSPDEV}/psp/lib" "${PSPDEV}/portlibs/psp/lib")
find_library(VORBIS_LIB NAMES vorbis REQUIRED PATHS "${PSPDEV}/psp/lib" "${PSPDEV}/portlibs/psp/lib")
find_library(MPG123_LIB NAMES mpg123 REQUIRED PATHS "${PSPDEV}/psp/lib" "${PSPDEV}/portlibs/psp/lib")

add_compile_definitions(PURE_SDL=1)

add_executable(MyPSPApp ${MAIN_FILE})

target_link_libraries(MyPSPApp
    SDL2_PSP::SDL2
    SDL2_PSP::SDL2_image
    SDL2_PSP::SDL2_mixer
    ${PNG_LIB}
    ${JPEG_LIB}
    ${ZLIB_LIB}
    ${OGG_LIB}
    ${VORBIS_LIB}
    ${MPG123_LIB}
    pspdisplay
    pspgu
    pspge
    pspctrl
    pspaudio
    pspuser
)
