#!/usr/bin/env bash
set -e

# 检查参数
if [ -z "$1" ]; then
  echo "[ERROR] Please specify the source path when running the script."
  echo "Usage: ./steam_build.sh <source_path>"
  exit 1
fi

SRC_DIR="$1"

if [ ! -d "$SRC_DIR" ]; then
  echo "[ERROR] The specified source path does not exist: $SRC_DIR"
  exit 1
fi

# 检查 Steamworks SDK 环境变量
if [ -z "$STEAMWORKS_SDK_PATH" ]; then
  echo "[ERROR] STEAMWORKS_SDK_PATH environment variable not detected. Please set it to the Steamworks SDK path."
  exit 1
fi

# 检查 CMake
if ! command -v cmake >/dev/null 2>&1; then
  echo "[ERROR] CMake not detected. Please install it."
  exit 1
fi

TOOLS_DIR="$(dirname "$0")/external/tools"
mkdir -p "$TOOLS_DIR"

# 自动安装 GN
if ! command -v gn >/dev/null 2>&1; then
  echo "[INFO] GN not found, downloading..."
  GN_URL="https://storage.googleapis.com/chrome-infra/gn/gn$(uname | tr '[:upper:]' '[:lower:]')"
  curl -L "$GN_URL" -o "$TOOLS_DIR/gn"
  chmod +x "$TOOLS_DIR/gn"
  export PATH="$TOOLS_DIR:$PATH"
  echo "[SUCCESS] GN installed to $TOOLS_DIR"
fi

# 自动安装 Ninja
if ! command -v ninja >/dev/null 2>&1; then
  echo "[INFO] Ninja not found, downloading..."
  NINJA_URL="https://github.com/ninja-build/ninja/releases/download/v1.11.1/ninja-$(uname | tr '[:upper:]' '[:lower:]').zip"
  curl -L "$NINJA_URL" -o "$TOOLS_DIR/ninja.zip"
  unzip -o "$TOOLS_DIR/ninja.zip" -d "$TOOLS_DIR"
  chmod +x "$TOOLS_DIR/ninja"
  export PATH="$TOOLS_DIR:$PATH"
  echo "[SUCCESS] Ninja installed to $TOOLS_DIR"
fi

# 优先 GN+Ninja 构建 ANGLE
if command -v gn >/dev/null 2>&1 && command -v ninja >/dev/null 2>&1; then
  echo "[INFO] GN and Ninja detected. Using GN/Ninja to build ANGLE first..."

  ANGLE_DIR="$(dirname "$0")/external/angle"
  if [ ! -d "$ANGLE_DIR" ]; then
    echo "[INFO] Cloning ANGLE repository..."
    git clone https://github.com/google/angle.git "$ANGLE_DIR"
  fi

  cd "$ANGLE_DIR"
  gn gen out/Release --args="is_debug=false angle_enable_gl=true angle_enable_vulkan=true"
  ninja -C out/Release libEGL libGLESv2
  echo "[SUCCESS] ANGLE built successfully with GN/Ninja."
  ANGLE_BUILT=1
  cd -
else
  ANGLE_BUILT=0
fi

# 使用 CMake 构建主项目
if [ "$ANGLE_BUILT" -eq 1 ]; then
  echo "[INFO] Configuring CMake project with local ANGLE build..."
  cmake -B build -S . -G "Ninja" -DCMAKE_BUILD_TYPE=Release -DSRC_DIR="$SRC_DIR" -DSTEAMWORKS_SDK_PATH="$STEAMWORKS_SDK_PATH" -DFETCHCONTENT_FULLY_DISCONNECTED=ON -DANGLE_DIR="$ANGLE_DIR/out/Release"
  cmake --build build
  echo "[SUCCESS] Build completed with GN/Ninja ANGLE! Executable located at build/MyStreamApp"
  exit 0
fi

# 回退到 GCC/Ninja
if command -v gcc >/dev/null 2>&1; then
  echo "[INFO] GCC compiler detected."
  cmake -B build -S . -G "Unix Makefiles" -DCMAKE_BUILD_TYPE=Release -DSRC_DIR="$SRC_DIR" -DSTEAMWORKS_SDK_PATH="$STEAMWORKS_SDK_PATH" -DFETCHCONTENT_FULLY_DISCONNECTED=ON
  cmake --build build
  echo "[SUCCESS] Build completed with GCC! Executable located at build/MyStreamApp"
  exit 0
fi

if command -v ninja >/dev/null 2>&1; then
  echo "[INFO] Ninja detected, using Ninja generator."
  cmake -B build -S . -G "Ninja" -DCMAKE_BUILD_TYPE=Release -DSRC_DIR="$SRC_DIR" -DSTEAMWORKS_SDK_PATH="$STEAMWORKS_SDK_PATH" -DFETCHCONTENT_FULLY_DISCONNECTED=ON
  cmake --build build
  echo "[SUCCESS] Build completed with Ninja! Executable located at build/MyStreamApp"
  exit 0
fi

echo "[ERROR] No supported compiler detected. Please install GN/Ninja or GCC."
exit 1
