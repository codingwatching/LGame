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
        if ! command -v pacman &> /dev/null; then
            echo "[ERROR] pacman not found. Please install pacman first."
            exit 1
        fi

        echo "[INFO] Adding devkitPro pacman repository..."
        sudo tee -a /etc/pacman.conf <<EOF
[dkp-libs]
Server = https://downloads.devkitpro.org/packages
SigLevel = Never
EOF

        echo "[INFO] Installing devkitPro toolchain for Switch..."
        sudo pacman -Sy --noconfirm devkitA64 switch-dev

        export DEVKITPRO=/opt/devkitpro
        export DEVKITARM=$DEVKITPRO/devkitA64
        echo "export DEVKITPRO=/opt/devkitpro" >> ~/.bashrc
        echo "export DEVKITARM=/opt/devkitpro/devkitA64" >> ~/.bashrc
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

# 检查 DEVKITARM
if [ -z "${DEVKITARM:-}" ]; then
    echo "[ERROR] DEVKITARM not set. Please ensure devkitPro is fully installed."
    exit 1
fi

TOOLCHAIN_FILE="$DEVKITPRO/cmake/Switch.cmake"
if [ ! -f "$TOOLCHAIN_FILE" ]; then
    echo "[ERROR] Switch Toolchain file not found: $TOOLCHAIN_FILE"
    exit 1
fi
echo "[INFO] Using devkitPro toolchain: $TOOLCHAIN_FILE"

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

# 配置项目（依赖由 CMake FetchContent 自动下载）
cmake -B "$BUILD_DIR" -S "$SCRIPT_DIR" \
    -G "$GENERATOR" \
    -DCMAKE_TOOLCHAIN_FILE="$TOOLCHAIN_FILE" \
    -DCMAKE_BUILD_TYPE="$BUILD_TYPE" \
    -DSRC_DIR="$SRC_DIR"

# 构建项目
cmake --build "$BUILD_DIR" -- -j"$(nproc)"

OUTPUT_NRO="$BUILD_DIR/MySwitchApp.nro"
OUTPUT_NSP="$BUILD_DIR/MySwitchApp.nsp"

if [ ! -f "$OUTPUT_NRO" ]; then
    echo "[ERROR] NRO file not generated: $OUTPUT_NRO"
    exit 1
fi

echo "[INFO] Generating NSP package..."
if command -v nspbuild &> /dev/null; then
    # 使用 devkitPro 提供的 nspbuild 工具
    nspbuild -o "$OUTPUT_NSP" "$OUTPUT_NRO"
    if [ ! -f "$OUTPUT_NSP" ]; then
        echo "[ERROR] NSP packaging failed."
        exit 1
    fi
    echo "[SUCCESS] Build completed! NRO and NSP files are located in $BUILD_DIR"
else
    echo "[WARNING] nspbuild not detected. Skipping NSP packaging."
    echo "[SUCCESS] Build completed! NRO file is located in $BUILD_DIR"
fi
