# Mechanical Mastery Reborn, the companion mod

Reborn's own blocks and machines (MMR26.1.2 ADR 0003): C.O.D., the essences, and later the Recycler, the lab and the handicaps. Started from the libworks template (5thlayer/libworks) and built on its convention plugin, but kept in this repo's Reborn folder rather than a repo of its own. The pack consumes it as a released jar, like the suite, and `../.packwizignore` keeps this folder out of the pack. Its terms are the line's, in `../GLOSSARY.md` and the root `GLOSSARY.md`; its issues are Reborn's, labelled `26.1.2`.

Its `src/main` depends on no suite mod at compile time, only on the libworks runtime it nests; the plugin's `checkPlaysAlone` fails a build that ships a suite mod's id.

## Where the template's pieces went

The template assumes the Module is the whole repo. Here:

- Every Gradle command runs from this folder: `sh ./gradlew ...`.
- CI is `.github/workflows/mmr-mod-ci.yml` at the repo root, a copy of libworks' `module-ci.yml` run from this folder, which runs only on changes under `MMR26.1.2/mod/`, to itself, and to what `scripts/check_offers.py` checks the Shop's Offers against (`kubejs/data/`, `decomposing.js` and the script), which it runs before the build. It builds the libworks plugin from the tag its `LIBWORKS_PLUGIN` names, which must be the version `build.gradle` applies: move both together, and take what changed in `module-ci.yml` at the new tag.
- The skills, the issue tracker and the triage labels are the repo's (`docs/agents/` at the root). There is no skills submodule here.
- The versioning rule is libworks' ADR 0001: below 1.0 an addition bumps the patch and only a breaking change bumps the minor. ADR numbers are the repo's, so this folder keeps no copy.

## Ids are forever

A registry id is saved in players' worlds, so once released it never changes. The essences' ids are fixed by #163 and checked by `EssenceIdsTest`; C.O.D. is `mechanicalmasteryreborn:cod`. A new id takes its name from the line's glossary.

## Testing

- `sh ./gradlew build` runs the JUnit tests, on a plain JVM with no Minecraft.
- `sh ./gradlew runGameTestServer` runs the game tests headless and names each one it ran; it fails if it ran none.
- The `skillworks:gametest-loop` skill runs a subset, `--tests 'mechanicalmasteryreborn:<glob>' 1`, so a ticket's game tests share a name prefix (`chain_`, `cod_`, `core_tier_`, `essence_`, `fe_drive_`, `floor_`, `generator_`, `hand_`, `hermetic_lab_`, `inflation_`, `life_`, `lab_`, `last_chapter_`, `last_stand_`, `money_`, `pace_`, `port_`, `quintessence_`, `recipe_`, `refund_`, `recycler_`, `recycling_`, `stop_`, `yield_`, and, in the suite run below, `suite_` before the ticket's own, as `suite_warding_`).
- `sh ./gradlew runSuiteGameTestServer` runs the suite game tests, the ones prefixed `suite_`, with the pack's own Craftworks, Wireworks, Fieldworks and Groundworks jars and the pack's assembling and harvesting recipes and its Fieldworks tags, from `../kubejs/data/`, as a datapack, so a Chain the pack writes on the suite's machines runs on a real Assembler and Harvester (#252). `runGameTestServer` loads none of them and registers none of those tests. The suite mods' versions in `build.gradle` follow `../mods/*.pw.toml`, and its libworks runtime is the newest they nest, which CI builds by its tag.
- The game tests play at a full yield, so amounts are exact; a test that needs another sets it for its body with `TestYield.at`.
- Game-test code lives in the `gametest` source set, which never ships (libworks ADRs 0004 and 0011). A new test class is registered by a line in `MechanicalMasteryRebornGameTests.registerTests`, and its tests stand on the `gametest/platform` structure.
- Neither run loads a client, so a change to a screen, a model or a texture isn't done when the tests pass: list it for the user to check in the dev client (`skillworks:quicklaunch`).
- Vanilla's code with NeoForge's patches is in `build/moddev/artifacts/minecraft-patched-*-sources.jar`; NeoForge's own in `~/.gradle/caches/modules-2/files-2.1/net.neoforged/neoforge/<neoforge_version>/*/neoforge-*-sources.jar`. Check a claim about either there before stating it.

## Releases

A change the pack or a player will notice adds its line under `## Unreleased` in `CHANGELOG.md` as it lands. Before bumping `mod_version`, tagging or uploading, read `docs/agents/releases.md`: releases go through `sh ./gradlew release -PreleaseVersion=<version>`, and a published version never changes.

The mod's tags are `mechanicalmasteryreborn-v<version>` (`libworks { tagPrefix }` in `build.gradle`), which never match the pack's own tags: `mmp1.19.2-v*` and Reborn's `mmr-v*`. The release script needs the whole repo clean, not just this folder, and pushes nothing.
