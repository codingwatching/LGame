#!/usr/bin/env bash
set -euo pipefail

# ---------------- 参数处理 ----------------
ACTION=$1
if [ -z "$ACTION" ]; then
    ACTION="Release"
    echo "[INFO] No parameter specified, defaulting to Release build"
else
    echo "[INFO] Action set to $ACTION"
fi

# ---------------- clean 操作 ----------------
if [ "$ACTION" = "clean" ]; then
    echo "[INFO] Cleaning build directories..."
    for ABI in arm64-v8a armeabi-v7a x86_64; do
        if [ -d "$(dirname "$0")/build_$ABI" ]; then
            rm -rf "$(dirname "$0")/build_$ABI"
        fi
    done
    echo "[SUCCESS] Clean completed."
    exit 0
fi

# ---------------- 源码路径处理 ----------------
if [ -z "$2" ]; then
    echo "[INFO] No source path specified, defaulting to script directory."
    SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
    SRC_DIR="$SCRIPT_DIR"
else
    SRC_DIR="$2"
fi

if [ ! -f "$SRC_DIR/CMakeLists.txt" ]; then
    echo "[ERROR] The specified source path does not contain CMakeLists.txt: $SRC_DIR"
    exit 1
else
    echo "[INFO] Using path: $SRC_DIR"
fi

# ---------------- ANDROID NDK 检测 ----------------
if [ -z "$ANDROID_NDK" ]; then
    echo "[ERROR] ANDROID_NDK environment variable not detected."
    echo "Please download and install Android NDK, then set the ANDROID_NDK environment variable."
    echo "Download: https://developer.android.com/ndk/downloads"
    exit 1
fi

if [ ! -d "$ANDROID_NDK" ]; then
    echo "[ERROR] ANDROID_NDK path is invalid: $ANDROID_NDK"
    exit 1
fi

echo "[INFO] Detected Android NDK path: $ANDROID_NDK"

# ---------------- 工具检测 ----------------
if ! command -v cmake >/dev/null 2>&1; then
    echo "[ERROR] CMake not detected. Please install CMake and add it to PATH."
    exit 1
fi

if command -v ninja >/dev/null 2>&1; then
    GENERATOR="Ninja"
    echo "[INFO] Ninja build system detected, using Ninja generator."
else
    GENERATOR="Unix Makefiles"
    echo "[INFO] Ninja not detected, falling back to Unix Makefiles."
fi

# ---------------- Gradle 检测 ----------------
if command -v gradle >/dev/null 2>&1; then
    GRADLEW="gradle"
    echo "[INFO] Global Gradle detected."
elif [ -x "$SRC_DIR/gradlew" ]; then
    GRADLEW="$SRC_DIR/gradlew"
    echo "[INFO] Using project-local Gradle wrapper."
else
    echo "[ERROR] Gradle not detected. Please install Gradle or include gradlew in your project."
    exit 1
fi

# ---------------- 平台版本参数 ----------------
if [ -z "$ANDROID_PLATFORM" ]; then
    ANDROID_PLATFORM="android-23"
fi

# ---------------- 多 ABI 构建 ----------------
ABIS="arm64-v8a armeabi-v7a x86_64"

for ABI in $ABIS; do
    echo "[INFO] Building for ABI: $ABI"

    BUILD_DIR="build_$ABI"
    mkdir -p "$BUILD_DIR"

    echo "[INFO] Running cmake with generator: $GENERATOR"

    cmake -B "$BUILD_DIR" -S "$SRC_DIR" \
        -G "$GENERATOR" \
        -DCMAKE_TOOLCHAIN_FILE="$ANDROID_NDK/build/cmake/android.toolchain.cmake" \
        -DANDROID_ABI="$ABI" \
        -DANDROID_PLATFORM="$ANDROID_PLATFORM" \
        -DCMAKE_BUILD_TYPE="$ACTION" \
        -DSRC_DIR="$SRC_DIR/src"

    cmake --build "$BUILD_DIR" -- -j"$(nproc)"

    OUTPUT_SO="$BUILD_DIR/libMyAndroidApp.so"
    if [ -f "$OUTPUT_SO" ]; then
        echo "[SUCCESS] Build completed for ABI: $ABI. SO located at: $OUTPUT_SO"
    else
        echo "[WARNING] SO file not found for ABI: $ABI. Please check your CMake configuration."
    fi
done

# ---------------- APK 打包 ----------------
echo "[INFO] Building APK with Gradle..."
"$GRADLEW" assemble"$ACTION"

echo "[SUCCESS] Multi-ABI build completed! APK files are located in app/build/outputs/apk/$ACTION/"
