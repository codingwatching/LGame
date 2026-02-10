cmake_minimum_required(VERSION 3.15)
project(MyUWPApp C)

set(CMAKE_C_STANDARD 11)
set(CMAKE_C_STANDARD_REQUIRED ON)

if(MSVC)
    add_compile_options(/Ox)
endif()

# 指定 UWP 平台
set(CMAKE_SYSTEM_NAME WindowsStore)
set(CMAKE_SYSTEM_VERSION 10.0)
set(CMAKE_GENERATOR_PLATFORM x64)

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
    message(STATUS "未找到 SDL2，自动下载...")
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
    message(STATUS "未找到 SDL2_image，自动下载...")
    FetchContent_Declare(SDL2_image
      GIT_REPOSITORY https://github.com/libsdl-org/SDL_image.git
      GIT_TAG release-2.8.8)
    FetchContent_MakeAvailable(SDL2_image)
endif()

# SDL2_mixer
set(SDL2MIXER_WAVPACK OFF CACHE BOOL "" FORCE)
find_package(SDL2_mixer QUIET)
if(NOT SDL2_MIXER_FOUND)
    message(STATUS "未找到 SDL2_mixer，自动下载...")
    FetchContent_Declare(SDL2_mixer
      GIT_REPOSITORY https://github.com/libsdl-org/SDL_mixer.git
      GIT_TAG release-2.8.1)
    FetchContent_MakeAvailable(SDL2_mixer)
endif()

# 第三方依赖库 (UWP 下也需要静态链接)
find_library(PNG_LIB png REQUIRED)
find_library(JPEG_LIB jpeg REQUIRED)
find_library(ZLIB_LIB z REQUIRED)
find_library(OGG_LIB ogg REQUIRED)
find_library(VORBIS_LIB vorbis REQUIRED)
find_library(MPG123_LIB mpg123 REQUIRED)

# 全局定义 pure_sdl 宏
add_compile_definitions(PURE_SDL=1)

add_executable(MyUWPApp ${MAIN_FILE})

target_link_libraries(MyUWPApp
    SDL2::SDL2
    SDL2_image::SDL2_image
    SDL2_mixer::SDL2_mixer
)

if(WIN32)
    target_link_libraries(MyUWPApp SDL2::SDL2main)

    set(OUTPUT_DIR ${CMAKE_BINARY_DIR}/bin)
    set_target_properties(MyUWPApp PROPERTIES
        RUNTIME_OUTPUT_DIRECTORY ${OUTPUT_DIR}
        LIBRARY_OUTPUT_DIRECTORY ${OUTPUT_DIR}
        ARCHIVE_OUTPUT_DIRECTORY ${OUTPUT_DIR}
    )

    foreach(target SDL2 SDL2_image SDL2_mixer)
        if(TARGET ${target})
            set_target_properties(${target} PROPERTIES
                RUNTIME_OUTPUT_DIRECTORY ${OUTPUT_DIR}
                LIBRARY_OUTPUT_DIRECTORY ${OUTPUT_DIR}
                ARCHIVE_OUTPUT_DIRECTORY ${OUTPUT_DIR}
            )
        endif()
    endforeach()

    if(NOT CMAKE_SYSTEM_NAME STREQUAL "WindowsStore")
        add_custom_command(TARGET MyUWPApp POST_BUILD
            COMMAND ${CMAKE_COMMAND} -E copy_if_different $<TARGET_FILE:SDL2::SDL2> $<TARGET_FILE_DIR:MyUWPApp>
            COMMAND ${CMAKE_COMMAND} -E copy_if_different $<TARGET_FILE:SDL2_image::SDL2_image> $<TARGET_FILE_DIR:MyUWPApp>
            COMMAND ${CMAKE_COMMAND} -E copy_if_different $<TARGET_FILE:SDL2_mixer::SDL2_mixer> $<TARGET_FILE_DIR:MyUWPApp>
        )
    else()
        message(STATUS "UWP 模式下使用 SDL 静态库链接，避免 DLL 依赖。")
        target_link_libraries(MyUWPApp
            SDL2::SDL2-static
            SDL2_image::SDL2_image-static
            SDL2_mixer::SDL2_mixer-static
            ${PNG_LIB}
            ${JPEG_LIB}
            ${ZLIB_LIB}
            ${OGG_LIB}
            ${VORBIS_LIB}
            ${MPG123_LIB}
        )
    endif()
endif()

set_target_properties(MyUWPApp PROPERTIES
    VS_WINDOWS_TARGET_PLATFORM_MIN_VERSION 10.0.17763.0
    VS_WINDOWS_TARGET_PLATFORM_VERSION 10.0
)
