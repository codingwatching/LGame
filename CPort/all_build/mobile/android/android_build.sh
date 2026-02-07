#!/usr/bin/env bash
set -euo pipefail

# ---------------- 参数处理 ----------------
ACTION="${1:-release}"

if [[ "$ACTION" == "clean" ]]; then
    echo "[INFO] Cleaning build directories..."
    for ABI in arm64-v8a armeabi-v7a x86_64; do
        rm -rf "$(dirname "$0")/build_$ABI"
    done
    echo "[SUCCESS] Clean completed."
    exit 0
fi

# ---------------- 源码路径处理 ----------------
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
SRC_DIR="${2:-$SCRIPT_DIR/src}"

if [[ ! -d "$SRC_DIR" ]]; then
    echo "[ERROR] The specified source path does not exist: $SRC_DIR"
    exit 1
else
    echo "[INFO] Using path: $SRC_DIR"
fi

# ---------------- ANDROID NDK 检测 ----------------
if [[ -z "${ANDROID_NDK:-}" ]]; then
    echo "[ERROR] ANDROID_NDK environment variable not detected."
    echo "Please download and install Android NDK, then set the ANDROID_NDK environment variable."
    echo "Download: https://developer.android.com/ndk/downloads"
    exit 1
fi

if [[ ! -d "$ANDROID_NDK" ]]; then
    echo "[ERROR] ANDROID_NDK path is invalid: $ANDROID_NDK"
    exit 1
fi

echo "[INFO] Detected Android NDK path: $ANDROID_NDK"

# ---------------- 工具检测 ----------------
if ! command -v cmake >/dev/null 2>&1; then
    echo "[ERROR] CMake not detected. Please install it."
    exit 1
fi

GENERATOR=""
if command -v ninja >/dev/null 2>&1; then
    GENERATOR="Ninja"
    echo "[INFO] Ninja build system detected, using Ninja generator."
else
    GENERATOR="Unix Makefiles"
    echo "[INFO] Ninja not detected, falling back to Unix Makefiles."
fi

# ---------------- 多 ABI 构建 ----------------
ABIS=("arm64-v8a" "armeabi-v7a" "x86_64")

for ABI in "${ABIS[@]}"; do
    echo "[INFO] Building for ABI: $ABI"

    BUILD_DIR="build_$ABI"
    mkdir -p "$BUILD_DIR"

    echo "[INFO] Running cmake with generator: $GENERATOR"

    cmake -B "$BUILD_DIR" -S "$SRC_DIR" \
        -G "$GENERATOR" \
        -DCMAKE_TOOLCHAIN_FILE="$ANDROID_NDK/build/cmake/android.toolchain.cmake" \
        -DANDROID_ABI="$ABI" \
        -DANDROID_PLATFORM=android-21 \
        -DCMAKE_BUILD_TYPE="$ACTION" \
        -DSRC_DIR="$SRC_DIR"

    echo "[INFO] Building..."
    if [[ "$GENERATOR" == "Ninja" ]]; then
        cmake --build "$BUILD_DIR" -- -j"$(nproc)"
    else
        make -C "$BUILD_DIR" -j"$(nproc)"
    fi

    OUTPUT_APK="$BUILD_DIR/MyAndroidApp-$ABI.apk"
    if [[ -f "$OUTPUT_APK" ]]; then
        echo "[SUCCESS] Build completed for ABI: $ABI. APK located at: $OUTPUT_APK"
    else
        echo "[WARNING] APK file not found for ABI: $ABI. Please check your CMake configuration."
    fi
done

echo "[SUCCESS] Multi-ABI build completed! APK files are located in their respective build directories."
