@echo off
setlocal

REM 检查是否传入源码路径参数
if "%~1"=="" (
    echo [错误] 请在运行脚本时指定源码路径。
    echo 用法: windows_build.bat <源码路径>
    pause
    exit /b 1
)

set SRC_DIR=%~1

REM 检查源码路径是否存在
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

REM 检查是否安装了 MSVC (cl.exe)
where cl >nul 2>nul
if %errorlevel%==0 (
    echo [信息] 检测到 MSVC 编译器，使用 /Ox 优化。
    cmake -B build -S . -G "Visual Studio 17 2022" -A x64 -DCMAKE_BUILD_TYPE=Release -DSRC_DIR="%SRC_DIR%"
    cmake --build build --config Release
    if errorlevel 1 (
        echo [错误] 构建失败。
        pause
        exit /b 1
    )
    echo [成功] 使用 MSVC 构建完成！可执行文件位于 build\Release\MySDLApp.exe
    pause
    exit /b 0
)

REM 检查是否安装了 GCC (Cygwin/MinGW)
where gcc >nul 2>nul
if %errorlevel%==0 (
    echo [信息] 检测到 GCC 编译器，使用 -O3 优化。
    cmake -B build -S . -G "Unix Makefiles" -DCMAKE_BUILD_TYPE=Release -DSRC_DIR="%SRC_DIR%"
    cmake --build build
    if errorlevel 1 (
        echo [错误] 构建失败。
        pause
        exit /b 1
    )
    echo [成功] 使用 GCC 构建完成！可执行文件位于 build\MySDLApp.exe
    pause
    exit /b 0
)

echo [错误] 未检测到 MSVC 或 GCC 编译器，请确认已安装 Visual Studio 或 Cygwin/MinGW。
pause
exit /b 1
