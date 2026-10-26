#!/usr/bin/env bash
# Removes the client-only mods from a server's mods folder: every jar whose name starts with one of
# the names below.
#
#   automation/remove-client-mods.sh
#
# Runs from the instance root or from the automation folder.
set -euo pipefail
cd "$(dirname "$0")/.."

client_mods=(
	fm_audio_extension
	auudio
	Barista
	BridgingMod
	Catalogue
	ClientTweaks
	Configured
	Controlling
	DefaultWorldType
	Ding
	DrippyLoadingScreen
	fancymenu
	FpsReducer
	ImmediatelyFast
	light-overlay
	Lightspeed
	MouseTweaks
	probejs
	ReAuth
	Reforgium
	rubidium-extra
	rubidium
	saturn
	SimpleDiscordRichPresence
	TextruesRubidiumOptions
	TipTheScales
	Yeetus
)

echo
echo "Removing client only mods..."
shopt -s nullglob
for jar in mods/*.jar; do
	name="$(basename "$jar")"
	for mod in "${client_mods[@]}"; do
		if [[ "$name" == "$mod"* ]]; then
			rm -f -- "$jar"
			echo "Removed the client mod $mod ($name)"
			break
		fi
	done
done
