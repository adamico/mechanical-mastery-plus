#!/usr/bin/env python3
# Checks the pack's Shop Offers (#213), the AdminShop recipes in kubejs/data/mechanicalmasteryreborn/recipe/shop/,
# against the rules every Offer keeps:
#
#   - no Sell Offer names an item an essence's mix holds, so what is sold takes a step after decomposing;
#   - no item or fluid has two Sell Offers, since a Seller takes whichever it finds first;
#   - whatever the Shop both buys and sells, it pays less than it charges;
#   - nothing only the Shop gives is sold back (water, the Permits);
#   - the Aureate Essence costs at least what the Shop pays for any other Transmuted Essence, so a won chain's extras,
#     sold at the money handicap's share while the challenge is played, help buy its quota but never swap for it one
#     for one (#232).
#
#   scripts/check_offers.py
#
# The mixes are read from kubejs/server_scripts/decomposing.js. Exits non-zero, naming each Offer that breaks a rule.
import json
import re
import sys
from pathlib import Path

PACK = Path(__file__).resolve().parent.parent
OFFERS = PACK / "kubejs/data/mechanicalmasteryreborn/recipe/shop"
MIXES = PACK / "kubejs/server_scripts/decomposing.js"
# What only the Shop gives: water, whose every other source starts from a bought bucket, and AdminShop's Permits.
SHOP_ONLY = {"minecraft:water", "adminshop:permit"}
# The Transmuted Essence the Shop only sells. The only essences it buys are the other Transmuted Essences.
AUREATE = "mechanicalmasteryreborn:aureate_essence"
ESSENCE = re.compile(r"mechanicalmasteryreborn:\w+_essence")


def mixes():
    """Every item any essence's mix holds, from the first argument of each decomposing recipe."""
    text = MIXES.read_text()
    held = set()
    for mix in re.findall(r"decomposing\(\[(.*?)\]", text, re.S):
        held.update(re.findall(r"'(?:\d+x )?([a-z0-9_.-]+:[a-z0-9_/.-]+)'", mix))
    return held


def offers():
    for path in sorted(OFFERS.rglob("*.json")):
        offer = json.loads(path.read_text())
        kind = offer["type"].split(":")[1]
        goods = offer.get("result") or offer.get("item") or {"id": "#" + offer["tag"], "count": offer["count"]}
        amount = goods.get("count", goods.get("amount", 1))
        yield path.relative_to(PACK), kind.endswith("selling"), goods["id"], offer["price"] / amount


def main():
    held = mixes()
    if not held:
        sys.exit(f"check_offers: no mix found in {MIXES}")
    problems = []
    sells, buys = {}, {}
    for path, selling, good, unit in offers():
        if selling:
            if good in held:
                problems.append(f"{path}: sells {good}, which a mix holds")
            if good in SHOP_ONLY:
                problems.append(f"{path}: sells {good}, which only the Shop gives")
            if good in sells:
                problems.append(f"{path}: sells {good}, which {sells[good][0]} sells too")
            sells[good] = (path, unit)
        else:
            # The cheapest Buy Offer of each good is the one a sale back must stay under.
            if good not in buys or unit < buys[good][1]:
                buys[good] = (path, unit)
    for good, (path, unit) in sells.items():
        if good in buys and unit >= buys[good][1]:
            problems.append(f"{path}: pays {unit:g} cents a unit for {good}, which {buys[good][0]} charges {buys[good][1]:g}")
    if AUREATE not in buys:
        problems.append(f"no Buy Offer sells {AUREATE}")
    else:
        path, unit = buys[AUREATE]
        for good in sorted(good for good in sells if ESSENCE.fullmatch(good)):
            if sells[good][1] > unit:
                problems.append(f"{path}: charges {unit:g} cents for {AUREATE}, less than {sells[good][0]} pays for {good}")
    for problem in problems:
        print(problem, file=sys.stderr)
    print(f"check_offers: {len(sells)} Sell and {len(buys)} Buy Offers, against {len(held)} items in the mixes: "
          f"{len(problems)} problem(s)")
    sys.exit(1 if problems else 0)


if __name__ == "__main__":
    main()
