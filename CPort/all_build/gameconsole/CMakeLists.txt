cmake_minimum_required(VERSION 3.15)
project(MySDLApp C)

set(CMAKE_C_STANDARD 11)
set(CMAKE_C_STANDARD_REQUIRED ON)
add_compile_options(-O3)

# 源码目录
if(NOT DEFINED SRC_DIR)
    set(SRC_DIR ${CMAKE_SOURCE_DIR}/src)
endif()

# 入口文件 all.c
set(MAIN_FILE ${SRC_DIR}/all.c)
if(NOT EXISTS ${MAIN_FILE})
    message(FATAL_ERROR "未找到入口文件 all.c，请确认源码目录是否正确: ${SRC_DIR}")
endif()

# 平台选择
if(NOT DEFINED PLATFORM)
    message(FATAL_ERROR "请通过 -DPLATFORM=<PS4|PS5|PSV|PSP|SWITCH|3DS|XBOX|DREAMCAST> 指定平台")
endif()

include(FetchContent)

# 禁止SDL2编译test文件夹 
set(SDL_TEST OFF CACHE BOOL "" FORCE)

if(PLATFORM STREQUAL "PS4")
    set(CMAKE_TOOLCHAIN_FILE "$ENV{SCE_PS4_SDK_DIR}/share/ps4.toolchain.cmake")
    set(SDL2_REPO https://github.com/ps4dev/SDL.git)
    set(SDL2_TAG master)

elseif(PLATFORM STREQUAL "PS5")
    set(CMAKE_TOOLCHAIN_FILE "$ENV{SCE_PS5_SDK_DIR}/share/ps5.toolchain.cmake")
    set(SDL2_REPO https://github.com/ps5-payload-dev/SDL.git)
    set(SDL2_TAG master)

elseif(PLATFORM STREQUAL "PSV")
    set(CMAKE_TOOLCHAIN_FILE "$ENV{VITASDK}/share/vita.toolchain.cmake")
    set(SDL2_REPO https://github.com/vitasdk/SDL.git)
    set(SDL2_TAG master)

elseif(PLATFORM STREQUAL "PSP")
    set(CMAKE_C_COMPILER "$ENV{PSPSDK}/bin/psp-gcc")
    set(CMAKE_CXX_COMPILER "$ENV{PSPSDK}/bin/psp-g++")
    set(SDL2_REPO https://github.com/pspdev/SDL.git)
    set(SDL2_TAG master)

elseif(PLATFORM STREQUAL "SWITCH")
    set(CMAKE_TOOLCHAIN_FILE "$ENV{DEVKITPRO}/cmake/Switch.cmake")
    set(SDL2_REPO https://github.com/devkitPro/SDL.git)
    set(SDL2_TAG master)

elseif(PLATFORM STREQUAL "3DS")
    set(CMAKE_TOOLCHAIN_FILE "$ENV{DEVKITPRO}/cmake/3DS.cmake")
    set(SDL2_REPO https://github.com/devkitPro/SDL.git)
    set(SDL2_TAG master)

elseif(PLATFORM STREQUAL "XBOX")
    set(CMAKE_C_COMPILER "$ENV{XBOX_SDK_DIR}/bin/xbox-clang")
    set(CMAKE_CXX_COMPILER "$ENV{XBOX_SDK_DIR}/bin/xbox-clang++")
    set(SDL2_REPO https://github.com/xboxdev/SDL.git)
    set(SDL2_TAG master)

elseif(PLATFORM STREQUAL "DREAMCAST")
    set(CMAKE_TOOLCHAIN_FILE "$ENV{KOS_BASE}/cmake/dreamcast.toolchain.cmake")
    set(SDL2_REPO https://github.com/KallistiOS/SDL.git)
    set(SDL2_TAG master)

else()
    message(FATAL_ERROR "未知平台: ${PLATFORM}")
endif()

# SDL2
find_package(SDL2 QUIET)
if(NOT SDL2_FOUND)
  FetchContent_Declare(SDL2_Platform
    GIT_REPOSITORY ${SDL2_REPO}
    GIT_TAG ${SDL2_TAG})
  FetchContent_MakeAvailable(SDL2_Platform)
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

# 构建可执行文件，只编译 all.c
add_executable(MySDLApp ${MAIN_FILE})

# 静态链接 SDL2 系列库
target_link_libraries(MySDLApp
    SDL2::SDL2-static
    SDL2_image::SDL2_image-static
    SDL2_mixer::SDL2_mixer-static
)

# 可选 ANGLE 支持
option(USE_ANGLE "Enable ANGLE support" OFF)

if(USE_ANGLE)
    message(STATUS "启用 ANGLE 支持 (静态链接)")
    if(DEFINED ANGLE_DIR AND EXISTS "${ANGLE_DIR}/libEGL.a" AND EXISTS "${ANGLE_DIR}/libGLESv2.a")
        target_link_directories(MySDLApp PRIVATE ${ANGLE_DIR})
        target_link_libraries(MySDLApp EGL GLESv2)
    else()
        find_package(angle QUIET)
        if(angle_FOUND)
            target_link_libraries(MySDLApp angle::libEGL-static angle::libGLESv2-static)
        endif()
    endif()
endif()
