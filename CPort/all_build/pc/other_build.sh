#!/bin/bash

# 检查是否传入源码路径参数
if [ -z "$1" ]; then
    echo "[错误] 请在运行脚本时指定源码路径。"
    echo "用法: ./other_build.sh <源码路径>"
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
    exit 1
fi

# 检查编译器
if command -v gcc &> /dev/null; then
    COMPILER=gcc
elif command -v clang &> /dev/null; then
    COMPILER=clang
else
    echo "[错误] 未检测到 GCC 或 Clang 编译器。"
    exit 1
fi

echo "[信息] 使用编译器: $COMPILER"

# 配置并编译
cmake -B build -S . -DCMAKE_BUILD_TYPE=Release -DSRC_DIR="$SRC_DIR"
if [ $? -ne 0 ]; then
    echo "[错误] CMake 配置失败。"
    exit 1
fi

cmake --build build --config Release
if [ $? -ne 0 ]; then
    echo "[错误] 构建失败。"
    exit 1
fi

echo "[成功] 构建完成！可执行文件位于 build/ 目录下。"
