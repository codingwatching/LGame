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

# Check if DEVKITPRO is set
if [ -z "$DEVKITPRO" ]; then
    echo "[ERROR] DEVKITPRO environment variable not detected. Please install devkitPro and set the environment variable."
    echo "Download: https://devkitpro.org"
    exit 1
fi

TOOLCHAIN_FILE="$DEVKITPRO/cmake/3DS.cmake"
if [ ! -f "$TOOLCHAIN_FILE" ]; then
    echo "[ERROR] 3DS Toolchain file not found: $TOOLCHAIN_FILE"
    exit 1
fi

echo "[INFO] Using 3DS toolchain: $TOOLCHAIN_FILE"

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

OUTPUT_ELF="build/MySDLApp.elf"
OUTPUT_3DSX="build/MySDLApp.3dsx"
OUTPUT_CIA="build/MySDLApp.cia"

if [ ! -f "$OUTPUT_ELF" ]; then
    echo "[ERROR] ELF file not generated: $OUTPUT_ELF"
    exit 1
fi

echo "[INFO] Converting ELF to 3DSX..."
3dsxtool "$OUTPUT_ELF" "$OUTPUT_3DSX"
if [ $? -ne 0 ]; then
    echo "[ERROR] Conversion to 3DSX failed."
    exit 1
fi

echo "[INFO] Generating CIA package..."
if command -v makerom &> /dev/null; then
    # Paths to banner/icon assets (replace with your own files)
    BANNER="assets/banner.bin"
    ICON="assets/icon.png"
    RSF="$DEVKITPRO/3ds_rules/template.rsf"

    if [ ! -f "$BANNER" ] || [ ! -f "$ICON" ]; then
        echo "[WARNING] Banner or icon not found in assets/. CIA will be generated without custom graphics."
        makerom -f cia -o "$OUTPUT_CIA" -elf "$OUTPUT_ELF" -rsf "$RSF"
    else
        echo "[INFO] Injecting banner and icon into CIA..."
        makerom -f cia -o "$OUTPUT_CIA" -elf "$OUTPUT_ELF" -rsf "$RSF" -banner "$BANNER" -icon "$ICON"
    fi

    if [ $? -ne 0 ]; then
        echo "[ERROR] CIA packaging failed."
        exit 1
    fi
    echo "[SUCCESS] Build completed! .3DSX and .CIA files are located in the build/ directory."
else
    echo "[WARNING] makerom not detected. Skipping CIA packaging."
    echo "[SUCCESS] Build completed! .3DSX file is located in the build/ directory."
fi
