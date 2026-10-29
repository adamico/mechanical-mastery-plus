# Mechanical Mastery Reborn (26.1.2)

The 26.1.2 pack line, on NeoForge: a skyblock where each team starts on its own island beside C.O.D. and its endless Advanced essence. Its terms are in `GLOSSARY.md`, its decisions in `docs/adr/`, the spec in #163.

## What is here

- The pack: a [packwiz](https://packwiz.infra.link) manifest, `pack.toml`, `index.toml` and a `.pw.toml` per mod in `mods/`, with `config/` and `kubejs/`. Every jar comes from CurseForge. `MODS.md` says what each mod is for.
- The island: Skyblock Builder's config in `config/skyblockbuilder/`, with the team island in `templates/islands/reborn.snbt`. `kubejs/server_scripts/team_of_one.js` gives a player with no team a team, and an island, of their own.
- `mod/`: the companion mod, a Gradle build of its own (see `mod/AGENTS.md`). `.packwizignore` keeps it, and the repo's other files, out of the pack.
- `PLAYTEST.md`: the checks to play through before a release.

## Working on the pack

From this folder, `mise install` installs the pinned packwiz. Add a mod with `packwiz cf add <slug>` and give it a role in `MODS.md`; `packwiz refresh` after changing a config or script. `packwiz cf export` writes the pack's zip.

## Playing it locally

Once: export the zip, import it into the CurseForge app as a profile named "Mechanical Mastery Reborn", and play it once from CurseForge, so it installs NeoForge.

Then build the companion mod (`sh ./gradlew build` in `mod/`) and run `scripts/launch.py`. It plays that profile offline, into its most recent save, as this checkout stands: the configs and `kubejs/` are copied in, and the companion mod's newest local build goes into `mods/`. The suite stays as the pack has it. `--released` plays the pack's own jars, `--dry-run` says what it would do. It is skillworks' quicklaunch pack launcher with the companion mod's place changed, so it needs skillworks installed, or `QUICKLAUNCH_LAUNCHER` naming its `launch_pack.py`; `QUICKLAUNCH_INSTANCE` names a profile elsewhere.

The companion mod is not on CurseForge yet, so the pack does not list it, and a profile imported from the zip has no C.O.D. until `scripts/launch.py` adds the local build.
