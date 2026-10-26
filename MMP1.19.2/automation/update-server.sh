#!/usr/bin/env bash
# Updates a server that runs from a git checkout of the pack. When the branch has new commits, it
# backs up the world and the mods, then pulls them; it always copies server_files/ into the
# server's folder, removes the client-only mods and copies the overrides, if there are any.
#
#   automation/update-server.sh [branch] [world folder] [backup folder] [back up world] \
#                               [back up mods] [backups to keep] [server files folder]
#
# Defaults: main world backups true true 12 server_files. Stop the Minecraft server first: the
# script asks before it changes anything, and the pull discards local changes to tracked files.
# Runs from anywhere: it works in the automation folder's parent.
set -euo pipefail

target_branch="${1:-main}"
world_folder="${2:-world}"
backup_folder="${3:-backups}"
backup_world="${4:-true}"
backup_mods="${5:-true}"
backups_to_keep="${6:-12}"
server_file_folder="${7:-server_files}"

mod_folder="mods"
overrides_folder="overrides"
stamp="$(date +%m.%d.%Y-%H.%M)"

# The instance root is the automation folder's parent, wherever the script is run from.
determine_location() {
	cd "$(dirname "$0")/.."
}

update_is_available() {
	echo
	echo "Checking if there are any updates available..."
	git fetch
	local behind
	if ! behind="$(git rev-list --left-only --count "origin/$target_branch...$target_branch")"; then
		echo "Can't compare $target_branch with origin/$target_branch; is this a checkout of that branch?" >&2
		exit 1
	fi
	if (( behind > 0 )); then
		echo "********************************************************************"
		echo
		echo "An update is available."
		echo
		echo "Please make sure the Minecraft Server is offline before you continue."
		echo
		echo "********************************************************************"
		read -r -p "Press Enter to continue or CTRL+C to quit"
		return 0
	fi
	return 1
}

# Keeps the newest $backups_to_keep backups in a folder and deletes the rest.
prune_folder() {
	local folder="$1"
	[[ -d "$folder" ]] || return 0
	local kept=0 old
	# Newest first, so everything past the limit is older.
	while IFS= read -r old; do
		[[ "$old" == *.tar.gz ]] || continue
		kept=$((kept + 1))
		if (( kept > backups_to_keep )); then
			echo "Removing old backup $folder/$old"
			rm -f -- "$folder/$old"
		fi
	done < <(ls -1t -- "$folder")
}

prune_backups() {
	echo
	echo "Pruning backups folder contents..."
	echo "The current limit for backups to keep is $backups_to_keep"
	if [[ -d "$backup_folder" ]]; then
		prune_folder "$backup_folder"
		prune_folder "$backup_folder/mods"
	else
		echo "No backups found."
	fi
}

backup_mods_folder() {
	[[ "$backup_mods" == "true" ]] || return 0
	echo
	echo "Backing up the mods folder..."
	mkdir -p "$backup_folder/mods"
	if [[ -d "$mod_folder" ]]; then
		if [[ -n "$(ls -A -- "$mod_folder")" ]]; then
			tar -czf "$backup_folder/mods/$stamp.tar.gz" -- "$mod_folder"
			rm -rf -- "$mod_folder"
		fi
	else
		mkdir -p "$mod_folder"
	fi
}

backup_world_folder() {
	[[ "$backup_world" == "true" ]] || return 0
	echo
	echo "Backing up the world folder..."
	mkdir -p "$backup_folder"
	if [[ -d "$world_folder" ]]; then
		if [[ -n "$(ls -A -- "$world_folder")" ]]; then
			tar -czf "$backup_folder/world-$stamp.tar.gz" -- "$world_folder"
		fi
	else
		mkdir -p "$world_folder"
	fi
}

pull_changes() {
	echo
	echo "Pulling changes from Git..."
	git stash
	git reset --hard
	git pull origin "$target_branch"
}

move_server_files() {
	echo
	echo "Copying server files to base folder..."
	echo "This will overwrite existing files."
	if [[ ! -d "$server_file_folder" ]]; then
		echo "No $server_file_folder folder, nothing to copy."
		return 0
	fi
	for file in "$server_file_folder"/*; do
		echo "Copying '$(basename "$file")' to the base folder."
		cp -rf -- "$file" .
	done
}

remove_client_only_mods() {
	automation/remove-client-mods.sh
}

copy_overrides() {
	echo
	echo "Copying contents of the overrides folder..."
	echo "This will overwrite existing files."
	if [[ ! -d "$overrides_folder" ]]; then
		echo "No $overrides_folder folder, nothing to copy."
		return 0
	fi
	cp -rf -- "$overrides_folder"/. .
}

determine_location

if update_is_available; then
	prune_backups
	backup_mods_folder
	backup_world_folder
	pull_changes
fi

move_server_files
remove_client_only_mods
copy_overrides
