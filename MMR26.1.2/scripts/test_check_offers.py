#!/usr/bin/env python3
# A case each of scripts/check_offers.py's price rules fails on, and the pack's own Offers, which pass (#319).
#
#   python3 scripts/test_check_offers.py
import sys
import unittest
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
import check_offers  # noqa: E402

def problems(*offers):
    return check_offers.check(offers, held=set())[0]


class CheckOffersTest(unittest.TestCase):
    def test_the_packs_offers_pass(self):
        self.assertEqual(check_offers.check(check_offers.offers(), check_offers.first_mix())[0], [])

    def test_a_sale_at_the_price_it_is_bought_for_fails(self):
        found = problems(("buy/raw/raw_iron.json", False, False, "minecraft:raw_iron", 400),
                         ("sell/raw/raw_iron.json", True, False, "minecraft:raw_iron", 400))
        self.assertEqual(len(found), 1)
        self.assertIn("pays 400 cents a unit for minecraft:raw_iron", found[0])

    def test_a_sale_above_the_price_it_is_bought_for_fails(self):
        found = problems(("buy/raw/raw_iron.json", False, False, "minecraft:raw_iron", 400),
                         ("sell/raw/raw_iron.json", True, False, "minecraft:raw_iron", 500))
        self.assertEqual(len(found), 1)

    def test_a_sale_below_the_price_it_is_bought_for_passes(self):
        self.assertEqual(problems(("buy/raw/raw_iron.json", False, False, "minecraft:raw_iron", 400),
                                  ("sell/raw/raw_iron.json", True, False, "minecraft:raw_iron", 300)), [])

    def test_the_old_lava_pair_fails(self):
        # Lava at $1000 a bucket and bought back at $500, as before #319: the sale is under the price, but only the
        # Shop gives lava.
        found = problems(("buy/fluids/lava.json", False, True, "minecraft:lava", 100),
                         ("sell/fluids/lava.json", True, True, "minecraft:lava", 50))
        self.assertEqual(len(found), 1)
        self.assertIn("a fluid only the Shop gives", found[0])

    def test_a_fluid_the_pack_makes_too_may_be_sold_back(self):
        made, check_offers.MADE_FLUIDS = check_offers.MADE_FLUIDS, {"fieldworks:crude_oil"}
        try:
            self.assertEqual(problems(("buy/fluids/crude_oil.json", False, True, "fieldworks:crude_oil", 50),
                                      ("sell/fluids/crude_oil.json", True, True, "fieldworks:crude_oil", 20)), [])
        finally:
            check_offers.MADE_FLUIDS = made

    def test_a_sold_fluid_the_shop_does_not_sell_passes(self):
        self.assertEqual(problems(("sell/fluids/sal_ammoniac.json", True, True, "theurgy:sal_ammoniac", 20)), [])

    def test_a_bought_item_sold_back_cheaper_is_not_a_fluid_rule(self):
        # The fluid rule is for fluids alone: an item the Shop sells may be sold back below its price.
        self.assertEqual(problems(("buy/blocks/sand.json", False, False, "minecraft:sand", 125),
                                  ("sell/blocks/sand.json", True, False, "minecraft:sand", 100)), [])


if __name__ == "__main__":
    unittest.main()
