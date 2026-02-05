@echo off
setlocal

if "%~1"=="" (
    echo [错误] 请在运行脚本时指定源码路径。
    echo 用法: xbox_build.bat <源码路径>
    pause
    exit /b 1
)

set SRC_DIR=%~1

if not exist "%SRC_DIR%" (
    echo [错误] 指定的源码路径不存在: %SRC_DIR%
    pause
    exit /b 1
)

if not "%GDKXSDK%"=="" (
    echo [信息] 检测到 GDKX SDK，使用 GDKX 工具链。
    cmake -B build -S . -G "Visual Studio 17 2022" -A x64 ^
        -DCMAKE_SYSTEM_NAME=WindowsStore ^
        -DCMAKE_SYSTEM_VERSION=10.0 ^
        -DCMAKE_GENERATOR_PLATFORM=Gaming.Desktop.x64 ^
        -DCMAKE_BUILD_TYPE=Release ^
        -DSRC_DIR="%SRC_DIR%" ^
        -DGDKXSDK="%GDKXSDK%"
    cmake --build build --config Release
    goto :done
)

if not "%XDKSDK%"=="" (
    echo [信息] 检测到 XDK SDK，使用 XDK 工具链。
    cmake -B build -S . -G "Visual Studio 17 2022" -A x64 ^
        -DCMAKE_SYSTEM_NAME=Windows ^
        -DCMAKE_SYSTEM_VERSION=10.0 ^
        -DCMAKE_GENERATOR_PLATFORM=Xbox360 ^
        -DCMAKE_BUILD_TYPE=Release ^
        -DSRC_DIR="%SRC_DIR%" ^
        -DXDKSDK="%XDKSDK%"
    cmake --build build --config Release
    goto :done
)

echo [错误] 未检测到 GDKXSDK 或 XDKSDK 环境变量，请设置其中之一。
pause
exit /b 1

:done
echo [成功] 构建完成！可执行文件位于 build\Release\MySDLApp.exe
pause
