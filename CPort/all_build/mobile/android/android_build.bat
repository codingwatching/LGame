#!/bin/bash

# 检查是否传入源码路径参数
if [ -z "$1" ]; then
    echo "[错误] 请在运行脚本时指定源码路径。"
    echo "用法: ./android_build.sh <源码路径>"
    exit 1
fi

SRC_DIR=$1

# 检查源码路径是否存在
if [ ! -d "$SRC_DIR" ]; then
    echo "[错误] 指定的源码路径不存在: $SRC_DIR"
    exit 1
fi

# 检查是否设置了 ANDROID_NDK 环境变量
if [ -z "$ANDROID_NDK" ]; then
    echo "[错误] 未检测到 ANDROID_NDK 环境变量。"
    echo "请先下载并安装 Android NDK，然后设置 ANDROID_NDK 环境变量。"
    echo "下载地址: https://developer.android.com/ndk/downloads"
    exit 1
fi

# 检查路径是否存在
if [ ! -d "$ANDROID_NDK" ]; then
    echo "[错误] ANDROID_NDK 路径无效: $ANDROID_NDK"
    exit 1
fi

echo "[信息] 检测到 Android NDK 路径: $ANDROID_NDK"

# 配置并编译项目
cmake -B build -S "$SRC_DIR" \
    -DCMAKE_TOOLCHAIN_FILE=$ANDROID_NDK/build/cmake/android.toolchain.cmake \
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

echo "[成功] 构建完成！可执行文件位于 build/ 目录下"
