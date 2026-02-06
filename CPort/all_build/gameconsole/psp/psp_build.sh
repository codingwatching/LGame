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

# Check if PSPDEV is set
if [ -z "$PSPDEV" ]; then
    echo "[ERROR] PSPDEV environment variable not detected. Please install PSPSDK and set the environment variable."
    echo "Download: https://github.com/pspdev/psptoolchain"
    exit 1
fi

TOOLCHAIN_FILE="$PSPDEV/psp.cmake"
if [ ! -f "$TOOLCHAIN_FILE" ]; then
    echo "[ERROR] PSP Toolchain file not found: $TOOLCHAIN_FILE"
    exit 1
fi

echo "[INFO] Using PSP toolchain: $TOOLCHAIN_FILE"

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
    -DCMAKE_BUILD_TYPE=Release \
    -DSRC_DIR="$SRC_DIR"

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

OUTPUT_PBP="build/EBOOT.PBP"

if [ ! -f "$OUTPUT_PBP" ]; then
    echo "[ERROR] EBOOT.PBP file not generated: $OUTPUT_PBP"
    exit 1
fi

echo "[SUCCESS] Build completed! EBOOT.PBP file is located in the build/ directory and can be run on PSP."
