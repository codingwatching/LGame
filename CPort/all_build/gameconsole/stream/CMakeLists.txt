cmake_minimum_required(VERSION 3.15)
project(MySDLApp C)

set(CMAKE_C_STANDARD 11)
set(CMAKE_C_STANDARD_REQUIRED ON)

if(MSVC)
    add_compile_options(/Ox /W4 /MP)
elseif(CMAKE_C_COMPILER_ID STREQUAL "GNU" OR CMAKE_C_COMPILER_ID STREQUAL "Clang")
    add_compile_options(-O3 -Wall)
endif()

if(APPLE)
    set(CMAKE_OSX_DEPLOYMENT_TARGET 11.0)
endif()

# 源码目录
if(NOT DEFINED SRC_DIR)
    set(SRC_DIR ${CMAKE_SOURCE_DIR}/src)
endif()

# 入口文件 all.c
set(MAIN_FILE ${SRC_DIR}/all.c)
if(NOT EXISTS ${MAIN_FILE})
    message(FATAL_ERROR "未找到入口文件 all.c，请确认源码目录是否正确: ${SRC_DIR}")
endif()

include(FetchContent)

# SDL2
find_package(SDL2 QUIET)
if(NOT SDL2_FOUND)
    FetchContent_Declare(
      SDL2
      GIT_REPOSITORY https://github.com/libsdl-org/SDL.git
      GIT_TAG release-2.32.10
    )
    FetchContent_MakeAvailable(SDL2)
endif()

set(SDL2IMAGE_AVIF OFF CACHE BOOL "" FORCE)
set(SDL2IMAGE_WEBP OFF CACHE BOOL "" FORCE)
set(SDL2IMAGE_TIF OFF CACHE BOOL "" FORCE)
set(SDL2IMAGE_VENDORED OFF CACHE BOOL "" FORCE)

# SDL2_image
find_package(SDL2_image QUIET)
if(NOT SDL2_image_FOUND)
    FetchContent_Declare(
      SDL2_image
      GIT_REPOSITORY https://github.com/libsdl-org/SDL_image.git
      GIT_TAG release-2.8.8
    )
    FetchContent_MakeAvailable(SDL2_image)
endif()

# SDL2_mixer
set(SDL2MIXER_WAVPACK OFF CACHE BOOL "" FORCE)
find_package(SDL2_mixer QUIET)
if(NOT SDL2_mixer_FOUND)
    FetchContent_Declare(
      SDL2_mixer
      GIT_REPOSITORY https://github.com/libsdl-org/SDL_mixer.git
      GIT_TAG release-2.8.1
    )
    FetchContent_MakeAvailable(SDL2_mixer)
endif()

# Steamworks SDK
if(NOT DEFINED STEAMWORKS_SDK_PATH)
    message(FATAL_ERROR "请通过 -DSTEAMWORKS_SDK_PATH 指定 Steamworks SDK 路径")
endif()

include_directories(${STEAMWORKS_SDK_PATH}/public)
link_directories(${STEAMWORKS_SDK_PATH}/redistributable_bin)

# 构建可执行文件，只编译 all.c
add_executable(MySDLApp ${MAIN_FILE})

# 链接依赖库
target_link_libraries(MySDLApp
    SDL2::SDL2
    SDL2_image::SDL2_image
    SDL2_mixer::SDL2_mixer
    steam_api
)

if(WIN32)
    target_link_libraries(MySDLApp SDL2::SDL2main)
endif()

# 可选 ANGLE 支持
option(USE_ANGLE "Enable ANGLE support" OFF)

if(USE_ANGLE)
    if(DEFINED ANGLE_DIR AND EXISTS "${ANGLE_DIR}/libEGL.dll" AND EXISTS "${ANGLE_DIR}/libGLESv2.dll")
        target_link_directories(MySDLApp PRIVATE ${ANGLE_DIR})
        target_link_libraries(MySDLApp libEGL libGLESv2)
    else()
        find_package(angle QUIET)
        if(angle_FOUND)
            target_link_libraries(MySDLApp angle::libEGL angle::libGLESv2)
        endif()
    endif()
endif()
