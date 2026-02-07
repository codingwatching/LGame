#!/usr/bin/env bash
set -euo pipefail

# ---------------- 参数处理 ----------------
ACTION="${1:-release}"

if [[ "$ACTION" == "clean" ]]; then
    echo "[INFO] Cleaning build directory..."
    rm -rf "$(dirname "$0")/build"
    echo "[SUCCESS] Clean completed."
    exit 0
fi

# ---------------- 源码路径处理 ----------------
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
SRC_DIR="${2:-$SCRIPT_DIR/src}"

if [[ ! -d "$SRC_DIR" ]]; then
    echo "[WARNING] The specified source path does not exist: $SRC_DIR"
    echo "Trying to use the script directory as base..."
    SRC_DIR="$SCRIPT_DIR/$2"
    if [[ ! -d "$SRC_DIR" ]]; then
        echo "[ERROR] Path still does not exist: $SRC_DIR"
        exit 1
    else
        echo "[INFO] Using corrected path: $SRC_DIR"
    fi
else
    echo "[INFO] Using path: $SRC_DIR"
fi

# ---------------- 工具检测 ----------------
if ! command -v emcmake >/dev/null 2>&1; then
    echo "[ERROR] Emscripten not detected. Please install and configure the environment."
    echo "Installation guide: https://emscripten.org/docs/getting_started/downloads.html"
    exit 1
fi

if ! command -v cmake >/dev/null 2>&1; then
    echo "[ERROR] CMake not detected. Please install it."
    exit 1
fi

# ---------------- Ninja 检测 ----------------
GENERATOR=""
if command -v ninja >/dev/null 2>&1; then
    GENERATOR="Ninja"
    echo "[INFO] Ninja build system detected, using Ninja generator."
else
    GENERATOR="Unix Makefiles"
    echo "[INFO] Ninja not detected, falling back to Unix Makefiles."
fi

# ---------------- 配置项目 ----------------
echo "[INFO] Running cmake with generator: $GENERATOR"
emcmake cmake -B build -S "$SRC_DIR" \
    -G "$GENERATOR" \
    -DCMAKE_BUILD_TYPE="$ACTION" \
    -DSRC_DIR="$SRC_DIR"

# ---------------- 构建项目 ----------------
if [[ "$GENERATOR" == "Ninja" ]]; then
    cmake --build build -- -j"$(nproc)"
else
    emmake make -C build -j"$(nproc)"
fi

# ---------------- 输出检查 ----------------
OUTPUT_JS="build/MySDLApp.js"
OUTPUT_WASM="build/MySDLApp.wasm"

if [[ -f "$OUTPUT_JS" && -f "$OUTPUT_WASM" ]]; then
    echo "[SUCCESS] Build completed! Files located at:"
    echo "  $OUTPUT_JS"
    echo "  $OUTPUT_WASM"
    echo "These can be run in a browser."
else
    echo "[WARNING] Expected output files not found. Please check your CMake configuration."
fi
