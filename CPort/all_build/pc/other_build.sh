#!/usr/bin/env bash
set -e

if [ -d "build" ]; then
    echo "[INFO] Removing old build directory..."
    rm -rf build
fi

if [ -z "$1" ]; then
    echo "[INFO] No source path specified, defaulting to src under script directory"
    SRC_DIR="$(dirname "$0")/src"
else
    SRC_DIR="$1"
fi

if [ ! -d "$SRC_DIR" ]; then
    echo "[ERROR] Source path does not exist: $SRC_DIR"
    exit 1
else
    echo "[INFO] Using source path: $SRC_DIR"
fi

if ! command -v cmake >/dev/null 2>&1; then
    echo "[ERROR] CMake not detected. Please install it."
    exit 1
fi

GENERATOR=""
if command -v ninja >/dev/null 2>&1; then
    echo "[INFO] Ninja detected, using Ninja generator."
    GENERATOR="Ninja"
elif command -v clang >/dev/null 2>&1; then
    echo "[INFO] Clang detected, using Unix Makefiles."
    GENERATOR="Unix Makefiles"
elif command -v gcc >/dev/null 2>&1; then
    echo "[INFO] GCC detected, using Unix Makefiles."
    GENERATOR="Unix Makefiles"
else
    echo "[ERROR] No supported compiler detected. Please install clang, gcc, or ninja."
    exit 1
fi

echo "[INFO] Running cmake with generator: $GENERATOR"
cmake -B build -S . -G "$GENERATOR" -DCMAKE_BUILD_TYPE=Release -DSRC_DIR="$SRC_DIR"
cmake --build build

if [ -f "build/MySDLApp" ]; then
    echo "[SUCCESS] Build completed! Executable located at build/MySDLApp"
elif [ -f "build/MySDLApp.exe" ]; then
    echo "[SUCCESS] Build completed! Executable located at build/MySDLApp.exe"
else
    echo "[WARNING] Build finished but executable not found in expected location."
fi
