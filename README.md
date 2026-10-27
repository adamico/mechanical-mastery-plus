
# Mechanical Mastery Plus

This is the official repository and issue-tracker for the modpack Mechanical Mastery Plus for Minecraft 1.19.2, a fork of the wonderful and beloved Mechanical Mastery skyblock modpack by Ammonium.

## Table of Contents

- [Setup](#setup)
- [Releasing](#releasing)
- [Links](#links)

# Setup

Follow these steps to get the bleeding edge version of this modpack.

## CurseForge App (Windows / Mac)

1. Install the [CurseForge App](https://www.curseforge.com/download/app), [Git](https://git-scm.com/downloads)
   and [mise](https://mise.jdx.dev) if you haven't already.
2. Clone the repository, then from `MMP1.19.2/` run `mise install` (it installs the pinned packwiz) and
   `packwiz cf export`, which writes the pack's zip from the current manifest.
3. In the CurseForge App, choose Create Custom Profile, then Import, and pick that zip.
4. To update, `git pull`, export again and import the new zip.

Note: the github version is probably unstable and tested by a single persion. Don't forget to backup your save if you decide to try this dev version.

# Releasing

The 1.19.2 pack in `MMP1.19.2/` is a [packwiz](https://packwiz.infra.link) manifest: `pack.toml`,
`index.toml` and a `.pw.toml` per mod in `mods/`, every jar coming from CurseForge. `mise.toml` pins
packwiz (`mise install`). Add or update a mod with `packwiz cf add <slug>` or `packwiz cf update <slug>`
from `MMP1.19.2/`; `.packwizignore` keeps the repo's own files out of the pack.

A change a player will notice adds its line under `## Unreleased` in `MMP1.19.2/CHANGELOG.md`; the
mods added, updated and removed are listed by the release itself (`tools/release.py mods`).

1. `tools/release.py cut <version>` from a clean pack folder: once the tool's tests pass, it writes
   the version's changelog section, bumps `pack.toml` and `config/bcc-common.toml`, exports the client
   zip into `releases/<version>/`, commits, and tags `mmp1.19.2-v<version>` with the zip's sha256. It
   pushes and publishes nothing, and a failure before the commit puts every file back.
2. `git push origin HEAD mmp1.19.2-v<version>`.
3. `tools/release.py publish <version>`: the client zip goes to CurseForge, then the server pack
   (`server_files/` as tagged, pointed at that zip's download link) as its child file, then both zips
   to the tag's GitHub release. The release record beside the zip keeps what each site has, so after
   a failure, fix it and publish again. `--dry-run` shows what it would send, `--site` sends to one
   site, and `--client-file-id <id>` stands in for a lost record. The CurseForge token comes from
   1Password through `tools/publish.env`.

Servers install only from the server pack (`docs/adr/0001`). `mise run test` runs the release
tool's tests; the pack identity it releases under is in `tools/release.toml`.

## Links

- [Mechanical Mastery Discord](https://discord.gg/PMhDEZge2W)
- [Mechanical Mastery Plus on Curseforge](https://www.curseforge.com/minecraft/modpacks/mechanical-mastery-plus)
