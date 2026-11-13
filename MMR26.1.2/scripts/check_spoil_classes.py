#!/usr/bin/env python3
# Checks the pack's spoil classes (#363), the item tags in kubejs/data/mechanicalmasteryreborn/tags/item/spoil_classes/,
# against the foods the pack's mods give: every item in #c:foods, as the jars' tags make it, is in one class tag. A food
# in none still rots, as an ingredient, on Spoiled's own recipe, so this flags the food a new or updated mod brings for
# the maintainer to class. A food in two tags is flagged too, as SpoilClassesTest does.
#
#   scripts/check_spoil_classes.py [mods folder] [NeoForge universal jar]
#
# The mods folder defaults to Reborn's CurseForge profile, of which only the jars mods/*.pw.toml name are read, since a
# profile keeps jars the pack has dropped; NeoForge's jar, whose tags put vanilla's foods in
# #c:foods, to the CurseForge install's copy of the version pack.toml names; the JUnit tests can't run this, having no
# jars. Exits non-zero, naming each food in no class and each in two.
import json
import re
import sys
import tomllib
import zipfile
from collections import defaultdict
from pathlib import Path

PACK = Path(__file__).resolve().parent.parent
CLASSES = PACK / "kubejs/data/mechanicalmasteryreborn/tags/item/spoil_classes"
PROFILE = Path.home() / "CF/Instances/Mechanical Mastery Reborn/mods"
LIBRARIES = Path.home() / "CF/Install/libraries/net/neoforged/neoforge"
TAG = re.compile(r"data/([^/]+)/tags/items?/(.+)\.json$")


def neoforge():
    """NeoForge's universal jar of the version pack.toml names, in the CurseForge install."""
    version = re.search(r'^neoforge = "([^"]+)"', (PACK / "pack.toml").read_text(), re.M)[1]
    return LIBRARIES / version / f"neoforge-{version}-universal.jar"


def pack_jars(mods):
    """The jars in mods that the pack's mods/*.pw.toml name."""
    named = {tomllib.loads(path.read_text())["filename"] for path in (PACK / "mods").glob("*.pw.toml")}
    return sorted(jar for jar in mods.glob("*.jar") if jar.name in named)


def read_jars(jars):
    """Every item tag the jars define, as tag id to the values of each file that adds to it, and the mod ids they load."""
    tags, loaded = defaultdict(list), {"minecraft"}
    for jar in jars:
        with zipfile.ZipFile(jar) as z:
            if "META-INF/neoforge.mods.toml" in z.namelist():
                try:
                    mods = tomllib.loads(z.read("META-INF/neoforge.mods.toml").decode()).get("mods", [])
                except tomllib.TOMLDecodeError:
                    mods = []
                loaded |= {mod["modId"] for mod in mods}
            for name in z.namelist():
                if match := TAG.match(name):
                    try:
                        tags[f"{match[1]}:{match[2]}"].append(json.loads(z.read(name)).get("values", []))
                    except ValueError:
                        continue
    return tags, loaded


def expand(tags, tag, seen=None):
    """The items tag holds, its nested tags followed, an optional entry's item kept only if its mod is loaded."""
    seen = seen if seen is not None else set()
    if tag in seen:
        return set()
    seen.add(tag)
    items = set()
    for values in tags.get(tag, []):
        for value in values:
            value = value["id"] if isinstance(value, dict) else value
            items |= expand(tags, value[1:], seen) if value.startswith("#") else {value}
    return items


def check(foods, classes, loaded):
    """Each problem: a food of a loaded mod in no class, and an item in two classes."""
    problems, class_of = [], {}
    for name, items in classes.items():
        for item in items:
            if item in class_of:
                problems.append(f"{item} is both {class_of[item]} and {name}")
            class_of[item] = name
    for food in sorted(foods):
        if food.split(":")[0] in loaded and food not in class_of:
            problems.append(f"{food} is in no spoil class")
    return problems


def main():
    mods = Path(sys.argv[1]) if len(sys.argv) > 1 else PROFILE
    jars = pack_jars(mods) + [Path(sys.argv[2]) if len(sys.argv) > 2 else neoforge()]
    # A food of a mod that isn't loaded is an optional tag entry, as another mod's tags name Croptopia's, and is left out.
    tags, loaded = read_jars(jars)
    classes = {path.stem: json.loads(path.read_text())["values"] for path in sorted(CLASSES.glob("*.json"))}
    problems = check(expand(tags, "c:foods"), classes, loaded)
    for problem in problems:
        print(problem)
    return 1 if problems else 0


if __name__ == "__main__":
    sys.exit(main())
