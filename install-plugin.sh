#!/bin/bash

# Auto-install Custom Rust Analyzer Plugin to RustRover

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PLUGIN_DIR="$SCRIPT_DIR/editors/rustrover"

# Use Java 21 (required for Gradle/IntelliJ plugin)
export JAVA_HOME="${JAVA_HOME:-$(/usr/libexec/java_home -v 21 2>/dev/null || echo "$HOME/Library/Java/JavaVirtualMachines/openjdk-21.0.1/Contents/Home")}"

echo "Building Custom Rust Analyzer Plugin..."

cd "$PLUGIN_DIR"
./gradlew buildPlugin

# Find the built plugin
PLUGIN_ZIP=$(ls -t build/distributions/custom-rust-analyzer-*.zip 2>/dev/null | head -1)

if [ -z "$PLUGIN_ZIP" ] || [ ! -f "$PLUGIN_ZIP" ]; then
    echo "Error: Plugin not found in build/distributions/"
    exit 1
fi

VERSION=$(basename "$PLUGIN_ZIP" | sed 's/custom-rust-analyzer-//' | sed 's/.zip//')
echo "Plugin built: $PLUGIN_ZIP (v$VERSION)"

# Find RustRover installation
JETBRAINS_BASE="$HOME/Library/Application Support/JetBrains"
RUSTROVER_DIRS=()

if [ -d "$JETBRAINS_BASE" ]; then
    for ide_dir in "$JETBRAINS_BASE"/RustRover*; do
        if [ -d "$ide_dir" ]; then
            RUSTROVER_DIRS+=("$ide_dir/plugins")
        fi
    done
fi

if [ ${#RUSTROVER_DIRS[@]} -eq 0 ]; then
    echo "No RustRover installation found"
    echo "Plugin available at: $PLUGIN_ZIP"
    echo "Install manually: Settings -> Plugins -> Install from Disk"
    exit 1
fi

echo "Found ${#RUSTROVER_DIRS[@]} RustRover installation(s)"

# Install to each RustRover
for plugin_dir in "${RUSTROVER_DIRS[@]}"; do
    ide_name=$(echo "$plugin_dir" | sed 's|.*/JetBrains/||' | sed 's|/plugins||')
    echo "Installing to $ide_name..."

    mkdir -p "$plugin_dir"

    # Remove old installation
    rm -rf "$plugin_dir/custom-rust-analyzer"

    # Unzip plugin
    unzip -q "$PLUGIN_ZIP" -d "$plugin_dir/"

    echo "  Installed to $ide_name"
done

echo ""
echo "Installation complete!"
echo ""
echo "Restarting RustRover..."

pkill -f "RustRover" 2>/dev/null || true
sleep 2

if [ -d "/Applications/RustRover.app" ]; then
    open -a "RustRover" 2>/dev/null || echo "RustRover failed to start"
fi

echo ""
echo "Plugin installed (v$VERSION)"
echo "Check: Settings -> Languages & Frameworks -> Rust -> rust-analyzer -> Server path"
echo ""
