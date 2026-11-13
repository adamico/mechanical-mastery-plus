#!/usr/bin/env python3
# A case each of scripts/check_spoil_classes.py's rules flags: a food in no spoil class, and an item in two (#363).
#
#   python3 scripts/test_check_spoil_classes.py
import sys
import unittest
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
from check_spoil_classes import check, expand  # noqa: E402

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


if __name__ == "__main__":
    unittest.main()
