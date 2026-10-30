# Gold outside the challenge mods (#209)

Every way a team can get gold in Mechanical Mastery Reborn without a challenge's mods, and how each fits the line. The maintainer has ruled that gold must not come from a challenge's mod (Theurgy's transmutation of copper or iron into gold doesn't count as a source here) and that the Galvanic recipe keeps its gold. Gold sulfur seeds are left aside.

What the energy challenge needs, from [theurgy-chain.md](theurgy-chain.md) and [advanced-mix.md](advanced-mix.md):

- **Route A**: 2 gold ingots per Galvanic Essence, or about 0.7 raw gold, since Theurgy liquefies a raw ore for three times an ingot's sulfur.
- **Routes B and C**: one raw gold, once, to seed the gold loop.
- **Building the apparatus**: gold ingots among other things, once (theurgy-chain.md, "Building it").

Raw gold is the better form, both for Theurgy and for [ADR 0006](../adr/0006-a-challenges-mods-take-only-raw-and-refined-items.md), which lets a challenge's mods take raw and refined items. Every source below gives raw gold, ingots or nuggets, so all of them are in the right bands. Whether a source fits is decided by the glossary instead: **decomposing is how resources enter this line**, and a source that brings gold in some other way bends that rule.

## Sources

Short prefixes:

- `FW/` = `fieldworks-26.1.2-0.6.0-local.jar!/data/fieldworks/`. Fieldworks' code is cited from the [5thlayer/fieldworks](https://github.com/5thlayer/fieldworks) source at the `fieldworks-v0.6.0` tag (`5bb31e13`), which is the jar's version.
- `VW/` = `voidworks-26.1.2-0.2.0-local.jar!/data/voidworks/`. Voidworks' code and spec are cited from [5thlayer/voidworks](https://github.com/5thlayer/voidworks) at `bc26ea55` ("chore: release 0.2.0").
- `V/` = `minecraft_26.1.2_client.jar!/data/minecraft/`. `NF/` = `neoforge-26.1.2.109-universal.jar!/data/neoforge/`.
- `SBB` = `SkyblockBuilder-26.1.4.jar`. `CG/` = `cobblegengalore-26.1.2-0.2.9.jar!/data/cobblegengalore/`. `NV/` = `neovitae-26.1.2-1.1.49.jar!/data/neovitae/`.

| Source | Gold, roughly | Needs | Under the energy handicap | Fits the line |
|---|---|---|---|---|
| Advanced's mix: Rune of Sacrifice II | 0.03 gold ingots per essence per rune at tier 1 | Nothing new | Yes | Yes, gold enters by decomposing; brings netherite scrap |
| Advanced's mix: Modular Routers' trace gold | 1/1150 ingot per essence per module, as nuggets | Packing nuggets to ingots | Yes | Its gold relies on a compress recipe |
| Fieldworks: crafted Gold Ore Patch (#170) | 3200 raw gold per block; 1 per second on a Burner Harvester | #170's recipe (not written); an iron pickaxe or a Burner Harvester and fuel | Yes, the Burner Harvester burns fuel; the Electric one stops | Yes, it is Fieldworks' role in MODS.md |
| Voidworks: Void Crucible | 1 gold ingot per iron ingot; 6 an hour in the Overworld, 12 in the End, 24 over the End's open void, per Crucible | A Crucible, which needs amethyst and crying obsidian the pack can't supply | Yes, it takes no power | Gold made from decomposed iron, outside the challenge mods |
| Voidworks: Mutation Chamber | 1 gold ingot per iron ingot, 200 void energy a try, 80% | Motes from a Displacer; amethyst and crying obsidian | Yes, it spends motes | As the Crucible |
| Nether fortress chests | About 1.2 gold ingots per chest, once | Nether access | Yes | No, world loot, once |
| Zombified piglins | Half a nugget per kill, 2.5% an ingot on a player kill | A spawning platform in the Nether, or a portal in the Overworld | Yes | No, world gold, unlimited |
| Neo Vitae | Gilded Blackstone → 9 nuggets; meteors with gold ore | Gilded Blackstone, which only a Nether meteor gives; 1,000,000 LP a meteor | Yes | Late, and Neo Vitae may become a challenge's mod |
| Cobblegen Galore, the island, the End, villagers, the Shop, FTB Quests, KubeJS | None | | | |

### 1. Advanced's mix

Covered in [advanced-mix.md](advanced-mix.md), sections 3 and 4. Without packing recipes:

- **Rune of Sacrifice II** (Neo Vitae) is the only true intermediate-2 item that carries whole gold ingots: 2, from the Rune of Sacrifice inside it (`NV/recipe/rune_2_sacrifice.json`, `NV/recipe/rune_sacrifice.json`). At tier 1 it returns 0.125 gold per rune recycled, or 1/32 gold per essence for each rune in the mix. The Runes mix (4 Outlets, 2 Pumps, 2 Runes) covers route A's gold at 32 essences per Galvanic, under its copper's 46.5. Its first recycle returns 4 netherite scrap, so that mix gives 0.5 scrap per essence, eight times its gold.
- **Modular Routers' trace gold.** Every module and upgrade is built on a Blank Module (½ gold nugget, `modular-routers-26.1.2.2.jar!/data/modularrouters/recipe/blank_module.json`) or a Blank Upgrade (¾ nugget, `…/blank_upgrade.json`). The true intermediate-2 ones, per item at full yield: Vacuum Module ½ nugget and 5 iron; Fluid Module ½ and 7 iron; Placer and Dropper Modules ½; Camouflage and Stack Upgrades ¾; Blast Upgrade ¾ and 1.5 iron; Fluid Upgrade ¼ and 1 iron; Muffler Upgrade 0.19; Augment Core 0.31. At tier 1 a decompose returns 1/64 of that, so a Vacuum Module in the mix gives 1/128 nugget per essence, and route A's 2 ingots (18 nuggets) would take about 2300 essences per module. Theurgy liquefies no nugget: its gold liquefaction is `from_ingots`, `from_raw_materials` and `from_ores` only (`theurgy-26.1.2-neoforge-1.116.1.jar!/data/theurgy/recipe/liquefaction/alchemical_sulfur_gold_from_*.json`). The nuggets reach it only through 9 nuggets → 1 ingot (`V/recipe/gold_ingot_from_nuggets.json`), a compress recipe. Under the packing rule this gold doesn't count, and it is too little to matter anyway.

### 2. Fieldworks: crafted ore blocks and the Harvester

**How it works.** Fieldworks' ore blocks hold a count, not a single drop. One break takes one unit and the block stays until the count reaches zero, then becomes stone (`OreMining.java`: "one break takes one unit and the block stands until the count reaches zero"). A Gold Ore Patch pays `minecraft:raw_gold` (`OreResource.java`: `GOLD("gold", "minecraft:raw_gold", 3.0f)`) and holds **3200** by default (`src/main/resources/fieldworks/ore/amounts.json`, `gold.default_amount`). That number is a classpath resource, not a datapack file, so the pack can't change it (`OreCorpus.java`: "A classpath resource rather than a datapack file").

**In this pack** no gold patch appears by itself. Fieldworks' patches are structures (`FW/worldgen/structure/patch_gold.json`), and Skyblock Builder generates only the structures its config allows, by default the Nether fortress alone (`SBB!/de/melanx/skyblockbuilder/config/common/StructuresConfig.class` defaults to `fortress`, and features `end_spike` and `end_gateway_return`; the pack ships no `structures.json5` to change it). So a patch has to be crafted, which is [#170](https://github.com/adamico/mechanical-mastery-plus/issues/170), still open: "Add KubeJS recipes that craft Fieldworks ore blocks from intermediates. A crafted ore block places full at its resource's default amount, so its cost is an investment." No such recipe exists yet in `kubejs/`.

**Working it.**

- **By hand**: gold needs an iron pickaxe (`fieldworks-…jar!/data/minecraft/tags/block/needs_iron_tool.json` lists `fieldworks:gold_ore`). A hand break draws a unit and the block survives. Hardness 3, as vanilla ore. A fake player counts as a hand (`OreMining.java`, comment on line 65), so a block breaker could work it too.
- **Burner Harvester** (`fieldworks:harvester_1`): one draw every 20 ticks at speed 1 (`OperationChoice.java`, `ORE_DRAW_TICKS = 20`; `FW/harvester_tier/harvester_1.json`, `"speed": 1.0`). That is **1 raw gold a second, 3600 an hour**, a block in under an hour. It burns 200 ticks of fuel per draw (`"power": {"fuel": true, "burn_per_operation": 200}`): coal or charcoal (1600 ticks, `NF/data_maps/item/furnace_fuels.json`) runs 8 draws, so 450 an hour, or a lava bucket (20000) runs 100. Charcoal comes from the island's tree. Recipe: 3 iron ingots, 4 cobblestone, a furnace and an iron pickaxe (`FW/recipe/harvester_1.json`).
- **Electric Harvester** (`fieldworks:harvester_2`): twice as fast, 40 FE a tick (`FW/harvester_tier/harvester_2.json`).

**Under the energy handicap.** The Burner Harvester takes no FE and is no generator: the handicap stops only the blocks in `#mechanicalmasteryreborn:generators`, which the pack fills with `#wireworks:generators` alone (`kubejs/server_scripts/generators.js`; `mod/…/challenge/Generators.java`). So it runs. The Electric Harvester stops once its team's Wireworks generators do, since nothing else in the pack makes FE for it.

**How it fits.** Exactly the role MODS.md gives Fieldworks: "ore blocks crafted from intermediates, and the Harvester that works them." The gold still enters by decomposing, through the intermediates the block is crafted from, and comes out raw, the band ADR 0006 wants. The lever is the recipe: one block is 3200 raw gold, route A's gold for about 4800 Galvanic Essences, so the recipe's price decides everything. A recipe that takes gold-free intermediates turns copper or iron into gold outside the challenge mods.

### 3. Voidworks: the Void Crucible and the Mutation Chamber

**How it works.** Voidworks upgrades an item up its Family: metal ingots go copper → iron → gold, and raw metals, nuggets and blocks the same (`VW/voidworks/family/metal_ingots.json`, `raw_metals.json`, `metal_nuggets.json`). Iron to gold costs 200 void energy at 80%, copper to iron 100 at 80%, one item for one (no `count`, so 1: [docs/spec/void-energy.md](https://github.com/5thlayer/voidworks/blob/bc26ea55f634168e1dd1967fcb4f16f03e7d62a7/docs/spec/void-energy.md), "Families").

- **Void Crucible**: upgrades for free, slowly, never fails, never Dormant. A result takes the Rung's cost × 30 ticks at the End's Void Pressure (`crucibleTicks`, 30, in `VoidworksConfig.java`), halved for each step of pressure above 3 and doubled for each below (`CrucibleTime.java`). Pressures are Nether 1, Overworld 2, End 3 (`VW/data_maps/dimension/void_pressure.json`), and 4 in the End over the open void (`VoidPressure.java`). So iron → gold takes 5 minutes in the End, 2.5 over the open void, 10 in the Overworld and 20 in the Nether: **6 gold an hour in the Overworld, 12 in the End, 24 over the void**, per Crucible. Crucibles within 8 blocks of each other share one rate (`crucibleRadius`, 8), so more need spreading out, and they work only while their chunk is loaded (FTB Chunks keeps a team's claims loaded). Copper → gold, two steps, takes 15 minutes in the Overworld.
- **Mutation Chamber**: the same upgrades, faster, for motes: 200 void energy a try at 80%, so 250 per gold on average. It wakes from Dormant for 1600 (`VW/data_maps/block/dormant_cost.json`). Motes come from a Void Displacer fed dense blocks, copper and coal blocks the lightest (`VW/tags/item/density/1.json`).
- **Unmaking Chamber**: breaks an item into its crafting ingredients, each back at 25% rising to 75% (`unmakingYieldStart`, `unmakingYieldCap`). It returns only gold an item already holds, so it is no source.

**What it needs.** No power, so all of it works under the energy handicap. But the Crucible is 6 amethyst shards, 2 crying obsidian and a cauldron (`VW/recipe/void_crucible.json`), and the Mutation Chamber the same around a smithing table. Neither material has a source in the pack outside a challenge's mod: no geodes or ruined portals generate, bartering for crying obsidian takes gold, Voidworks' own gems Family makes amethyst only in a Mutation Chamber, and the one recipe for crying obsidian is Theurgy's (`theurgy-…jar!/data/theurgy/recipe/incubation/crying_obsidian.json`). A Void Crucible is itself a true intermediate-2 item (a Cauldron's 7 iron, at depth 2), so Advanced's mix or a quest could hand one out whole.

**How it fits.** Voidworks' role in MODS.md is "void harvested from the world and carried between dimensions". The Crucible turns decomposed iron into gold, so gold still enters by decomposing, as with Fieldworks; it is a transmutation, like Theurgy's, but in a mod no challenge owns. Its slowness is its balance, and it rewards carrying iron to the End.

### 4. The Nether

The void Nether is reachable (`config/skyblockbuilder/dimensions.json5`: "the Nether and the End are reachable, and empty"), with every Nether biome (Skyblock Builder's default `world.json5`, which the pack doesn't override, excludes none). A portal needs obsidian, which Cobblegen Galore makes from water and lava (`CG/recipe/blockgen/obsidian.json`) or a player makes from the island's water and lava, and fire to light it.

- **Fortresses** generate, the one structure Skyblock Builder allows by default (see section 2; vanilla places them on a 27-chunk grid, `V/worldgen/structure_set/nether_complexes.json`, sharing it with bastions, which don't generate). A fortress chest rolls 2 to 4 times, gold ingots at 15 in 78, 1 to 3 each (`V/loot_table/chests/nether_bridge.json`), about **1.2 gold ingots a chest**, plus golden swords, chestplates and horse armour that smelt to nuggets (`V/recipe/gold_nugget_from_smelting.json`). A fortress has a handful of chests: a few ingots, once. Enough for routes B and C's one raw gold seed (as ingots, if a gold ingot does the seed's job), not for route A.
- **Zombified piglins** spawn in nether wastes and crimson forests (`V/worldgen/biome/nether_wastes.json`, weight 100; `crimson_forest.json`), and in fortresses. Each drops 0 to 1 gold nugget, and an ingot at 2.5% when a player kills it (`V/loot_table/entities/zombified_piglin.json`): about 0.08 ingots a kill. A spawning platform in the void Nether is the classic skyblock gold farm; at a few hundred kills an hour it gives tens of ingots, unlimited. In the Overworld a portal's blocks spawn them too, while the `NETHER_PORTAL_SPAWNS_PIGLINS` game rule is on (`minecraft_26.1.2_client.jar!/net/minecraft/world/level/block/NetherPortalBlock.class`), more slowly. Their nuggets need 9 → 1 into ingots for Theurgy.
- **Piglin bartering** takes gold and gives none back (`V/loot_table/gameplay/piglin_bartering.json`). A sink, not a source.

No power is involved, so all of it works under the energy handicap. **How it fits:** gold from the world, not from decomposing. The fortress's few ingots are a one-off a team finds; a piglin farm is an unlimited gold source the line's loop never sees, and it would make the gold question moot for anyone who builds one.

### 5. Neo Vitae

- The Alchemy Table turns Gilded Blackstone into 9 gold nuggets (`NV/recipe/alchemytable/gold_ore_from_gilded.json`, 200 LP, table upgrade 2). Gilded Blackstone comes only from bastions, which don't generate, or a Nether meteor.
- The Mark of the Falling Tower calls a meteor for 1,000,000 LP. The iron meteor, from an iron block, holds gold ore at weight 30 among iron, copper, lapis and redstone (`NV/recipe/meteor/iron.json`); the Nether one, from glowstone, nether gold ore and Gilded Blackstone (`NV/recipe/meteor/nether.json`).

Late, expensive, and Neo Vitae's place is open: MODS.md calls it "another path to make from decomposed resources", and if it becomes the life challenge's mod it stops counting as a source.

### 6. What gives no gold

- **Cobblegen Galore** makes only stone kinds, basalt, netherrack, end stone, mud and obsidian (`CG/recipe/blockgen/*.json`; the `ae2`, `atm`, `create` and `xycraft` recipes need mods the pack doesn't have). Its gold generator is a tier made from 6 gold ingots (`CG/recipe/crafting/block_gen_gold.json`): a sink.
- **The island** (`config/skyblockbuilder/templates/islands/reborn.snbt`) is stone, dirt, grass, an oak tree, a water and a lava source, C.O.D. and the lab. No chest, no ore. Skyblock Builder's starting inventory is empty.
- **The End** keeps its main island and nothing else (`dimensions.json5`, `keepMainIsland`), and only `end_spike` and `end_gateway_return` generate, so there are no End cities.
- **Villagers**: no villages generate, and no vanilla trade gives gold; the cleric buys it (`V/villager_trade/cleric/2/gold_ingot_emerald.json`). The wandering trader's one gold item is a Golden Dandelion (`V/villager_trade/wandering_trader/emerald_golden_dandelion.json`).
- **The Shop** is only a glossary term. The money handicap exists but the lab can't choose it yet (`mod/…/challenge/Handicap.java`: `MONEY("money", false)`, "for now only energy, the others coming later (#171)"), and nothing in the mod or `kubejs/` sells anything.
- **FTB Quests**: the pack ships no quest files (`config/` holds only `defaultworldtype/` and `skyblockbuilder/`). The local instance's quest book has emergency items only, a lava bucket, a water bucket and an oak sapling, and drops no loot crates.
- **KubeJS**: `decomposing.js` (Advanced's placeholder mix and the Recycler's parts, no gold), `generators.js` (the handicap's tag) and `team_of_one.js`. None gives gold.
- **Overworld mobs**: zombies, drowned and the rest drop no gold in vanilla; of the entity loot tables only the zombified piglin's names gold.

## Recommended options

Ranked, for the maintainer to decide.

1. **A crafted Gold Ore Patch (#170), worked by hand or by a Burner Harvester.** It is Fieldworks' job in the pack, gives raw gold (the band ADR 0006 and Theurgy both prefer), keeps gold entering by decomposing, runs under the energy handicap, and needs only a recipe in `kubejs/`. Its risk is size: 3200 raw gold a block, fixed by Fieldworks, so the recipe must cost enough intermediates to be the investment #170 means, or route A's gold stops mattering after one block.
2. **A Void Crucible, fed recycled iron.** Free, slow and powerless, so it suits the energy challenge: 2 gold per Galvanic is 20 minutes of one Crucible in the Overworld, 5 over the End's void. It turns the iron every mix already carries into gold, outside the challenge mods. It needs the pack to supply the Crucible, since amethyst and crying obsidian have no source: a Void Crucible in a mix (it is a true intermediate 2) or as a quest reward.
3. **The Runes mix.** Advanced's mix carries its own gold through two Runes of Sacrifice II, with no new mechanic. The price is half a netherite scrap per essence and a Neo Vitae item in the mix.

A zombified piglin farm in the void Nether works today with nothing added, and is the strongest gold source by far, but it brings gold in from the world, not by decomposing. It is worth deciding whether to allow it, block it, or leave it as the side path it would be anyway.

## Open questions

- **Gold from the world.** Does gold from mobs and loot fit "decomposing is how resources enter this line"? If not, does the pack stop piglin farms (a spawn rule, the portal game rule), or accept them as a side path?
- **The patch's price.** What does a Gold Ore Patch cost, at 3200 raw gold a block? Does #170 craft gold patches at all, or only copper and iron?
- **Future challenges.** The money, protection and life challenges' mods aren't chosen (#171). If Fieldworks, Voidworks or Neo Vitae becomes one, its gold stops counting. Is any of them a candidate?
- **The Crucible's materials.** If a Void Crucible is the answer, where does it come from: Advanced's mix, a quest, or a pack recipe for amethyst and crying obsidian?
- **The seed.** Routes B and C need one raw gold to seed the loop. Do a fortress's ingots, or a crafted patch's first draw, serve, or should the seed come with the challenge?
- **Checks in game.** That fortresses generate in the void Nether, that zombified piglins spawn on a platform there, that a Burner Harvester keeps drawing while its team plays the energy challenge, and the Crucible's times, were read from data and code, not seen.
