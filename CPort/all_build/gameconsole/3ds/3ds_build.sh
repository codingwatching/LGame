#!/bin/bash

if [ -z "$1" ]; then
    echo "[错误] 请在运行脚本时指定源码路径。"
    echo "用法: ./3ds_build.sh <源码路径>"
    exit 1
fi

SRC_DIR=$1

if [ ! -d "$SRC_DIR" ]; then
    echo "[错误] 指定的源码路径不存在: $SRC_DIR"
    exit 1
fi

if [ -z "$DEVKITPRO" ]; then
    echo "[错误] 未检测到 DEVKITPRO 环境变量，请先安装 devkitPro 并设置环境变量。"
    echo "下载地址: https://devkitpro.org"
    exit 1
fi

TOOLCHAIN_FILE=$DEVKITPRO/cmake/3DS.cmake
if [ ! -f "$TOOLCHAIN_FILE" ]; then
    echo "[错误] 未找到 3DS Toolchain 文件: $TOOLCHAIN_FILE"
    exit 1
fi

echo "[信息] 使用 3DS 工具链: $TOOLCHAIN_FILE"

cmake -B build -S . \
    -DCMAKE_TOOLCHAIN_FILE=$TOOLCHAIN_FILE \
    -DCMAKE_BUILD_TYPE=Release \
    -DSRC_DIR="$SRC_DIR"

if [ $? -ne 0 ]; then
    echo "[错误] CMake 配置失败。"
    exit 1
fi

cmake --build build
if [ $? -ne 0 ]; then
    echo "[错误] 构建失败。"
    exit 1
fi

OUTPUT_ELF="build/MySDLApp.elf"
OUTPUT_3DSX="build/MySDLApp.3dsx"

if [ ! -f "$OUTPUT_ELF" ]; then
    echo "[错误] 未生成 ELF 文件: $OUTPUT_ELF"
    exit 1
fi

echo "[信息] 正在转换 ELF 为 3DSX..."
3dsxtool "$OUTPUT_ELF" "$OUTPUT_3DSX"

if [ $? -ne 0 ]; then
    echo "[错误] 转换为 3DSX 失败。"
    exit 1
fi

echo "[成功] 构建完成！.3DSX 文件位于 build/ 目录下，可在 3DS 上运行。"
