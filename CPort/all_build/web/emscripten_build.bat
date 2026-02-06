@echo off
setlocal enabledelayedexpansion

:: Check if source path parameter is provided
if "%~1"=="" (
    echo [INFO] No source path specified, defaulting to "src" under the script directory.
    set SCRIPT_DIR=%~dp0
    set SRC_DIR=!SCRIPT_DIR!src
) else (
    set SRC_DIR=%~1
)

:: Check if source path exists
if not exist "!SRC_DIR!" (
    echo [WARNING] The specified source path does not exist: !SRC_DIR!
    echo Trying to use the script directory as base...

    set SCRIPT_DIR=%~dp0
    set SRC_DIR=!SCRIPT_DIR!%~1

    if not exist "!SRC_DIR!" (
        echo [ERROR] Path still does not exist: !SRC_DIR!
        pause
        exit /b 1
    ) else (
        echo [INFO] Using corrected path: !SRC_DIR!
    )
) else (
    echo [INFO] Using path: !SRC_DIR!
)

:: Check if Emscripten is installed
where emcmake >nul 2>nul
if errorlevel 1 (
    echo [ERROR] Emscripten not detected. Please install and configure the environment.
    echo Installation guide: https://emscripten.org/docs/getting_started/downloads.html
    pause
    exit /b 1
)

echo [INFO] Using Emscripten toolchain for build

:: Check if Ninja is available
where ninja >nul 2>nul
if %errorlevel%==0 (
    set GENERATOR=Ninja
    echo [INFO] Ninja build system detected, using Ninja generator.
) else (
    set GENERATOR=Unix Makefiles
    echo [INFO] Ninja not detected, falling back to Unix Makefiles.
)

:: Configure project
echo [INFO] Running cmake with generator: !GENERATOR!
emcmake cmake -B build -S "!SRC_DIR!" ^
    -G "!GENERATOR!" ^
    -DCMAKE_BUILD_TYPE=Release ^
    -DSRC_DIR="!SRC_DIR!"

if errorlevel 1 (
    echo [ERROR] CMake configuration failed.
    pause
    exit /b 1
)

:: Build project with parallel jobs
if "!GENERATOR!"=="Ninja" (
    cmake --build build -- -j%NUMBER_OF_PROCESSORS%
) else (
    emmake make -C build -j%NUMBER_OF_PROCESSORS%
)

if errorlevel 1 (
    echo [ERROR] Build failed.
    pause
    exit /b 1
)

set OUTPUT_JS=build\MySDLApp.js
set OUTPUT_WASM=build\MySDLApp.wasm

if exist "!OUTPUT_JS!" if exist "!OUTPUT_WASM!" (
    echo [SUCCESS] Build completed! Files located at:
    echo   !OUTPUT_JS!
    echo   !OUTPUT_WASM!
    echo These can be run in a browser.
) else (
    echo [WARNING] Expected output files not found. Please check your CMake configuration.
)

pause
