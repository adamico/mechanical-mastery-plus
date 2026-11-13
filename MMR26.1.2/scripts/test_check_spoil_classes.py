#!/usr/bin/env python3
# A case each of scripts/check_spoil_classes.py's rules flags: a food in no spoil class, and an item in two (#363), and a
# recipe the Recycler reads that makes rotten flesh (#364).
#
#   python3 scripts/test_check_spoil_classes.py
import sys
import unittest
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
from check_spoil_classes import PACK, assembling_types, check, expand, read_data, rot_makers  # noqa: E402

CLASSES = {"raw": ["minecraft:beef"], "meal": ["farmersdelight:beef_stew"]}
LOADED = {"minecraft", "farmersdelight"}


class CheckSpoilClassesTest(unittest.TestCase):
    def test_every_food_in_one_class_passes(self):
        self.assertEqual(check({"minecraft:beef", "farmersdelight:beef_stew"}, CLASSES, LOADED), [])

    def test_a_food_in_no_class_is_flagged(self):
        self.assertEqual(check({"minecraft:beef", "minecraft:bread"}, CLASSES, LOADED),
                         ["minecraft:bread is in no spoil class"])

    def test_an_item_in_two_classes_is_flagged(self):
        self.assertEqual(check({"minecraft:beef"}, dict(CLASSES, ingredient=["minecraft:beef"]), LOADED),
                         ["minecraft:beef is both raw and ingredient"])

    def test_a_food_of_a_mod_not_loaded_is_left_out(self):
        self.assertEqual(check({"minecraft:beef", "croptopia:tofu"}, CLASSES, LOADED), [])

    def test_nested_and_optional_tag_entries_are_followed(self):
        tags = {"c:foods": [["#c:foods/raw_meat", {"id": "croptopia:tofu", "required": False}]],
                "c:foods/raw_meat": [["minecraft:beef"]]}
        self.assertEqual(expand(tags, "c:foods"), {"minecraft:beef", "croptopia:tofu"})


    def test_a_cooking_crafting_or_assembling_recipe_making_rotten_flesh_is_flagged(self):
        recipes = {
            "pack:smoked": {"type": "minecraft:smoking", "result": {"id": "minecraft:rotten_flesh"}},
            "pack:shaped": {"type": "minecraft:crafting_shaped", "result": {"id": "minecraft:rotten_flesh", "count": 2}},
            "pack:assembled": {"type": "craftworks:assembling", "results": [{"id": "minecraft:bone"}, "minecraft:rotten_flesh"]},
        }
        self.assertEqual(rot_makers(recipes, {"craftworks:assembling"}, LOADED), [
            "pack:assembled (craftworks:assembling) makes rotten flesh",
            "pack:shaped (minecraft:crafting_shaped) makes rotten flesh",
            "pack:smoked (minecraft:smoking) makes rotten flesh",
        ])

    def test_a_recipe_the_recycler_doesnt_read_or_of_a_missing_mod_may_make_rotten_flesh(self):
        recipes = {
            "spoiled:rot": {"type": "spoiled:spoil_recipe", "result": "minecraft:rotten_flesh"},
            "croptopia:rot": {"type": "minecraft:crafting_shapeless", "result": {"id": "minecraft:rotten_flesh"},
                              "neoforge:conditions": [{"type": "neoforge:mod_loaded", "modid": "croptopia"}]},
            "pack:ward_tallow": {"type": "craftworks:assembling", "results": []},
        }
        self.assertEqual(rot_makers(recipes, {"craftworks:assembling"}, LOADED), [])

    def test_the_packs_own_recipes_make_no_rotten_flesh(self):
        recipes = read_data(PACK / "kubejs/data")
        self.assertTrue(recipes)
        self.assertEqual(rot_makers(recipes, assembling_types(PACK / "defaultconfigs/mechanicalmasteryreborn-server.toml"), LOADED), [])


if __name__ == "__main__":
    unittest.main()
