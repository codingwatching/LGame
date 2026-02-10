cmake_minimum_required(VERSION 3.15)
project(MyDesktopApp C)

set(CMAKE_C_STANDARD 11)
set(CMAKE_C_STANDARD_REQUIRED ON)

# 编译选项
if(MSVC)
    add_compile_options(/Ox /W4 /MP)
elseif(CMAKE_C_COMPILER_ID STREQUAL "GNU" OR CMAKE_C_COMPILER_ID STREQUAL "Clang")
    add_compile_options(-O3 -Wall)
endif()

# macOS 部署目标
if(APPLE)
    set(CMAKE_OSX_DEPLOYMENT_TARGET 11.0)
endif()

# 源码目录和入口文件
if(NOT DEFINED SRC_DIR)
    set(SRC_DIR ${CMAKE_SOURCE_DIR}/src)
endif()

set(MAIN_FILE ${SRC_DIR}/all.c)
if(NOT EXISTS ${MAIN_FILE})
    message(FATAL_ERROR "未找到入口文件 all.c，请确认源码目录是否正确: ${SRC_DIR}")
endif()

include(FetchContent)

# SDL2
set(SDL_TEST OFF CACHE BOOL "" FORCE)
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

# 输出目录
set(OUTPUT_DIR ${CMAKE_BINARY_DIR}/bin)

# 全局宏
add_compile_definitions(PURE_SDL=1)

# 可执行文件
add_executable(MyDesktopApp ${MAIN_FILE})

target_link_libraries(MyDesktopApp
    SDL2::SDL2
    SDL2_image::SDL2_image
    SDL2_mixer::SDL2_mixer
)

set_target_properties(MyDesktopApp PROPERTIES
    RUNTIME_OUTPUT_DIRECTORY ${OUTPUT_DIR}
    LIBRARY_OUTPUT_DIRECTORY ${OUTPUT_DIR}
    ARCHIVE_OUTPUT_DIRECTORY ${OUTPUT_DIR}
)

# 平台设置
if(WIN32)
    set_target_properties(MyDesktopApp PROPERTIES WIN32_EXECUTABLE TRUE)
    target_link_libraries(MyDesktopApp SDL2::SDL2main)

    add_custom_command(TARGET MyDesktopApp POST_BUILD
        COMMAND ${CMAKE_COMMAND} -E copy_if_different $<TARGET_FILE:SDL2::SDL2> $<TARGET_FILE_DIR:MyDesktopApp>
        COMMAND ${CMAKE_COMMAND} -E copy_if_different $<TARGET_FILE:SDL2_image::SDL2_image> $<TARGET_FILE_DIR:MyDesktopApp>
        COMMAND ${CMAKE_COMMAND} -E copy_if_different $<TARGET_FILE:SDL2_mixer::SDL2_mixer> $<TARGET_FILE_DIR:MyDesktopApp>
    )
elseif(APPLE)
    set_target_properties(MyDesktopApp PROPERTIES MACOSX_BUNDLE TRUE)

    find_library(COCOA_FRAMEWORK Cocoa)
    find_library(IOKIT_FRAMEWORK IOKit)
    find_library(COREVIDEO_FRAMEWORK CoreVideo)
    find_library(OPENGL_FRAMEWORK OpenGL)
    find_library(COREAUDIO_FRAMEWORK CoreAudio)
    find_library(AUDIOTOOLBOX_FRAMEWORK AudioToolbox)
    find_library(FORCEFEEDBACK_FRAMEWORK ForceFeedback)
    find_library(CARBON_FRAMEWORK Carbon)

    if(COCOA_FRAMEWORK)
        target_link_libraries(MyDesktopApp PRIVATE ${COCOA_FRAMEWORK})
    endif()
    if(IOKIT_FRAMEWORK)
        target_link_libraries(MyDesktopApp PRIVATE ${IOKIT_FRAMEWORK})
    endif()
    if(COREVIDEO_FRAMEWORK)
        target_link_libraries(MyDesktopApp PRIVATE ${COREVIDEO_FRAMEWORK})
    endif()
    if(OPENGL_FRAMEWORK)
        target_link_libraries(MyDesktopApp PRIVATE ${OPENGL_FRAMEWORK})
    endif()
    if(COREAUDIO_FRAMEWORK)
        target_link_libraries(MyDesktopApp PRIVATE ${COREAUDIO_FRAMEWORK})
    endif()
    if(AUDIOTOOLBOX_FRAMEWORK)
        target_link_libraries(MyDesktopApp PRIVATE ${AUDIOTOOLBOX_FRAMEWORK})
    endif()
    if(FORCEFEEDBACK_FRAMEWORK)
        target_link_libraries(MyDesktopApp PRIVATE ${FORCEFEEDBACK_FRAMEWORK})
    endif()
    if(CARBON_FRAMEWORK)
        target_link_libraries(MyDesktopApp PRIVATE ${CARBON_FRAMEWORK})
    endif()
elseif(UNIX)
    target_link_libraries(MyDesktopApp
        pthread
        dl
        m
    )
endif()

# 可选 ANGLE 支持
option(USE_ANGLE "Enable ANGLE support" OFF)

if(USE_ANGLE)
    if(WIN32)
        if(DEFINED ANGLE_DIR AND EXISTS "${ANGLE_DIR}/libEGL.lib" AND EXISTS "${ANGLE_DIR}/libGLESv2.lib")
            target_link_directories(MyDesktopApp PRIVATE ${ANGLE_DIR})
            target_link_libraries(MyDesktopApp libEGL libGLESv2)
        else()
            find_package(angle QUIET)
            if(angle_FOUND)
                target_link_libraries(MyDesktopApp angle::libEGL angle::libGLESv2)
            endif()
        endif()
    else()
        if(DEFINED ANGLE_DIR AND EXISTS "${ANGLE_DIR}/libEGL.a" AND EXISTS "${ANGLE_DIR}/libGLESv2.a")
            target_link_directories(MyDesktopApp PRIVATE ${ANGLE_DIR})
            target_link_libraries(MyDesktopApp EGL GLESv2)
        else()
            find_package(angle QUIET)
            if(angle_FOUND)
                target_link_libraries(MyDesktopApp angle::libEGL angle::libGLESv2)
            endif()
        endif()
    endif()
endif()
