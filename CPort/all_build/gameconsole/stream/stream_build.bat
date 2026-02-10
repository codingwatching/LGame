@echo off
setlocal enabledelayedexpansion

:: ---------------- 参数检查 ----------------
if "%~1"=="" (
    echo [ERROR] Please specify the source path when running the script.
    echo Usage: steam_build.bat <source_path>
    pause
    exit /b 1
)

set SRC_DIR=%~1

if not exist "%SRC_DIR%" (
    echo [ERROR] The specified source path does not exist: %SRC_DIR%
    pause
    exit /b 1
)

:: ---------------- Steamworks SDK 检测 ----------------
if "%STEAMWORKS_SDK_PATH%"=="" (
    echo [ERROR] STEAMWORKS_SDK_PATH environment variable not detected. Please set it to the Steamworks SDK path.
    pause
    exit /b 1
)

:: ---------------- 工具检测 ----------------
where cmake >nul 2>nul
if errorlevel 1 (
    echo [ERROR] CMake not detected. Please install and add it to PATH.
    pause
    exit /b 1
)

set TOOLS_DIR=%~dp0external\tools
if not exist "%TOOLS_DIR%" mkdir "%TOOLS_DIR%"

:: 自动安装 GN
where gn >nul 2>nul
if errorlevel 1 (
    echo [INFO] GN not found, downloading...
    set GN_URL=https://storage.googleapis.com/chrome-infra/gn/gn.exe
    powershell -Command "Invoke-WebRequest '%GN_URL%' -OutFile '%TOOLS_DIR%\gn.exe'"
    set PATH=%TOOLS_DIR%;%PATH%
    echo [SUCCESS] GN installed to %TOOLS_DIR%
)

:: 自动安装 Ninja
where ninja >nul 2>nul
if errorlevel 1 (
    echo [INFO] Ninja not found, downloading...
    set NINJA_URL=https://github.com/ninja-build/ninja/releases/download/v1.11.1/ninja-win.zip
    powershell -Command "Invoke-WebRequest '%NINJA_URL%' -OutFile '%TOOLS_DIR%\ninja.zip'"
    powershell -Command "Expand-Archive '%TOOLS_DIR%\ninja.zip' -DestinationPath '%TOOLS_DIR%' -Force"
    set PATH=%TOOLS_DIR%;%PATH%
    echo [SUCCESS] Ninja installed to %TOOLS_DIR%
)

:: ---------------- ANGLE 构建 ----------------
where gn >nul 2>nul
set HAS_GN=!errorlevel!
where ninja >nul 2>nul
set HAS_NINJA=!errorlevel!

if %HAS_GN%==0 if %HAS_NINJA%==0 (
    echo [INFO] GN and Ninja detected. Using GN/Ninja to build ANGLE first...

    set ANGLE_DIR=%~dp0external\angle
    if not exist "%ANGLE_DIR%" (
        echo [INFO] Cloning ANGLE repository...
        git clone https://github.com/google/angle.git "%ANGLE_DIR%"
    )

    cd /d "%ANGLE_DIR%"
    gn gen out/Release --args="is_debug=false angle_enable_d3d11=true angle_enable_gl=false angle_enable_vulkan=false"
    if errorlevel 1 (
        echo [ERROR] GN generation failed.
        pause
        exit /b 1
    )

    ninja -C out/Release libEGL libGLESv2
    if errorlevel 1 (
        echo [ERROR] Ninja build failed.
        pause
        exit /b 1
    )

    echo [SUCCESS] ANGLE built successfully with GN/Ninja.
    set ANGLE_BUILT=1
    cd /d "%~dp0"
) else (
    set ANGLE_BUILT=0
)

:: ---------------- 主项目构建 ----------------
if %ANGLE_BUILT%==1 (
    echo [INFO] Configuring CMake project with local ANGLE build...
    cmake -B build -S . -G "Ninja" -DCMAKE_BUILD_TYPE=Release ^
        -DSRC_DIR="%SRC_DIR%" ^
        -DSTEAMWORKS_SDK_PATH="%STEAMWORKS_SDK_PATH%" ^
        -DFETCHCONTENT_FULLY_DISCONNECTED=ON ^
        -DANGLE_DIR="%~dp0external/angle/out/Release"
    cmake --build build
    if errorlevel 1 (
        echo [ERROR] Build failed.
        pause
        exit /b 1
    )
    echo [SUCCESS] Build completed with GN/Ninja ANGLE! Executable located at build\MyStreamApp.exe
    goto :deploy
)

:: ---------------- Visual Studio / Ninja 回退 ----------------
set GENERATOR=
cmake --help | findstr /C:"Visual Studio 18 2026" >nul && set GENERATOR=Visual Studio 18 2026
if "%GENERATOR%"=="" cmake --help | findstr /C:"Visual Studio 17 2022" >nul && set GENERATOR=Visual Studio 17 2022
if "%GENERATOR%"=="" cmake --help | findstr /C:"Visual Studio 16 2019" >nul && set GENERATOR=Visual Studio 16 2019

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

cmake -B build -S . -G "%GENERATOR%" -A x64 -DCMAKE_BUILD_TYPE=Release ^
    -DSRC_DIR="%SRC_DIR%" ^
    -DSTEAMWORKS_SDK_PATH="%STEAMWORKS_SDK_PATH%" ^
    -DFETCHCONTENT_FULLY_DISCONNECTED=ON
if errorlevel 1 (
    echo [ERROR] CMake configuration failed.
    pause
    exit /b 1
)

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

echo [SUCCESS] Build completed! Executable file is located at build\Release\MyStreamApp.exe

:deploy
:: ---------------- 自动部署 ----------------
set "OUTPUT_EXE=build\Release\MyStreamApp.exe"
if exist "%OUTPUT_EXE%" (
    copy /y "%OUTPUT_EXE%" "%~dp0deploy\" >nul
    echo [SUCCESS] Deployment completed. Executable copied to deploy\MyStreamApp.exe
) else (
    echo [WARNING] Executable not found: %OUTPUT_EXE%
)

pause
exit /b 0
