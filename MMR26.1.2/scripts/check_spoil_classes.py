#!/usr/bin/env python3
# Checks the pack's spoil classes (#363), the item tags in kubejs/data/mechanicalmasteryreborn/tags/item/spoil_classes/,
# against the foods the pack's mods give: every item in #c:foods, as the jars' tags make it, is in one class tag. A food
# in none still rots, as an ingredient, on Spoiled's own recipe, so this flags the food a new or updated mod brings for
# the maintainer to class. A food in two tags is flagged too, as SpoilClassesTest does.
#
# And that no recipe the Recycler reads, cooking, crafting or the assembling types the server config names, makes rotten
# flesh, in the jars or in kubejs/data/, whose recipe replaces a jar's of the same id (#364): rot is only ever what
# spoiling makes, and the Recycler denies it. A recipe whose mod_loaded condition names a mod the pack lacks is left out.
# Crafting is told by a recipe's own type, minecraft:crafting_*, so a mod's crafting serializer of its own goes unseen,
# as do the recipes KubeJS's server scripts add, which only a running game knows.
#
#   scripts/check_spoil_classes.py [mods folder] [NeoForge universal jar]
#
# The mods folder defaults to Reborn's CurseForge profile, of which only the jars mods/*.pw.toml name are read, since a
# profile keeps jars the pack has dropped; NeoForge's jar, whose tags put vanilla's foods in
# #c:foods, to the CurseForge install's copy of the version pack.toml names; the JUnit tests can't run this, having no
# jars. Exits non-zero, naming each food in no class and each in two, and each recipe that makes rotten flesh.
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
DATA = PACK / "kubejs/data"
SERVER_CONFIG = PACK / "defaultconfigs/mechanicalmasteryreborn-server.toml"
TAG = re.compile(r"data/([^/]+)/tags/items?/(.+)\.json$")
RECIPE = re.compile(r"data/([^/]+)/recipes?/(.+)\.json$")
ROT = "minecraft:rotten_flesh"
COOKING = {"minecraft:smelting", "minecraft:blasting", "minecraft:smoking", "minecraft:campfire_cooking"}


def neoforge():
    """NeoForge's universal jar of the version pack.toml names, in the CurseForge install."""
    version = re.search(r'^neoforge = "([^"]+)"', (PACK / "pack.toml").read_text(), re.M)[1]
    return LIBRARIES / version / f"neoforge-{version}-universal.jar"


def pack_jars(mods):
    """The jars in mods that the pack's mods/*.pw.toml name."""
    named = {tomllib.loads(path.read_text())["filename"] for path in (PACK / "mods").glob("*.pw.toml")}
    return sorted(jar for jar in mods.glob("*.jar") if jar.name in named)


def read_jars(jars):
    """
    Every item tag the jars define, as tag id to the values of each file that adds to it, the mod ids they load, and
    their recipes, as id to recipe.
    """
    tags, loaded, recipes = defaultdict(list), {"minecraft"}, {}
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
                elif match := RECIPE.match(name):
                    try:
                        recipes[f"{match[1]}:{match[2]}"] = json.loads(z.read(name))
                    except ValueError:
                        continue
    return tags, loaded, recipes


def read_data(data):
    """The recipes a datapack folder holds, as id to recipe."""
    recipes = {}
    for path in sorted(data.glob("*/recipe/**/*.json")):
        namespace = path.relative_to(data).parts[0]
        recipes[f"{namespace}:{path.relative_to(data / namespace / 'recipe').with_suffix('').as_posix()}"] = json.loads(path.read_text())
    return recipes


def assembling_types(config):
    """The recipe types the server config has the Recycler read as assembling."""
    return set(tomllib.loads(config.read_text()).get("recycler", {}).get("assembling_types", []))


def results(recipe):
    """The item ids a recipe makes: its result, or each of its results."""
    made = [recipe["result"]] if "result" in recipe else recipe.get("results", [])
    ids = []
    for result in made if isinstance(made, list) else []:
        result = result.get("id", result.get("item")) if isinstance(result, dict) else result
        if isinstance(result, str):
            ids.append(result)
    return ids


def present(recipe, loaded):
    """Whether no mod_loaded condition of the recipe names a mod that isn't loaded."""
    conditions = recipe.get("neoforge:conditions", [])
    return all(condition.get("modid") in loaded for condition in conditions
               if isinstance(condition, dict) and condition.get("type") == "neoforge:mod_loaded")


def rot_makers(recipes, assembling, loaded):
    """Each problem: a recipe the Recycler reads, cooking, crafting or assembling, that makes rotten flesh."""
    problems = []
    for recipe_id, recipe in sorted(recipes.items()):
        kind = recipe.get("type", "") if isinstance(recipe, dict) else ""
        read = kind in COOKING or kind.startswith("minecraft:crafting_") or kind in assembling
        if read and present(recipe, loaded) and ROT in results(recipe):
            problems.append(f"{recipe_id} ({kind}) makes rotten flesh")
    return problems


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
    tags, loaded, recipes = read_jars(jars)
    classes = {path.stem: json.loads(path.read_text())["values"] for path in sorted(CLASSES.glob("*.json"))}
    problems = check(expand(tags, "c:foods"), classes, loaded)
    problems += rot_makers(recipes | read_data(DATA), assembling_types(SERVER_CONFIG), loaded)
    for problem in problems:
        print(problem)
    return 1 if problems else 0


if __name__ == "__main__":
    sys.exit(main())
