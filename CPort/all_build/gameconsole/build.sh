#!/bin/bash

# Check if source path and platform are provided
if [ -z "$1" ] || [ -z "$2" ]; then
    echo "[ERROR] Please specify the source path and platform."
    echo "Usage: ./build.sh <source_path> <platform>"
    echo "Supported platforms: PS4 PS5 PSV PSP SWITCH 3DS XBOX DREAMCAST"
    exit 1
fi

SRC_DIR=$1
PLATFORM=$2

# Check if source path exists
if [ ! -d "$SRC_DIR" ]; then
    echo "[ERROR] The specified source path does not exist: $SRC_DIR"
    exit 1
fi

echo "[INFO] Target platform: $PLATFORM"

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

# SDK environment variable checks per platform
case $PLATFORM in
    PS4)
        if [ -z "$PS4SDK" ]; then
            echo "[ERROR] PS4SDK environment variable not detected. Please set PS4SDK to your PS4 SDK path."
            exit 1
        fi
        ;;
    PS5)
        if [ -z "$PS5SDK" ]; then
            echo "[ERROR] PS5SDK environment variable not detected. Please set PS5SDK to your PS5 SDK path."
            exit 1
        fi
        ;;
    PSV)
        if [ -z "$PSVSDK" ]; then
            echo "[ERROR] PSVSDK environment variable not detected. Please set PSVSDK to your PSV SDK path."
            exit 1
        fi
        ;;
    PSP)
        if [ -z "$PSPSDK" ]; then
            echo "[ERROR] PSPSDK environment variable not detected. Please set PSPSDK to your PSP SDK path."
            exit 1
        fi
        ;;
    SWITCH)
        if [ -z "$SWITCHSDK" ]; then
            echo "[ERROR] SWITCHSDK environment variable not detected. Please set SWITCHSDK to your Switch SDK path."
            exit 1
        fi
        ;;
    3DS)
        if [ -z "$CTRSDK" ]; then
            echo "[ERROR] CTRSDK environment variable not detected. Please set CTRSDK to your 3DS SDK path."
            exit 1
        fi
        ;;
    XBOX)
        if [ -z "$XBOXSDK" ]; then
            echo "[ERROR] XBOXSDK environment variable not detected. Please set XBOXSDK to your Xbox SDK path."
            exit 1
        fi
        ;;
    DREAMCAST)
        if [ -z "$DCSDK" ]; then
            echo "[ERROR] DCSDK environment variable not detected. Please set DCSDK to your Dreamcast SDK path."
            exit 1
        fi
        ;;
    *)
        echo "[WARNING] Undefined platform. SDK check skipped."
        ;;
esac

# Configure project
cmake -B build -S . -G "$GENERATOR" -DCMAKE_BUILD_TYPE=Release -DSRC_DIR="$SRC_DIR" -DPLATFORM=$PLATFORM
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

# Packaging per platform
case $PLATFORM in
    PSV)
        echo "[INFO] Packaging VPK..."
        vita-make-fself build/MySDLApp.elf build/eboot.bin
        vita-pack-vpk build/MySDLApp.vpk -s sce_sys/icon0.png -s sce_sys/param.sfo build/eboot.bin
        echo "[SUCCESS] VPK file located in build/ directory."
        ;;
    PSP)
        echo "[INFO] Packaging EBOOT.PBP..."
        pack-pbp build/EBOOT.PBP build/MySDLApp.elf
        echo "[SUCCESS] EBOOT.PBP file located in build/ directory."
        ;;
    3DS)
        echo "[INFO] Converting to 3DSX..."
        3dsxtool build/MySDLApp.elf build/MySDLApp.3dsx
        echo "[SUCCESS] 3DSX file located in build/ directory."
        ;;
    SWITCH)
        echo "[INFO] Packaging NRO..."
        elf2nro build/MySDLApp.elf build/MySDLApp.nro
        echo "[SUCCESS] NRO file located in build/ directory."
        ;;
    PS4)
        echo "[SUCCESS] SELF file located in build/ directory."
        ;;
    PS5)
        echo "[SUCCESS] PKG file located in build/ directory."
        ;;
    XBOX)
        echo "[SUCCESS] EXE file located in build/ directory."
        ;;
    DREAMCAST)
        echo "[SUCCESS] BIN file located in build/ directory."
        ;;
    *)
        echo "[WARNING] Undefined packaging step. Please handle manually."
        ;;
esac
