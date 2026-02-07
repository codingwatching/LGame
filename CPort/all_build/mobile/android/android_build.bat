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
    echo [INFO] Cleaning build directories...
    for %%A in (arm64-v8a armeabi-v7a x86_64) do (
        if exist "%~dp0build_%%A" rmdir /s /q "%~dp0build_%%A"
    )
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

:: ---------------- ANDROID NDK 检测 ----------------
if "%ANDROID_NDK%"=="" (
    echo [ERROR] ANDROID_NDK environment variable not detected.
    echo Please download and install Android NDK, then set the ANDROID_NDK environment variable.
    echo Download: https://developer.android.com/ndk/downloads
    exit /b 1
)

if not exist "%ANDROID_NDK%" (
    echo [ERROR] ANDROID_NDK path is invalid: %ANDROID_NDK%
    exit /b 1
)

echo [INFO] Detected Android NDK path: %ANDROID_NDK%

:: ---------------- 工具检测 ----------------
where cmake >nul 2>nul
if errorlevel 1 (
    echo [ERROR] CMake not detected. Please install CMake and add it to PATH.
    exit /b 1
)

where ninja >nul 2>nul
if not errorlevel 1 (
    set "GENERATOR=Ninja"
    echo [INFO] Ninja build system detected, using Ninja generator.
) else (
    set "GENERATOR=Unix Makefiles"
    echo [INFO] Ninja not detected, falling back to Unix Makefiles.
)

:: ---------------- 多 ABI 构建 ----------------
set ABIS=arm64-v8a armeabi-v7a x86_64

for %%A in (%ABIS%) do (
    echo [INFO] Building for ABI: %%A

    set "BUILD_DIR=build_%%A"
    if not exist "!BUILD_DIR!" mkdir "!BUILD_DIR!"

    echo [INFO] Running cmake with generator: !GENERATOR!

    cmake -B "!BUILD_DIR!" -S "!SRC_DIR!" ^
        -G "!GENERATOR!" ^
        -DCMAKE_TOOLCHAIN_FILE="%ANDROID_NDK%\build\cmake\android.toolchain.cmake" ^
        -DANDROID_ABI=%%A ^
        -DANDROID_PLATFORM=android-21 ^
        -DCMAKE_BUILD_TYPE=%ACTION% ^
        -DSRC_DIR="!SRC_DIR!"

    if errorlevel 1 (
        echo [ERROR] CMake configuration failed for ABI: %%A
        exit /b 1
    )

    cmake --build "!BUILD_DIR!" -- -j%NUMBER_OF_PROCESSORS%
    if errorlevel 1 (
        echo [ERROR] Build failed for ABI: %%A
        exit /b 1
    )

    set "OUTPUT_APK=!BUILD_DIR!\MyAndroidApp-%%A.apk"
    if exist "!OUTPUT_APK!" (
        echo [SUCCESS] Build completed for ABI: %%A. APK located at: !OUTPUT_APK!
    ) else (
        echo [WARNING] APK file not found for ABI: %%A. Please check your CMake configuration.
    )
)

echo [SUCCESS] Multi-ABI build completed! APK files are located in their respective build directories.
