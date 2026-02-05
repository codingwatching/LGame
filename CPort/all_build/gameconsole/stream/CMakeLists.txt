cmake_minimum_required(VERSION 3.15)
project(MySDLApp C)

set(CMAKE_C_STANDARD 11)
set(CMAKE_C_STANDARD_REQUIRED ON)

if(MSVC)
    add_compile_options(/Ox)
elseif(CMAKE_C_COMPILER_ID STREQUAL "GNU" OR CMAKE_C_COMPILER_ID STREQUAL "Clang")
    add_compile_options(-O3)
endif()

if(NOT DEFINED SRC_DIR)
    set(SRC_DIR ${CMAKE_SOURCE_DIR}/src)
endif()

file(GLOB_RECURSE ALL_FILES ${SRC_DIR}/*)
set(SOURCES "")
foreach(file ${ALL_FILES})
    if(file MATCHES "\\.c$")
        list(APPEND SOURCES ${file})
    endif()
endforeach()

set(MAIN_FILE ${SRC_DIR}/all.c)
if(NOT EXISTS ${MAIN_FILE})
    message(FATAL_ERROR "未找到入口文件all.c，请确认源码目录是否正确: ${SRC_DIR}")
endif()

include(FetchContent)

# ANGLE
find_package(ANGLE QUIET)
if(NOT ANGLE_FOUND)
    message(STATUS "未找到 ANGLE，自动下载...")
    FetchContent_Declare(angle
      GIT_REPOSITORY https://github.com/google/angle.git
      GIT_TAG main)
    FetchContent_MakeAvailable(angle)
endif()

# SDL2
find_package(SDL2 QUIET)
if(NOT SDL2_FOUND)
    message(STATUS "未找到 SDL2，自动下载...")
    FetchContent_Declare(SDL2
      GIT_REPOSITORY https://github.com/libsdl-org/SDL.git
      GIT_TAG release-2.32.10)
    FetchContent_MakeAvailable(SDL2)
endif()

# SDL2_image
find_package(SDL2_image QUIET)
if(NOT SDL2_IMAGE_FOUND)
    FetchContent_Declare(SDL2_image
      GIT_REPOSITORY https://github.com/libsdl-org/SDL_image.git
      GIT_TAG release-2.8.8)
    FetchContent_MakeAvailable(SDL2_image)
endif()

# SDL2_mixer
find_package(SDL2_mixer QUIET)
if(NOT SDL2_MIXER_FOUND)
    FetchContent_Declare(SDL2_mixer
      GIT_REPOSITORY https://github.com/libsdl-org/SDL_mixer.git
      GIT_TAG release-2.8.1)
    FetchContent_MakeAvailable(SDL2_mixer)
endif()

# Steamworks SDK
if(NOT DEFINED STEAMWORKS_SDK_PATH)
    message(FATAL_ERROR "请通过 -DSTEAMWORKS_SDK_PATH 指定 Steamworks SDK 路径")
endif()

include_directories(${STEAMWORKS_SDK_PATH}/public)
link_directories(${STEAMWORKS_SDK_PATH}/redistributable_bin)

# 构建可执行文件
add_executable(MySDLApp ${SOURCES} ${MAIN_FILE})

# 链接依赖库
target_link_libraries(MySDLApp
    SDL2
    SDL2_image
    SDL2_mixer
    angle
    steam_api
)
