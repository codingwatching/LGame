@echo off
setlocal enabledelayedexpansion

:: ---------------- 参数处理 ----------------
set "ACTION="
if "%~1"=="" (
    set "ACTION=release"
    echo [INFO] No parameter specified, defaulting to Release build
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
    set "SRC_DIR=!SCRIPT_DIR!src"
) else (
    set "SRC_DIR=%~2"
)

if not exist "!SRC_DIR!" (
    echo [WARNING] The specified source path does not exist: !SRC_DIR!
    echo Trying to use the script directory as base...

    set "SCRIPT_DIR=%~dp0"
    set "SRC_DIR=!SCRIPT_DIR!%~2"

    if not exist "!SRC_DIR!" (
        echo [ERROR] Path still does not exist: !SRC_DIR!
        exit /b 1
    ) else (
        echo [INFO] Using corrected path: !SRC_DIR!
    )
) else (
    echo [INFO] Using path: !SRC_DIR!
)

:: ---------------- 工具检测 ----------------
where emcmake >nul 2>nul
if errorlevel 1 (
    echo [ERROR] Emscripten not detected. Please install and configure the environment.
    echo Installation guide: https://emscripten.org/docs/getting_started/downloads.html
    exit /b 1
)

where cmake >nul 2>nul
if errorlevel 1 (
    echo [ERROR] CMake not detected. Please install and add it to PATH.
    exit /b 1
)

:: ---------------- Visual Studio 环境检测（可选） ----------------
set "GENERATOR="
cmake --help | findstr /C:"Visual Studio 18 2026" >nul && set "GENERATOR=Visual Studio 18 2026"
if "!GENERATOR!"=="" cmake --help | findstr /C:"Visual Studio 17 2022" >nul && set "GENERATOR=Visual Studio 17 2022"
if "!GENERATOR!"=="" cmake --help | findstr /C:"Visual Studio 16 2019" >nul && set "GENERATOR=Visual Studio 16 2019"

:: ---------------- Ninja 检测 ----------------
if "!GENERATOR!"=="" (
    where ninja >nul 2>nul
    if %errorlevel%==0 (
        set "GENERATOR=Ninja"
        echo [INFO] Ninja build system detected, using Ninja generator.
    ) else (
        set "GENERATOR=Unix Makefiles"
        echo [INFO] Ninja not detected, falling back to Unix Makefiles.
    )
) else (
    echo [INFO] Using generator: !GENERATOR!
)

:: ---------------- 配置项目 ----------------
echo [INFO] Running cmake with generator: !GENERATOR!
emcmake cmake -B build -S "!SRC_DIR!" ^
    -G "!GENERATOR!" ^
    -DCMAKE_BUILD_TYPE=%ACTION% ^
    -DSRC_DIR="!SRC_DIR!"

if errorlevel 1 (
    echo [ERROR] CMake configuration failed.
    exit /b 1
)

:: ---------------- 构建项目 ----------------
if "!GENERATOR!"=="Ninja" (
    cmake --build build -- -j%NUMBER_OF_PROCESSORS%
) else (
    emmake make -C build -j%NUMBER_OF_PROCESSORS%
)

if errorlevel 1 (
    echo [ERROR] Build failed.
    exit /b 1
)

:: ---------------- 输出检查 ----------------
set "OUTPUT_JS=build\MySDLApp.js"
set "OUTPUT_WASM=build\MySDLApp.wasm"

if exist "!OUTPUT_JS!" if exist "!OUTPUT_WASM!" (
    echo [SUCCESS] Build completed! Files located at:
    echo   !OUTPUT_JS!
    echo   !OUTPUT_WASM!
    echo These can be run in a browser.
) else (
    echo [WARNING] Expected output files not found. Please check your CMake configuration.
)
