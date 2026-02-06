@echo off
setlocal enabledelayedexpansion

if "%~1"=="" (
    echo [ERROR] Please specify the source path when running the script.
    echo Usage: xbox_build.bat <source_path>
    pause
    exit /b 1
)

set SRC_DIR=%~1

if not exist "!SRC_DIR!" (
    echo [ERROR] The specified source path does not exist: !SRC_DIR!
    pause
    exit /b 1
)

if not "%GDKXSDK%"=="" (
    echo [INFO] GDKX SDK detected, using GDKX toolchain.

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

    cmake -B build -S . -G "!GENERATOR!" -A x64 ^
        -DCMAKE_SYSTEM_NAME=WindowsStore ^
        -DCMAKE_SYSTEM_VERSION=10.0 ^
        -DCMAKE_GENERATOR_PLATFORM=Gaming.Desktop.x64 ^
        -DCMAKE_BUILD_TYPE=Release ^
        -DSRC_DIR="!SRC_DIR!" ^
        -DGDKXSDK="%GDKXSDK%"
    if errorlevel 1 (
        echo [ERROR] CMake configuration failed.
        pause
        exit /b 1
    )
    cmake --build build --config Release
    if errorlevel 1 (
        echo [ERROR] Build failed.
        pause
        exit /b 1
    )
    goto :done
)

if not "%XDKSDK%"=="" (
    echo [INFO] XDK SDK detected, using XDK toolchain.

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

    cmake -B build -S . -G "!GENERATOR!" -A x64 ^
        -DCMAKE_SYSTEM_NAME=Windows ^
        -DCMAKE_SYSTEM_VERSION=10.0 ^
        -DCMAKE_GENERATOR_PLATFORM=Xbox360 ^
        -DCMAKE_BUILD_TYPE=Release ^
        -DSRC_DIR="!SRC_DIR!" ^
        -DXDKSDK="%XDKSDK%"
    if errorlevel 1 (
        echo [ERROR] CMake configuration failed.
        pause
        exit /b 1
    )
    cmake --build build --config Release
    if errorlevel 1 (
        echo [ERROR] Build failed.
        pause
        exit /b 1
    )
    goto :done
)

echo [ERROR] Neither GDKXSDK nor XDKSDK environment variable detected. Please set one of them.
pause
exit /b 1

:done
echo [SUCCESS] Build completed! Executable file is located at build\Release\MySDLApp.exe
pause
