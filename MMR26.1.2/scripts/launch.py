#!/usr/bin/env python3
# Plays Reborn's CurseForge profile as this checkout stands, with the companion mod's local build.
#
#   scripts/launch.py [--dry-run] [--released] [save]
#
# It is skillworks' quicklaunch pack launcher, launch_pack.py, with one change: the companion mod is
# found in mod/build/libs, inside this pack line's folder, rather than in a checkout beside the repo.
# Each run copies the pack's configs and kubejs/ into the profile and puts the newest local build of
# the companion mod in mods/, in place of the pack's jar when the pack carries one, or beside the
# pack's mods while the mod is not yet on CurseForge. The next run takes it out again, so --released
# plays the pack's own jars. The suite is always the pack's released jars.
#
# launch_pack.py is found in skillworks' plugin, or where QUICKLAUNCH_LAUNCHER names it.
import argparse
import importlib.util
import json
import os
import shutil
import subprocess
import sys
import tempfile
import tomllib
from pathlib import Path

PACK = Path(__file__).resolve().parent.parent
MOD_ID = "mechanicalmasteryreborn"
PLUGIN = Path.home() / ".claude/plugins/cache/skillworks/skillworks"


def fail(message):
    sys.exit(f"launch: {message}")


def launcher():
    if named := os.environ.get("QUICKLAUNCH_LAUNCHER"):
        found = Path(named)
    else:
        versions = sorted(PLUGIN.glob("*/skills/quicklaunch/launch_pack.py"),
                          key=lambda p: tuple(int(n) for n in p.parts[-4].split(".") if n.isdigit()))
        if not versions:
            fail(f"no skillworks quicklaunch in {PLUGIN}; install skillworks, or set QUICKLAUNCH_LAUNCHER "
                 "to its launch_pack.py.")
        found = versions[-1]
    spec = importlib.util.spec_from_file_location("launch_pack", found)
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module


def main():
    parser = argparse.ArgumentParser(description="Play Reborn with the companion mod's local build.")
    parser.add_argument("--dry-run", action="store_true", help="say what it would copy, swap and run; change nothing")
    parser.add_argument("--released", action="store_true", help="play the pack's own mod jars, no local build")
    parser.add_argument("save", nargs="?", help="the save to open; the most recent by default")
    args = parser.parse_args()
    lp = launcher()

    pack = tomllib.loads((PACK / "pack.toml").read_text())
    game = lp.instance(pack)
    running = subprocess.run(["pgrep", "-f", "--", f"--gameDir {game}"], capture_output=True, text=True).stdout.split()
    if running:
        fail(f"a game of the profile is running (pid {', '.join(running)}); close it first.")
    saves = game / "saves"
    save = args.save
    if save and not (saves / save / "level.dat").is_file():
        fail(f"no save \"{save}\" in {saves}.")
    if not save:
        latest = sorted(saves.glob("*/level.dat"), key=lambda p: p.stat().st_mtime)
        save = latest[-1].parent.name if latest else None

    # launch_pack looks for a mod's build in <beside>/<mod id>/build/libs: a folder holding only the
    # companion mod, by its id, swaps in that one and leaves the suite as the pack has it.
    with tempfile.TemporaryDirectory(prefix="reborn-launch-") as beside:
        beside = Path(beside)
        (beside / MOD_ID).symlink_to(PACK / "mod")
        built = None if args.released else lp.local_build(beside, MOD_ID)
        if not args.released and not built:
            fail(f"no build of the companion mod in {PACK / 'mod/build/libs'}: build it first, "
                 f"(cd {PACK / 'mod'} && sh ./gradlew build).")
        jar, version = (built[0].resolve(), built[1]) if built else (None, None)
        lp.sync(PACK, pack, game, beside, args.released, args.dry_run)

    if jar:
        aside = game / lp.SET_ASIDE
        record = aside / "installed.json"
        installed = json.loads(record.read_text()) if record.is_file() else []
        # The pack's own jar: in mods/, or set aside by this or (on a dry run) the last run.
        packs = [d[1] for j in [*(game / "mods").glob("*.jar"), *aside.glob("*.jar")]
                 if j.name not in installed and (d := lp.jar_mod(j)) and d[0] == MOD_ID]
        if not packs:
            # The pack carries no companion mod yet, so there was nothing to swap: add the build, and
            # record it where launch_pack's next run takes it out again.
            name = f"{jar.stem}-local.jar"
            lp.note(f"mods: {MOD_ID} not in the pack -> local {version} ({jar.relative_to(PACK)})")
            if not args.dry_run:
                aside.mkdir(exist_ok=True)
                shutil.copyfile(jar, game / "mods" / name)
                record.write_text(json.dumps(installed + [name], indent=2) + "\n")
        elif lp.version_key(version) < lp.version_key(packs[0]):
            lp.note(f"mods: the pack's {MOD_ID} {packs[0]} is newer than the local build {version}: "
                    "playing the pack's; build the mod at its version or later to play yours.")

    cmd, name, _ = lp.command(game, pack, save)
    lp.note(f"profile: {game}")
    lp.note(f"save: {save or 'none, to the menu'}; player: {name}")
    if args.dry_run:
        lp.note("run: " + " ".join(cmd[:2]) + " ... " + " ".join(cmd[-4:]))
        return
    os.chdir(game)
    sys.exit(subprocess.call(cmd))


if __name__ == "__main__":
    main()
