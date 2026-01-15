#!/bin/bash
#
# Build script for CustomCert Early Plugin
#
# Uses Javassist for runtime bytecode manipulation.
# No Hytale binaries are distributed - only transformation code.
#

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
LIB_DIR="$SCRIPT_DIR/lib/HytaleServer"
HYTALE_JAR="$SCRIPT_DIR/lib/HytaleServer.jar"
JAVASSIST_JAR="$SCRIPT_DIR/lib/javassist.jar"
BUILD_DIR="$SCRIPT_DIR/build/latest"
OUTPUT_JAR="$BUILD_DIR/CustomCert.jar"

echo "========================================"
echo "  Building CustomCert Early Plugin"
echo "========================================"
echo ""

# Check dependencies
if [ ! -f "$HYTALE_JAR" ]; then
    echo "ERROR: lib/HytaleServer.jar not found!"
    exit 1
fi

if [ ! -f "$JAVASSIST_JAR" ]; then
    echo "ERROR: lib/javassist.jar not found!"
    echo "Download from: https://repo1.maven.org/maven2/org/javassist/javassist/3.30.2-GA/javassist-3.30.2-GA.jar"
    exit 1
fi

# Unpack HytaleServer.jar if needed (for compilation)
if [ ! -d "$LIB_DIR" ]; then
    echo "[0/3] Unpacking HytaleServer.jar..."
    mkdir -p "$LIB_DIR"
    unzip -q "$HYTALE_JAR" -d "$LIB_DIR"
    echo "      Done."
    echo ""
fi

# Archive previous latest build
if [ -d "$BUILD_DIR" ]; then
    TIMESTAMP=$(date +"%Y-%m-%d_%H-%M-%S")
    mv "$BUILD_DIR" "$SCRIPT_DIR/build/$TIMESTAMP"
    echo "Archived previous build to build/$TIMESTAMP"
    echo ""
fi

# Clean and prepare
rm -rf "$SCRIPT_DIR/out"
mkdir -p "$SCRIPT_DIR/out"
mkdir -p "$BUILD_DIR"

# Step 1: Unpack Javassist into out/ (for fat-jar)
echo "[1/3] Unpacking Javassist..."
unzip -q "$JAVASSIST_JAR" -d "$SCRIPT_DIR/out"
# Remove Javassist's META-INF (we'll use our own)
rm -rf "$SCRIPT_DIR/out/META-INF"

# Step 2: Compile our code
echo "[2/3] Compiling CustomCert..."
javac -cp "$LIB_DIR:$JAVASSIST_JAR" \
    -d "$SCRIPT_DIR/out" \
    "$SCRIPT_DIR/src/net/hybuild/customcert/CertificatePatchTransformer.java"

if [ $? -ne 0 ]; then
    echo "ERROR: Compilation failed"
    exit 1
fi

# Step 3: Create JAR
echo "[3/3] Creating JAR..."

# Copy META-INF with service registration
cp -r "$SCRIPT_DIR/resources/META-INF" "$SCRIPT_DIR/out/"

# Create JAR
cd "$SCRIPT_DIR/out"
jar cf "$OUTPUT_JAR" .

if [ $? -ne 0 ]; then
    echo "ERROR: Failed to create JAR"
    exit 1
fi

# Clean temp
rm -rf "$SCRIPT_DIR/out"

echo ""
echo "========================================"
echo "  Build successful!"
echo "========================================"
echo ""
echo "Output: $OUTPUT_JAR"
echo ""
echo "Deploy to: earlyplugins/CustomCert.jar"
echo "Start with: java -jar HytaleServer.jar --allow-early-plugins"
