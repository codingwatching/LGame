cmake_minimum_required(VERSION 3.15)
project(MySDLApp C)

set(CMAKE_C_STANDARD 11)
set(CMAKE_C_STANDARD_REQUIRED ON)

if(MSVC)
    message(STATUS "检测到 MSVC 编译器，使用 /Ox 优化 （约等于O3）")
    add_compile_options(/Ox)
elseif(CMAKE_C_COMPILER_ID STREQUAL "GNU" OR CMAKE_C_COMPILER_ID STREQUAL "Clang")
    message(STATUS "检测到 GCC/Clang 编译器，使用 -O3 优化")
    add_compile_options(-O3)
endif()

if(APPLE)
    set(CMAKE_OSX_DEPLOYMENT_TARGET 11.0)
    message(STATUS "设置 macOS 部署目标为 11.0")
endif()

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

set(FETCHCONTENT_QUIET OFF)
set(CMAKE_FETCHCONTENT_PROGRESS TRUE)

message(STATUS "配置 ANGLE...")
FetchContent_Declare(
  angle
  GIT_REPOSITORY https://github.com/google/angle.git
  GIT_TAG main
  DOWNLOAD_EXTRACT_TIMESTAMP TRUE
)
FetchContent_MakeAvailable(angle)

if(WIN32)
    # Windows 默认使用 D3D11
    set(ANGLE_ENABLE_D3D11 ON CACHE BOOL "Enable D3D11 backend" FORCE)
    set(ANGLE_ENABLE_OPENGL OFF CACHE BOOL "Disable OpenGL backend" FORCE)
    set(ANGLE_ENABLE_VULKAN OFF CACHE BOOL "Disable Vulkan backend" FORCE)
elseif(APPLE)
    # macOS 使用 Metal
    set(ANGLE_ENABLE_METAL ON CACHE BOOL "Enable Metal backend" FORCE)
    set(ANGLE_ENABLE_OPENGL OFF CACHE BOOL "Disable OpenGL backend" FORCE)
    set(ANGLE_ENABLE_VULKAN OFF CACHE BOOL "Disable Vulkan backend" FORCE)
else()
    # Linux 默认使用 OpenGL 或 Vulkan
    set(ANGLE_ENABLE_OPENGL ON CACHE BOOL "Enable OpenGL backend" FORCE)
    set(ANGLE_ENABLE_VULKAN ON CACHE BOOL "Enable Vulkan backend" FORCE)
endif()

FetchContent_Declare(
  SDL2
  GIT_REPOSITORY https://github.com/libsdl-org/SDL.git
  GIT_TAG release-2.32.10
  DOWNLOAD_EXTRACT_TIMESTAMP TRUE
)
FetchContent_MakeAvailable(SDL2)

FetchContent_Declare(
  SDL2_image
  GIT_REPOSITORY https://github.com/libsdl-org/SDL_image.git
  GIT_TAG release-2.8.8
  DOWNLOAD_EXTRACT_TIMESTAMP TRUE
)
FetchContent_MakeAvailable(SDL2_image)

FetchContent_Declare(
  SDL2_mixer
  GIT_REPOSITORY https://github.com/libsdl-org/SDL_mixer.git
  GIT_TAG release-2.8.1
  DOWNLOAD_EXTRACT_TIMESTAMP TRUE
)
FetchContent_MakeAvailable(SDL2_mixer)

add_executable(MySDLApp ${SOURCES} ${MAIN_FILE})

target_link_libraries(MySDLApp
    SDL2
    SDL2_image
    SDL2_mixer
    EGL
    GLESv2
)

if(NOT CMAKE_C_STANDARD EQUAL 11)
    message(WARNING "未启用 C11 标准，可能导致编译错误")
endif()

if(SOURCES STREQUAL "")
    message(FATAL_ERROR "未找到任何 .c 源文件，请确认源码目录是否正确: ${SRC_DIR}")
endif()
