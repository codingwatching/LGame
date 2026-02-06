@echo off
setlocal

:: Check if source path parameter is provided
if "%~1"=="" (
    echo [ERROR] Please specify the source path when running the script.
    echo Usage: steam_build.bat <source_path>
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

:: Check if STEAMWORKS_SDK_PATH is set
if "%STEAMWORKS_SDK_PATH%"=="" (
    echo [ERROR] STEAMWORKS_SDK_PATH environment variable not detected. Please set it to the Steamworks SDK path.
    pause
    exit /b 1
)

:: Detect available Visual Studio version
set GENERATOR=
cmake --help | findstr /C:"Visual Studio 18 2026" >nul && set GENERATOR=Visual Studio 18 2026
if "%GENERATOR%"=="" (
    cmake --help | findstr /C:"Visual Studio 17 2022" >nul && set GENERATOR=Visual Studio 17 2022
)
if "%GENERATOR%"=="" (
    cmake --help | findstr /C:"Visual Studio 16 2019" >nul && set GENERATOR=Visual Studio 16 2019
)

:: If no Visual Studio found, fallback to Ninja or MSBuild
if "%GENERATOR%"=="" (
    where ninja >nul 2>nul
    if not errorlevel 1 (
        set GENERATOR=Ninja
        echo [INFO] Visual Studio not found, using Ninja generator.
    ) else (
        where msbuild >nul 2>nul
        if not errorlevel 1 (
            set GENERATOR="Visual Studio 17 2022"
            echo [INFO] Visual Studio not detected, but MSBuild found. Using default VS generator.
        ) else (
            echo [ERROR] No supported build system found (VS2019/2022/2026, Ninja, MSBuild).
            pause
            exit /b 1
        )
    )
)

echo [INFO] Using generator: %GENERATOR%

:: Configure project with CMake
cmake -B build -S . -G "%GENERATOR%" -A x64 -DCMAKE_BUILD_TYPE=Release -DSRC_DIR="%SRC_DIR%" -DSTEAMWORKS_SDK_PATH="%STEAMWORKS_SDK_PATH%"
if errorlevel 1 (
    echo [ERROR] CMake configuration failed.
    pause
    exit /b 1
)

:: Build project
if "%GENERATOR%"=="Ninja" (
    cmake --build build -- -j
) else (
    cmake --build build --config Release
)

if errorlevel 1 (
    echo [ERROR] Build failed.
    pause
    exit /b 1
)

echo [SUCCESS] Build completed! Executable file is located at build\Release\MySDLApp.exe and can be run through Steam.
pause
