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

# Check if STEAMWORKS_SDK_PATH is set
if [ -z "$STEAMWORKS_SDK_PATH" ]; then
    echo "[ERROR] STEAMWORKS_SDK_PATH environment variable not detected. Please set it to the Steamworks SDK path."
    exit 1
fi

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
    -DCMAKE_BUILD_TYPE=Release \
    -DSRC_DIR="$SRC_DIR" \
    -DSTEAMWORKS_SDK_PATH="$STEAMWORKS_SDK_PATH"

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

OUTPUT_EXE="build/MySDLApp"

if [ ! -f "$OUTPUT_EXE" ]; then
    echo "[ERROR] Executable file not generated: $OUTPUT_EXE"
    exit 1
fi

echo "[SUCCESS] Build completed! Executable file is located in the build/ directory and can be run through Steam."
