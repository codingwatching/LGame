#!/bin/bash

APP_NAME=${2:-MyPS4App}   # Second parameter specifies app name, default MyPS4App
BUILD_TYPE=${3:-Release}  # Third parameter specifies build type, default Release

# Check if source path parameter is provided
if [ -z "$1" ]; then
    echo "[ERROR] Please specify the source path when running the script."
    echo "Usage: ./ps4_build.sh <source_path> [app_name] [build_type]"
    exit 1
fi

SRC_DIR=$1

# Check if source path exists
if [ ! -d "$SRC_DIR" ]; then
    echo "[ERROR] The specified source path does not exist: $SRC_DIR"
    exit 1
fi

# Check if PS4SDK is set
if [ -z "$PS4SDK" ]; then
    echo "[ERROR] PS4SDK environment variable not detected. Please install PlayStation 4 SDK and set the environment variable."
    exit 1
fi

TOOLCHAIN_FILE="$PS4SDK/share/ps4.toolchain.cmake"
if [ ! -f "$TOOLCHAIN_FILE" ]; then
    echo "[ERROR] PS4 Toolchain file not found: $TOOLCHAIN_FILE"
    exit 1
fi

echo "[INFO] Using PS4 toolchain: $TOOLCHAIN_FILE"

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

# Configure project
cmake -B build -S . \
    -G "$GENERATOR" \
    -DCMAKE_TOOLCHAIN_FILE="$TOOLCHAIN_FILE" \
    -DCMAKE_BUILD_TYPE="$BUILD_TYPE" \
    -DSRC_DIR="$SRC_DIR" \
    -DAPP_NAME="$APP_NAME"

if [ $? -ne 0 ]; then
    echo "[ERROR] CMake configuration failed."
    exit 1
fi

# Build project with parallel jobs
cmake --build build -- -j"$(nproc)"
if [ $? -ne 0 ]; then
    echo "[ERROR] Build failed."
    exit 1
fi

OUTPUT_PKG="build/${APP_NAME}.pkg"

if [ -f "$OUTPUT_PKG" ]; then
    echo "[SUCCESS] Build completed! PKG file located at: $OUTPUT_PKG"
else
    echo "[WARNING] Build completed, but PKG file not found. Please check CMake configuration."
fi
