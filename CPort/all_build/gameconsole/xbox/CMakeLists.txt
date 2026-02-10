cmake_minimum_required(VERSION 3.15)
project(MyXBoxApp C)

set(CMAKE_C_STANDARD 11)
set(CMAKE_C_STANDARD_REQUIRED ON)
set(CMAKE_SYSTEM_PROCESSOR x86_64)

if(MSVC)
    add_compile_options(/Ox)
endif()

# 检查 Xbox SDK 环境变量
if(DEFINED ENV{GDKXSDK})
    message(STATUS "检测到 GDKX SDK，使用 GDKX 工具链")
    set(CMAKE_SYSTEM_NAME WindowsStore)
    set(CMAKE_SYSTEM_VERSION 10.0)
    set(CMAKE_GENERATOR_PLATFORM Gaming.Desktop.x64)
    set(GDKXSDK $ENV{GDKXSDK})
    set(CMAKE_TOOLCHAIN_FILE "${GDKXSDK}/share/gdkx.toolchain.cmake")
    set(XBOXSDK_PATH "${GDKXSDK}")
elseif(DEFINED ENV{XDKSDK})
    message(STATUS "检测到 XDK SDK，使用 XDK 工具链")
    set(CMAKE_SYSTEM_NAME Generic)
    set(CMAKE_SYSTEM_VERSION 10.0)
    set(CMAKE_GENERATOR_PLATFORM Xbox360)
    set(XDKSDK $ENV{XDKSDK})
    set(CMAKE_TOOLCHAIN_FILE "${XDKSDK}/share/xdk.toolchain.cmake")
    set(XBOXSDK_PATH "${XDKSDK}")
elseif(DEFINED ENV{NXDK})
    message(STATUS "检测到 NXDK 环境，使用原始 Xbox 工具链")
    set(CMAKE_SYSTEM_NAME Generic)
    set(XBOXSDK_PATH $ENV{NXDK})
else()
    message(FATAL_ERROR "未检测到 GDKX、XDK 或 NXDK SDK，请设置环境变量")
endif()

# 源码目录
if(NOT DEFINED SRC_DIR)
    set(SRC_DIR ${CMAKE_SOURCE_DIR}/src)
endif()

file(GLOB_RECURSE SOURCES ${SRC_DIR}/*.c)

set(MAIN_FILE ${SRC_DIR}/all.c)
if(NOT EXISTS ${MAIN_FILE})
    message(FATAL_ERROR "未找到入口文件 all.c")
endif()

include(FetchContent)

# 禁止 SDL2 编译 test 文件夹
set(SDL_TEST OFF CACHE BOOL "" FORCE)

set(FETCHCONTENT_BASE_DIR ${CMAKE_BINARY_DIR}/third_party)

# 限定搜索路径到 Xbox SDK，避免误用主机 SDL2
set(CMAKE_PREFIX_PATH "${XBOXSDK_PATH}" ${CMAKE_PREFIX_PATH})

# SDL2
find_package(SDL2 QUIET)
if(SDL2_FOUND AND SDL2_INCLUDE_DIRS MATCHES "${XBOXSDK_PATH}")
    message(STATUS "使用 Xbox SDK 中的 SDL2 库")
    add_library(SDL2_Xbox::SDL2 ALIAS SDL2::SDL2-static)
elseif(DEFINED ENV{NXDK})
    message(STATUS "未找到 Xbox SDK SDL2，开始下载 nxdk-sdl (原始 Xbox)")
    FetchContent_Declare(
      SDL2_Xbox
      GIT_REPOSITORY https://github.com/XboxDev/nxdk-sdl.git
      GIT_TAG master
      SOURCE_DIR ${FETCHCONTENT_BASE_DIR}/sdl2_xbox-src
      BINARY_DIR ${FETCHCONTENT_BASE_DIR}/sdl2_xbox-build
    )
    FetchContent_GetProperties(SDL2_Xbox)
    if(NOT SDL2_Xbox_POPULATED)
        FetchContent_Populate(SDL2_Xbox)
        file(GLOB NXDK_SDL_SRC ${SDL2_Xbox_SOURCE_DIR}/src/*.c)
        add_library(SDL2_Xbox::SDL2 STATIC ${NXDK_SDL_SRC})
        target_include_directories(SDL2_Xbox::SDL2 PUBLIC
            ${SDL2_Xbox_SOURCE_DIR}/include
        )
    endif()
else()
    message(STATUS "未找到 Xbox SDK SDL2，开始下载官方 SDL2 源码")
    FetchContent_Declare(
      SDL2_Xbox
      GIT_REPOSITORY https://github.com/libsdl-org/SDL.git
      GIT_TAG release-2.32.10
      SOURCE_DIR ${FETCHCONTENT_BASE_DIR}/sdl2_xbox-src
      BINARY_DIR ${FETCHCONTENT_BASE_DIR}/sdl2_xbox-build
    )
    FetchContent_MakeAvailable(SDL2_Xbox)
    add_library(SDL2_Xbox::SDL2 ALIAS SDL2::SDL2-static)
endif()

# SDL2_image
set(SDL2IMAGE_AVIF OFF CACHE BOOL "" FORCE)
set(SDL2IMAGE_WEBP OFF CACHE BOOL "" FORCE)
set(SDL2IMAGE_TIF OFF CACHE BOOL "" FORCE)
set(SDL2IMAGE_VENDORED OFF CACHE BOOL "" FORCE)
find_package(SDL2_image QUIET)
if(SDL2_IMAGE_FOUND AND SDL2_IMAGE_INCLUDE_DIRS MATCHES "${XBOXSDK_PATH}")
    message(STATUS "使用 Xbox SDK 中的 SDL2_image 库")
    add_library(SDL2_Xbox::SDL2_image ALIAS SDL2_image::SDL2_image-static)
else()
    message(STATUS "未找到 Xbox SDK SDL2_image，开始下载")
    FetchContent_Declare(
      SDL2_image_Xbox
      GIT_REPOSITORY https://github.com/libsdl-org/SDL_image.git
      GIT_TAG release-2.8.8
      SOURCE_DIR ${FETCHCONTENT_BASE_DIR}/sdl2_image_xbox-src
      BINARY_DIR ${FETCHCONTENT_BASE_DIR}/sdl2_image_xbox-build
    )
    FetchContent_MakeAvailable(SDL2_image_Xbox)
    add_library(SDL2_Xbox::SDL2_image ALIAS SDL2_image::SDL2_image-static)
endif()

# SDL2_mixer
set(SDL2MIXER_WAVPACK OFF CACHE BOOL "" FORCE)
find_package(SDL2_mixer QUIET)
if(SDL2_MIXER_FOUND AND SDL2_MIXER_INCLUDE_DIRS MATCHES "${XBOXSDK_PATH}")
    message(STATUS "使用 Xbox SDK 中的 SDL2_mixer 库")
    add_library(SDL2_Xbox::SDL2_mixer ALIAS SDL2_mixer::SDL2_mixer-static)
else()
    message(STATUS "未找到 Xbox SDK SDL2_mixer，开始下载")
    FetchContent_Declare(
      SDL2_mixer_Xbox
      GIT_REPOSITORY https://github.com/libsdl-org/SDL_mixer.git
      GIT_TAG release-2.8.1
      SOURCE_DIR ${FETCHCONTENT_BASE_DIR}/sdl2_mixer_xbox-src
      BINARY_DIR ${FETCHCONTENT_BASE_DIR}/sdl2_mixer_xbox-build
    )
    FetchContent_MakeAvailable(SDL2_mixer_Xbox)
    add_library(SDL2_Xbox::SDL2_mixer ALIAS SDL2_mixer::SDL2_mixer-static)
endif()

# 查找第三方依赖库 (仅在 XDK/NXDK 环境下需要)
if(DEFINED ENV{XDKSDK} OR DEFINED ENV{NXDK})
    find_library(PNG_LIB png REQUIRED PATHS "${XBOXSDK_PATH}/lib" "${XBOXSDK_PATH}/portlibs/lib")
    find_library(JPEG_LIB jpeg REQUIRED PATHS "${XBOXSDK_PATH}/lib" "${XBOXSDK_PATH}/portlibs/lib")
    find_library(ZLIB_LIB z REQUIRED PATHS "${XBOXSDK_PATH}/lib" "${XBOXSDK_PATH}/portlibs/lib")
    find_library(OGG_LIB ogg REQUIRED PATHS "${XBOXSDK_PATH}/lib" "${XBOXSDK_PATH}/portlibs/lib")
    find_library(VORBIS_LIB vorbis REQUIRED PATHS "${XBOXSDK_PATH}/lib" "${XBOXSDK_PATH}/portlibs/lib")
    find_library(MPG123_LIB mpg123 REQUIRED PATHS "${XBOXSDK_PATH}/lib" "${XBOXSDK_PATH}/portlibs/lib")
endif()

# 全局定义 pure_sdl 宏
add_compile_definitions(PURE_SDL=1)

add_executable(MyXBoxApp ${SOURCES} ${MAIN_FILE})

# 链接依赖库
target_link_libraries(MyXBoxApp
    SDL2_Xbox::SDL2
    SDL2_Xbox::SDL2_image
    SDL2_Xbox::SDL2_mixer
)

if(WIN32)
    target_link_libraries(MyXBoxApp SDL2::SDL2main)
endif()

# 根据不同 SDK 链接系统库
if(DEFINED ENV{GDKXSDK})
    target_link_libraries(MyXBoxApp d3d11 xinput xaudio2)
elseif(DEFINED ENV{XDKSDK})
    target_link_libraries(MyXBoxApp
        ${PNG_LIB}
        ${JPEG_LIB}
        ${ZLIB_LIB}
        ${OGG_LIB}
        ${VORBIS_LIB}
        ${MPG123_LIB}
        xboxkrnl
    )
elseif(DEFINED ENV{NXDK})
    target_link_libraries(MyXBoxApp
        ${PNG_LIB}
        ${JPEG_LIB}
        ${ZLIB_LIB}
        ${OGG_LIB}
        ${VORBIS_LIB}
        ${MPG123_LIB}
        nxdk
    )
endif()

# 可选 ANGLE 支持
option(USE_ANGLE "Enable ANGLE support" OFF)

if(USE_ANGLE)
    message(STATUS "启用 ANGLE 支持")
    if(DEFINED ANGLE_DIR AND EXISTS "${ANGLE_DIR}/libEGL.a" AND EXISTS "${ANGLE_DIR}/libGLESv2.a")
        target_link_directories(MyXBoxApp PRIVATE ${ANGLE_DIR})
        target_link_libraries(MyXBoxApp EGL GLESv2)
    else()
        find_package(angle QUIET)
        if(angle_FOUND)
            target_link_libraries(MyXBoxApp angle::libEGL-static angle::libGLESv2-static)
        endif()
    endif()
endif()

# 安装规则（可选）
install(TARGETS MyXBoxApp
    RUNTIME DESTINATION bin
    LIBRARY DESTINATION lib
    ARCHIVE DESTINATION lib
)

message(STATUS "CMake 配置完成，目标程序为 MyXBoxApp")
