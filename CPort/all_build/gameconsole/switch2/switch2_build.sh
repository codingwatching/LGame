#!/bin/bash

if [ -z "$1" ]; then
    echo "[错误] 请在运行脚本时指定源码路径。"
    echo "用法: ./switch2_build.sh <源码路径>"
    exit 1
fi

SRC_DIR=$1

if [ ! -d "$SRC_DIR" ]; then
    echo "[错误] 指定的源码路径不存在: $SRC_DIR"
    exit 1
fi

if [ -z "$SWITCH2DEV" ]; then
    echo "[错误] 未检测到 SWITCH2DEV 环境变量，请先安装 Switch2 SDK 并设置环境变量。"
    exit 1
fi

TOOLCHAIN_FILE=$SWITCH2DEV/cmake/Switch2.cmake
if [ ! -f "$TOOLCHAIN_FILE" ]; then
    echo "[错误] 未找到 Switch2 Toolchain 文件: $TOOLCHAIN_FILE"
    exit 1
fi

echo "[信息] 使用 Switch2 工具链: $TOOLCHAIN_FILE"

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

echo "[成功] 构建完成！NRO 文件位于 build/ 目录下，可在 Switch2 上运行。"
