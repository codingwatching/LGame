cmake_minimum_required(VERSION 3.15)
project(MySDLApp C)

set(CMAKE_C_STANDARD 11)
set(CMAKE_C_STANDARD_REQUIRED ON)

if(MSVC)
    message(STATUS "检测到 MSVC 编译器，使用 /Ox 优化 （约等于O3）")
    add_compile_options(/Ox)
elseif(CMAKE_C_COMPILER_ID STREQUAL "GNU" OR CMAKE_C_COMPILER_ID STREQUAL "Clang")
    message(STATUS "检测到 GCC/Clang 编译器，使用 -O3 优化")
    add_compile_options(-O3)
endif()

if(APPLE)
    set(CMAKE_OSX_DEPLOYMENT_TARGET 11.0)
    message(STATUS "设置 macOS 部署目标为 11.0")
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
    message(STATUS "未找到 SDL2_image，自动下载...")
    FetchContent_Declare(SDL2_image
      GIT_REPOSITORY https://github.com/libsdl-org/SDL_image.git
      GIT_TAG release-2.8.8)
    FetchContent_MakeAvailable(SDL2_image)
endif()

# SDL2_mixer
find_package(SDL2_mixer QUIET)
if(NOT SDL2_MIXER_FOUND)
    message(STATUS "未找到 SDL2_mixer，自动下载...")
    FetchContent_Declare(SDL2_mixer
      GIT_REPOSITORY https://github.com/libsdl-org/SDL_mixer.git
      GIT_TAG release-2.8.1)
    FetchContent_MakeAvailable(SDL2_mixer)
endif()

add_executable(MySDLApp ${SOURCES} ${MAIN_FILE})

# 链接依赖库
target_link_libraries(MySDLApp
    SDL2
    SDL2_image
    SDL2_mixer
    angle
)

if(NOT CMAKE_C_STANDARD EQUAL 11)
    message(WARNING "未启用 C11 标准，可能导致编译错误")
endif()

if(SOURCES STREQUAL "")
    message(FATAL_ERROR "未找到任何 .c 源文件，请确认源码目录是否正确: ${SRC_DIR}")
endif()
