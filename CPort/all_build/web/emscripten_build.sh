#!/bin/bash

if [ -z "$1" ]; then
    echo "[错误] 请在运行脚本时指定源码路径。"
    echo "用法: ./emscripten_build.sh <源码路径>"
    exit 1
fi

SRC_DIR=$1

if [ ! -d "$SRC_DIR" ]; then
    echo "[错误] 指定的源码路径不存在: $SRC_DIR"
    exit 1
fi

# 检查是否安装了 Emscripten
if ! command -v emcmake &> /dev/null; then
    echo "[错误] 未检测到 Emscripten，请先安装并配置环境。"
    echo "安装指南: https://emscripten.org/docs/getting_started/downloads.html"
    exit 1
fi

echo "[信息] 使用 Emscripten 工具链进行构建"

emcmake cmake -B build -S . -DCMAKE_BUILD_TYPE=Release -DSRC_DIR="$SRC_DIR"
if [ $? -ne 0 ]; then
    echo "[错误] CMake 配置失败。"
    exit 1
fi

emmake make -C build
if [ $? -ne 0 ]; then
    echo "[错误] 构建失败。"
    exit 1
fi

echo "[成功] 构建完成！可在 build/ 目录下找到 MySDLApp.js 和 MySDLApp.wasm，可在浏览器中运行。"
