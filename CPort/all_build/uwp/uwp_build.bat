@echo off
setlocal enabledelayedexpansion

:: Check if source path parameter is provided
if "%~1"=="" (
    echo [ERROR] Please specify the source path when running the script.
    echo Usage: build_uwp.bat <source_path>
    pause
    exit /b 1
)

set SRC_DIR=%~1

:: Check if source path exists
if not exist "%SRC_DIR%" (
    echo [ERROR] The specified source path does not exist: %SRC_DIR%
    pause
    exit /b 1
)

:: Check if CMake is installed
where cmake >nul 2>nul
if errorlevel 1 (
    echo [ERROR] CMake not detected. Please install CMake and add it to PATH.
    pause
    exit /b 1
)

:: Detect available Visual Studio version
set GENERATOR=
cmake --help | findstr /C:"Visual Studio 18 2026" >nul && set GENERATOR=Visual Studio 18 2026
if "!GENERATOR!"=="" (
    cmake --help | findstr /C:"Visual Studio 17 2022" >nul && set GENERATOR=Visual Studio 17 2022
)
if "!GENERATOR!"=="" (
    cmake --help | findstr /C:"Visual Studio 16 2019" >nul && set GENERATOR=Visual Studio 16 2019
)

if "!GENERATOR!"=="" (
    echo [ERROR] No supported Visual Studio generator found (2019, 2022, 2026).
    pause
    exit /b 1
)

echo [INFO] Using generator: !GENERATOR!
echo [INFO] Using UWP toolchain for build

:: Build for x64
echo [INFO] Configuring UWP project for x64...
cmake -B build_x64 -S . -G "!GENERATOR!" -A x64 ^
    -DCMAKE_SYSTEM_NAME=WindowsStore ^
    -DCMAKE_SYSTEM_VERSION=10.0 ^
    -DCMAKE_GENERATOR_PLATFORM=x64 ^
    -DCMAKE_BUILD_TYPE=Release ^
    -DSRC_DIR="!SRC_DIR!"

if errorlevel 1 (
    echo [ERROR] CMake configuration failed for x64.
    pause
    exit /b 1
)

cmake --build build_x64 --config Release
if errorlevel 1 (
    echo [ERROR] Build failed for x64.
    pause
    exit /b 1
)

:: Build for ARM64
echo [INFO] Configuring UWP project for ARM64...
cmake -B build_arm64 -S . -G "!GENERATOR!" -A ARM64 ^
    -DCMAKE_SYSTEM_NAME=WindowsStore ^
    -DCMAKE_SYSTEM_VERSION=10.0 ^
    -DCMAKE_GENERATOR_PLATFORM=ARM64 ^
    -DCMAKE_BUILD_TYPE=Release ^
    -DSRC_DIR="!SRC_DIR!"

if errorlevel 1 (
    echo [ERROR] CMake configuration failed for ARM64.
    pause
    exit /b 1
)

cmake --build build_arm64 --config Release
if errorlevel 1 (
    echo [ERROR] Build failed for ARM64.
    pause
    exit /b 1
)

echo [SUCCESS] Build completed! UWP app packages are located at:
echo   build_x64\Release\MySDLApp.appx   (x64)
echo   build_arm64\Release\MySDLApp.appx (ARM64)
pause
