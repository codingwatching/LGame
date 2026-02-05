cmake_minimum_required(VERSION 3.15)
project(MySDLApp C)

set(CMAKE_C_STANDARD 11)
set(CMAKE_C_STANDARD_REQUIRED ON)
add_compile_options(-O3)

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
    message(FATAL_ERROR "未找到入口文件 all.c，请确认源码目录是否正确: ${SRC_DIR}")
endif()

# 平台选择
if(NOT DEFINED PLATFORM)
    message(FATAL_ERROR "请通过 -DPLATFORM=<PS4|PS5|PSV|PSP|SWITCH|3DS|XBOX|DREAMCAST> 指定平台")
endif()

include(FetchContent)

if(PLATFORM STREQUAL "PS4")
    set(CMAKE_TOOLCHAIN_FILE "$ENV{SCE_PS4_SDK_DIR}/share/ps4.toolchain.cmake")
    set(SDL2_REPO https://github.com/ps4dev/SDL.git)
    set(SDL2_TAG release-2.30.x-ps4)

elseif(PLATFORM STREQUAL "PS5")
    set(CMAKE_TOOLCHAIN_FILE "$ENV{SCE_PS5_SDK_DIR}/share/ps5.toolchain.cmake")
    set(SDL2_REPO https://github.com/ps5-payload-dev/SDL.git)
    set(SDL2_TAG release-2.30.x-ps5)

elseif(PLATFORM STREQUAL "PSV")
    set(CMAKE_TOOLCHAIN_FILE "$ENV{VITASDK}/share/vita.toolchain.cmake")
    set(SDL2_REPO https://github.com/vitasdk/SDL.git)
    set(SDL2_TAG vita-2.30.x)

elseif(PLATFORM STREQUAL "PSP")
    set(CMAKE_C_COMPILER "$ENV{PSPSDK}/bin/psp-gcc")
    set(CMAKE_CXX_COMPILER "$ENV{PSPSDK}/bin/psp-g++")
    set(SDL2_REPO https://github.com/pspdev/SDL.git)
    set(SDL2_TAG psp-2.30.x)

elseif(PLATFORM STREQUAL "SWITCH")
    set(CMAKE_TOOLCHAIN_FILE "$ENV{DEVKITPRO}/cmake/Switch.cmake")
    set(SDL2_REPO https://github.com/devkitPro/SDL.git)
    set(SDL2_TAG switch-2.30.x)

elseif(PLATFORM STREQUAL "3DS")
    set(CMAKE_TOOLCHAIN_FILE "$ENV{DEVKITPRO}/cmake/3DS.cmake")
    set(SDL2_REPO https://github.com/devkitPro/SDL.git)
    set(SDL2_TAG 3ds-2.30.x)

elseif(PLATFORM STREQUAL "XBOX")
    set(CMAKE_C_COMPILER "$ENV{XBOX_SDK_DIR}/bin/xbox-clang")
    set(CMAKE_CXX_COMPILER "$ENV{XBOX_SDK_DIR}/bin/xbox-clang++")
    set(SDL2_REPO https://github.com/xboxdev/SDL.git)
    set(SDL2_TAG xbox-2.30.x)

elseif(PLATFORM STREQUAL "DREAMCAST")
    set(CMAKE_TOOLCHAIN_FILE "$ENV{KOS_BASE}/cmake/dreamcast.toolchain.cmake")
    set(SDL2_REPO https://github.com/KallistiOS/SDL.git)
    set(SDL2_TAG dc-2.30.x)

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

add_executable(MySDLApp ${SOURCES} ${MAIN_FILE})
target_link_libraries(MySDLApp SDL2 SDL2_image SDL2_mixer)
