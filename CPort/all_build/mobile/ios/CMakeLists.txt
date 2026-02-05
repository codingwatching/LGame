cmake_minimum_required(VERSION 3.15)
project(MySDLApp C)

set(CMAKE_C_STANDARD 11)
set(CMAKE_C_STANDARD_REQUIRED ON)
add_compile_options(-O3)

set(CMAKE_SYSTEM_NAME iOS)
set(CMAKE_OSX_SYSROOT iphoneos)
set(CMAKE_OSX_ARCHITECTURES arm64)
set(CMAKE_OSX_DEPLOYMENT_TARGET 11.0)

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

include(FetchContent)

find_package(ANGLE QUIET)
if(NOT ANGLE_FOUND)
    FetchContent_Declare(angle GIT_REPOSITORY https://github.com/google/angle.git GIT_TAG main)
    FetchContent_MakeAvailable(angle)
endif()

find_package(SDL2 QUIET)
if(NOT SDL2_FOUND)
    FetchContent_Declare(SDL2 GIT_REPOSITORY https://github.com/libsdl-org/SDL.git GIT_TAG release-2.32.10)
    FetchContent_MakeAvailable(SDL2)
endif()

find_package(SDL2_image QUIET)
if(NOT SDL2_IMAGE_FOUND)
    FetchContent_Declare(SDL2_image GIT_REPOSITORY https://github.com/libsdl-org/SDL_image.git GIT_TAG release-2.8.8)
    FetchContent_MakeAvailable(SDL2_image)
endif()

find_package(SDL2_mixer QUIET)
if(NOT SDL2_MIXER_FOUND)
    FetchContent_Declare(SDL2_mixer GIT_REPOSITORY https://github.com/libsdl-org/SDL_mixer.git GIT_TAG release-2.8.1)
    FetchContent_MakeAvailable(SDL2_mixer)
endif()

#构建iOS静态库（供Xcode项目使用）
add_library(MySDLApp STATIC ${SOURCES} ${MAIN_FILE})

target_link_libraries(MySDLApp
    SDL2
    SDL2_image
    SDL2_mixer
    angle
)
