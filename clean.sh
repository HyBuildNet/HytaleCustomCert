#!/bin/bash
#
# Clean script - removes build artifacts and unpacked JAR
#

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

echo "Cleaning build artifacts..."

rm -rf "$SCRIPT_DIR/lib/HytaleServer"
rm -rf "$SCRIPT_DIR/out"
rm -rf "$SCRIPT_DIR/build"
rm -rf "$SCRIPT_DIR/resources/patched"

echo "Done."
