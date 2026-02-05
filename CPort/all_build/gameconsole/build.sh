#!/bin/bash

if [ -z "$1" ] || [ -z "$2" ]; then
    echo "[错误] 请在运行脚本时指定源码路径和平台。"
    echo "用法: ./build.sh <源码路径> <平台>"
    echo "支持平台: PS4 PS5 PSV PSP SWITCH 3DS XBOX DREAMCAST"
    exit 1
fi

SRC_DIR=$1
PLATFORM=$2

if [ ! -d "$SRC_DIR" ]; then
    echo "[错误] 指定的源码路径不存在: $SRC_DIR"
    exit 1
fi

echo "[信息] 构建平台: $PLATFORM"

cmake -B build -S . -DCMAKE_BUILD_TYPE=Release -DSRC_DIR="$SRC_DIR" -DPLATFORM=$PLATFORM
if [ $? -ne 0 ]; then
    echo "[错误] CMake 配置失败。"
    exit 1
fi

cmake --build build
if [ $? -ne 0 ]; then
    echo "[错误] 构建失败。"
    exit 1
fi

case $PLATFORM in
    PSV)
        echo "[信息] 打包 VPK..."
        vita-make-fself build/MySDLApp.elf build/eboot.bin
        vita-pack-vpk build/MySDLApp.vpk -s sce_sys/icon0.png -s sce_sys/param.sfo build/eboot.bin
        echo "[成功] VPK 文件位于 build/ 目录下。"
        ;;
    PSP)
        echo "[信息] 打包 EBOOT.PBP..."
        pack-pbp build/EBOOT.PBP build/MySDLApp.elf
        echo "[成功] EBOOT.PBP 文件位于 build/ 目录下。"
        ;;
    3DS)
        echo "[信息] 转换为 3DSX..."
        3dsxtool build/MySDLApp.elf build/MySDLApp.3dsx
        echo "[成功] 3DSX 文件位于 build/ 目录下。"
        ;;
    SWITCH)
        echo "[信息] 打包 NRO..."
        elf2nro build/MySDLApp.elf build/MySDLApp.nro
        echo "[成功] NRO 文件位于 build/ 目录下。"
        ;;
    PS4)
        echo "[成功] SELF 文件位于 build/ 目录下。"
        ;;
    PS5)
        echo "[成功] PKG 文件位于 build/ 目录下。"
        ;;
    XBOX)
        echo "[成功] EXE 文件位于 build/ 目录下。"
        ;;
    DREAMCAST)
        echo "[成功] BIN 文件位于 build/ 目录下。"
        ;;
    *)
        echo "[警告] 未定义的打包步骤，请手动处理。"
        ;;
esac
