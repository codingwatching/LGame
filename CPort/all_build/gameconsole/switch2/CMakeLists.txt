cmake_minimum_required(VERSION 3.20)
project(MySwitch2App C)

set(CMAKE_C_STANDARD 11)
set(CMAKE_C_STANDARD_REQUIRED ON)

add_compile_options(-O3)

# 指定交叉编译目标为 Switch2
set(CMAKE_SYSTEM_NAME Generic)
set(CMAKE_SYSTEM_PROCESSOR aarch64)

# 检查 SWITCH2DEV 环境变量
if(NOT DEFINED ENV{SWITCH2DEV})
    message(FATAL_ERROR "未检测到 SWITCH2DEV 环境变量，请先安装 Switch2 SDK 并设置环境变量。")
endif()

set(SWITCH2DEV $ENV{SWITCH2DEV})
set(CMAKE_TOOLCHAIN_FILE "${SWITCH2DEV}/cmake/Switch2.cmake")

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

# =========================
# ANGLE
# =========================
message(STATUS "配置 ANGLE for Switch2...")
FetchContent_Declare(
  angle
  GIT_REPOSITORY https://github.com/google/angle.git
  GIT_TAG main
  DOWNLOAD_EXTRACT_TIMESTAMP TRUE
)
FetchContent_MakeAvailable(angle)

set(ANGLE_ENABLE_VULKAN ON CACHE BOOL "Enable Vulkan backend" FORCE)
set(ANGLE_ENABLE_OPENGL ON CACHE BOOL "Enable OpenGL backend" FORCE)
set(ANGLE_ENABLE_D3D11 OFF CACHE BOOL "Disable D3D11 backend" FORCE)
set(ANGLE_ENABLE_METAL OFF CACHE BOOL "Disable Metal backend" FORCE)

# SDL2/SDL3 检测与配置
find_package(SDL2 QUIET)
if(SDL2_FOUND)
    message(STATUS "检测到 SDL2，使用 SDL2 构建 (静态链接)")
    set(SDL_LIB SDL2::SDL2-static)
    set(SDL_IMAGE SDL2_image::SDL2_image-static)
    set(SDL_MIXER SDL2_mixer::SDL2_mixer-static)

    # SDL2_image
    set(SDL2IMAGE_AVIF OFF CACHE BOOL "" FORCE)
    set(SDL2IMAGE_WEBP OFF CACHE BOOL "" FORCE)
    set(SDL2IMAGE_TIF OFF CACHE BOOL "" FORCE)
    set(SDL2IMAGE_VENDORED OFF CACHE BOOL "" FORCE)
    find_package(SDL2_image QUIET)
    if(NOT SDL2_IMAGE_FOUND)
        FetchContent_Declare(SDL2_image
          GIT_REPOSITORY https://github.com/libsdl-org/SDL_image.git
          GIT_TAG release-2.8.8
          DOWNLOAD_EXTRACT_TIMESTAMP TRUE)
        FetchContent_MakeAvailable(SDL2_image)
    endif()

    # SDL2_mixer
    set(SDL2MIXER_WAVPACK OFF CACHE BOOL "" FORCE)
    find_package(SDL2_mixer QUIET)
    if(NOT SDL2_MIXER_FOUND)
        FetchContent_Declare(SDL2_mixer
          GIT_REPOSITORY https://github.com/libsdl-org/SDL_mixer.git
          GIT_TAG release-2.8.1
          DOWNLOAD_EXTRACT_TIMESTAMP TRUE)
        FetchContent_MakeAvailable(SDL2_mixer)
    endif()

else()
    message(STATUS "未检测到 SDL2，切换到 SDL3 构建 (静态链接)")
    set(SDL_LIB SDL3::SDL3-static)
    set(SDL_IMAGE SDL3_image::SDL3_image-static)
    set(SDL_MIXER SDL3_mixer::SDL3_mixer-static)

    # SDL3
    FetchContent_Declare(SDL3
      GIT_REPOSITORY https://github.com/libsdl-org/SDL.git
      GIT_TAG SDL3
      DOWNLOAD_EXTRACT_TIMESTAMP TRUE)
    FetchContent_MakeAvailable(SDL3)

    # SDL3_image
    set(SDL2IMAGE_AVIF OFF CACHE BOOL "" FORCE)
    set(SDL2IMAGE_WEBP OFF CACHE BOOL "" FORCE)
    set(SDL2IMAGE_TIF OFF CACHE BOOL "" FORCE)
    set(SDL2IMAGE_VENDORED OFF CACHE BOOL "" FORCE)
    FetchContent_Declare(SDL3_image
      GIT_REPOSITORY https://github.com/libsdl-org/SDL_image.git
      GIT_TAG SDL3
      DOWNLOAD_EXTRACT_TIMESTAMP TRUE)
    FetchContent_MakeAvailable(SDL3_image)

    # SDL3_mixer
    set(SDL2MIXER_WAVPACK OFF CACHE BOOL "" FORCE)
    FetchContent_Declare(SDL3_mixer
      GIT_REPOSITORY https://github.com/libsdl-org/SDL_mixer.git
      GIT_TAG SDL3
      DOWNLOAD_EXTRACT_TIMESTAMP TRUE)
    FetchContent_MakeAvailable(SDL3_mixer)
endif()

# 全局定义 pure_sdl 宏
add_compile_definitions(PURE_SDL=1)

# 构建 Switch2 可执行文件，只编译 all.c
add_executable(MySwitch2App ${MAIN_FILE})

# 在 Switch2 下使用静态链接，避免运行时依赖动态库
target_link_libraries(MySwitch2App
    ${SDL_LIB}
    ${SDL_IMAGE}
    ${SDL_MIXER}
    angle::libEGL-static
    angle::libGLESv2-static
)
