cmake_minimum_required(VERSION 3.15)
project(MyAndroidApp C)

set(CMAKE_C_STANDARD 11)
set(CMAKE_C_STANDARD_REQUIRED ON)

add_compile_options(-O3)

# ---------------- Android 交叉编译配置 ----------------
set(CMAKE_SYSTEM_NAME Android)

# 平台版本由外部传入，默认 23
if(NOT DEFINED ANDROID_PLATFORM)
    set(ANDROID_PLATFORM 23)
endif()
set(CMAKE_SYSTEM_VERSION ${ANDROID_PLATFORM})

if(NOT DEFINED ANDROID_ABI)
    set(ANDROID_ABI arm64-v8a)
endif()
set(ANDROID_ABI ${ANDROID_ABI})

# ---------------- 源码目录 ----------------
if(NOT DEFINED SRC_DIR)
    set(SRC_DIR ${CMAKE_SOURCE_DIR}/src)
endif()

set(MAIN_FILE ${SRC_DIR}/all.c)
if(NOT EXISTS ${MAIN_FILE})
    message(FATAL_ERROR "未找到入口文件 all.c，请确认源码目录是否正确: ${SRC_DIR}")
endif()

include(FetchContent)

# ---------------- SDL2 相关依赖 ----------------
# 强制静态编译
set(SDL_SHARED OFF CACHE BOOL "" FORCE)
set(SDL_STATIC ON CACHE BOOL "" FORCE)

set(SDL2IMAGE_SHARED OFF CACHE BOOL "" FORCE)
set(SDL2IMAGE_STATIC ON CACHE BOOL "" FORCE)

set(SDL2MIXER_SHARED OFF CACHE BOOL "" FORCE)
set(SDL2MIXER_STATIC ON CACHE BOOL "" FORCE)

# 禁止 SDL2 编译 test 文件夹
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

# ---------------- 编译选项与宏定义 ----------------
add_compile_definitions(PURE_SDL=1)

# ---------------- 构建共享库 ----------------
add_library(MyAndroidApp SHARED ${MAIN_FILE})

# 链接静态 SDL 库和 Android 系统库
target_link_libraries(MyAndroidApp
    SDL2::SDL2
    SDL2_image::SDL2_image
    SDL2_mixer::SDL2_mixer
    log
    android
)

# ---------------- 输出路径 ----------------
set_target_properties(MyAndroidApp PROPERTIES
    LIBRARY_OUTPUT_DIRECTORY ${CMAKE_BINARY_DIR}
    RUNTIME_OUTPUT_DIRECTORY ${CMAKE_BINARY_DIR}
)
