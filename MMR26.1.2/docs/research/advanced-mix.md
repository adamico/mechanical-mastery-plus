# The Advanced mix: what Advanced Mechanical Essence decomposes into (#209)

Research for Advanced Mechanical Essence's mix, redone against the frozen mod list (#210). `advanced-mix.json` holds the chosen mix and, for every item at intermediate 2 or deeper that carries copper, iron, gold or redstone, its recycle tree, level and yields.

## Decided

- **Advanced's mix is M5** (section 3): 20 kinds, 22 items, all stacking to 64. #207 tunes the amounts and the yield, and may scale them down; the kinds stay.
- **Intermediate 2** is any item that doesn't recycle completely into refined and raw items in one step. Every ingredient counts, wood and stone as much as metal: a Hopper (iron and a Chest) is intermediate 2, a Piston, a Chest or a Stick intermediate 1. Deeper items, as Beltworks' Feeder, are in. **Refined** is anything only smelted or cut from raw, however many times: ingots, planks, glass, stone, smooth stone (glossary **Band**).
- **Recycling undoes only crafting and Craftworks' assembling**, never smelting, and refuses refined and raw items. Ores come only from the Shop until Basic ([ADR 0008](../adr/0008-recycling-stops-at-refined.md)).
- **Packing is allowed**: an item built on storage blocks qualifies, and the extra metal a team gets by crafting the blocks back to ingots is accepted.
- **Out of every mix**: waxed items, and armor, weapons and tools (Gearworks' suits and lances among them). Gearworks' upgrades are in.
- **Challenge mods' items are in.** A chain's recipes, the machines' own processing recipes among them, stay locked until the challenge starts (ADR 0007), so a chain machine from a mix does no work before then.
- **Gold comes from the mix**: M5 carries 0.64 per essence, enough for route A. The Void Crucible's own recipe is left alone, and the Fieldworks Gold Ore Patch (#170) has no gold duty.
- **The Recycler's parts** are made from wood and stone only (planks, sticks, cobblestone, smooth stone, chests) and are no part of a mix.
- **A mix may hold any number of kinds.** A Recycler whose outputs hold fewer kinds than a decompose gives returns as many as fit, picked at random, and loses the rest. M5's 20 kinds don't fit a tier-1 Recycler's 6 outputs (18 kinds) today, so #209 waits on that change.

This replaces the earlier research's rules (true intermediate 2 only, no packing, no whole machines), under which the best mix cost 34 to 230 essences per Galvanic Essence.

Sources, all primary: the vanilla 26.1.2 client jar, the NeoForge 26.1.2.109 universal jar (its `data/minecraft/recipe/` replaces vanilla's piston, hopper, bucket and others by id), and all 110 jars in the CurseForge instance's `mods/` folder, unzipped and read as data; the companion mod's `RecyclingRules.java`, `Recycling.java` and `Yield.java` for the rules; `ServerConfig.java` for the tier-1 yield, 0.25. Gateways to Eternity, Apothic Attributes and Placebo add no candidate: the Gate Pearl carries no metal, and the other two have no items. The trees were worked out with a script.

## Rule as applied

- **Recycling reverses crafting only** (grid crafting and Craftworks assembling; assembling ranks first, an ore ingredient loses, ties by recipe id; uncompress recipes never run). Smelting, blasting, smoking and campfire are never reversed: an item with any of those recipes is **refined** and a leaf (glass, stone, smooth stone, ingots, netherite ingot via FTB Materials' dust smelting).
- **Raw/refined (level 0)**: no crafting recipe the Recycler runs; anything smelted; planks; anything stonecut from raw/refined (stone bricks, polished stones, etc.; copper cut blocks excluded since they are cut from a block); andesite/diorite/granite (mined, though vanilla has crafting recipes for them); the four metals and their nuggets (nuggets reach ingots by 9 → 1, packing, now allowed; counted as 1/9 ingot below).
- **Level** = 1 + the deepest ingredient, over every ingredient (wood and stone count like metal). Stick, chest, furnace, crafting table, piston, bucket, minecart, cauldron, dropper, observer, storage blocks (9 ingots → block) are intermediate 1. Hopper, Blast Furnace, Redstone Torch, Crafter, Rail are 2; Comparator, Repeater, Dispenser (via the bow, which is 2) are 3. Level ≥ 2 qualifies.
- Out: waxed items (32 in vanilla), armor/weapons/tools (67, by name, plus Gearworks suits/lances/railgun), the Recycler's own parts.
- **Tier 1**: each recycle 0.25, repeated until everything is refined ("all the way"; leaves at depth d count 0.25^d). "Crafted" = a storage block that comes out is crafted back to 9 at full yield, as a player would do (packing allowed). Per essence = 0.25 (decompose) × Σ over the whole mix.
- Model caveat: one recipe the model can't read, Functional Storage's drawer ingredient (`functionalstorage:tag_without_component`, a drawer tag), shows as "(planks tag)"; it carries no metal.

Reference levels: piston 1, chest 1, furnace 1, stick 1, bucket 1, minecart 1, cauldron 1, observer 1, dropper 1, smithing table 1, copper/iron/redstone block 1, lightning rod 1, iron bars 1; hopper 2, blast furnace 2, redstone torch 2, crafter 2, rail 2, barrel 2 (wooden slab), daylight detector 2, lever 2; comparator 3, repeater 3, dispenser 3; glass pane 3 (it recycles into Connected Glass's scratched pane by id order, an artefact the research already flagged).

## 1. Suite items re-levelled

Old = the level under the earlier rule, which counted only ingredients that carry metal. Sub-amounts in brackets are per one of the parent ingredient. Every row qualifies (level ≥ 2) unless noted. Stack: every row is 64 except Drilling Mud / Magma Slurry buckets (1) and Gearworks' suits, lances and Railgun (1; tools/weapons, out, kept for reference). Gold/iron nuggets are shown as ingot-equivalents (÷9) in the tier-1 column.

| Item | Lvl old→new | Recycle tree (level 1 [level 2]) | Full | Tier 1 per item, recycled all the way | Stack | Packing / notable |
|---|---|---|---|---|---|---|
| beltworks:express_belt_tile | 3→3 | 1 beltworks:improved_belt_tile [1 beltworks:belt_tile, 0.125 iron_ingot]; 0.125 gold_ingot | Cu 0.188, Fe 0.125, Au 0.125 | Cu 0.00293, Fe 0.00781, Au 0.0312 | 64 |  |
| beltworks:express_feeder | 5→6 | 1 beltworks:express_loader [1 beltworks:improved_loader, 0.5 gold_ingot]; 1 beltworks:express_belt_tile [1 beltworks:improved_belt_tile, 0.125 gold_ingot] | Cu 0.781, Fe 3.12, Au 0.625 | Cu 0.00278, Fe 0.0122, Au 0.0391 | 64 |  |
| beltworks:express_loader | 4→5 | 1 beltworks:improved_loader [1 beltworks:loader, 0.5 iron_ingot]; 0.5 gold_ingot | Cu 0.594, Fe 3, Au 0.5 | Cu 0.00818, Fe 0.041, Au 0.125 | 64 |  |
| beltworks:express_splitter | 5→6 | 1 beltworks:improved_splitter [1 beltworks:splitter, 2 iron_ingot]; 2 gold_ingot | Cu 1.38, Fe 2, Au 2, Rs 3 | Cu 0.0171, Fe 0.125, Au 0.5, Rs 0.00293 | 64 |  |
| beltworks:feeder | 3→4 | 1 beltworks:loader [0.5 hopper, 0.5 copper_ingot, 0.5 beltworks:belt_tile]; 1 beltworks:belt_tile [0.1875 dried_kelp, 0.1875 copper_ingot] | Cu 0.781, Fe 2.5 | Cu 0.0444, Fe 0.0391 | 64 |  |
| beltworks:improved_belt_tile | 2→2 | 1 beltworks:belt_tile [0.1875 dried_kelp, 0.1875 copper_ingot]; 0.125 iron_ingot | Cu 0.188, Fe 0.125 | Cu 0.0117, Fe 0.0312 | 64 |  |
| beltworks:improved_feeder | 4→5 | 1 beltworks:improved_loader [1 beltworks:loader, 0.5 iron_ingot]; 1 beltworks:improved_belt_tile [1 beltworks:belt_tile, 0.125 iron_ingot] | Cu 0.781, Fe 3.12 | Cu 0.0111, Fe 0.0488 | 64 |  |
| beltworks:improved_loader | 3→4 | 1 beltworks:loader [0.5 hopper, 0.5 copper_ingot, 0.5 beltworks:belt_tile]; 0.5 iron_ingot | Cu 0.594, Fe 3 | Cu 0.0327, Fe 0.164 | 64 |  |
| beltworks:improved_splitter | 4→5 | 1 beltworks:splitter [2 beltworks:belt_tile, 1 comparator, 1 copper_ingot]; 2 iron_ingot | Cu 1.38, Fe 2, Rs 3 | Cu 0.0684, Fe 0.5, Rs 0.0117 | 64 |  |
| beltworks:loader | 2→3 | 0.5 hopper [5 iron_ingot, 1 chest]; 0.5 copper_ingot; 0.5 beltworks:belt_tile [0.1875 dried_kelp, 0.1875 copper_ingot] | Cu 0.594, Fe 2.5 | Cu 0.131, Fe 0.156 | 64 |  |
| beltworks:splitter | 3→4 | 2 beltworks:belt_tile [0.1875 dried_kelp, 0.1875 copper_ingot]; 1 comparator [3 redstone_torch, 1 quartz, 3 stone]; 1 copper_ingot | Cu 1.38, Rs 3 | Cu 0.273, Rs 0.0469 | 64 |  |
| beltworks:turbo_belt_tile | 4→4 | 1 beltworks:express_belt_tile [1 beltworks:improved_belt_tile, 0.125 gold_ingot]; 0.125 diamond | Cu 0.188, Fe 0.125, Au 0.125 | Cu 0.00073, Fe 0.00195, Au 0.00781 | 64 | diamond |
| beltworks:turbo_feeder | 6→7 | 1 beltworks:turbo_loader [1 beltworks:express_loader, 0.5 diamond]; 1 beltworks:turbo_belt_tile [1 beltworks:express_belt_tile, 0.125 diamond] | Cu 0.781, Fe 3.12, Au 0.625 | Cu 0.00069, Fe 0.00305, Au 0.00977 | 64 | diamond |
| beltworks:turbo_loader | 5→6 | 1 beltworks:express_loader [1 beltworks:improved_loader, 0.5 gold_ingot]; 0.5 diamond | Cu 0.594, Fe 3, Au 0.5 | Cu 0.00204, Fe 0.0103, Au 0.0312 | 64 | diamond |
| beltworks:turbo_splitter | 6→7 | 1 beltworks:express_splitter [1 beltworks:improved_splitter, 2 gold_ingot]; 2 diamond | Cu 1.38, Fe 2, Au 2, Rs 3 | Cu 0.00427, Fe 0.0312, Au 0.125, Rs 0.00073 | 64 | diamond |
| craftworks:assembler_1 | 3→3 | 1 crafter [5 iron_ingot, 1 crafting_table, 2 redstone, 1 dropper]; 9 iron_ingot; 5 redstone | Fe 14, Rs 8 | Fe 2.56, Rs 1.39 | 64 |  |
| craftworks:assembler_2 | 4→4 | 1 craftworks:assembler_1 [1 crafter, 9 iron_ingot, 5 redstone]; 6 gold_ingot; 10 redstone | Fe 14, Au 6, Rs 18 | Fe 0.641, Au 1.5, Rs 2.85 | 64 |  |
| craftworks:assembler_3 | 5→5 | 1 craftworks:assembler_2 [1 craftworks:assembler_1, 6 gold_ingot, 10 redstone]; 4 diamond; 2 redstone_block [9 redstone] | Fe 14, Au 6, Rs 36 | Fe 0.16, Au 0.375, Rs 1.84 (crafted: Fe 0.16, Au 0.375, Rs 5.21) | 64 | packing: redstone_block; diamond |
| craftworks:refiner | 2→3 | 1 blast_furnace [5 iron_ingot, 1 furnace, 3 smooth_stone]; 4 iron_ingot; 5 redstone | Fe 9, Rs 5 | Fe 1.31, Rs 1.25 | 64 |  |
| fieldworks:drilling_mud_bucket | 3→3 | 1 water_bucket [1 theurgy:crystallized_water, 1 bucket]; 1 clay_ball | Fe 12 | Fe 0.05 | 1 | water_bucket |
| fieldworks:harvester_1 | 2→3 | 3 iron_ingot; 4 cobblestone; 1 furnace [8 cobblestone]; 1 iron_pickaxe [3 iron_ingot, 2 stick] | Fe 6 | Fe 0.938 | 64 |  |
| fieldworks:harvester_2 | 3→4 | 4 copper_ingot; 2 redstone; 2 iron_ingot; 1 fieldworks:harvester_1 [3 iron_ingot, 4 cobblestone, 1 furnace, 1 iron_pickaxe] | Cu 4, Fe 8, Rs 2 | Cu 1, Fe 0.734, Rs 0.5 | 64 |  |
| fieldworks:magma_slurry_bucket | 3→3 | 1 lava_bucket [1 theurgy:crystallized_lava, 1 bucket]; 1 soul_soil | Fe 12 | Fe 0.05 | 1 | lava_bucket |
| gearworks:boost_upgrade | 2→4 | 4 gold_ingot; 4 firework_rocket [0.3333 gunpowder, 0.3333 paper]; 1 dispenser [7 cobblestone, 1 bow, 1 redstone] | Au 4, Rs 1 | Au 1, Rs 0.0625 | 64 |  |
| gearworks:combustion_upgrade | 2→3 | 4 iron_ingot; 1 blast_furnace [5 iron_ingot, 1 furnace, 3 smooth_stone]; 1 redstone | Fe 9, Rs 1 | Fe 1.31, Rs 0.25 | 64 |  |
| gearworks:diamond_bore_lance | 3→4 | 1 gearworks:iron_bore_lance [6 copper_ingot, 2 redstone, 1 iron_spear]; 3 diamond | Cu 6, Fe 1, Rs 2 | Cu 0.375, Fe 0.0156, Rs 0.125 | 1 (out: tool/weapon) | diamond |
| gearworks:diamond_gear | 3→3 | 1 gearworks:iron_gear [6 copper_ingot, 2 redstone, 1 iron_chestplate]; 8 diamond | Cu 6, Fe 8, Rs 2 | Cu 0.375, Fe 0.125, Rs 0.125 | 1 (out: tool/weapon) | diamond |
| gearworks:gear_bench | 2→2 | 6 iron_ingot; 2 redstone; 1 smithing_table [2 iron_ingot, 4 oak_planks] | Fe 8, Rs 2 | Fe 1.62, Rs 0.5 | 64 |  |
| gearworks:iron_bore_lance | 2→3 | 6 copper_ingot; 2 redstone; 1 iron_spear [1 iron_ingot, 2 stick] | Cu 6, Fe 1, Rs 2 | Cu 1.5, Fe 0.0625, Rs 0.5 | 1 (out: tool/weapon) |  |
| gearworks:iron_gear | 2→2 | 6 copper_ingot; 2 redstone; 1 iron_chestplate [8 iron_ingot] | Cu 6, Fe 8, Rs 2 | Cu 1.5, Fe 0.5, Rs 0.5 | 1 (out: tool/weapon) |  |
| gearworks:jump_upgrade | 2→2 | 4 iron_ingot; 3 slime_ball; 1 piston [3 oak_planks, 4 cobblestone, 1 iron_ingot, 1 redstone]; 1 redstone | Fe 5, Rs 2 | Fe 1.06, Rs 0.312 | 64 | slime_ball |
| gearworks:kinetic_upgrade | 2→2 | 4 copper_ingot; 1 piston [3 oak_planks, 4 cobblestone, 1 iron_ingot, 1 redstone]; 1 redstone | Cu 4, Fe 1, Rs 2 | Cu 1, Fe 0.0625, Rs 0.312 | 64 |  |
| gearworks:metabolic_upgrade | 2→2 | 4 copper_ingot; 1 golden_carrot [8 gold_nugget, 1 carrot]; 1 redstone | Cu 4, Au 0.889, Rs 1 | Cu 1, Au 0.0556, Rs 0.25 | 64 |  |
| gearworks:night_vision_upgrade | 2→4 | 4 iron_ingot; 3 glass_pane [1 connectedglass:scratched_glass_pane]; 1 golden_carrot [8 gold_nugget, 1 carrot]; 1 redstone | Fe 4, Au 0.889, Rs 1 | Fe 1, Au 0.0556, Rs 0.25 | 64 |  |
| gearworks:nuclear_upgrade | 2→2 | 5 iron_ingot; 1 redstone_block [9 redstone]; 2 netherite_ingot; 1 nether_star | Fe 5, Rs 9 | Fe 1.25, Rs 0.562 (crafted: Fe 1.25, Rs 2.25) | 64 | packing: redstone_block; nether_star, netherite_ingot |
| gearworks:railgun | 2→2 | 1 lightning_rod [3 copper_ingot]; 2 copper_block [9 copper_ingot]; 1 redstone_block [9 redstone]; 1 netherite_ingot | Cu 21, Rs 9 | Cu 1.31, Rs 0.562 (crafted: Cu 4.69, Rs 2.25) | 1 (out: tool/weapon) | packing: copper_block, redstone_block; netherite_ingot |
| gearworks:reach_upgrade | 2→2 | 4 iron_ingot; 3 ender_pearl; 1 piston [3 oak_planks, 4 cobblestone, 1 iron_ingot, 1 redstone]; 1 redstone | Fe 5, Rs 2 | Fe 1.06, Rs 0.312 | 64 | ender_pearl |
| gearworks:shield_upgrade | 2→2 | 4 iron_ingot; 2 redstone; 2 obsidian; 1 shield [6 oak_planks, 1 iron_ingot] | Fe 5, Rs 2 | Fe 1.06, Rs 0.5 | 64 | obsidian |
| gearworks:solar_upgrade | 1→4 | 3 glass_pane [1 connectedglass:scratched_glass_pane]; 4 copper_ingot; 1 daylight_detector [3 glass, 3 quartz, 3 oak_slab]; 1 redstone | Cu 4, Rs 1 | Cu 1, Rs 0.25 | 64 |  |
| gearworks:speed_upgrade | 2→4 | 4 iron_ingot; 1 sugar [0.3333 honey_bottle]; 2 piston [3 oak_planks, 4 cobblestone, 1 iron_ingot, 1 redstone]; 1 redstone | Fe 6, Rs 3 | Fe 1.12, Rs 0.375 | 64 | honey_block, honey_bottle |
| pipeworks:outlet | 2→4 | 4 pipeworks:pipe [1 copper_ingot]; 1 dispenser [7 cobblestone, 1 bow, 1 redstone] | Cu 4, Rs 1 | Cu 0.25, Rs 0.0625 | 64 |  |
| pipeworks:pump | 2→2 | 3 pipeworks:pipe [1 copper_ingot]; 1 piston [3 oak_planks, 4 cobblestone, 1 iron_ingot, 1 redstone]; 1 bucket [3 iron_ingot] | Cu 3, Fe 4, Rs 1 | Cu 0.188, Fe 0.25, Rs 0.0625 | 64 |  |
| voidworks:end_void_displacer | 3→3 | 4 end_stone_bricks [1 end_stone]; 2 popped_chorus_fruit; 2 end_rod [0.25 blaze_rod, 0.25 popped_chorus_fruit]; 1 sticky_piston [1 slime_ball, 1 piston] | Fe 1, Rs 1 | Fe 0.0156, Rs 0.0156 | 64 | blaze_rod, slime_ball |
| voidworks:mutation_chamber | 2→2 | 6 amethyst_shard; 2 crying_obsidian; 1 smithing_table [2 iron_ingot, 4 oak_planks] | Fe 2 | Fe 0.125 | 64 | amethyst_shard, crying_obsidian |
| voidworks:nether_void_displacer | 3→3 | 4 polished_blackstone_bricks [1 polished_blackstone]; 2 magma_cream [1 blaze_powder, 1 slime_ball]; 2 blaze_rod; 1 sticky_piston [1 slime_ball, 1 piston] | Fe 1, Rs 1 | Fe 0.0156, Rs 0.0156 | 64 | blaze_powder, blaze_rod, slime_ball |
| voidworks:overworld_void_displacer | 3→3 | 4 crying_obsidian; 4 ender_pearl; 1 sticky_piston [1 slime_ball, 1 piston] | Fe 1, Rs 1 | Fe 0.0156, Rs 0.0156 | 64 | crying_obsidian, ender_pearl, slime_ball |
| voidworks:void_crucible | 2→2 | 6 amethyst_shard; 2 crying_obsidian; 1 cauldron [7 iron_ingot] | Fe 7 | Fe 0.438 | 64 | amethyst_shard, crying_obsidian |
| wireworks:accumulator | 2→2 | 4 iron_block [9 iron_ingot]; 4 wireworks:battery [1 copper_ingot, 1 iron_ingot, 1 redstone]; 1 copper_ingot | Cu 5, Fe 40, Rs 4 | Cu 0.5, Fe 2.5, Rs 0.25 (crafted: Cu 0.5, Fe 9.25, Rs 0.25) | 64 | packing: iron_block |
| wireworks:boiler | 2→2 | 7 iron_ingot; 1 bucket [3 iron_ingot]; 1 furnace [8 cobblestone] | Fe 10 | Fe 1.94 | 64 |  |
| wireworks:reactor | 2→3 | 4 iron_block [9 iron_ingot]; 2 wireworks:plastic; 2 netherite_block [9 netherite_ingot]; 1 blast_furnace [5 iron_ingot, 1 furnace, 3 smooth_stone] | Fe 41 | Fe 2.56 (crafted: Fe 9.31) | 64 | packing: iron_block, netherite_block; netherite_block, netherite_ingot |
| wireworks:small_pole | 1→2 | 0.5 stick [0.5 oak_planks]; 1 copper_ingot | Cu 1 | Cu 0.25 | 64 |  |
| wireworks:solar_panel | 2→3 | 2 glass; 1 daylight_detector [3 glass, 3 quartz, 3 oak_slab]; 2 copper_ingot; 1 wireworks:plastic; 1 iron_block [9 iron_ingot] | Cu 2, Fe 9 | Cu 0.5, Fe 0.562 (crafted: Cu 0.5, Fe 2.25) | 64 | packing: iron_block |
| wireworks:steam_engine | 2→2 | 5 iron_ingot; 1 piston [3 oak_planks, 4 cobblestone, 1 iron_ingot, 1 redstone]; 2 copper_ingot; 1 iron_block [9 iron_ingot] | Cu 2, Fe 15, Rs 1 | Cu 0.5, Fe 1.88, Rs 0.0625 (crafted: Cu 0.5, Fe 3.56, Rs 0.0625) | 64 | packing: iron_block |
| wireworks:steam_turbine | 2→2 | 4 copper_block [9 copper_ingot]; 2 wireworks:plastic; 2 piston [3 oak_planks, 4 cobblestone, 1 iron_ingot, 1 redstone]; 1 iron_block [9 iron_ingot] | Cu 36, Fe 11, Rs 2 | Cu 2.25, Fe 0.688, Rs 0.125 (crafted: Cu 9, Fe 2.38, Rs 0.125) | 64 | packing: copper_block, iron_block |
| wireworks:transformer | 2→2 | 4 iron_ingot; 3 copper_ingot; 1 iron_block [9 iron_ingot]; 1 wireworks:plastic | Cu 3, Fe 13 | Cu 0.75, Fe 1.56 (crafted: Cu 0.75, Fe 3.25) | 64 | packing: iron_block |
| wireworks:transmission_pole | 2→2 | 2 copper_ingot; 1 iron_bars [0.375 iron_ingot]; 2 wireworks:plastic; 2 iron_ingot | Cu 2, Fe 2.38 | Cu 0.5, Fe 0.523 | 64 |  |

Changes against the old metal-only levels:
- **New at level 2+**: Small Pole (2, its stick), Solar Upgrade (4, its daylight detector and glass pane). Still intermediate 1: Pipe, Storage Tank, Belt Tile, Medium and Large Pole, Battery, Fuel Cell, Battery Upgrade.
- **Deeper**: Outlet 2→4 and Splitter 3→4 (dispenser's bow, comparator's torches), Harvester 1/2 and Refiner up one (furnace), Loader/Feeder chain up one (hopper's chest). Nothing drops out.
- Voidworks' Void Well, Rim, Casing, Hatch and Unmaking Chamber are now level 2–4 but carry no Cu/Fe/Au/Rs, so they are not in the table.
- Every M4 member qualifies: Steam Turbine, Transformer, Steam Engine, Accumulator, Pump, Transmission Pole, Kinetic and Metabolic Upgrades at 2; Harvester 2, Assembler 2, Splitter, Outlet at 4.

## 2. Vanilla and the other mods

All 306 non-suite, non-waxed, non-tool items at level 2+ that carry copper, iron, gold or redstone were scanned. The table lists the ones worth at least 0.5 ingot-or-dust per item at tier 1 (crafted), richest first; decorative families are summarised after it. Stack 1 is from the item class (`stacksTo(1)` in Refined Storage's disks, storage blocks, controller, cards, wireless items; Functional Storage's and Pipez' tools; LaserIO's holder/cloner); "64?" means no `stacksTo`/`durability` was found in the class but the item is a held tool, so check in game.

| Item | Lvl | Recycle tree (level 1 [level 2]) | Full | Tier 1 per item, recycled all the way | Stack | Packing / notable |
|---|---|---|---|---|---|---|
| pipez:ultimate_upgrade | 4 | 4 netherite_ingot; 4 redstone_block [9 redstone]; 1 pipez:advanced_upgrade [4 diamond, 4 redstone_block, 1 pipez:improved_upgrade] | Fe 4.44, Au 4, Rs 77 | Fe 0.0174, Au 0.0625, Rs 2.88 (crafted: Fe 0.0174, Au 0.0625, Rs 11.3) | 64 | packing: redstone_block; diamond, netherite_ingot |
| pipez:advanced_upgrade | 3 | 4 diamond; 4 redstone_block [9 redstone]; 1 pipez:improved_upgrade [4 gold_ingot, 4 redstone, 1 pipez:basic_upgrade] | Fe 4.44, Au 4, Rs 41 | Fe 0.0694, Au 0.25, Rs 2.52 (crafted: Fe 0.0694, Au 0.25, Rs 9.27) | 64 | packing: redstone_block; diamond |
| functionalstorage:gold_upgrade | 4 | 3 gold_ingot; 3 gold_block [9 gold_ingot]; 2 chest [8 oak_planks]; 1 functionalstorage:copper_upgrade [4 copper_ingot, 2 copper_block, 2 chest, 1 functionalstorage:oak_2] | Cu 22, Au 30 | Cu 0.531, Au 2.44 (crafted: Cu 1.38, Au 7.5) | 64 | packing: copper_block, gold_block |
| minecraft:anvil | 2 | 3 iron_block [9 iron_ingot]; 4 iron_ingot | Fe 31 | Fe 2.69 (crafted: Fe 7.75) | 64 | packing: iron_block |
| functionalstorage:redstone_upgrade | 4 | 4 redstone; 2 redstone_block [9 redstone]; 2 comparator [3 redstone_torch, 1 quartz, 3 stone]; 1 functionalstorage:oak_2 [3.5 (planks tag), 1 chest] | Rs 28 | Rs 2.22 (crafted: Rs 5.59) | 64 | packing: redstone_block |
| functionalstorage:copper_upgrade | 3 | 4 copper_ingot; 2 copper_block [9 copper_ingot]; 2 chest [8 oak_planks]; 1 functionalstorage:oak_2 [3.5 (planks tag), 1 chest] | Cu 22 | Cu 2.12 (crafted: Cu 5.5) | 64 | packing: copper_block |
| theurgy:fermentation_vat | 3 | 6 copper_ingot; 1 theurgy:alchemical_sulfur_rotten_flesh; 1 barrel [6 oak_planks, 2 oak_slab]; 1 copper_block [9 copper_ingot] | Cu 15 | Cu 2.06 (crafted: Cu 3.75) | 64 | packing: copper_block |
| theurgy:calcination_oven | 2 | 4 iron_ingot; 1 copper_block [9 copper_ingot] | Cu 9, Fe 4 | Cu 0.562, Fe 1 (crafted: Cu 2.25, Fe 1) | 64 | packing: copper_block |
| theurgy:distiller | 2 | 3 iron_ingot; 1 copper_block [9 copper_ingot]; 3 andesite | Cu 9, Fe 3 | Cu 0.562, Fe 0.75 (crafted: Cu 2.25, Fe 0.75) | 64 | packing: copper_block |
| laserio:card_energy | 2 | 2 redstone; 1 redstone_block [9 redstone]; 2 quartz; 1 laserio:logic_chip; 3 gold_nugget | Au 0.333, Rs 11 | Au 0.0833, Rs 1.06 (crafted: Au 0.0833, Rs 2.75) | 64 | packing: redstone_block |
| modularrouters:energy_output_module | 3 | 1 redstone_block [9 redstone]; 2 gold_ingot; 1 modularrouters:blank_module [0.1667 redstone, 0.5 paper, 0.5 gold_nugget]; 1 quartz | Au 2.06, Rs 9.17 | Au 0.503, Rs 0.573 (crafted: Au 0.503, Rs 2.26) | 64 | packing: redstone_block |
| neovitae:hellfire_forge | 2 | 2 iron_ingot; 4 deepslate; 1 neovitae:tabula_rasa; 1 iron_block [9 iron_ingot] | Fe 11 | Fe 1.06 (crafted: Fe 2.75) | 64 | packing: iron_block |
| modularrouters:energy_upgrade | 3 | 4 quartz; 1 redstone_block [9 redstone]; 1 modularrouters:blank_upgrade [1 paper, 0.75 gold_nugget, 0.25 lapis_lazuli]; 1 gold_ingot | Au 1.08, Rs 9 | Au 0.255, Rs 0.562 (crafted: Au 0.255, Rs 2.25) | 64 | packing: redstone_block |
| pipez:improved_upgrade | 2 | 4 gold_ingot; 4 redstone; 1 pipez:basic_upgrade [4 iron_ingot, 4 iron_nugget, 1 redstone] | Fe 4.44, Au 4, Rs 5 | Fe 0.278, Au 1, Rs 1.06 | 64 |  |
| functionalstorage:diamond_upgrade | 5 | 4 diamond; 2 diamond_block [9 diamond]; 2 chest [8 oak_planks]; 1 functionalstorage:gold_upgrade [3 gold_ingot, 3 gold_block, 2 chest, 1 functionalstorage:copper_upgrade] | Cu 22, Au 30 | Cu 0.133, Au 0.609 (crafted: Cu 0.344, Au 1.88) | 64 | packing: copper_block, diamond_block, gold_block; diamond, diamond_block |
| functionalstorage:iron_downgrade | 3 | 8 iron_ingot; 1 functionalstorage:oak_2 [3.5 (planks tag), 1 chest] | Fe 8 | Fe 2 | 64 |  |
| laserio:card_holder | 3 | 4 iron_ingot; 2 copper_chest [8 copper_ingot, 1 chest]; 1 laserio:logic_chip | Cu 16, Fe 4 | Cu 1, Fe 1 | 1 |  |
| minecraft:copper_chest | 2 | 8 copper_ingot; 1 chest [8 oak_planks] | Cu 8 | Cu 2 | 64 |  |
| minecraft:crafter | 2 | 5 iron_ingot; 1 crafting_table [4 oak_planks]; 2 redstone; 1 dropper [7 cobblestone, 1 redstone] | Fe 5, Rs 3 | Fe 1.25, Rs 0.562 | 64 |  |
| pipez:filter_destination_tool | 4 | 5 iron_ingot; 2 redstone; 1 glass_pane [1 connectedglass:scratched_glass_pane]; 1 oak_button [1 oak_planks] | Fe 5, Rs 2 | Fe 1.25, Rs 0.5 | 1 |  |
| theurgy:mercury_catalyst | 2 | 4 iron_ingot; 1 theurgy:mercury_shard; 3 gold_ingot; 1 quartz_block [4 quartz] | Fe 4, Au 3 | Fe 1, Au 0.75 | 64 |  |
| theurgy:sal_ammoniac_tank | 2 | 4 iron_ingot; 3 copper_ingot; 2 stick [0.5 oak_planks] | Cu 3, Fe 4 | Cu 0.75, Fe 1 | 64 |  |
| theurgy:liquefaction_cauldron | 2 | 5 copper_ingot; 1 cauldron [7 iron_ingot]; 3 andesite | Cu 5, Fe 7 | Cu 1.25, Fe 0.438 | 64 |  |
| laserio:laser_connector_advanced | 2 | 2 ender_pearl; 1 diamond; 2 redstone; 1 laserio:laser_connector [1 glass, 2 redstone, 1 laserio:logic_chip, 3 iron_ingot]; 3 gold_ingot | Fe 3, Au 3, Rs 4 | Fe 0.188, Au 0.75, Rs 0.625 | 64 | diamond, ender_pearl |
| shrink:shrinking_device | 2 | 6 iron_ingot; 1 ender_pearl; 1 glass; 1 stone_button [1 stone] | Fe 6 | Fe 1.5 | 64? | ender_pearl |
| theurgy:mercury_capacitor | 3 | 4 gold_ingot; 2 theurgy:mercury_shard; 2 quartz_block [4 quartz]; 1 theurgy:mercury_catalyst [4 iron_ingot, 1 theurgy:mercury_shard, 3 gold_ingot, 1 quartz_block] | Fe 4, Au 7 | Fe 0.25, Au 1.19 | 64 |  |
| laserio:laser_node | 4 | 4 iron_ingot; 4 glass_pane [1 connectedglass:scratched_glass_pane]; 1 laserio:laser_connector [1 glass, 2 redstone, 1 laserio:logic_chip, 3 iron_ingot] | Fe 7, Rs 2 | Fe 1.19, Rs 0.125 | 64 |  |
| minecraft:blast_furnace | 2 | 5 iron_ingot; 1 furnace [8 cobblestone]; 3 smooth_stone | Fe 5 | Fe 1.25 | 64 |  |
| minecraft:hopper | 2 | 5 iron_ingot; 1 chest [8 oak_planks] | Fe 5 | Fe 1.25 | 64 |  |
| refinedstorage:external_storage | 3 | 1 refinedstorage:construction_core [1 refinedstorage:basic_processor, 1 glowstone_dust]; 3 refinedstorage:quartz_enriched_iron [0.75 iron_ingot, 0.25 quartz]; 1 refinedstorage:destruction_core [1 refinedstorage:basic_processor, 1 quartz]; 2 copper_chest [8 copper_ingot, 1 chest]; 1 refinedstorage:cable [0.5 refinedstorage:quartz_enriched_copper, 0.1667 glass, 0.0833 redstone]; 1 refinedstorage:improved_processor | Cu 16.4, Fe 2.25, Rs 0.0833 | Cu 1.01, Fe 0.141, Rs 0.00521 | 64 |  |
| modularrouters:activator_module | 4 | 4 redstone; 1 lever [1 stick, 1 cobblestone]; 2 dispenser [7 cobblestone, 1 bow, 1 redstone]; 1 modularrouters:blank_module [0.1667 redstone, 0.5 paper, 0.5 gold_nugget]; 1 quartz | Au 0.0556, Rs 6.17 | Au 0.00347, Rs 1.14 | 64 |  |
| functionalstorage:collector_upgrade | 3 | 4 andesite; 2 hopper [5 iron_ingot, 1 chest]; 2 redstone; 1 functionalstorage:oak_2 [3.5 (planks tag), 1 chest] | Fe 10, Rs 2 | Fe 0.625, Rs 0.5 | 64 |  |
| refinedstorage:64k_storage_disk | 6 | 2 glass; 3 redstone; 1 refinedstorage:64k_storage_part [4 refinedstorage:advanced_processor, 1 refinedstorage:quartz_enriched_iron, 3 refinedstorage:16k_storage_part, 1 redstone]; 3 refinedstorage:quartz_enriched_iron [0.75 iron_ingot, 0.25 quartz] | Fe 32.2, Rs 43 | Fe 0.173, Rs 0.921 | 1 |  |
| tiab:time_in_a_bottle | 2 | 3 gold_ingot; 2 diamond; 1 clock [4 gold_ingot, 1 redstone]; 2 lapis_lazuli; 1 glass_bottle [1 glass] | Au 7, Rs 1 | Au 1, Rs 0.0625 | 64? | diamond |
| refinedstorage:16k_storage_disk | 5 | 2 glass; 3 redstone; 1 refinedstorage:16k_storage_part [4 refinedstorage:improved_processor, 1 refinedstorage:quartz_enriched_iron, 3 refinedstorage:4k_storage_part, 1 redstone]; 3 refinedstorage:quartz_enriched_iron [0.75 iron_ingot, 0.25 quartz] | Fe 12, Rs 16 | Fe 0.168, Rs 0.895 | 1 |  |
| refinedstorage:4096b_fluid_storage_disk | 6 | 2 glass; 3 redstone; 1 refinedstorage:4096b_fluid_storage_part [4 refinedstorage:advanced_processor, 1 refinedstorage:quartz_enriched_iron, 3 refinedstorage:1024b_fluid_storage_part, 1 bucket]; 3 refinedstorage:quartz_enriched_iron [0.75 iron_ingot, 0.25 quartz] | Fe 152, Rs 3 | Fe 0.301, Rs 0.75 | 1 |  |
| modularrouters:bulk_item_filter | 3 | 4 iron_ingot; 2 glass; 2 modularrouters:blank_module [0.1667 redstone, 0.5 paper, 0.5 gold_nugget]; 1 diamond | Fe 4, Au 0.111, Rs 0.333 | Fe 1, Au 0.00694, Rs 0.0208 | 64 | diamond |
| refinedstorage:1024b_fluid_storage_disk | 5 | 2 glass; 3 redstone; 1 refinedstorage:1024b_fluid_storage_part [4 refinedstorage:improved_processor, 1 refinedstorage:quartz_enriched_iron, 3 refinedstorage:256b_fluid_storage_part, 1 bucket]; 3 refinedstorage:quartz_enriched_iron [0.75 iron_ingot, 0.25 quartz] | Fe 51, Rs 3 | Fe 0.276, Rs 0.75 | 1 |  |
| refinedstorage:4k_storage_disk | 4 | 2 glass; 3 redstone; 1 refinedstorage:4k_storage_part [4 refinedstorage:basic_processor, 1 refinedstorage:quartz_enriched_iron, 3 refinedstorage:1k_storage_part, 1 redstone]; 3 refinedstorage:quartz_enriched_iron [0.75 iron_ingot, 0.25 quartz] | Fe 5.25, Rs 7 | Fe 0.161, Rs 0.859 | 1 |  |
| refinedstorage:security_manager | 5 | 4 refinedstorage:quartz_enriched_iron [0.75 iron_ingot, 0.25 quartz]; 1 copper_chest [8 copper_ingot, 1 chest]; 2 refinedstorage:security_card [6 refinedstorage:quartz_enriched_iron, 2 refinedstorage:network_card, 1 refinedstorage:advanced_processor]; 1 refinedstorage:machine_casing [8 refinedstorage:quartz_enriched_iron, 1 andesite]; 1 refinedstorage:fallback_security_card [1 refinedstorage:security_card, 1 paper] | Cu 8, Fe 49.5 | Cu 0.5, Fe 0.519 | 64 |  |
| laserio:card_cloner | 2 | 4 iron_ingot; 2 paper [1 sugar_cane]; 1 laserio:logic_chip | Fe 4 | Fe 1 | 1 |  |
| minecraft:redstone_lamp | 2 | 4 redstone; 1 glowstone [4 glowstone_dust] | Rs 4 | Rs 1 | 64 |  |
| minecraft:target | 2 | 4 redstone; 1 hay_block [9 wheat] | Rs 4 | Rs 1 | 64 | packing: hay_block |
| neovitae:guide_book | 3 | 4 redstone; 1 book [3 paper, 1 leather] | Rs 4 | Rs 1 | 1 |  |
| refinedstorage:256b_fluid_storage_disk | 4 | 2 glass; 3 redstone; 1 refinedstorage:256b_fluid_storage_part [4 refinedstorage:basic_processor, 1 refinedstorage:quartz_enriched_iron, 3 refinedstorage:64b_fluid_storage_part, 1 bucket]; 3 refinedstorage:quartz_enriched_iron [0.75 iron_ingot, 0.25 quartz] | Fe 17.2, Rs 3 | Fe 0.243, Rs 0.75 | 1 |  |
| refinedstorage:1k_storage_disk | 3 | 2 glass; 3 redstone; 1 refinedstorage:1k_storage_part [4 ftbmaterials:silicon_gem, 1 refinedstorage:quartz_enriched_iron, 3 glass, 1 redstone]; 3 refinedstorage:quartz_enriched_iron [0.75 iron_ingot, 0.25 quartz] | Fe 3, Rs 4 | Fe 0.152, Rs 0.812 | 1 |  |
| refinedstorage:64b_fluid_storage_disk | 3 | 2 glass; 3 redstone; 1 refinedstorage:64b_fluid_storage_part [4 ftbmaterials:silicon_gem, 1 refinedstorage:quartz_enriched_iron, 3 glass, 1 bucket]; 3 refinedstorage:quartz_enriched_iron [0.75 iron_ingot, 0.25 quartz] | Fe 6, Rs 3 | Fe 0.199, Rs 0.75 | 1 |  |
| refinedstorage:pattern | 2 | 3 glass; 3 redstone; 3 refinedstorage:quartz_enriched_iron [0.75 iron_ingot, 0.25 quartz] | Fe 2.25, Rs 3 | Fe 0.141, Rs 0.75 | 64 |  |
| refinedstorage:storage_housing | 2 | 2 glass; 3 redstone; 3 refinedstorage:quartz_enriched_iron [0.75 iron_ingot, 0.25 quartz] | Fe 2.25, Rs 3 | Fe 0.141, Rs 0.75 | 64 |  |
| refinedstorage:disk_drive | 3 | 6 refinedstorage:quartz_enriched_iron [0.75 iron_ingot, 0.25 quartz]; 1 copper_chest [8 copper_ingot, 1 chest]; 1 refinedstorage:machine_casing [8 refinedstorage:quartz_enriched_iron, 1 andesite]; 1 refinedstorage:advanced_processor | Cu 8, Fe 10.5 | Cu 0.5, Fe 0.375 | 64 |  |
| refinedstorage:regulator_upgrade | 4 | 5 refinedstorage:quartz_enriched_iron [0.75 iron_ingot, 0.25 quartz]; 1 comparator [3 redstone_torch, 1 quartz, 3 stone]; 2 redstone; 1 refinedstorage:upgrade [6 refinedstorage:quartz_enriched_iron, 2 glass, 1 refinedstorage:improved_processor] | Fe 8.25, Rs 5 | Fe 0.305, Rs 0.547 | 64 |  |
| refinedstorage:64k_storage_block | 6 | 6 refinedstorage:quartz_enriched_iron [0.75 iron_ingot, 0.25 quartz]; 1 refinedstorage:64k_storage_part [4 refinedstorage:advanced_processor, 1 refinedstorage:quartz_enriched_iron, 3 refinedstorage:16k_storage_part, 1 redstone]; 1 refinedstorage:machine_casing [8 refinedstorage:quartz_enriched_iron, 1 andesite]; 1 redstone | Fe 40.5, Rs 41 | Fe 0.407, Rs 0.421 | 1 |  |
| refinedstorage:64k_storage_part | 5 | 4 refinedstorage:advanced_processor; 1 refinedstorage:quartz_enriched_iron [0.75 iron_ingot, 0.25 quartz]; 3 refinedstorage:16k_storage_part [4 refinedstorage:improved_processor, 1 refinedstorage:quartz_enriched_iron, 3 refinedstorage:4k_storage_part, 1 redstone]; 1 redstone | Fe 30, Rs 40 | Fe 0.128, Rs 0.684 | 64 |  |
| refinedstorage:16k_storage_block | 5 | 6 refinedstorage:quartz_enriched_iron [0.75 iron_ingot, 0.25 quartz]; 1 refinedstorage:16k_storage_part [4 refinedstorage:improved_processor, 1 refinedstorage:quartz_enriched_iron, 3 refinedstorage:4k_storage_part, 1 redstone]; 1 refinedstorage:machine_casing [8 refinedstorage:quartz_enriched_iron, 1 andesite]; 1 redstone | Fe 20.2, Rs 14 | Fe 0.402, Rs 0.395 | 1 |  |
| refinedstorage:interface | 4 | 4 refinedstorage:quartz_enriched_iron [0.75 iron_ingot, 0.25 quartz]; 1 refinedstorage:importer [1 refinedstorage:cable, 1 refinedstorage:destruction_core, 1 refinedstorage:improved_processor]; 2 redstone; 1 refinedstorage:machine_casing [8 refinedstorage:quartz_enriched_iron, 1 andesite]; 1 refinedstorage:exporter [1 refinedstorage:cable, 1 refinedstorage:construction_core, 1 refinedstorage:improved_processor] | Cu 0.75, Fe 9, Rs 2.17 | Cu 0.00293, Fe 0.281, Rs 0.503 | 64 |  |
| refinedstorage:4096b_fluid_storage_block | 6 | 6 refinedstorage:quartz_enriched_iron [0.75 iron_ingot, 0.25 quartz]; 1 refinedstorage:4096b_fluid_storage_part [4 refinedstorage:advanced_processor, 1 refinedstorage:quartz_enriched_iron, 3 refinedstorage:1024b_fluid_storage_part, 1 bucket]; 1 refinedstorage:machine_casing [8 refinedstorage:quartz_enriched_iron, 1 andesite]; 1 redstone | Fe 160, Rs 1 | Fe 0.535, Rs 0.25 | 1 |  |
| neovitae:sanguine_crafter | 3 | 7 neovitae:sanguine_casing [1 neovitae:bloodstone, 0.25 diamond, 0.75 iron_ingot, 0.25 neovitae:tabula_animata]; 1 neovitae:tabula_spiritus; 1 crafter [5 iron_ingot, 1 crafting_table, 2 redstone, 1 dropper] | Fe 10.2, Rs 3 | Fe 0.641, Rs 0.141 | 64 | diamond, neovitae:essentia_vitae_bucket |
| laserio:card_fluid | 2 | 2 redstone; 1 bucket [3 iron_ingot]; 2 quartz; 1 laserio:logic_chip; 3 gold_nugget | Fe 3, Au 0.333, Rs 2 | Fe 0.188, Au 0.0833, Rs 0.5 | 64 |  |
| refinedstorage:1024b_fluid_storage_block | 5 | 6 refinedstorage:quartz_enriched_iron [0.75 iron_ingot, 0.25 quartz]; 1 refinedstorage:1024b_fluid_storage_part [4 refinedstorage:improved_processor, 1 refinedstorage:quartz_enriched_iron, 3 refinedstorage:256b_fluid_storage_part, 1 bucket]; 1 refinedstorage:machine_casing [8 refinedstorage:quartz_enriched_iron, 1 andesite]; 1 redstone | Fe 59.2, Rs 1 | Fe 0.51, Rs 0.25 | 1 |  |
| refinedstorage:4k_storage_block | 4 | 6 refinedstorage:quartz_enriched_iron [0.75 iron_ingot, 0.25 quartz]; 1 refinedstorage:4k_storage_part [4 refinedstorage:basic_processor, 1 refinedstorage:quartz_enriched_iron, 3 refinedstorage:1k_storage_part, 1 redstone]; 1 refinedstorage:machine_casing [8 refinedstorage:quartz_enriched_iron, 1 andesite]; 1 redstone | Fe 13.5, Rs 5 | Fe 0.396, Rs 0.359 | 1 |  |
| neovitae:ara_vitae | 2 | 4 deepslate; 1 furnace [8 cobblestone]; 3 gold_ingot | Au 3 | Au 0.75 | 64 |  |
| theurgy:sal_ammoniac_accumulator | 2 | 3 andesite; 3 iron_ingot; 2 stick [0.5 oak_planks] | Fe 3 | Fe 0.75 | 64 |  |
| refinedstorage:256b_fluid_storage_block | 4 | 6 refinedstorage:quartz_enriched_iron [0.75 iron_ingot, 0.25 quartz]; 1 refinedstorage:256b_fluid_storage_part [4 refinedstorage:basic_processor, 1 refinedstorage:quartz_enriched_iron, 3 refinedstorage:64b_fluid_storage_part, 1 bucket]; 1 refinedstorage:machine_casing [8 refinedstorage:quartz_enriched_iron, 1 andesite]; 1 redstone | Fe 25.5, Rs 1 | Fe 0.478, Rs 0.25 | 1 |  |
| refinedstorage:disk_interface | 3 | 4 refinedstorage:quartz_enriched_iron [0.75 iron_ingot, 0.25 quartz]; 2 refinedstorage:storage_housing [2 glass, 3 redstone, 3 refinedstorage:quartz_enriched_iron]; 1 refinedstorage:construction_core [1 refinedstorage:basic_processor, 1 glowstone_dust]; 1 refinedstorage:machine_casing [8 refinedstorage:quartz_enriched_iron, 1 andesite]; 1 refinedstorage:destruction_core [1 refinedstorage:basic_processor, 1 quartz] | Fe 13.5, Rs 6 | Fe 0.352, Rs 0.375 | 64 |  |
| refinedstorage:1k_storage_block | 3 | 6 refinedstorage:quartz_enriched_iron [0.75 iron_ingot, 0.25 quartz]; 1 refinedstorage:1k_storage_part [4 ftbmaterials:silicon_gem, 1 refinedstorage:quartz_enriched_iron, 3 glass, 1 redstone]; 1 refinedstorage:machine_casing [8 refinedstorage:quartz_enriched_iron, 1 andesite]; 1 redstone | Fe 11.2, Rs 2 | Fe 0.387, Rs 0.312 | 1 |  |
| modularrouters:energy_distributor_module | 6 | 1 modularrouters:energy_output_module [1 redstone_block, 2 gold_ingot, 1 modularrouters:blank_module, 1 quartz]; 1 modularrouters:distributor_module [3 modularrouters:sender_module_2, 1 modularrouters:blank_module] | Au 2.28, Rs 9.83 | Au 0.127, Rs 0.146 (crafted: Au 0.127, Rs 0.568) | 64 | packing: redstone_block; ender_pearl |
| refinedstorage:16k_storage_part | 4 | 4 refinedstorage:improved_processor; 1 refinedstorage:quartz_enriched_iron [0.75 iron_ingot, 0.25 quartz]; 3 refinedstorage:4k_storage_part [4 refinedstorage:basic_processor, 1 refinedstorage:quartz_enriched_iron, 3 refinedstorage:1k_storage_part, 1 redstone]; 1 redstone | Fe 9.75, Rs 13 | Fe 0.108, Rs 0.578 | 64 |  |
| refinedstorage:64b_fluid_storage_block | 3 | 6 refinedstorage:quartz_enriched_iron [0.75 iron_ingot, 0.25 quartz]; 1 refinedstorage:64b_fluid_storage_part [4 ftbmaterials:silicon_gem, 1 refinedstorage:quartz_enriched_iron, 3 glass, 1 bucket]; 1 refinedstorage:machine_casing [8 refinedstorage:quartz_enriched_iron, 1 andesite]; 1 redstone | Fe 14.2, Rs 1 | Fe 0.434, Rs 0.25 | 1 |  |
| refinedstorage:4096b_fluid_storage_part | 5 | 4 refinedstorage:advanced_processor; 1 refinedstorage:quartz_enriched_iron [0.75 iron_ingot, 0.25 quartz]; 3 refinedstorage:1024b_fluid_storage_part [4 refinedstorage:improved_processor, 1 refinedstorage:quartz_enriched_iron, 3 refinedstorage:256b_fluid_storage_part, 1 bucket]; 1 bucket [3 iron_ingot] | Fe 150 | Fe 0.641 | 64 |  |
| functionalstorage:compacting_framed_drawer | 3 | 5 iron_nugget; 2 piston [3 oak_planks, 4 cobblestone, 1 iron_ingot, 1 redstone]; 1 functionalstorage:oak_2 [3.5 (planks tag), 1 chest]; 1 iron_ingot | Fe 3.56, Rs 2 | Fe 0.514, Rs 0.125 | 64 |  |
| modularrouters:redstone_augment | 4 | 2 redstone_torch [1 redstone, 1 stick]; 2 redstone; 1 modularrouters:augment_core [0.25 modularrouters:blank_module, 0.25 modularrouters:blank_upgrade] | Au 0.0347, Rs 4.04 | Au 0.000542, Rs 0.626 | 64 |  |
| minecraft:hopper_minecart | 3 | 1 hopper [5 iron_ingot, 1 chest]; 1 minecart [5 iron_ingot] | Fe 10 | Fe 0.625 | 1 |  |
| refinedstorage:autocrafting_monitor | 3 | 2 refinedstorage:improved_processor; 2 refinedstorage:pattern [3 glass, 3 redstone, 3 refinedstorage:quartz_enriched_iron]; 3 glass; 1 refinedstorage:quartz_enriched_iron [0.75 iron_ingot, 0.25 quartz]; 1 refinedstorage:machine_casing [8 refinedstorage:quartz_enriched_iron, 1 andesite] | Fe 11.2, Rs 6 | Fe 0.211, Rs 0.375 | 64 |  |
| functionalstorage:puller_upgrade | 3 | 6 andesite; 1 hopper [5 iron_ingot, 1 chest]; 1 functionalstorage:oak_2 [3.5 (planks tag), 1 chest]; 1 redstone | Fe 5, Rs 1 | Fe 0.312, Rs 0.25 | 64 |  |
| functionalstorage:pusher_upgrade | 3 | 6 andesite; 1 redstone; 1 functionalstorage:oak_2 [3.5 (planks tag), 1 chest]; 1 hopper [5 iron_ingot, 1 chest] | Fe 5, Rs 1 | Fe 0.312, Rs 0.25 | 64 |  |
| neovitae:rune_capacity | 2 | 4 deepslate; 3 bucket [3 iron_ingot]; 1 neovitae:rune_blank [7 deepslate, 1 neovitae:tabula_rasa, 1 (planks tag)]; 1 neovitae:tabula_animata | Fe 9 | Fe 0.562 | 64 |  |
| functionalstorage:obsidian_upgrade | 4 | 4 functionalstorage:dripping_upgrade [6 andesite, 1 pointed_dripstone, 1 cauldron, 1 lava_bucket]; 1 functionalstorage:water_generator_upgrade [6 andesite, 2 water_bucket, 1 bucket] | Fe 85 | Fe 0.559 | 64 | lava_bucket, water_bucket |
| refinedstorage:1024b_fluid_storage_part | 4 | 4 refinedstorage:improved_processor; 1 refinedstorage:quartz_enriched_iron [0.75 iron_ingot, 0.25 quartz]; 3 refinedstorage:256b_fluid_storage_part [4 refinedstorage:basic_processor, 1 refinedstorage:quartz_enriched_iron, 3 refinedstorage:64b_fluid_storage_part, 1 bucket]; 1 bucket [3 iron_ingot] | Fe 48.8 | Fe 0.542 | 64 |  |
| functionalstorage:framed_simple_compacting_drawer | 3 | 6 iron_nugget; 1 functionalstorage:oak_2 [3.5 (planks tag), 1 chest]; 1 piston [3 oak_planks, 4 cobblestone, 1 iron_ingot, 1 redstone]; 1 iron_ingot | Fe 2.67, Rs 1 | Fe 0.479, Rs 0.0625 | 64 |  |
| refinedstorage:4k_storage_part | 3 | 4 refinedstorage:basic_processor; 1 refinedstorage:quartz_enriched_iron [0.75 iron_ingot, 0.25 quartz]; 3 refinedstorage:1k_storage_part [4 ftbmaterials:silicon_gem, 1 refinedstorage:quartz_enriched_iron, 3 glass, 1 redstone]; 1 redstone | Fe 3, Rs 4 | Fe 0.082, Rs 0.438 | 64 |  |
| pipez:universal_pipe | 3 | 0.3333 pipez:item_pipe [0.375 iron_ingot, 0.125 dropper, 0.0625 redstone]; 0.3333 pipez:energy_pipe [0.375 iron_ingot, 0.125 redstone_block, 0.0625 redstone]; 0.3333 pipez:fluid_pipe [0.375 iron_ingot, 0.125 bucket, 0.0625 redstone]; 0.3333 iron_ingot; 0.1667 redstone_block [9 redstone] | Fe 0.833, Rs 1.98 | Fe 0.109, Rs 0.104 (crafted: Fe 0.109, Rs 0.403) | 64 | packing: redstone_block |
| functionalstorage:compacting_drawer | 3 | 5 andesite; 2 piston [3 oak_planks, 4 cobblestone, 1 iron_ingot, 1 redstone]; 1 functionalstorage:oak_2 [3.5 (planks tag), 1 chest]; 1 iron_ingot | Fe 3, Rs 2 | Fe 0.375, Rs 0.125 | 64 |  |
| functionalstorage:configuration_tool | 3 | 5 paper [1 sugar_cane]; 2 gold_ingot; 1 functionalstorage:oak_2 [3.5 (planks tag), 1 chest]; 1 emerald | Au 2 | Au 0.5 | 1 | emerald |
| functionalstorage:linking_tool | 3 | 5 paper [1 sugar_cane]; 2 gold_ingot; 1 functionalstorage:oak_2 [3.5 (planks tag), 1 chest]; 1 diamond | Au 2 | Au 0.5 | 1 | diamond |
| neovitae:rune_2_efficiency | 3 | 6 netherite_scrap; 2 neovitae:rune_efficiency [4 redstone, 1 neovitae:tabula_aetherea, 2 neovitae:hellforged_parts, 1 neovitae:rune_blank, 1 (planks tag)]; 1 cobblestone | Rs 8 | Rs 0.5 | 64 | netherite_scrap, neovitae:hellforged_parts |
| refinedstorage:raw_basic_processor | 2 | 1 refinedstorage:processor_binding [0.25 string, 0.125 slime_ball]; 1 iron_ingot; 1 ftbmaterials:silicon_gem; 1 redstone | Fe 1, Rs 1 | Fe 0.25, Rs 0.25 | 64 | slime_ball |
| refinedstorage:raw_improved_processor | 2 | 1 refinedstorage:processor_binding [0.25 string, 0.125 slime_ball]; 1 gold_ingot; 1 ftbmaterials:silicon_gem; 1 redstone | Au 1, Rs 1 | Au 0.25, Rs 0.25 | 64 | slime_ball |
| theurgy:caloric_flux_emitter | 3 | 1 campfire [3 stick, 1 coal, 3 dark_oak_log]; 2 gold_ingot; 1 theurgy:mercury_shard; 3 andesite | Au 2 | Au 0.5 | 64 |  |
| theurgy:digestion_vat | 2 | 1 theurgy:sal_ammoniac_crystal [1 theurgy:sal_ammoniac_bucket]; 2 gold_ingot; 1 decorated_pot [4 brick]; 3 red_sandstone [4 red_sand] | Au 2 | Au 0.5 | 64 | theurgy:sal_ammoniac_bucket |
| theurgy:sulfuric_flux_emitter | 2 | 1 theurgy:sal_ammoniac_crystal [1 theurgy:sal_ammoniac_bucket]; 2 gold_ingot; 1 theurgy:alchemical_sulfur_rotten_flesh; 3 andesite | Au 2 | Au 0.5 | 64 | theurgy:sal_ammoniac_bucket |

### What is worth knowing from the table

- **Copper**: Copper Chest (8 Cu + chest, level 2 now that the chest counts; Cu 2.0 per item, no packing) is the best clean vanilla copper carrier. Theurgy's Fermentation Vat (Cu 15, 3.75 crafted) and Functional Storage's Copper Upgrade (Cu 22, 5.5 crafted) are richer through copper blocks; the Vat also returns 1 Theurgy rotten-flesh sulfur. LaserIO's Card Holder and Refined Storage's External Storage carry copper only through copper chests and are stack 1 / heavy with processors. Theurgy's Liquefaction Cauldron (Cu 5, Fe 7 via a cauldron) is a clean 1.25 Cu.
- **Iron**: Anvil (3 iron blocks + 4 iron; Fe 2.69, **7.75 crafted**, stack 64) is the richest clean iron block. Then Accumulator/Reactor (suite), Functional Storage's Iron Downgrade (Fe 2.0), Boiler 1.94, Gear Bench 1.63, Shrinking Device 1.5 (stack unconfirmed, returns an ender pearl), Blast Furnace and Hopper 1.25, Crafter 1.25 + Rs 0.56.
- **Gold**: Assembler 2 (1.5), Functional Storage's Gold Upgrade (2.44, **7.5 crafted** through 3 gold blocks; level 4, contains a Copper Upgrade), Pipez Improved Upgrade (Au 1, Rs 1.06, clean, level 2), Time in a Bottle (Au 1; stack unconfirmed), Theurgy's Mercury Catalyst/Capacitor (0.75/1.19, return Theurgy mercury shards), LaserIO Advanced Connector (0.75), Neo Vitae's Ara Vitae (0.75), Powered Rail (0.25).
- **Redstone**: Pipez Advanced/Ultimate Upgrades (Rs 9.3/11.3 crafted, but diamonds / netherite), Functional Storage's Redstone Upgrade (2.2, 5.6 crafted), Assembler 2 (2.85), Redstone Lamp and Target (1.0).
- **Byproducts to avoid**: Pipez Advanced (4 diamonds) and Ultimate (4 netherite ingots + diamonds); Functional Storage's Diamond and Netherite upgrades; Gearworks' Nuclear Upgrade (2 netherite, nether star), Reactor (2 netherite blocks), Assembler 3 (4 diamonds); Shrinking Device and Gearworks' Reach Upgrade (ender pearls); Void Crucible and Mutation Chamber (amethyst, crying obsidian); Theurgy machines (mercury shards, sulfur).
- **Below the cut-off** (all < 0.5 per item): Refined Storage's cables, importers/exporters, grids, processors (raw processors ≤ 0.5), most Modular Routers modules and augments (redstone and gold nuggets in traces), Pipez pipes, LaserIO filters/cards, Neo Vitae runes and casings, Functional Storage drawers and fluid drawers, vanilla rails, minecarts (stack 1), maps, sticky piston, tripwire hook, trapped chest. All in `advanced-mix.json`.

### Decorative and other families (level 2+, summarised)

- **No Cu/Fe/Au/Rs at all** (582 items): vanilla glass panes and stained glass (33), slabs/stairs/walls of crafted blocks (68), fences and gates (25), banners (22), shulker boxes (17), beds, bundles, candles, carpets, dyed wool (16 each), concrete powder, terracotta (14 each), signs, boats, bricks, books, foods; Connected Glass (85); Neo Vitae (52, mostly blood-tech and decorative blocks); Functional Storage wooden drawers (32); OpenBlocks Elevators (16, wool + ender pearl); FTB Quests screens (6); Construction Sticks templates; Eccentric Tome; FTB Filter System; the Gateways pearl. None carries metal.
- **Copper decoration** (packing, unwaxed): Cut Copper and Copper Grate (Cu 9; 0.56 recycled, 2.25 crafted), Cut Copper Stairs/Slab, Chiseled Copper, Copper Bulb; Copper Lantern (8 copper nuggets + torch, about 0.22 Cu ingot). The exposed/weathered/oxidised variants bottom out at their oxidised copper block (no recipe), so they carry no metal. Hanging signs (iron chain, 0.03 Fe, stack 16). Lanterns (iron nuggets). Armor-trim templates: the Bolt template (duplication recipe: diamonds + copper block) returns about 7 diamonds per item in full; out.

## 3. Mix M5

**M4** (2 Steam Turbine, Transformer, Steam Engine, Accumulator, Harvester 2, Assembler 2, Splitter, Pump, Outlet, Transmission Pole, Kinetic Upgrade, Metabolic Upgrade) **+ Void Crucible, Hopper, Comparator + 2 Copper Chests, Anvil, Blast Furnace, Pipez Improved Upgrade, Theurgy Liquefaction Cauldron**: 20 kinds, 22 items, all level 2+, all stack 64, no waxed item, no tool.

| Count | Item | Lvl | Tier 1 per item, recycled: Cu / Fe / Au / Rs | Blocks crafted: Cu / Fe / Au / Rs | Stack |
|---|---|---|---|---|---|
| 2 | wireworks:steam_turbine | 2 | 2.25 / 0.688 / 0 / 0.125 | 9 / 2.38 / 0 / 0.125 | 64 |
| 1 | wireworks:transformer | 2 | 0.75 / 1.56 / 0 / 0 | 0.75 / 3.25 / 0 / 0 | 64 |
| 1 | wireworks:steam_engine | 2 | 0.5 / 1.88 / 0 / 0.0625 | 0.5 / 3.56 / 0 / 0.0625 | 64 |
| 1 | wireworks:accumulator | 2 | 0.5 / 2.5 / 0 / 0.25 | 0.5 / 9.25 / 0 / 0.25 | 64 |
| 1 | fieldworks:harvester_2 | 4 | 1 / 0.734 / 0 / 0.5 | 1 / 0.734 / 0 / 0.5 | 64 |
| 1 | craftworks:assembler_2 | 4 | 0 / 0.641 / 1.5 / 2.85 | 0 / 0.641 / 1.5 / 2.85 | 64 |
| 1 | beltworks:splitter | 4 | 0.273 / 0 / 0 / 0.0469 | 0.273 / 0 / 0 / 0.0469 | 64 |
| 1 | pipeworks:pump | 2 | 0.188 / 0.25 / 0 / 0.0625 | 0.188 / 0.25 / 0 / 0.0625 | 64 |
| 1 | pipeworks:outlet | 4 | 0.25 / 0 / 0 / 0.0625 | 0.25 / 0 / 0 / 0.0625 | 64 |
| 1 | wireworks:transmission_pole | 2 | 0.5 / 0.523 / 0 / 0 | 0.5 / 0.523 / 0 / 0 | 64 |
| 1 | gearworks:kinetic_upgrade | 2 | 1 / 0.0625 / 0 / 0.312 | 1 / 0.0625 / 0 / 0.312 | 64 |
| 1 | gearworks:metabolic_upgrade | 2 | 1 / 0 / 0.0556 / 0.25 | 1 / 0 / 0.0556 / 0.25 | 64 |
| 1 | voidworks:void_crucible | 2 | 0 / 0.438 / 0 / 0 | 0 / 0.438 / 0 / 0 | 64 |
| 1 | minecraft:hopper | 2 | 0 / 1.25 / 0 / 0 | 0 / 1.25 / 0 / 0 | 64 |
| 1 | minecraft:comparator | 3 | 0 / 0 / 0 / 0.188 | 0 / 0 / 0 / 0.188 | 64 |
| 2 | minecraft:copper_chest | 2 | 2 / 0 / 0 / 0 | 2 / 0 / 0 / 0 | 64 |
| 1 | minecraft:anvil | 2 | 0 / 2.69 / 0 / 0 | 0 / 7.75 / 0 / 0 | 64 |
| 1 | minecraft:blast_furnace | 2 | 0 / 1.25 / 0 / 0 | 0 / 1.25 / 0 / 0 | 64 |
| 1 | pipez:improved_upgrade | 2 | 0 / 0.278 / 1 / 1.06 | 0 / 0.278 / 1 / 1.06 | 64 |
| 1 | theurgy:liquefaction_cauldron | 2 | 1.25 / 0.438 / 0 / 0 | 1.25 / 0.438 / 0 / 0 | 64 |

Why these additions: the Copper Chest is the best clean copper per item (2.0) in vanilla; the Anvil is the best iron block (7.75 crafted); Blast Furnace and Hopper add iron and are the decided machines; the Pipez Improved Upgrade adds whole gold and redstone with no packing and no byproduct; the Liquefaction Cauldron adds 1.25 Cu with only andesite as byproduct. The Comparator stays for its redstone and for the Recycler's parts.

### Per essence, tier 1 (decompose 0.25 × the whole mix)

Routes per Galvanic Essence ([theurgy-chain.md](theurgy-chain.md)): **A** 16 Cu, 4 Fe, 2 Au ingots (+1 Rs once); **B** 79 copper ingots, 1 sulfur each (+1 Fe, 1 raw gold, 1 Rs once); **C** 16.4 Fe ingots (+1 Cu, 1 raw gold, 1 Rs once). A = max(16/Cu, 4/Fe, 2/Au), B = 79/Cu, C = 16.4/Fe, seed = 1/Rs. Each cell: blocks crafted / blocks recycled.

| Mix | Kinds / items | Cu | Fe | Au | Rs | A (binding) | B | C | Rs seed |
|---|---|---|---|---|---|---|---|---|---|
| M4 (reference) | 12 / 13 | 5.99 / 2.62 | 5.76 / 2.38 | 0.39 | 1.16 | 5.1 (Au) / 6.1 (Cu) | 13.2 / 30.2 | 2.8 / 6.9 | 0.86 |
| **M5** | 20 / 22 | **7.30 / 3.93** | **8.61 / 3.97** | **0.64** | **1.47** | **3.1 (Au) / 4.1 (Cu)** | **10.8 / 20.1** | **1.9 / 4.1** | **0.68** |
| M5, 1 of each | 20 / 20 | 4.55 / 2.87 | 8.01 / 3.79 | 0.64 | 1.44 | 3.5 (Cu) / 5.6 (Cu) | 17.4 / 27.6 | 2.0 / 4.3 | 0.69 |
| M5, 4 Copper Chests | 20 / 24 | 8.30 / 4.93 | 8.61 / 3.97 | 0.64 | 1.47 | 3.1 (Au) / 3.2 (Cu) | 9.5 / 16.0 | 1.9 / 4.1 | 0.68 |
| M5 + FS Gold Upgrade | 21 / 23 | 7.65 / 4.06 | 8.61 / 3.97 | 2.51 / 1.25 | 1.47 | 2.1 (Cu) / 3.9 (Cu) | 10.3 / 19.5 | 1.9 / 4.1 | 0.68 |
| M5 + FS Copper Upgrade + Fermentation Vat | 22 / 24 | 9.62 / 4.98 | 8.61 / 3.97 | 0.64 | 1.47 | 3.1 (Au) / 3.2 (Cu) | 8.2 / 15.9 | 1.9 / 4.1 | 0.68 |

Gold: M5's 0.64 = Assembler 2 (0.375) + Pipez Improved (0.25) + Metabolic's 8 gold nuggets (0.014). Route A is gold-bound with blocks crafted; one Functional Storage Gold Upgrade fixes that (A 2.1) at the cost of a level-4 item built on gold and copper blocks.

**Byproducts per essence (tier 1, all the way)**: 0.77 cobblestone, 0.72 oak planks, 0.44 plastic, 0.38 amethyst shard, 0.23 stone, 0.19 smooth stone, 0.19 andesite, 0.13 crying obsidian, 0.08 quartz, traces of carrot, string, dried kelp. **No netherite, diamond, nether star or ender item.** The amethyst and crying obsidian come from the Void Crucible only.

**Notes**
- Bootstrap: the Recycler's parts are made from wood and stone only, so a team's first Recycler needs nothing from the mix.
- Usable as decomposed: Hopper, Blast Furnace, Anvil, Copper Chest and the suite machines. The Liquefaction Cauldron, Theurgy's first machine in the Chain, stays in: its recipes are locked until the energy challenge starts (ADR 0007, #214), so a decomposed one does no work before then.
- Route B now counts copper ingots (79 per Galvanic); raw copper is Shop-only and is not needed.
- The Anvil and the Wireworks blocks carry most of the iron through iron blocks; recycled rather than crafted, route C rises from 1.9 to 4.1.

## Caveats

- Data read from jars, not seen in EMI's recycling view: tag first-items, NeoForge recipe overrides, Connected Glass's pane recipes, FTB Materials' conditional recipes.
- Stack sizes: vanilla from knowledge of vanilla items; mods from `stacksTo`/`durability` calls in the item classes (javap). LaserIO's seven `stacksTo(1)` lambdas were not mapped item by item; Time in a Bottle and the Shrinking Device unconfirmed.
- Stonecut-as-refined and andesite/diorite/granite-as-raw are interpretations of "cut from raw" and "raw"; neither touches a mix member's metal.
