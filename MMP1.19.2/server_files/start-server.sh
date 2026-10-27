#!/bin/bash
# Starts the server through ServerStarter, which installs Forge and the pack on its first run, as
# server-setup-config.yaml says. Runs from anywhere: it works in its own folder.
set -euo pipefail
cd "$(dirname "$0")"

SERVERSTARTER_VERSION="2.4.0"
SERVERSTARTER_JAR="serverstarter-$SERVERSTARTER_VERSION.jar"
SERVERSTARTER_URL="https://github.com/EnigmaticaModpacks/ServerStarter/releases/download/v$SERVERSTARTER_VERSION/$SERVERSTARTER_JAR"
JAVA_VERSION=17

fail() { echo "start-server: $*" >&2; exit 1; }

# Minecraft 1.19.2 needs Java 17.
command -v java > /dev/null || fail "Java $JAVA_VERSION is needed and java isn't on the PATH."
java_major="$(java -version 2>&1 | sed -n 's/.* version "\([0-9]*\).*/\1/p' | head -n 1)"
if [[ -z "$java_major" || "$java_major" -lt "$JAVA_VERSION" ]]; then
    fail "Java $JAVA_VERSION is needed; java -version says ${java_major:-something else}."
elif [[ "$java_major" -ne "$JAVA_VERSION" ]]; then
    echo "start-server: Java $java_major found; the pack is made for Java $JAVA_VERSION, so mods may fail." >&2
fi

# Downloads into a temporary file, so a failed download never leaves a broken jar to run next time.
if [[ ! -s "$SERVERSTARTER_JAR" ]]; then
    echo "Downloading $SERVERSTARTER_URL"
    if command -v curl > /dev/null; then
        curl -fL -o "$SERVERSTARTER_JAR.part" "$SERVERSTARTER_URL" || fail "couldn't download $SERVERSTARTER_JAR."
    elif command -v wget > /dev/null; then
        wget -O "$SERVERSTARTER_JAR.part" "$SERVERSTARTER_URL" || fail "couldn't download $SERVERSTARTER_JAR."
    else
        fail "neither curl nor wget is installed, so $SERVERSTARTER_JAR can't be downloaded."
    fi
    mv "$SERVERSTARTER_JAR.part" "$SERVERSTARTER_JAR"
else
    echo "Using the existing $SERVERSTARTER_JAR"
fi

# The world runs on a RAM disk when the config says ramDisk: true, from its copy in <world>_backup.
if grep -Eq '^\s*ramDisk:\s*true\s*$' server-setup-config.yaml; then
    [[ -f server.properties ]] || fail "ramDisk needs the server to have run once fully first."
    save_dir="$(sed -n 's/^level-name=//p' server.properties | tr -d '\r')"
    save_dir="${save_dir:-world}"
    mv -- "$save_dir" "${save_dir}_backup"
    mkdir -- "$save_dir"
    sudo mount -t tmpfs -o size=2G tmpfs "$save_dir"
    # However the server stops, the RAM disk is unmounted and the world put back.
    trap 'sudo umount "$save_dir"; rm -rf -- "$save_dir"; mv -- "${save_dir}_backup" "$save_dir"' EXIT
fi

java -jar "$SERVERSTARTER_JAR"
