#!/bin/bash

# Check if source path parameter is provided
if [ -z "$1" ]; then
    echo "[INFO] No source path specified, defaulting to 'src' under the script directory."
    SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
    SRC_DIR="$SCRIPT_DIR/src"
else
    SRC_DIR="$1"
fi

# Check if source path exists
if [ ! -d "$SRC_DIR" ]; then
    echo "[WARNING] The specified source path does not exist: $SRC_DIR"
    echo "Trying to use the script directory as base..."

    SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
    SRC_DIR="$SCRIPT_DIR/$1"

    if [ ! -d "$SRC_DIR" ]; then
        echo "[ERROR] Path still does not exist: $SRC_DIR"
        exit 1
    else
        echo "[INFO] Using corrected path: $SRC_DIR"
    fi
else
    echo "[INFO] Using path: $SRC_DIR"
fi

# Check if Emscripten is installed
if ! command -v emcmake &> /dev/null; then
    echo "[ERROR] Emscripten not detected. Please install and configure the environment."
    echo "Installation guide: https://emscripten.org/docs/getting_started/downloads.html"
    exit 1
fi

echo "[INFO] Using Emscripten toolchain for build"

# Check if Ninja is available
if command -v ninja &> /dev/null; then
    GENERATOR="Ninja"
    echo "[INFO] Ninja build system detected, using Ninja generator."
else
    GENERATOR="Unix Makefiles"
    echo "[INFO] Ninja not detected, falling back to Unix Makefiles."
fi

# Configure project
echo "[INFO] Running cmake with generator: $GENERATOR"
emcmake cmake -B "build" -S "$SRC_DIR" \
    -G "$GENERATOR" \
    -DCMAKE_BUILD_TYPE=Release \
    -DSRC_DIR="$SRC_DIR"

if [ $? -ne 0 ]; then
    echo "[ERROR] CMake configuration failed."
    exit 1
fi

# Build project with parallel jobs
if [ "$GENERATOR" = "Ninja" ]; then
    cmake --build "build" -- -j"$(nproc)"
else
    emmake make -C "build" -j"$(nproc)"
fi

if [ $? -ne 0 ]; then
    echo "[ERROR] Build failed."
    exit 1
fi

OUTPUT_JS="build/MySDLApp.js"
OUTPUT_WASM="build/MySDLApp.wasm"

if [ -f "$OUTPUT_JS" ] && [ -f "$OUTPUT_WASM" ]; then
    echo "[SUCCESS] Build completed! Files located at:"
    echo "  $OUTPUT_JS"
    echo "  $OUTPUT_WASM"
    echo "These can be run in a browser."
else
    echo "[WARNING] Expected output files not found. Please check your CMake configuration."
fi
