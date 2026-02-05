@echo off
setlocal

if "%~1"=="" (
    echo [错误] 请在运行脚本时指定源码路径。
    echo 用法: build_uwp.bat <源码路径>
    pause
    exit /b 1
)

set SRC_DIR=%~1

if not exist "%SRC_DIR%" (
    echo [错误] 指定的源码路径不存在: %SRC_DIR%
    pause
    exit /b 1
)

REM 检查是否安装了 CMake
where cmake >nul 2>nul
if errorlevel 1 (
    echo [错误] 未检测到 CMake，请先安装并加入 PATH。
    pause
    exit /b 1
)

REM 检查是否安装了 Visual Studio UWP 工具集
where msbuild >nul 2>nul
if errorlevel 1 (
    echo [错误] 未检测到 MSBuild，请确认已安装 Visual Studio 并启用 UWP 工作负载。
    pause
    exit /b 1
)

echo [信息] 使用 UWP 工具链进行构建

cmake -B build -S . -G "Visual Studio 17 2022" -A x64 ^
    -DCMAKE_SYSTEM_NAME=WindowsStore ^
    -DCMAKE_SYSTEM_VERSION=10.0 ^
    -DCMAKE_GENERATOR_PLATFORM=x64 ^
    -DCMAKE_BUILD_TYPE=Release ^
    -DSRC_DIR="%SRC_DIR%"

if errorlevel 1 (
    echo [错误] CMake 配置失败。
    pause
    exit /b 1
)

cmake --build build --config Release
if errorlevel 1 (
    echo [错误] 构建失败。
    pause
    exit /b 1
)

echo [成功] 构建完成！UWP 应用包位于 build\Release\MySDLApp.appx，可在 Windows 10/11 UWP 环境中运行。
pause
