#!/bin/bash
set -euo pipefail

trap 'echo "[ERROR] Script failed at line $LINENO"; exit 1' ERR

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
SRC_DIR="${1:-$SCRIPT_DIR/src}"
BUILD_DIR="${2:-$SCRIPT_DIR/build}"
BUILD_TYPE="${3:-Release}"

echo "[INFO] Source directory: $SRC_DIR"
echo "[INFO] Build directory: $BUILD_DIR"
echo "[INFO] Build type: $BUILD_TYPE"

# 检查源码目录
if [ ! -d "$SRC_DIR" ]; then
    echo "[ERROR] Source path does not exist: $SRC_DIR"
    exit 1
fi

# 检查 DEVKITPRO
if [ -z "${DEVKITPRO:-}" ]; then
    echo "[WARNING] DEVKITPRO not set. Attempting to install devkitPro toolchain..."

    if [[ "$OSTYPE" == "linux-gnu"* || "$OSTYPE" == "darwin"* ]]; then
        # Linux/macOS 使用 pacman
        if ! command -v pacman &> /dev/null; then
            echo "[ERROR] pacman not found. Please install pacman first (on macOS via brew, on Linux via package manager)."
            exit 1
        fi

        echo "[INFO] Adding devkitPro pacman repository..."
        sudo tee -a /etc/pacman.conf <<EOF
[dkp-libs]
Server = https://downloads.devkitpro.org/packages
SigLevel = Never
EOF

        echo "[INFO] Installing devkitPro toolchain..."
        sudo pacman -Sy --noconfirm devkitARM 3ds-dev

        export DEVKITPRO=/opt/devkitpro
        echo "export DEVKITPRO=/opt/devkitpro" >> ~/.bashrc
        echo "[INFO] devkitPro installed at $DEVKITPRO"

    elif [[ "$OSTYPE" == "msys"* || "$OSTYPE" == "cygwin"* || "$OSTYPE" == "win32"* ]]; then
        echo "[INFO] Windows detected. Please download and run the devkitPro installer manually:"
        echo "https://github.com/devkitPro/installer/releases"
        exit 1
    else
        echo "[ERROR] Unsupported OS type: $OSTYPE"
        exit 1
    fi
fi

TOOLCHAIN_FILE="$DEVKITPRO/cmake/3DS.cmake"
if [ ! -f "$TOOLCHAIN_FILE" ]; then
    echo "[ERROR] Toolchain file not found: $TOOLCHAIN_FILE"
    exit 1
fi
echo "[INFO] Using toolchain: $TOOLCHAIN_FILE"

# 检查 CMake
if ! command -v cmake &> /dev/null; then
    echo "[ERROR] CMake not detected. Please install CMake."
    exit 1
fi

# 检查 Ninja
if command -v ninja &> /dev/null; then
    GENERATOR="Ninja"
    echo "[INFO] Ninja detected, using Ninja generator."
else
    GENERATOR="Unix Makefiles"
    echo "[INFO] Falling back to Unix Makefiles."
fi

# 清理旧构建目录
rm -rf "$BUILD_DIR"
mkdir -p "$BUILD_DIR"

# 配置项目
cmake -B "$BUILD_DIR" -S "$SCRIPT_DIR" \
    -G "$GENERATOR" \
    -DCMAKE_TOOLCHAIN_FILE="$TOOLCHAIN_FILE" \
    -DCMAKE_BUILD_TYPE="$BUILD_TYPE" \
    -DSRC_DIR="$SRC_DIR"

# 构建项目
cmake --build "$BUILD_DIR" -- -j"$(nproc)"

OUTPUT_ELF="$BUILD_DIR/My3DSApp.elf"
OUTPUT_3DSX="$BUILD_DIR/My3DSApp.3dsx"
OUTPUT_CIA="$BUILD_DIR/My3DSApp.cia"

if [ ! -f "$OUTPUT_ELF" ]; then
    echo "[ERROR] ELF file not generated: $OUTPUT_ELF"
    exit 1
fi

echo "[INFO] Converting ELF to 3DSX..."
3dsxtool "$OUTPUT_ELF" "$OUTPUT_3DSX"

if [ ! -f "$OUTPUT_3DSX" ]; then
    echo "[ERROR] 3DSX file not generated."
    exit 1
fi

echo "[INFO] Generating CIA package..."
if command -v makerom &> /dev/null; then
    BANNER="assets/banner.bin"
    ICON="assets/icon.png"
    RSF="$DEVKITPRO/3ds_rules/template.rsf"

    if [ -f "$BANNER" ] && [ -f "$ICON" ]; then
        makerom -f cia -o "$OUTPUT_CIA" -elf "$OUTPUT_ELF" -rsf "$RSF" -banner "$BANNER" -icon "$ICON"
    else
        echo "[WARNING] Banner or icon not found, generating CIA without custom graphics."
        makerom -f cia -o "$OUTPUT_CIA" -elf "$OUTPUT_ELF" -rsf "$RSF"
    fi

    if [ ! -f "$OUTPUT_CIA" ]; then
        echo "[ERROR] CIA packaging failed."
        exit 1
    fi
    echo "[SUCCESS] Build completed! Files are in $BUILD_DIR"
else
    echo "[WARNING] makerom not detected. Skipping CIA packaging."
    echo "[SUCCESS] Build completed! ELF and 3DSX files are in $BUILD_DIR"
fi
