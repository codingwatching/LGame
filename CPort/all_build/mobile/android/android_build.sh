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
    echo "[ERROR] The specified source path does not exist: $SRC_DIR"
    exit 1
fi

# Check if ANDROID_NDK is set
if [ -z "$ANDROID_NDK" ]; then
    echo "[ERROR] ANDROID_NDK environment variable not detected."
    echo "Please download and install Android NDK, then set the ANDROID_NDK environment variable."
    echo "Download: https://developer.android.com/ndk/downloads"
    exit 1
fi

# Check if ANDROID_NDK path exists
if [ ! -d "$ANDROID_NDK" ]; then
    echo "[ERROR] ANDROID_NDK path is invalid: $ANDROID_NDK"
    exit 1
fi

echo "[INFO] Detected Android NDK path: $ANDROID_NDK"

# Check if CMake is installed
if ! command -v cmake &> /dev/null; then
    echo "[ERROR] CMake not detected. Please install CMake and add it to PATH."
    exit 1
fi

# Check if Ninja is available
if command -v ninja &> /dev/null; then
    GENERATOR="Ninja"
    echo "[INFO] Ninja build system detected, using Ninja generator."
else
    GENERATOR="Unix Makefiles"
    echo "[INFO] Ninja not detected, falling back to Unix Makefiles."
fi

# Define target ABIs
ABIS=("arm64-v8a" "armeabi-v7a" "x86_64")

# Build for each ABI
for ABI in "${ABIS[@]}"; do
    echo "[INFO] Building for ABI: $ABI"

    BUILD_DIR="build_$ABI"
    mkdir -p "$BUILD_DIR"

    cmake -B "$BUILD_DIR" -S "$SRC_DIR" \
        -G "$GENERATOR" \
        -DCMAKE_TOOLCHAIN_FILE="$ANDROID_NDK/build/cmake/android.toolchain.cmake" \
        -DANDROID_ABI="$ABI" \
        -DANDROID_PLATFORM=android-21 \
        -DCMAKE_BUILD_TYPE=Release \
        -DSRC_DIR="$SRC_DIR"

    if [ $? -ne 0 ]; then
        echo "[ERROR] CMake configuration failed for ABI: $ABI"
        exit 1
    fi

    cmake --build "$BUILD_DIR" -- -j"$(nproc)"
    if [ $? -ne 0 ]; then
        echo "[ERROR] Build failed for ABI: $ABI"
        exit 1
    fi

    OUTPUT_APK="$BUILD_DIR/MyAndroidApp-$ABI.apk"
    if [ -f "$OUTPUT_APK" ]; then
        echo "[SUCCESS] Build completed for ABI: $ABI. APK located at: $OUTPUT_APK"
    else
        echo "[WARNING] APK file not found for ABI: $ABI. Please check your CMake configuration."
    fi
done

echo "[SUCCESS] Multi-ABI build completed! APK files are located in their respective build directories."
