cmake_minimum_required(VERSION 3.20)
project(MySDLApp C)

set(CMAKE_C_STANDARD 11)
set(CMAKE_C_STANDARD_REQUIRED ON)

add_compile_options(-O3)

set(CMAKE_SYSTEM_NAME Generic)
set(CMAKE_SYSTEM_PROCESSOR aarch64)

if(NOT DEFINED ENV{SWITCH2DEV})
    message(FATAL_ERROR "未检测到 SWITCH2DEV 环境变量，请先安装 Switch2 SDK 并设置环境变量。")
endif()

set(SWITCH2DEV $ENV{SWITCH2DEV})
set(CMAKE_TOOLCHAIN_FILE "${SWITCH2DEV}/cmake/Switch2.cmake")

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

find_package(SDL2 QUIET)
if(SDL2_FOUND)
    message(STATUS "检测到 SDL2，使用 SDL2 构建")
    set(SDL_LIB SDL2)
    set(SDL_IMAGE SDL2_image)
    set(SDL_MIXER SDL2_mixer)

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

else()
    message(STATUS "未检测到 SDL2，切换到 SDL3 构建")
    set(SDL_LIB SDL3)
    set(SDL_IMAGE SDL3_image)
    set(SDL_MIXER SDL3_mixer)

    # SDL3
    FetchContent_Declare(SDL3
      GIT_REPOSITORY https://github.com/libsdl-org/SDL.git
      GIT_TAG SDL3)
    FetchContent_MakeAvailable(SDL3)

    # SDL3_image
    FetchContent_Declare(SDL3_image
      GIT_REPOSITORY https://github.com/libsdl-org/SDL_image.git
      GIT_TAG SDL3)
    FetchContent_MakeAvailable(SDL3_image)

    # SDL3_mixer
    FetchContent_Declare(SDL3_mixer
      GIT_REPOSITORY https://github.com/libsdl-org/SDL_mixer.git
      GIT_TAG SDL3)
    FetchContent_MakeAvailable(SDL3_mixer)

endif()

add_executable(MySDLApp ${SOURCES} ${MAIN_FILE})

target_link_libraries(MySDLApp
    ${SDL_LIB}
    ${SDL_IMAGE}
    ${SDL_MIXER}
    angle_gl
)
