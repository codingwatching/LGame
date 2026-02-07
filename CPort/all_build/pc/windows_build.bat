@echo off
setlocal enabledelayedexpansion

:: 根目录
set "ROOT_DIR=%~dp0"
set "GN_DIR=%ROOT_DIR%gn"
set "ANGLE_DIR=%ROOT_DIR%external\angle"

:: ---------------- 参数处理 ----------------
set "ACTION="
set "BUILD_ANGLE=false"

if "%~1"=="" (
    set "ACTION=release"
    echo [INFO] No parameter specified, defaulting to Release build
) else (
    set "ACTION=%~1"
    echo [INFO] Action set to %ACTION%
)

if /i "%~2"=="--angle" (
    set "BUILD_ANGLE=true"
    echo [INFO] ANGLE build enabled
) else (
    echo [INFO] ANGLE build disabled (default)
)

:: ---------------- clean 操作 ----------------
if /i "%ACTION%"=="clean" (
    echo [INFO] Cleaning build, external, and gn directories...
    if exist "%ROOT_DIR%build" rmdir /s /q "%ROOT_DIR%build"
    if exist "%ROOT_DIR%external" rmdir /s /q "%ROOT_DIR%external"
    if exist "%GN_DIR%" rmdir /s /q "%GN_DIR%"
    echo [SUCCESS] Clean completed.
    exit /b 0
)

:: ---------------- 构建模式 ----------------
set "BUILD_TYPE=%ACTION%"
echo [INFO] Build type: %BUILD_TYPE%

:: ---------------- 源码路径处理 ----------------
if "%~3"=="" (
    echo [INFO] No source path specified, defaulting to "src" under the script directory
    set "SRC_DIR=%~dp0src"
) else (
    set "SRC_DIR=%~3"
)

if not exist "%SRC_DIR%" (
    echo [ERROR] Source path does not exist: %SRC_DIR%
    exit /b 1
) else (
    echo [INFO] Using path: %SRC_DIR%
)

:: ---------------- 工具检测 ----------------
where cmake >nul 2>nul
if errorlevel 1 (
    echo [ERROR] CMake not detected. Please install and add it to PATH.
    exit /b 1
)

:: ---------------- Visual Studio 环境检测 ----------------
set "VSWHERE=%ProgramFiles(x86)%\Microsoft Visual Studio\Installer\vswhere.exe"
set "VCVARS="
set "VSGENERATOR="

if exist "%VSWHERE%" (
    for /f "usebackq tokens=*" %%i in (`"%VSWHERE%" -latest -products * -requires Microsoft.VisualStudio.Component.VC.Tools.x86.x64 -property installationPath`) do (
        set "VCVARS=%%i\VC\Auxiliary\Build\vcvarsall.bat"
        echo [INFO] Found Visual Studio installation at %%i
        echo [INFO] vcvarsall path: !VCVARS!
        echo %%i | findstr /i "2019" >nul && set "VSGENERATOR=Visual Studio 16 2019"
        echo %%i | findstr /i "2022" >nul && set "VSGENERATOR=Visual Studio 17 2022"
        echo %%i | findstr /i "2026" >nul && set "VSGENERATOR=Visual Studio 18 2026"
    )
)

if not "!VCVARS!"=="" (
    echo [INFO] Calling vcvarsall: !VCVARS!
    call "!VCVARS!" x64
)

:: ---------------- GN 构建 ----------------
set "GN_OUT=%GN_DIR%\out"
set "GN_EXE=%GN_OUT%\gn.exe"

if not exist "%GN_EXE%" (
    if not exist "%GN_DIR%" (
        echo [INFO] Cloning GN repository...
        git clone https://gn.googlesource.com/gn "%GN_DIR%" || exit /b 1
    )
    cd /d "%GN_DIR%"
    python build/gen.py || exit /b 1
    ninja -C out || exit /b 1
    powershell -Command "(Get-Content 'out/build.ninja') -replace '/WX','' | Set-Content 'out/build.ninja'"
    echo [SUCCESS] GN built successfully: %GN_EXE%
)

:: ---------------- ANGLE 构建（可选） ----------------
if /i "%BUILD_ANGLE%"=="true" (
    if not exist "%ROOT_DIR%external" mkdir "%ROOT_DIR%external"
    if not exist "%ANGLE_DIR%" (
        echo [INFO] Cloning ANGLE repository...
        git clone https://chromium.googlesource.com/angle/angle "%ANGLE_DIR%" || exit /b 1
    )
    cd /d "%ANGLE_DIR%"

    if exist "out/%BUILD_TYPE%/libEGL.dll" if exist "out/%BUILD_TYPE%/libGLESv2.dll" (
        echo [INFO] ANGLE already built, skipping rebuild
    ) else (
        echo [INFO] Generating ANGLE build files with GN...
        "%GN_EXE%" gen "out/%BUILD_TYPE%" --args="is_debug=%BUILD_TYPE:Release=false% angle_enable_d3d11=true angle_enable_gl=false angle_enable_vulkan=false" || exit /b 1

        if exist "out/%BUILD_TYPE%/build.ninja" (
            powershell -Command "(Get-Content 'out/%BUILD_TYPE%/build.ninja') -replace '/WX','' | Set-Content 'out/%BUILD_TYPE%/build.ninja'"
        )

        echo [INFO] Building ANGLE with Ninja...
        ninja -C "out/%BUILD_TYPE%" libEGL libGLESv2 -j%NUMBER_OF_PROCESSORS% --quiet || exit /b 1
        echo [SUCCESS] ANGLE built successfully with GN/Ninja.
    )
) else (
    echo [INFO] Skipping ANGLE build
)

:SDL_BUILD
:: ---------------- SDL 项目构建 ----------------
set "SDL_USE_ANGLE=-DUSE_ANGLE=OFF"
set "SDL_ANGLE_DIR="

if /i "%BUILD_ANGLE%"=="true" (
    set "SDL_USE_ANGLE=-DUSE_ANGLE=ON"
    set "SDL_ANGLE_DIR=-DANGLE_DIR=%ANGLE_DIR%\out\%BUILD_TYPE%"
    echo [INFO] SDL build will enable ANGLE
) else (
    if exist "%ANGLE_DIR%\out\%BUILD_TYPE%\libEGL.dll" if exist "%ANGLE_DIR%\out\%BUILD_TYPE%\libGLESv2.dll" (
        set "SDL_USE_ANGLE=-DUSE_ANGLE=ON"
        set "SDL_ANGLE_DIR=-DANGLE_DIR=%ANGLE_DIR%\out\%BUILD_TYPE%"
        echo [INFO] Detected existing ANGLE build, enabling ANGLE for SDL
    ) else (
        echo [INFO] SDL build will not use ANGLE
    )
)

where cl >nul 2>nul
if !errorlevel!==0 (
    if "!VSGENERATOR!"=="" (
        cmake -B build -S . -G "Ninja" -Wno-dev ^
            -DCMAKE_BUILD_TYPE=%BUILD_TYPE% -DSRC_DIR="!SRC_DIR!" %SDL_USE_ANGLE% %SDL_ANGLE_DIR% ^
            -DCMAKE_C_COMPILER=cl -DCMAKE_CXX_COMPILER=cl ^
            -DCMAKE_C_FLAGS="/w" -DCMAKE_CXX_FLAGS="/w" ^
            -DCMAKE_C_FLAGS_RELEASE="/w" -DCMAKE_CXX_FLAGS_RELEASE="/w" ^
            -DCMAKE_C_FLAGS_DEBUG="/w" -DCMAKE_CXX_FLAGS_DEBUG="/w" ^
            -DCMAKE_ASM_MASM_FLAGS="" -DCMAKE_ASM_MASM_FLAGS_RELEASE="" -DCMAKE_ASM_MASM_FLAGS_DEBUG="" ^
            -DCMAKE_POLICY_DEFAULT_CMP0091=NEW -DCMAKE_MSVC_RUNTIME_LIBRARY="MultiThreaded$<$<CONFIG:Debug>:Debug>DLL"
        cmake --build build -- -j%NUMBER_OF_PROCESSORS% --quiet || exit /b 1
        echo [SUCCESS] Build completed with MSVC+Ninja! Executable located at build\MySDLApp.exe
        exit /b 0
    ) else (
        cmake -B build -S . -G "!VSGENERATOR!" -A x64 -Wno-dev ^
            -DCMAKE_BUILD_TYPE=%BUILD_TYPE% -DSRC_DIR="!SRC_DIR!" %SDL_USE_ANGLE% %SDL_ANGLE_DIR% ^
            -DCMAKE_C_FLAGS="/w" -DCMAKE_CXX_FLAGS="/w" ^
            -DCMAKE_C_FLAGS_RELEASE="/w" -DCMAKE_CXX_FLAGS_RELEASE="/w" ^
            -DCMAKE_C_FLAGS_DEBUG="/w" -DCMAKE_CXX_FLAGS_DEBUG="/w" ^
            -DCMAKE_ASM_MASM_FLAGS="" -DCMAKE_ASM_MASM_FLAGS_RELEASE="" -DCMAKE_ASM_MASM_FLAGS_DEBUG="" ^
            -DCMAKE_POLICY_DEFAULT_CMP0091=NEW -DCMAKE_MSVC_RUNTIME_LIBRARY="MultiThreaded$<$<CONFIG:Debug>:Debug>DLL"
        cmake --build build --config %BUILD_TYPE% -- /maxcpucount:%NUMBER_OF_PROCESSORS% /nologo /nowarn
    )
)
