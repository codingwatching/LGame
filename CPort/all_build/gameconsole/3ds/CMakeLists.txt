cmake_minimum_required(VERSION 3.15)
project(MySDLApp C)

set(CMAKE_C_STANDARD 11)
set(CMAKE_C_STANDARD_REQUIRED ON)
add_compile_options(-O3)

# 指定交叉编译目标为 3DS
set(CMAKE_SYSTEM_NAME Generic)
set(CMAKE_SYSTEM_PROCESSOR arm)

# 检查 DEVKITPRO 环境变量
if(NOT DEFINED ENV{DEVKITPRO})
    message(FATAL_ERROR "未检测到 DEVKITPRO 环境变量，请设置 DEVKITPRO 指向 devkitPro 根目录")
endif()

set(DEVKITPRO $ENV{DEVKITPRO})
set(CMAKE_TOOLCHAIN_FILE "${DEVKITPRO}/cmake/3DS.cmake")

# 源码目录
if(NOT DEFINED SRC_DIR)
    set(SRC_DIR ${CMAKE_SOURCE_DIR}/src)
endif()

# 入口文件 all.c
set(MAIN_FILE ${SRC_DIR}/all.c)
if(NOT EXISTS ${MAIN_FILE})
    message(FATAL_ERROR "未找到入口文件 all.c")
endif()

include(FetchContent)

# 禁止SDL2编译test文件夹 
set(SDL_TEST OFF CACHE BOOL "" FORCE)

set(FETCHCONTENT_BASE_DIR ${CMAKE_BINARY_DIR}/third_party)

# 限定搜索路径到 3DS devkitPro，避免误用主机 SDL2
set(CMAKE_PREFIX_PATH "${DEVKITPRO}/portlibs/3ds" ${CMAKE_PREFIX_PATH})

# SDL2 for 3DS
find_package(SDL2 QUIET)
if(SDL2_FOUND AND SDL2_INCLUDE_DIRS MATCHES "${DEVKITPRO}")
    message(STATUS "使用 DEVKITPRO 中的 SDL2 库")
    add_library(SDL2_3DS::SDL2 ALIAS SDL2::SDL2-static)
else()
    message(STATUS "未找到 DEVKITPRO SDL2，开始下载 SDL-3DS")
    FetchContent_Declare(
      SDL2_3DS
      GIT_REPOSITORY https://github.com/devkitPro/SDL.git
      GIT_TAG master
      SOURCE_DIR ${FETCHCONTENT_BASE_DIR}/sdl2_3ds-src
      BINARY_DIR ${FETCHCONTENT_BASE_DIR}/sdl2_3ds-build
    )
    FetchContent_MakeAvailable(SDL2_3DS)
    add_library(SDL2_3DS::SDL2 ALIAS SDL2::SDL2-static)
endif()

# SDL2_image
set(SDL2IMAGE_AVIF OFF CACHE BOOL "" FORCE)
set(SDL2IMAGE_WEBP OFF CACHE BOOL "" FORCE)
set(SDL2IMAGE_TIF OFF CACHE BOOL "" FORCE)
set(SDL2IMAGE_VENDORED OFF CACHE BOOL "" FORCE)
find_package(SDL2_image QUIET)
if(SDL2_IMAGE_FOUND AND SDL2_IMAGE_INCLUDE_DIRS MATCHES "${DEVKITPRO}")
    message(STATUS "使用 DEVKITPRO 中的 SDL2_image 库")
    add_library(SDL2_3DS::SDL2_image ALIAS SDL2_image::SDL2_image-static)
else()
    message(STATUS "未找到 DEVKITPRO SDL2_image，开始下载")
    FetchContent_Declare(
      SDL2_image_3DS
      GIT_REPOSITORY https://github.com/libsdl-org/SDL_image.git
      GIT_TAG release-2.8.8
      SOURCE_DIR ${FETCHCONTENT_BASE_DIR}/sdl2_image_3ds-src
      BINARY_DIR ${FETCHCONTENT_BASE_DIR}/sdl2_image_3ds-build
    )
    FetchContent_MakeAvailable(SDL2_image_3DS)
    add_library(SDL2_3DS::SDL2_image ALIAS SDL2_image::SDL2_image-static)
endif()

# SDL2_mixer
set(SDL2MIXER_WAVPACK OFF CACHE BOOL "" FORCE)
find_package(SDL2_mixer QUIET)
if(SDL2_MIXER_FOUND AND SDL2_MIXER_INCLUDE_DIRS MATCHES "${DEVKITPRO}")
    message(STATUS "使用 DEVKITPRO 中的 SDL2_mixer 库")
    add_library(SDL2_3DS::SDL2_mixer ALIAS SDL2_mixer::SDL2_mixer-static)
else()
    message(STATUS "未找到 DEVKITPRO SDL2_mixer，开始下载")
    FetchContent_Declare(
      SDL2_mixer_3DS
      GIT_REPOSITORY https://github.com/libsdl-org/SDL_mixer.git
      GIT_TAG release-2.8.1
      SOURCE_DIR ${FETCHCONTENT_BASE_DIR}/sdl2_mixer_3ds-src
      BINARY_DIR ${FETCHCONTENT_BASE_DIR}/sdl2_mixer_3ds-build
    )
    FetchContent_MakeAvailable(SDL2_mixer_3DS)
    add_library(SDL2_3DS::SDL2_mixer ALIAS SDL2_mixer::SDL2_mixer-static)
endif()

# 3DS 额外依赖库 (来自 devkitPro portlibs)
# SDL2_image 需要 libpng、libjpeg、zlib
# SDL2_mixer 需要 libogg、libvorbis、libmpg123
find_library(PNG_LIB png REQUIRED PATHS "${DEVKITPRO}/portlibs/3ds/lib")
find_library(JPEG_LIB jpeg REQUIRED PATHS "${DEVKITPRO}/portlibs/3ds/lib")
find_library(ZLIB_LIB z REQUIRED PATHS "${DEVKITPRO}/portlibs/3ds/lib")
find_library(OGG_LIB ogg REQUIRED PATHS "${DEVKITPRO}/portlibs/3ds/lib")
find_library(VORBIS_LIB vorbis REQUIRED PATHS "${DEVKITPRO}/portlibs/3ds/lib")
find_library(MPG123_LIB mpg123 REQUIRED PATHS "${DEVKITPRO}/portlibs/3ds/lib")

# 构建 3DS 可执行文件 (.3dsx 格式)，只编译 all.c
add_executable(MySDLApp ${MAIN_FILE})

# 在 3DS 下使用静态链接，避免运行时依赖动态库
target_link_libraries(MySDLApp
    SDL2_3DS::SDL2
    SDL2_3DS::SDL2_image
    SDL2_3DS::SDL2_mixer
    ${PNG_LIB}
    ${JPEG_LIB}
    ${ZLIB_LIB}
    ${OGG_LIB}
    ${VORBIS_LIB}
    ${MPG123_LIB}
    citro2d
    citro3d
    ctru
    m
)
