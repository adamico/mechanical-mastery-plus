#!/usr/bin/env python3
# Checks the pack's Shop Offers (#213), the AdminShop recipes in kubejs/data/mechanicalmasteryreborn/recipe/shop/,
# against the rules every Offer keeps:
#
#   - no Sell Offer names an item the first essence's mix holds, the Advanced's, so a team's first money takes a step
#     after decomposing; from the first win on, a team has other money, and what a lower mix holds may be sold (#305);
#   - no item or fluid has two Sell Offers, since a Seller takes whichever it finds first;
#   - whatever the Shop both buys and sells, it pays less than it charges;
#   - nothing only the Shop gives is sold back (water, the Permits), and no fluid the Shop sells has a Sell Offer
#     unless something in the pack makes it, so a bought bucket is never sold back (#319).
#   - the Shop sells no Transmuted Essence, each being made by its chain, and buys no Coin, so a team's gold is either
#     sold or minted, never both (ADR 0017, #323).
#
# A Transmuted Essence's price, above what made it and under twice that, is worked out by hand from its chain (#319).
#
#   scripts/check_offers.py
#
# scripts/test_check_offers.py holds a case each price rule fails on.
#
# The Advanced mix is read from kubejs/server_scripts/decomposing.js. Exits non-zero, naming each Offer that breaks a rule.
import json
import re
import sys
from pathlib import Path

PACK = Path(__file__).resolve().parent.parent
OFFERS = PACK / "kubejs/data/mechanicalmasteryreborn/recipe/shop"
MIXES = PACK / "kubejs/server_scripts/decomposing.js"
# What only the Shop gives: water, whose every other source starts from a bought bucket, and AdminShop's Permits.
SHOP_ONLY = {"minecraft:water", "adminshop:permit"}
# The Transmuted Essences, which the Shop buys but never sells, and the mint's Coins, which it neither buys nor sells.
ESSENCES = {f"mechanicalmasteryreborn:{e}_essence" for e in ("galvanic", "warding", "vital", "aureate")}
COINS = {f"mechanicalmasteryreborn:{m}_coin" for m in ("copper", "iron", "gold")}
# The fluids the Shop sells that something in the pack makes too, and so may be sold back. Every other fluid with a
# Buy Offer is one only the Shop gives, as lava is since dripstone is left out of the pack.
MADE_FLUIDS = set()


def first_mix():
    """Every item the first essence's mix holds, from the ADVANCED_MIX const."""
    mix = re.search(r"ADVANCED_MIX = \[(.*?)\]", MIXES.read_text(), re.S)
    return set(re.findall(r"'(?:\d+x )?([a-z0-9_.-]+:[a-z0-9_/.-]+)'", mix.group(1))) if mix else set()


def offers():
    """Each Offer as (path, selling, fluid, good, price a unit in cents)."""
    for path in sorted(OFFERS.rglob("*.json")):
        offer = json.loads(path.read_text())
        kind = offer["type"].split(":")[1]
        goods = offer.get("result") or offer.get("item") or {"id": "#" + offer["tag"], "count": offer["count"]}
        amount = goods.get("count", goods.get("amount", 1))
        yield path.relative_to(PACK), kind.endswith("selling"), kind.startswith("fluid"), goods["id"], offer["price"] / amount


def check(offers, held):
    """Every rule an Offer of offers breaks, against held, the items the Advanced mix holds."""
    problems = []
    sells, buys, bought_fluids = {}, {}, set()
    for path, selling, fluid, good, unit in offers:
        if good in COINS:
            problems.append(f"{path}: trades {good}, which the Shop never trades")
        if selling:
            if good in held:
                problems.append(f"{path}: sells {good}, which the Advanced mix holds")
            if good in SHOP_ONLY:
                problems.append(f"{path}: sells {good}, which only the Shop gives")
            if good in sells:
                problems.append(f"{path}: sells {good}, which {sells[good][0]} sells too")
            sells[good] = (path, unit)
        else:
            if good in ESSENCES:
                problems.append(f"{path}: charges for {good}, which only its chain makes")
            if fluid:
                bought_fluids.add(good)
            # The cheapest Buy Offer of each good is the one a sale back must stay under.
            if good not in buys or unit < buys[good][1]:
                buys[good] = (path, unit)
    for good, (path, unit) in sells.items():
        if good in buys and unit >= buys[good][1]:
            problems.append(f"{path}: pays {unit:g} cents a unit for {good}, which {buys[good][0]} charges {buys[good][1]:g}")
        if good in bought_fluids and good not in MADE_FLUIDS and good not in SHOP_ONLY:
            problems.append(f"{path}: sells {good}, a fluid only the Shop gives, which {buys[good][0]} sells")
    return problems, len(sells), len(buys)


def main():
    held = first_mix()
    if not held:
        sys.exit(f"check_offers: no Advanced mix found in {MIXES}")
    problems, sells, buys = check(offers(), held)
    for problem in problems:
        print(problem, file=sys.stderr)
    print(f"check_offers: {sells} Sell and {buys} Buy Offers, against {len(held)} items in the Advanced mix: "
          f"{len(problems)} problem(s)")
    sys.exit(1 if problems else 0)


if __name__ == "__main__":
    main()
