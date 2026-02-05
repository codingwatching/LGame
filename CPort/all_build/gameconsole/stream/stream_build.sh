#!/bin/bash

if [ -z "$1" ]; then
    echo "[错误] 请在运行脚本时指定源码路径。"
    echo "用法: ./steam_build.sh <源码路径>"
    exit 1
fi

SRC_DIR=$1

if [ ! -d "$SRC_DIR" ]; then
    echo "[错误] 指定的源码路径不存在: $SRC_DIR"
    exit 1
fi

if [ -z "$STEAMWORKS_SDK_PATH" ]; then
    echo "[错误] 未检测到 STEAMWORKS_SDK_PATH 环境变量，请设置为 Steamworks SDK 路径。"
    exit 1
fi

cmake -B build -S . -DCMAKE_BUILD_TYPE=Release -DSRC_DIR="$SRC_DIR" -DSTEAMWORKS_SDK_PATH="$STEAMWORKS_SDK_PATH"
cmake --build build --config Release

echo "[成功] 构建完成！可执行文件位于 build/ 目录下，可通过 Steam 运行。"
