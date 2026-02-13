cmake_minimum_required(VERSION 3.15)
project(MySwitchApp C)

set(CMAKE_C_STANDARD 11)
set(CMAKE_C_STANDARD_REQUIRED ON)

# 全局静态链接设置
set(CMAKE_FIND_LIBRARY_SUFFIXES ".a")
set(BUILD_SHARED_LIBS OFF)

add_compile_options(-O3)

# 检查 DEVKITPRO 环境变量
if(NOT DEFINED ENV{DEVKITPRO})
    message(FATAL_ERROR "未检测到 DEVKITPRO 环境变量，请设置 DEVKITPRO 指向 devkitPro 根目录")
endif()

set(DEVKITPRO $ENV{DEVKITPRO})

# 使用官方 Switch 工具链文件
set(SWITCH_TOOLCHAIN_FILE "${DEVKITPRO}/cmake/Switch.cmake")
if(EXISTS "${SWITCH_TOOLCHAIN_FILE}")
    set(CMAKE_TOOLCHAIN_FILE "${SWITCH_TOOLCHAIN_FILE}" CACHE FILEPATH "Switch toolchain file")
    message(STATUS "使用的 Switch toolchain 路径: ${SWITCH_TOOLCHAIN_FILE}")
else()
    message(FATAL_ERROR "未找到 Switch.cmake，请检查 DEVKITPRO 路径是否正确")
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

set(SDL_TEST OFF CACHE BOOL "" FORCE)
set(FETCHCONTENT_BASE_DIR ${CMAKE_BINARY_DIR}/third_party)

# 限定搜索路径到 Switch portlibs
list(APPEND CMAKE_PREFIX_PATH "${DEVKITPRO}/portlibs/switch")

# 强制 SDL 系列库静态编译
set(SDL_SHARED OFF CACHE BOOL "" FORCE)
set(SDL_STATIC ON CACHE BOOL "" FORCE)
set(SDL2IMAGE_SHARED OFF CACHE BOOL "" FORCE)
set(SDL2IMAGE_STATIC ON CACHE BOOL "" FORCE)
set(SDL2MIXER_SHARED OFF CACHE BOOL "" FORCE)
set(SDL2MIXER_STATIC ON CACHE BOOL "" FORCE)

# SDL2
find_package(SDL2 QUIET)
if(SDL2_FOUND)
    message(STATUS "使用 DEVKITPRO 中的 SDL2 静态库")
    add_library(SDL2_Switch::SDL2 ALIAS SDL2::SDL2)
else()
    message(STATUS "未找到 DEVKITPRO SDL2，开始下载 SDL-Switch")
    FetchContent_Declare(
      SDL2_Switch
      GIT_REPOSITORY https://github.com/devkitPro/SDL.git
      GIT_TAG master
      SOURCE_DIR ${FETCHCONTENT_BASE_DIR}/sdl2_switch-src
      BINARY_DIR ${FETCHCONTENT_BASE_DIR}/sdl2_switch-build
    )
    FetchContent_MakeAvailable(SDL2_Switch)
    add_library(SDL2_Switch::SDL2 ALIAS SDL2::SDL2)
endif()

# SDL2_image
set(SDL2IMAGE_AVIF OFF CACHE BOOL "" FORCE)
set(SDL2IMAGE_WEBP OFF CACHE BOOL "" FORCE)
set(SDL2IMAGE_TIF OFF CACHE BOOL "" FORCE)
set(SDL2IMAGE_VENDORED OFF CACHE BOOL "" FORCE)

find_package(SDL2_image QUIET)
if(SDL2_IMAGE_FOUND)
    message(STATUS "使用本地 SDL2_image 静态库")
    add_library(SDL2_Switch::SDL2_image ALIAS SDL2_image::SDL2_image)
else()
    message(STATUS "未找到本地 SDL2_image，开始下载官方 SDL2_image")
    FetchContent_Declare(
      SDL2_image_Switch
      GIT_REPOSITORY https://github.com/libsdl-org/SDL_image.git
      GIT_TAG release-2.8.8
      SOURCE_DIR ${FETCHCONTENT_BASE_DIR}/sdl2_image_switch-src
      BINARY_DIR ${FETCHCONTENT_BASE_DIR}/sdl2_image_switch-build
    )
    FetchContent_MakeAvailable(SDL2_image_Switch)
    add_library(SDL2_Switch::SDL2_image ALIAS SDL2_image::SDL2_image)
endif()

# SDL2_mixer
set(SDL2MIXER_WAVPACK OFF CACHE BOOL "" FORCE)

find_package(SDL2_mixer QUIET)
if(SDL2_MIXER_FOUND)
    message(STATUS "使用本地 SDL2_mixer 静态库")
    add_library(SDL2_Switch::SDL2_mixer ALIAS SDL2_mixer::SDL2_mixer)
else()
    message(STATUS "未找到本地 SDL2_mixer，开始下载官方 SDL2_mixer")
    FetchContent_Declare(
      SDL2_mixer_Switch
      GIT_REPOSITORY https://github.com/libsdl-org/SDL_mixer.git
      GIT_TAG release-2.8.1
      SOURCE_DIR ${FETCHCONTENT_BASE_DIR}/sdl2_mixer_switch-src
      BINARY_DIR ${FETCHCONTENT_BASE_DIR}/sdl2_mixer_switch-build
    )
    FetchContent_MakeAvailable(SDL2_mixer_Switch)
    add_library(SDL2_Switch::SDL2_mixer ALIAS SDL2_mixer::SDL2_mixer)
endif()

# 第三方依赖库 (Switch portlibs 静态版本)
find_library(PNG_LIB NAMES png REQUIRED PATHS "${DEVKITPRO}/portlibs/switch/lib")
find_library(JPEG_LIB NAMES jpeg REQUIRED PATHS "${DEVKITPRO}/portlibs/switch/lib")
find_library(ZLIB_LIB NAMES z REQUIRED PATHS "${DEVKITPRO}/portlibs/switch/lib")
find_library(OGG_LIB NAMES ogg REQUIRED PATHS "${DEVKITPRO}/portlibs/switch/lib")
find_library(VORBIS_LIB NAMES vorbis REQUIRED PATHS "${DEVKITPRO}/portlibs/switch/lib")
find_library(MPG123_LIB NAMES mpg123 REQUIRED PATHS "${DEVKITPRO}/portlibs/switch/lib")

add_compile_definitions(PURE_SDL=1)

# 构建 Switch 可执行文件（.nro 格式）
add_executable(MySwitchApp ${MAIN_FILE})

target_link_libraries(MySwitchApp
    SDL2_Switch::SDL2
    SDL2_Switch::SDL2_image
    SDL2_Switch::SDL2_mixer
    ${PNG_LIB}
    ${JPEG_LIB}
    ${ZLIB_LIB}
    ${OGG_LIB}
    ${VORBIS_LIB}
    ${MPG123_LIB}
    nx
)

# 可选 ANGLE 支持
option(USE_ANGLE "Enable ANGLE support" OFF)

if(USE_ANGLE)
    if(DEFINED ANGLE_DIR AND EXISTS "${ANGLE_DIR}/libEGL.a" AND EXISTS "${ANGLE_DIR}/libGLESv2.a")
        target_link_directories(MySwitchApp PRIVATE ${ANGLE_DIR})
        target_link_libraries(MySwitchApp EGL GLESv2)
    else()
        find_package(angle QUIET)
        if(angle_FOUND)
            target_link_libraries(MySwitchApp angle::libEGL angle::libGLESv2)
        endif()
    endif()
endif()
