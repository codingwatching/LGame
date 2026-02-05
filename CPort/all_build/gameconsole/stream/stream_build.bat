@echo off
setlocal

if "%~1"=="" (
    echo [错误] 请在运行脚本时指定源码路径。
    echo 用法: steam_build.bat <源码路径>
    pause
    exit /b 1
)

set SRC_DIR=%~1

if not exist "%SRC_DIR%" (
    echo [错误] 指定的源码路径不存在: %SRC_DIR%
    pause
    exit /b 1
)

if "%STEAMWORKS_SDK_PATH%"=="" (
    echo [错误] 未检测到 STEAMWORKS_SDK_PATH 环境变量，请设置为 Steamworks SDK 路径。
    pause
    exit /b 1
)

cmake -B build -S . -G "Visual Studio 17 2022" -A x64 -DCMAKE_BUILD_TYPE=Release -DSRC_DIR="%SRC_DIR%" -DSTEAMWORKS_SDK_PATH="%STEAMWORKS_SDK_PATH%"
cmake --build build --config Release

echo [成功] 构建完成！可执行文件位于 build\Release\MySDLApp.exe，可通过 Steam 运行。
pause
