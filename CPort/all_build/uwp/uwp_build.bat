@echo off
setlocal enabledelayedexpansion

:: ---------------- 参数处理 ----------------
set "ACTION="
set "BUILD_ANGLE=false"

if "%~1"=="" (
    set "ACTION=release"
    echo [INFO] No parameter specified, defaulting to Release build
) else (
    set "ACTION=%~1"
    echo [INFO] Action set to %ACTION%
)

if /i "%~2"=="--angle" (
    set "BUILD_ANGLE=true"
    echo [INFO] ANGLE build enabled
) else (
    echo [INFO] ANGLE build disabled (default)
)

:: ---------------- clean 操作 ----------------
if /i "%ACTION%"=="clean" (
    echo [INFO] Cleaning build directories...
    if exist "%~dp0build_x64" rmdir /s /q "%~dp0build_x64"
    if exist "%~dp0build_arm64" rmdir /s /q "%~dp0build_arm64"
    if exist "%~dp0external" rmdir /s /q "%~dp0external"
    if exist "%~dp0gn" rmdir /s /q "%~dp0gn"
    echo [SUCCESS] Clean completed.
    exit /b 0
)

:: ---------------- 源码路径处理 ----------------
if "%~3"=="" (
    echo [INFO] No source path specified, defaulting to "src"
    set "SRC_DIR=%~dp0src"
) else (
    set "SRC_DIR=%~3"
)

if not exist "%SRC_DIR%" (
    echo [ERROR] Source path does not exist: %SRC_DIR%
    exit /b 1
) else (
    echo [INFO] Using path: %SRC_DIR%
)

:: ---------------- 工具检测 ----------------
where cmake >nul 2>nul
if errorlevel 1 (
    echo [ERROR] CMake not detected. Please install and add it to PATH.
    exit /b 1
)

:: ---------------- Visual Studio 环境检测 ----------------
set "GENERATOR="
cmake --help | findstr /C:"Visual Studio 18 2026" >nul && set "GENERATOR=Visual Studio 18 2026"
if "!GENERATOR!"=="" cmake --help | findstr /C:"Visual Studio 17 2022" >nul && set "GENERATOR=Visual Studio 17 2022"
if "!GENERATOR!"=="" cmake --help | findstr /C:"Visual Studio 16 2019" >nul && set "GENERATOR=Visual Studio 16 2019"

if "!GENERATOR!"=="" (
    echo [ERROR] No supported Visual Studio generator found (2019, 2022, 2026).
    exit /b 1
)

echo [INFO] Using generator: !GENERATOR!
echo [INFO] Using UWP toolchain for build

:: ---------------- ANGLE 配置 ----------------
set "SDL_USE_ANGLE=-DUSE_ANGLE=OFF"
set "SDL_ANGLE_DIR="

if /i "%BUILD_ANGLE%"=="true" (
    set "SDL_USE_ANGLE=-DUSE_ANGLE=ON"
    set "SDL_ANGLE_DIR=-DANGLE_DIR=%~dp0external\angle\out\%ACTION%"
    echo [INFO] SDL build will enable ANGLE
)

:: ---------------- 构建 x64 ----------------
echo [INFO] Configuring UWP project for x64...
cmake -B build_x64 -S . -G "!GENERATOR!" -A x64 ^
    -DCMAKE_SYSTEM_NAME=WindowsStore ^
    -DCMAKE_SYSTEM_VERSION=10.0 ^
    -DCMAKE_GENERATOR_PLATFORM=x64 ^
    -DCMAKE_BUILD_TYPE=%ACTION% ^
    -DSRC_DIR="!SRC_DIR!" %SDL_USE_ANGLE% %SDL_ANGLE_DIR%

if errorlevel 1 (
    echo [ERROR] CMake configuration failed for x64.
    exit /b 1
)

cmake --build build_x64 --config %ACTION%
if errorlevel 1 (
    echo [ERROR] Build failed for x64.
    exit /b 1
)

:: ---------------- 构建 ARM64 ----------------
echo [INFO] Configuring UWP project for ARM64...
cmake -B build_arm64 -S . -G "!GENERATOR!" -A ARM64 ^
    -DCMAKE_SYSTEM_NAME=WindowsStore ^
    -DCMAKE_SYSTEM_VERSION=10.0 ^
    -DCMAKE_GENERATOR_PLATFORM=ARM64 ^
    -DCMAKE_BUILD_TYPE=%ACTION% ^
    -DSRC_DIR="!SRC_DIR!" %SDL_USE_ANGLE% %SDL_ANGLE_DIR%

if errorlevel 1 (
    echo [ERROR] CMake configuration failed for ARM64.
    exit /b 1
)

cmake --build build_arm64 --config %ACTION%
if errorlevel 1 (
    echo [ERROR] Build failed for ARM64.
    exit /b 1
)

echo [SUCCESS] Build completed! UWP app packages are located at:
echo   build_x64\%ACTION%\MySDLApp.appx   (x64)
echo   build_arm64\%ACTION%\MySDLApp.appx (ARM64)
