#!/usr/bin/env bash
# Fails when the manifest is not what packwiz builds from the files beside it.
set -euo pipefail
cd "$(dirname "$0")/.."
packwiz() { if command -v packwiz > /dev/null; then command packwiz "$@"; else mise exec -- packwiz "$@"; fi; }
packwiz refresh > /dev/null
git diff --exit-code -- pack.toml index.toml mods/ resourcepacks/
