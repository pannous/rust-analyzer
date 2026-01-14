#!/bin/bash
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../../.." && pwd)"
PLUGIN_DIR="$SCRIPT_DIR/.."
RESOURCES_DIR="$PLUGIN_DIR/src/main/resources/bin"

echo "Building custom rust-analyzer for macOS..."
cd "$PROJECT_ROOT"

echo "Building for aarch64 (Apple Silicon)..."
cargo build --release --target aarch64-apple-darwin

echo "Building for x86_64 (Intel)..."
cargo build --release --target x86_64-apple-darwin

echo "Copying binaries to plugin resources..."
mkdir -p "$RESOURCES_DIR/macos-arm64" "$RESOURCES_DIR/macos-x64"

cp "$PROJECT_ROOT/target/aarch64-apple-darwin/release/rust-analyzer" \
   "$RESOURCES_DIR/macos-arm64/rust-analyzer"

cp "$PROJECT_ROOT/target/x86_64-apple-darwin/release/rust-analyzer" \
   "$RESOURCES_DIR/macos-x64/rust-analyzer"

echo "Binary sizes:"
ls -lh "$RESOURCES_DIR/macos-arm64/rust-analyzer"
ls -lh "$RESOURCES_DIR/macos-x64/rust-analyzer"

echo "Done! Now run: cd editors/rustrover && ./gradlew buildPlugin"
