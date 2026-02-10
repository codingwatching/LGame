#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")" && pwd)"
GN_DIR="$ROOT_DIR/gn"
ANGLE_DIR="$ROOT_DIR/external/angle"

ACTION="${1:-release}"
BUILD_ANGLE=false

if [[ "$ACTION" == "clean" ]]; then
    echo "[INFO] Cleaning build, external, and gn directories..."
    rm -rf "$ROOT_DIR/build" "$ROOT_DIR/external" "$GN_DIR"
    echo "[SUCCESS] Clean completed."
    exit 0
fi

if [[ "${2:-}" == "--angle" ]]; then
    BUILD_ANGLE=true
    echo "[INFO] ANGLE build enabled"
else
    echo "[INFO] ANGLE build disabled (default)"
fi

SRC_DIR="${3:-$ROOT_DIR/src}"
if [[ ! -d "$SRC_DIR" ]]; then
    echo "[ERROR] Source path does not exist: $SRC_DIR"
    exit 1
else
    echo "[INFO] Using path: $SRC_DIR"
fi

# ---------------- 工具检测 ----------------
if ! command -v cmake >/dev/null 2>&1; then
    echo "[ERROR] CMake not detected. Please install it."
    exit 1
fi

if ! command -v git >/dev/null 2>&1; then
    echo "[ERROR] Git not detected. Please install it."
    exit 1
fi

if ! command -v ninja >/dev/null 2>&1; then
    echo "[ERROR] Ninja not detected. Please install it."
    exit 1
fi

# ---------------- GN 构建 ----------------
GN_OUT="$GN_DIR/out"
GN_EXE="$GN_OUT/gn"

if [[ ! -x "$GN_EXE" ]]; then
    if [[ ! -d "$GN_DIR" ]]; then
        echo "[INFO] Cloning GN repository..."
        git clone https://gn.googlesource.com/gn "$GN_DIR"
    fi
    cd "$GN_DIR"
    python3 build/gen.py
    ninja -C out
    sed -i 's/\/WX//g' out/build.ninja
    echo "[SUCCESS] GN built successfully: $GN_EXE"
fi

# ---------------- ANGLE 构建（可选） ----------------
if $BUILD_ANGLE; then
    mkdir -p "$ROOT_DIR/external"
    if [[ ! -d "$ANGLE_DIR" ]]; then
        echo "[INFO] Cloning ANGLE repository..."
        git clone https://chromium.googlesource.com/angle/angle "$ANGLE_DIR"
    fi
    cd "$ANGLE_DIR"

    if [[ -f "out/$ACTION/libEGL.dylib" && -f "out/$ACTION/libGLESv2.dylib" ]]; then
        echo "[INFO] ANGLE already built, skipping rebuild"
    else
        echo "[INFO] Generating ANGLE build files with GN..."
        "$GN_EXE" gen "out/$ACTION" --args="is_debug=$( [[ "$ACTION" == "release" ]] && echo false || echo true ) angle_enable_gl=false angle_enable_vulkan=false"
        sed -i 's/\/WX//g' "out/$ACTION/build.ninja"
        echo "[INFO] Building ANGLE with Ninja..."
        ninja -C "out/$ACTION" libEGL libGLESv2 -j"$(nproc)" --quiet
        echo "[SUCCESS] ANGLE built successfully with GN/Ninja."
    fi
else
    echo "[INFO] Skipping ANGLE build"
fi

# ---------------- SDL 项目构建 ----------------
SDL_USE_ANGLE="-DUSE_ANGLE=OFF"
SDL_ANGLE_DIR=""

if $BUILD_ANGLE; then
    SDL_USE_ANGLE="-DUSE_ANGLE=ON"
    SDL_ANGLE_DIR="-DANGLE_DIR=$ANGLE_DIR/out/$ACTION"
    echo "[INFO] SDL build will enable ANGLE"
elif [[ -f "$ANGLE_DIR/out/$ACTION/libEGL.dylib" && -f "$ANGLE_DIR/out/$ACTION/libGLESv2.dylib" ]]; then
    SDL_USE_ANGLE="-DUSE_ANGLE=ON"
    SDL_ANGLE_DIR="-DANGLE_DIR=$ANGLE_DIR/out/$ACTION"
    echo "[INFO] Detected existing ANGLE build, enabling ANGLE for SDL"
else
    echo "[INFO] SDL build will not use ANGLE"
fi

echo "[INFO] Running CMake..."
cmake -B build -S . -G Ninja -Wno-dev \
    -DCMAKE_BUILD_TYPE="$ACTION" -DSRC_DIR="$SRC_DIR" $SDL_USE_ANGLE $SDL_ANGLE_DIR

echo "[INFO] Building project..."
cmake --build build -- -j"$(nproc)" --quiet

echo "[SUCCESS] Build completed! Executable located at build/MyDesktopApp"
