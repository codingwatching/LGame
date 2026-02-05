#!/bin/bash

# 检查是否传入源码路径参数
if [ -z "$1" ]; then
    echo "[错误] 请在运行脚本时指定源码路径。"
    echo "用法: ./switch_build.sh <源码路径>"
    exit 1
fi

SRC_DIR=$1

# 检查源码路径是否存在
if [ ! -d "$SRC_DIR" ]; then
    echo "[错误] 指定的源码路径不存在: $SRC_DIR"
    exit 1
fi

# 检查是否设置了 devkitPro 环境变量
if [ -z "$DEVKITPRO" ]; then
    echo "[错误] 未检测到 DEVKITPRO 环境变量。"
    echo "请先安装 devkitPro 并设置环境变量，例如:"
    echo "  export DEVKITPRO=/opt/devkitpro"
    exit 1
fi

if [ -z "$DEVKITARM" ]; then
    echo "[错误] 未检测到 DEVKITARM 环境变量。"
    echo "请确认 devkitPro 安装完整，并设置环境变量，例如:"
    echo "  export DEVKITARM=/opt/devkitpro/devkitA64"
    exit 1
fi

TOOLCHAIN_FILE=$DEVKITPRO/cmake/Switch.cmake
if [ ! -f "$TOOLCHAIN_FILE" ]; then
    echo "[错误] 未找到 Switch Toolchain 文件: $TOOLCHAIN_FILE"
    echo "请确认 devkitPro 安装正确。"
    exit 1
fi

echo "[信息] 使用 devkitPro 工具链: $TOOLCHAIN_FILE"

# 配置并编译项目
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

echo "[成功] 构建完成！NRO 文件位于 build/ 目录下，可在 Switch 上运行。"
