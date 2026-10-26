
# Mechanical Mastery Plus

This is the official repository and issue-tracker for the modpack Mechanical Mastery Plus for Minecraft 1.19.2, a fork of the wonderful and beloved Mechanical Mastery skyblock modpack by Ammonium.

## Table of Contents

- [Setup](#setup)
- [Releasing](#releasing)
- [Links](#links)

# Setup

Follow these steps to get the bleeding edge version of this modpack.

## CurseForge App (Windows / Mac)

1. Download the [CurseForge App](https://www.curseforge.com/download/app) and [Git](https://git-scm.com/downloads) if you haven't already.
2. Fork and clone the repository to the Instances folder of the CurseForge App, the default path is `C:\Users\{UserName}\Documents\Curse\Minecraft\Instances`.
3. Double click the script `Cat-Downloader-Legacy_Setup.bat` or  `Cat-Downloader-Legacy_Setup.sh` in the `automation` folder to sync the instance.
4. Open the CurseForge App and you should see the modpack. If you already had CurseForge App open, restart it.

Note: the github version is probably unstable and tested by a single persion. Don't forget to backup your save if you decide to try this dev version.

# Releasing

The 1.19.2 pack in `MMP1.19.2/` is a [packwiz](https://packwiz.infra.link) manifest: `pack.toml`,
`index.toml` and a `.pw.toml` per mod in `mods/`, every jar coming from CurseForge. `mise.toml` pins
packwiz (`mise install`). Add or update a mod with `packwiz cf add <slug>` or `packwiz cf update <slug>`
from `MMP1.19.2/`; `.packwizignore` keeps the repo's own files out of the pack.

A change a player will notice adds its line under `## Unreleased` in `MMP1.19.2/CHANGELOG.md`; the
mods added, updated and removed are listed by the release itself (`tools/changelog.py mods`).

1. `tools/release.sh <version>` from a clean pack folder: it writes the version's changelog section,
   bumps `pack.toml` and `config/bcc-common.toml`, checks the manifest, exports
   `mechanical-mastery-plus-<version>.zip`, commits, and tags `mmp1.19.2-v<version>` with the zip's
   sha256. It pushes and uploads nothing.
2. `git push origin HEAD mmp1.19.2-v<version>`.
3. `tools/upload.py <version>`: the zip goes to CurseForge, then the server pack (`server_files/`,
   pointed at that zip's download link) as its child file, then both zips to the tag's GitHub release.
   `--dry-run` shows what it would send; `--site curseforge|server|github` retries one, and
   `--site server` or `--site github` on its own needs `--client-file-id <id>`, which the CurseForge
   upload prints. The CurseForge token comes from 1Password through `tools/upload.env`.

## Links

- [Mechanical Mastery Discord](https://discord.gg/PMhDEZge2W)
- [Mechanical Mastery Plus on Curseforge](https://www.curseforge.com/minecraft/modpacks/mechanical-mastery-plus)
