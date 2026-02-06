@echo off
setlocal enabledelayedexpansion

if exist build (
    echo [INFO] Removing old build directory...
    rmdir /s /q build
)

if "%~1"=="" (
    echo [INFO] No source path specified, defaulting to "src" under the script directory
    set SRC_DIR=%~dp0src
) else (
    set SRC_DIR=%~1
)

if not exist "%SRC_DIR%" (
    echo [WARNING] The specified source path does not exist: %SRC_DIR%
    echo Trying to use the script directory as base...

    set SCRIPT_DIR=%~dp0
    set SRC_DIR=%SCRIPT_DIR%%~1

    if not exist "%SRC_DIR%" (
        echo [ERROR] Path still does not exist: %SRC_DIR%
        pause
        exit /b 1
    ) else (
        echo [INFO] Using corrected path: %SRC_DIR%
    )
) else (
    echo [INFO] Using path: %SRC_DIR%
)

where cmake >nul 2>nul
if errorlevel 1 (
    echo [ERROR] CMake not detected. Please install and add it to PATH.
    pause
    exit /b 1
)

set VSWHERE="%ProgramFiles(x86)%\Microsoft Visual Studio\Installer\vswhere.exe"
set VCVARS=
set VSGENERATOR=

if exist %VSWHERE% (
    for /f "usebackq tokens=*" %%i in (`%VSWHERE% -latest -products * -requires Microsoft.VisualStudio.Component.VC.Tools.x86.x64 -property installationPath`) do (
        set VCVARS=%%i\VC\Auxiliary\Build\vcvarsall.bat
        echo [INFO] Found Visual Studio installation at %%i
        echo [INFO] vcvarsall path: !VCVARS!

        echo %%i | findstr /i "2019" >nul && set VSGENERATOR=Visual Studio 16 2019
        echo %%i | findstr /i "2022" >nul && set VSGENERATOR=Visual Studio 17 2022
        echo %%i | findstr /i "2026" >nul && set VSGENERATOR=Visual Studio 18 2026
    )
)

if "!VCVARS!"=="" (
    if exist "%ProgramFiles%\Microsoft Visual Studio\2022\Community\VC\Auxiliary\Build\vcvarsall.bat" (
        set VCVARS=%ProgramFiles%\Microsoft Visual Studio\2022\Community\VC\Auxiliary\Build\vcvarsall.bat
        set VSGENERATOR=Visual Studio 17 2022
    )
)

if "!VSGENERATOR!"=="" (
    echo [WARNING] Could not detect Visual Studio version, defaulting to Ninja
    set VSGENERATOR=Ninja
)

echo [INFO] Final generator selected: !VSGENERATOR!

if not "!VCVARS!"=="" (
    echo [INFO] Calling vcvarsall: !VCVARS!
    call "!VCVARS!" x64
) else (
    echo [WARNING] Could not locate Visual Studio vcvarsall.bat. If using Ninja or GCC, this is fine.
)

where cl >nul 2>nul
if !errorlevel!==0 (
    echo [INFO] MSVC compiler detected.
    echo [INFO] Running cmake with generator: !VSGENERATOR!
    cmake -B build -S . -G "!VSGENERATOR!" -A x64 -DCMAKE_BUILD_TYPE=Release -DSRC_DIR="!SRC_DIR!"
    cmake --build build --config Release
    if errorlevel 1 (
        echo [ERROR] Build failed.
        pause
        exit /b 1
    )
    echo [SUCCESS] Build completed with MSVC! Executable located at build\Release\MySDLApp.exe
    pause
    exit /b 0
)

where gcc >nul 2>nul
if !errorlevel!==0 (
    echo [INFO] GCC compiler detected, using -O3 optimization.
    cmake -B build -S . -G "Unix Makefiles" -DCMAKE_BUILD_TYPE=Release -DSRC_DIR="!SRC_DIR!"
    cmake --build build
    if errorlevel 1 (
        echo [ERROR] Build failed.
        pause
        exit /b 1
    )
    echo [SUCCESS] Build completed with GCC! Executable located at build/MySDLApp
    pause
    exit /b 0
)

where ninja >nul 2>nul
if !errorlevel!==0 (
    echo [INFO] Ninja detected, using Ninja generator.
    cmake -B build -S . -G "Ninja" -DCMAKE_BUILD_TYPE=Release -DSRC_DIR="!SRC_DIR!"
    cmake --build build
    if errorlevel 1 (
        echo [ERROR] Build failed.
        pause
        exit /b 1
    )
    echo [SUCCESS] Build completed with Ninja! Executable located at build/MySDLApp
    pause
    exit /b 0
)

echo [ERROR] No supported compiler detected. Please install Visual Studio, GCC, or Ninja.
pause
exit /b 1
