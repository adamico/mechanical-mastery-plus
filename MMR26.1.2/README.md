# Mechanical Mastery Reborn (26.1.2)

The 26.1.2 pack line, on NeoForge: a skyblock where each team starts on its own island beside C.O.D. and its endless Advanced essence. Its terms are in `GLOSSARY.md`, its decisions in `docs/adr/`, the spec in #163.

## What is here

- The pack: a [packwiz](https://packwiz.infra.link) manifest, `pack.toml`, `index.toml` and a `.pw.toml` per mod in `mods/`, with `config/` and `kubejs/`. Every jar comes from CurseForge. `MODS.md` says what each mod is for.
- The island: Skyblock Builder's config in `config/skyblockbuilder/`, with the team island in `templates/islands/reborn.snbt`: C.O.D., the lab and a tree, and no Recycler, which the team builds from the island's wood and stone. `kubejs/server_scripts/team_of_one.js` gives a player with no team a team, and an island, of their own.
- The recipes: `kubejs/server_scripts/decomposing.js` gives each essence its mix and the Recycler's parts their recipes. What any other item recycles into the companion mod derives from its recipes, and `defaultconfigs/mechanicalmasteryreborn-server.toml` has it read Craftworks' Assembler recipes too (#184).
- `mod/`: the companion mod, a Gradle build of its own (see `mod/AGENTS.md`). `.packwizignore` keeps it, and the repo's other files, out of the pack.

## Working on the pack

From this folder, `mise install` installs the pinned packwiz. Add a mod with `packwiz cf add <slug>` and give it a role in `MODS.md`; `packwiz refresh` on `main` after merging a change to a config or script; a branch leaves `index.toml` and `pack.toml` alone, and `scripts/launch.py` refreshes them for its run (`AGENTS.md`). `packwiz cf export` writes the pack's zip.

## Playing it locally

Once: export the zip, import it into the CurseForge app as a profile named "Mechanical Mastery Reborn", and play it once from CurseForge, so it installs NeoForge.

Then build the companion mod (`sh ./gradlew build` in `mod/`) and run `scripts/launch.py`. It plays that profile offline, into its most recent save, as this checkout stands: the configs and `kubejs/` are copied in, and the companion mod's newest local build goes into `mods/`. The suite stays as the pack has it. `--released` plays the pack's own jars, `--dry-run` says what it would do. It is skillworks' quicklaunch pack launcher with only the companion mod built locally, so it needs skillworks 0.11.7 or later installed, or `QUICKLAUNCH_LAUNCHER` naming its `launch_pack.py`; `QUICKLAUNCH_INSTANCE` names a profile elsewhere.

The companion mod is not on CurseForge yet, so the pack does not list it, and a profile imported from the zip has no C.O.D. until `scripts/launch.py` adds the local build.

`scripts/check_offers.py` checks the Shop's Offers against the rules every Offer keeps (#213).

## Releasing

Reborn releases on its own (ADR 0004), with `tools/release.py`, Mechanical Mastery Plus's release tool pointed at Reborn's identity in `tools/release.toml`: its slug, its tag prefix `mmr26.1.2-v`, its CurseForge project (once #291 makes it; until then `publish` refuses every site, since the server pack names the client zip's CurseForge download, and `--dry-run` still shows what each would be sent) and the GitHub repo. Until 0.1.0 the pack is an alpha: its files' display names say "alpha", but every file is uploaded as a Release file, so it reaches the CurseForge app.

Servers install a release from its server pack (ADR 0001 at the repo's root), which ServerStarter runs. A mod servers mustn't install has `side = "client"` in its `.pw.toml`, the only record of which mods are client-only: each server pack tells ServerStarter to ignore every CurseForge project of the manifest and to download the other mods straight from CurseForge's CDN, so `server_files/server-setup-config.yaml` keeps `ignoreProject` empty and lists in `additionalFiles` only files the pack doesn't have.

A change a player will notice adds its line under `## Unreleased` in `CHANGELOG.md`; the mods added, updated and removed are listed by the release itself (`tools/release.py mods`).

1. `tools/release.py cut <version>` on `main` from a clean pack folder: once the tool's tests pass, it writes the version's changelog section, bumps `pack.toml`, exports the client zip into `releases/<version>/`, commits, and tags `mmr26.1.2-v<version>` with the zip's sha256. It pushes and publishes nothing, and a failure before the commit puts every file back.
2. `git push origin HEAD mmr26.1.2-v<version>`.
3. `tools/release.py publish <version>`: the client zip goes to CurseForge, then the server pack (`server_files/` as tagged, pointed at that zip's download link and the tag's NeoForge, and leaving out the tag's client-only mods) as its child file, then both zips to the tag's GitHub release. The release record beside the zip keeps what each site has, so after a failure, fix it and publish again. `--dry-run` shows what it would send, `--site` sends to one site, and `--client-file-id` stands in for a lost record. The CurseForge token comes from 1Password through `tools/publish.env`.

`mise run test` runs the release tool's tests.
