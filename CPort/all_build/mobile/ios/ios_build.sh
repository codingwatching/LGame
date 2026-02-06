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

# Check if CMake is installed
if ! command -v cmake &> /dev/null; then
    echo "[ERROR] CMake not detected. Please install CMake."
    echo "On macOS you can run: brew install cmake"
    exit 1
fi

# Check if ios-cmake Toolchain is installed
TOOLCHAIN_FILE=~/ios-cmake/ios.toolchain.cmake
if [ ! -f "$TOOLCHAIN_FILE" ]; then
    echo "[ERROR] iOS Toolchain file not found: $TOOLCHAIN_FILE"
    echo "Please download ios-cmake: https://github.com/leetal/ios-cmake"
    exit 1
fi

echo "[INFO] Using iOS Toolchain: $TOOLCHAIN_FILE"

# Check if Ninja is available
if command -v ninja &> /dev/null; then
    GENERATOR="Ninja"
    echo "[INFO] Ninja build system detected, using Ninja generator."
else
    GENERATOR="Unix Makefiles"
    echo "[INFO] Ninja not detected, falling back to Unix Makefiles."
fi

# Define targets: iOS arm64, iOS x86_64 (simulator), macOS Catalyst
TARGETS=("ios_arm64" "ios_x86_64" "macos_catalyst")

# Build for each target
for TARGET in "${TARGETS[@]}"; do
    echo "[INFO] Building for target: $TARGET"

    BUILD_DIR="build_$TARGET"
    mkdir -p "$BUILD_DIR"

    case $TARGET in
        ios_arm64)
            cmake -B "$BUILD_DIR" -S "$SRC_DIR" \
                -G "$GENERATOR" \
                -DCMAKE_TOOLCHAIN_FILE="$TOOLCHAIN_FILE" \
                -DCMAKE_BUILD_TYPE=Release \
                -DPLATFORM=OS64 \
                -DARCHS=arm64 \
                -DSRC_DIR="$SRC_DIR"
            ;;
        ios_x86_64)
            cmake -B "$BUILD_DIR" -S "$SRC_DIR" \
                -G "$GENERATOR" \
                -DCMAKE_TOOLCHAIN_FILE="$TOOLCHAIN_FILE" \
                -DCMAKE_BUILD_TYPE=Release \
                -DPLATFORM=SIMULATOR64 \
                -DARCHS=x86_64 \
                -DSRC_DIR="$SRC_DIR"
            ;;
        macos_catalyst)
            cmake -B "$BUILD_DIR" -S "$SRC_DIR" \
                -G "$GENERATOR" \
                -DCMAKE_TOOLCHAIN_FILE="$TOOLCHAIN_FILE" \
                -DCMAKE_BUILD_TYPE=Release \
                -DPLATFORM=MACCATALYST \
                -DARCHS=arm64 \
                -DSRC_DIR="$SRC_DIR"
            ;;
    esac

    if [ $? -ne 0 ]; then
        echo "[ERROR] CMake configuration failed for target: $TARGET"
        exit 1
    fi

    cmake --build "$BUILD_DIR" -- -j"$(sysctl -n hw.ncpu)"
    if [ $? -ne 0 ]; then
        echo "[ERROR] Build failed for target: $TARGET"
        exit 1
    fi
done

# Create XCFramework
echo "[INFO] Creating XCFramework..."
xcodebuild -create-xcframework \
    -library build_ios_arm64/libMyIOSApp.a \
    -library build_ios_x86_64/libMyIOSApp.a \
    -library build_macos_catalyst/libMyIOSApp.a \
    -output build/MyIOSApp.xcframework

if [ $? -ne 0 ]; then
    echo "[ERROR] XCFramework creation failed."
    exit 1
fi

echo "[SUCCESS] Build completed! XCFramework is located at build/MyIOSApp.xcframework and supports iOS (device + simulator) and macOS Catalyst."
