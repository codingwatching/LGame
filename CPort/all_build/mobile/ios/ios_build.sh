#!/bin/bash

# 检查是否传入源码路径参数
if [ -z "$1" ]; then
    echo "[错误] 请在运行脚本时指定源码路径。"
    echo "用法: ./ios_build.sh <源码路径>"
    exit 1
fi

SRC_DIR=$1

# 检查源码路径是否存在
if [ ! -d "$SRC_DIR" ]; then
    echo "[错误] 指定的源码路径不存在: $SRC_DIR"
    exit 1
fi

# 检查是否安装了 CMake
if ! command -v cmake &> /dev/null; then
    echo "[错误] 未检测到 CMake，请先安装 CMake。"
    echo "在 macOS 上可运行: brew install cmake"
    exit 1
fi

# 检查是否安装了 ios-cmake Toolchain
TOOLCHAIN_FILE=~/ios-cmake/ios.toolchain.cmake
if [ ! -f "$TOOLCHAIN_FILE" ]; then
    echo "[错误] 未找到 iOS Toolchain 文件: $TOOLCHAIN_FILE"
    echo "请先下载 ios-cmake: https://github.com/leetal/ios-cmake"
    exit 1
fi

echo "[信息] 使用 iOS Toolchain: $TOOLCHAIN_FILE"

# 配置并编译项目
cmake -B build -S "$SRC_DIR" \
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

echo "[成功] 构建完成！静态库位于 build/ 目录下，可在 Xcode 项目中链接使用。"
