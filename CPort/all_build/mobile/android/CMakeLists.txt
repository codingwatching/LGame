cmake_minimum_required(VERSION 3.15)
project(MyAndroidApp C)

set(CMAKE_C_STANDARD 11)
set(CMAKE_C_STANDARD_REQUIRED ON)

add_compile_options(-O3)

# 指定交叉编译目标为 Android
set(CMAKE_SYSTEM_NAME Android)
set(CMAKE_SYSTEM_VERSION 21)
set(CMAKE_ANDROID_ARCH_ABI arm64-v8a)
set(CMAKE_ANDROID_STL_TYPE c++_shared)

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

# 禁止SDL2编译test文件夹 
set(SDL_TEST OFF CACHE BOOL "" FORCE)

# SDL2
find_package(SDL2 QUIET)
if(NOT SDL2_FOUND)
    FetchContent_Declare(SDL2
      GIT_REPOSITORY https://github.com/libsdl-org/SDL.git
      GIT_TAG release-2.32.10)
    FetchContent_MakeAvailable(SDL2)
endif()

# SDL2_image
set(SDL2IMAGE_AVIF OFF CACHE BOOL "" FORCE)
set(SDL2IMAGE_WEBP OFF CACHE BOOL "" FORCE)
set(SDL2IMAGE_TIF OFF CACHE BOOL "" FORCE)
set(SDL2IMAGE_VENDORED OFF CACHE BOOL "" FORCE)
find_package(SDL2_image QUIET)
if(NOT SDL2_IMAGE_FOUND)
    FetchContent_Declare(SDL2_image
      GIT_REPOSITORY https://github.com/libsdl-org/SDL_image.git
      GIT_TAG release-2.8.8)
    FetchContent_MakeAvailable(SDL2_image)
endif()

# SDL2_mixer
set(SDL2MIXER_WAVPACK OFF CACHE BOOL "" FORCE)
find_package(SDL2_mixer QUIET)
if(NOT SDL2_MIXER_FOUND)
    FetchContent_Declare(SDL2_mixer
      GIT_REPOSITORY https://github.com/libsdl-org/SDL_mixer.git
      GIT_TAG release-2.8.1)
    FetchContent_MakeAvailable(SDL2_mixer)
endif()

# 全局定义 pure_sdl 宏
add_compile_definitions(PURE_SDL=1)

# 构建 Android 共享库（供 APK 使用），只编译 all.c
add_library(MyAndroidApp SHARED ${MAIN_FILE})

# 在 Android 下使用静态链接，避免运行时依赖外部 .so
target_link_libraries(MyAndroidApp
    SDL2::SDL2-static
    SDL2_image::SDL2_image-static
    SDL2_mixer::SDL2_mixer-static
    log
    android
)
