cmake_minimum_required(VERSION 3.15)
project(MySDLApp C)

set(CMAKE_C_STANDARD 11)
set(CMAKE_C_STANDARD_REQUIRED ON)

if(MSVC)
    add_compile_options(/Ox /W4 /MP)
elseif(CMAKE_C_COMPILER_ID STREQUAL "GNU" OR CMAKE_C_COMPILER_ID STREQUAL "Clang")
    add_compile_options(-O3 -Wall)
endif()

if(APPLE)
    set(CMAKE_OSX_DEPLOYMENT_TARGET 11.0)
endif()

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
    FetchContent_Declare(
      SDL2
      GIT_REPOSITORY https://github.com/libsdl-org/SDL.git
      GIT_TAG release-2.32.10
    )
    FetchContent_MakeAvailable(SDL2)
endif()

# SDL2_image
set(SDL2IMAGE_AVIF OFF CACHE BOOL "" FORCE)
set(SDL2IMAGE_WEBP OFF CACHE BOOL "" FORCE)
set(SDL2IMAGE_TIF OFF CACHE BOOL "" FORCE)
set(SDL2IMAGE_VENDORED OFF CACHE BOOL "" FORCE)

if(MSVC)
    enable_language(ASM_MASM)
    set(CMAKE_ASM_MASM_FLAGS "")
    set(CMAKE_ASM_MASM_FLAGS_RELEASE "")
    set(CMAKE_ASM_MASM_FLAGS_DEBUG "")
    set(CMAKE_POLICY_DEFAULT_CMP0091 NEW)
    set(CMAKE_MSVC_RUNTIME_LIBRARY "MultiThreaded$<$<CONFIG:Debug>:Debug>DLL")
    set_property(GLOBAL PROPERTY ASM_MASM_SKIP_MSVC_RUNTIME_LIBRARY ON)
endif()

find_package(SDL2_image QUIET)
if(NOT SDL2_image_FOUND)
    FetchContent_Declare(
      SDL2_image
      GIT_REPOSITORY https://github.com/libsdl-org/SDL_image.git
      GIT_TAG release-2.8.8
    )
    FetchContent_MakeAvailable(SDL2_image)
endif()

# SDL2_mixer
set(SDL2MIXER_WAVPACK OFF CACHE BOOL "" FORCE)

find_package(SDL2_mixer QUIET)
if(NOT SDL2_mixer_FOUND)
    FetchContent_Declare(
      SDL2_mixer
      GIT_REPOSITORY https://github.com/libsdl-org/SDL_mixer.git
      GIT_TAG release-2.8.1
    )
    FetchContent_MakeAvailable(SDL2_mixer)
endif()

# 设置统一输出目录
set(OUTPUT_DIR ${CMAKE_BINARY_DIR}/bin)

add_executable(MySDLApp ${MAIN_FILE})

target_link_libraries(MySDLApp
    SDL2::SDL2
    SDL2_image::SDL2_image
    SDL2_mixer::SDL2_mixer
)

# 设置输出目录
set_target_properties(MySDLApp PROPERTIES
    RUNTIME_OUTPUT_DIRECTORY ${OUTPUT_DIR}
    LIBRARY_OUTPUT_DIRECTORY ${OUTPUT_DIR}
    ARCHIVE_OUTPUT_DIRECTORY ${OUTPUT_DIR}
)

# 平台设置
if(WIN32)
    set_target_properties(MySDLApp PROPERTIES WIN32_EXECUTABLE TRUE)
    target_link_libraries(MySDLApp SDL2::SDL2main)

    # 自动拷贝 DLL 到输出目录
    add_custom_command(TARGET MySDLApp POST_BUILD
        COMMAND ${CMAKE_COMMAND} -E copy_if_different $<TARGET_FILE:SDL2::SDL2> $<TARGET_FILE_DIR:MySDLApp>
        COMMAND ${CMAKE_COMMAND} -E copy_if_different $<TARGET_FILE:SDL2_image::SDL2_image> $<TARGET_FILE_DIR:MySDLApp>
        COMMAND ${CMAKE_COMMAND} -E copy_if_different $<TARGET_FILE:SDL2_mixer::SDL2_mixer> $<TARGET_FILE_DIR:MySDLApp>
    )
elseif(APPLE)
    set_target_properties(MySDLApp PROPERTIES MACOSX_BUNDLE TRUE)
    target_link_libraries(MySDLApp
        "-framework Cocoa"
        "-framework IOKit"
        "-framework CoreVideo"
        "-framework CoreAudio"
        "-framework AudioToolbox"
        "-framework ForceFeedback"
        "-framework Carbon"
        "-framework OpenGL"
    )
elseif(UNIX)
    target_link_libraries(MySDLApp
        pthread
        dl
        m
    )
endif()

# 可选 ANGLE 支持
option(USE_ANGLE "Enable ANGLE support" OFF)

if(USE_ANGLE)
    if(WIN32)
        # Windows 下使用 .lib 文件
        if(DEFINED ANGLE_DIR AND EXISTS "${ANGLE_DIR}/libEGL.lib" AND EXISTS "${ANGLE_DIR}/libGLESv2.lib")
            target_link_directories(MySDLApp PRIVATE ${ANGLE_DIR})
            target_link_libraries(MySDLApp libEGL libGLESv2)
        else()
            find_package(angle QUIET)
            if(angle_FOUND)
                target_link_libraries(MySDLApp angle::libEGL angle::libGLESv2)
            endif()
        endif()
    else()
        # 类 Unix 平台使用 .a 文件
        if(DEFINED ANGLE_DIR AND EXISTS "${ANGLE_DIR}/libEGL.a" AND EXISTS "${ANGLE_DIR}/libGLESv2.a")
            target_link_directories(MySDLApp PRIVATE ${ANGLE_DIR})
            target_link_libraries(MySDLApp EGL GLESv2)
        else()
            find_package(angle QUIET)
            if(angle_FOUND)
                target_link_libraries(MySDLApp angle::libEGL angle::libGLESv2)
            endif()
        endif()
    endif()
endif()
