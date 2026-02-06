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

# =========================
# ANGLE
# =========================
message(STATUS "配置 ANGLE for iOS...")
FetchContent_Declare(
  angle
  GIT_REPOSITORY https://github.com/google/angle.git
  GIT_TAG main
  DOWNLOAD_EXTRACT_TIMESTAMP TRUE
)
FetchContent_MakeAvailable(angle)

set(ANGLE_ENABLE_METAL ON CACHE BOOL "Enable Metal backend" FORCE)
set(ANGLE_ENABLE_OPENGL OFF CACHE BOOL "Disable OpenGL backend" FORCE)
set(ANGLE_ENABLE_VULKAN OFF CACHE BOOL "Disable Vulkan backend" FORCE)
set(ANGLE_ENABLE_D3D11 OFF CACHE BOOL "Disable D3D11 backend" FORCE)

# =========================
# SDL2
# =========================
FetchContent_Declare(
  SDL2
  GIT_REPOSITORY https://github.com/libsdl-org/SDL.git
  GIT_TAG release-2.32.10
  DOWNLOAD_EXTRACT_TIMESTAMP TRUE
)
FetchContent_MakeAvailable(SDL2)

# =========================
# SDL2_image
# =========================
FetchContent_Declare(
  SDL2_image
  GIT_REPOSITORY https://github.com/libsdl-org/SDL_image.git
  GIT_TAG release-2.8.8
  DOWNLOAD_EXTRACT_TIMESTAMP TRUE
)
FetchContent_MakeAvailable(SDL2_image)

# =========================
# SDL2_mixer
# =========================
FetchContent_Declare(
  SDL2_mixer
  GIT_REPOSITORY https://github.com/libsdl-org/SDL_mixer.git
  GIT_TAG release-2.8.1
  DOWNLOAD_EXTRACT_TIMESTAMP TRUE
)
FetchContent_MakeAvailable(SDL2_mixer)

add_library(MySDLApp STATIC ${SOURCES} ${MAIN_FILE})

target_link_libraries(MySDLApp
    SDL2
    SDL2_image
    SDL2_mixer
    EGL
    GLESv2
)
