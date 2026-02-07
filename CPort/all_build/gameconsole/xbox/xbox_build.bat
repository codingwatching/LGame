@echo off
setlocal enabledelayedexpansion

:: ---------------- 参数处理 ----------------
set "ACTION="
if "%~1"=="" (
    set "ACTION=release"
    echo [INFO] No parameter specified, defaulting to Release build
    echo Usage: xbox_build.bat [release|debug|clean] <source_path>
) else (
    set "ACTION=%~1"
    echo [INFO] Action set to %ACTION%
)

:: ---------------- clean 操作 ----------------
if /i "%ACTION%"=="clean" (
    echo [INFO] Cleaning build directory...
    if exist "%~dp0build" rmdir /s /q "%~dp0build"
    echo [SUCCESS] Clean completed.
    exit /b 0
)

:: ---------------- 源码路径处理 ----------------
if "%~2"=="" (
    echo [INFO] No source path specified, defaulting to "src" under the script directory.
    set "SCRIPT_DIR=%~dp0"
    set "SRC_DIR=%SCRIPT_DIR%src"
) else (
    set "SRC_DIR=%~2"
)

if not exist "%SRC_DIR%" (
    echo [ERROR] The specified source path does not exist: %SRC_DIR%
    exit /b 1
) else (
    echo [INFO] Using path: %SRC_DIR%
)

:: ---------------- 工具检测 ----------------
where cmake >nul 2>nul
if errorlevel 1 (
    echo [ERROR] CMake not detected. Please install CMake and add it to PATH.
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

:: ---------------- GDKX 构建 ----------------
if not "%GDKXSDK%"=="" (
    echo [INFO] GDKX SDK detected, using GDKX toolchain.

    cmake -B build -S . -G "!GENERATOR!" -A x64 ^
        -DCMAKE_SYSTEM_NAME=WindowsStore ^
        -DCMAKE_SYSTEM_VERSION=10.0 ^
        -DCMAKE_GENERATOR_PLATFORM=Gaming.Desktop.x64 ^
        -DCMAKE_BUILD_TYPE=%ACTION% ^
        -DSRC_DIR="!SRC_DIR!" ^
        -DGDKXSDK="%GDKXSDK%"
    if errorlevel 1 (
        echo [ERROR] CMake configuration failed (GDKX).
        exit /b 1
    )
    cmake --build build --config %ACTION%
    if errorlevel 1 (
        echo [ERROR] Build failed (GDKX).
        exit /b 1
    )
    goto :done
)

:: ---------------- XDK 构建 ----------------
if not "%XDKSDK%"=="" (
    echo [INFO] XDK SDK detected, using XDK toolchain.

    cmake -B build -S . -G "!GENERATOR!" -A x64 ^
        -DCMAKE_SYSTEM_NAME=Windows ^
        -DCMAKE_SYSTEM_VERSION=10.0 ^
        -DCMAKE_GENERATOR_PLATFORM=Xbox360 ^
        -DCMAKE_BUILD_TYPE=%ACTION% ^
        -DSRC_DIR="!SRC_DIR!" ^
        -DXDKSDK="%XDKSDK%"
    if errorlevel 1 (
        echo [ERROR] CMake configuration failed (XDK).
        exit /b 1
    )
    cmake --build build --config %ACTION%
    if errorlevel 1 (
        echo [ERROR] Build failed (XDK).
        exit /b 1
    )
    goto :done
)

echo [ERROR] Neither GDKXSDK nor XDKSDK environment variable detected. Please set one of them.
exit /b 1

:done
echo [SUCCESS] Build completed! Executable file is located at build\%ACTION%\MySDLApp.exe
