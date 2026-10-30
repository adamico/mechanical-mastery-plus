# The Advanced mix: intermediate-2 items for the energy challenge (#209)

Research for Advanced Mechanical Essence's mix. Advanced decomposes into the band intermediate 2, and the Recycler brings that down to the refined copper, iron, gold and redstone the energy challenge's Chain takes into Theurgy ([ADR 0006](../adr/0006-a-challenges-mods-take-only-raw-and-refined-items.md), [theurgy-chain.md](theurgy-chain.md)). `advanced-mix.json` holds the same content as data, with every candidate's full tree.

**Amended for packing.** The maintainer has since ruled that packing recipes are excluded: a candidate whose depth or yield relies on a storage block (9 ingots ↔ 1 block) or any other compress/uncompress pair is dropped from the mix, and blocks are not a band step. The Recycler's code does not change. The mix stays true intermediate 2, exactly two recycles to refined. This amendment marks the candidates that relied on blocks as excluded, adds four candidates a full rescan found, and recomputes the mixes. What the exclusion costs is in section 4. Gold from outside the challenge mods is in [gold-sources.md](gold-sources.md).

Sources, all primary:

- **Code**: `mod/src/main/java/io/github/_5thlayer/mechanicalmasteryreborn/recipe/RecyclingRules.java`, `Recycling.java` and `Yield.java`, `ServerConfig.java` (`recycler.yield.tier_1`, 0.25), and `defaultconfigs/mechanicalmasteryreborn-server.toml` (`assembling_types = ["craftworks:assembling"]`).
- **Recipes**: the vanilla 26.1.2 client jar (`~/.gradle/caches/neoformruntime/artifacts/minecraft_26.1.2_client.jar`), the NeoForge 26.1.2.109 universal jar from the CurseForge install (its `data/minecraft/recipe/` replaces vanilla's piston, hopper, bucket and others by id), and every jar in the instance's `mods/` folder. All were unzipped and read as data. Short prefixes below:
  - `V/` = `minecraft_26.1.2_client.jar!/data/minecraft/recipe/`
  - `NF/` = `neoforge-26.1.2.109-universal.jar!/data/minecraft/recipe/`
  - `PW/` = `pipeworks-26.1.2-0.4.1-local.jar!/data/pipeworks/recipe/`
  - `WW/` = `wireworks-26.1.2-0.6.7-local.jar!/data/wireworks/recipe/`
  - `BW/` = `beltworks-26.1.2-0.5.6-local.jar!/data/beltworks/recipe/`
  - `CW/` = `craftworks-26.1.2-0.11.0-local.jar!/data/craftworks/recipe/`
  - `GW/` = `gearworks-26.1.2-0.2.1-local.jar!/data/gearworks/recipe/`
  - `FW/` = `fieldworks-26.1.2-0.6.0-local.jar!/data/fieldworks/recipe/`
  - `RS/` = `refinedstorage-neoforge-3.2.1.jar!/data/refinedstorage/recipe/`
  - `MR/` = `modular-routers-26.1.2.2.jar!/data/modularrouters/recipe/`
  - `NV/` = `neovitae-26.1.2-1.1.49.jar!/data/neovitae/recipe/`
- **Pack scripts**: `kubejs/server_scripts/decomposing.js` only adds the decompose and the Recycler's parts. No pack script adds or replaces a recipe any candidate uses.

The trees were worked out by applying `RecyclingRules` to those recipe files with a script. Tag ingredients took their first item, in NeoForge's and vanilla's tag order.

## 1. How recycling works

1. A recycle runs one recipe that makes the item backwards: smelting first, then Craftworks' assembling, then crafting (blasting, smoking and campfire only when nothing else makes it). Uncompressing recipes never run backwards, an ore ingredient loses to any other, and ties go to the recipe id. A `mechanicalmasteryreborn:recycling` recipe overrides all of it (`RecyclingRules.recyclesInto`).
2. A tag ingredient returns its first item. A recipe that makes several returns each one's share: 3 copper make 3 Pipes, so a Pipe returns 1 copper (`Recycling.read`, `RecyclingRules.cost`).
3. Each decompose and each recycle returns its yield of the full amounts. Each leftover part is a chance of one more, so the average is exact (`Yield.roll`). A tier-1 core, and decomposing by hand, yields 0.25.
4. An intermediate-2 item reaches refined in two recycles, so one item returns 0.25 × 0.25 = **1/16** of the metal in its tree. The decompose's own quarter comes on top: one Advanced essence returns **1/64** of its mix's metal.
5. **The block shortcut.** A copper, iron or redstone block recycles into 9 at a quarter, but crafts into 9 at full yield (`V/copper_ingot.json`, `V/iron_ingot_from_iron_block.json`, `V/redstone.json`). An item built on blocks then returns 1/4 of its tree, not 1/16. The figures marked "craft" below count that. **Under the packing rule, every such item is now excluded**, so the "craft" figures are kept only to show what was lost.
6. **The wax shortcut.** A waxed copper item recycles to its unwaxed form and honeycomb, then to copper, so it passes at depth 2 with no block. But an axe scrapes the wax off a placed block for free: the waxed and unwaxed pairs are in vanilla's waxables map (`minecraft_26.1.2_client.jar!/net/minecraft/world/item/HoneycombItem.class`, which `AxeItem` reverses), so a player skips a recycle, as with the Waxed Block of Copper. It is not a packing recipe, so these items are flagged, not excluded, and left out of the mixes.

## 2. What Theurgy needs per Galvanic Essence

From `theurgy-chain.json` (`chains[].starts`) and theurgy-chain.md section 4. A liquefied ingot makes 1 sulfur and a raw ore 3 (`theurgy-…jar!/data/theurgy/recipe/liquefaction/alchemical_sulfur_copper_from_ingots_copper.json`, `…_from_raw_materials_copper.json`). Recycling delivers ingots, so route B is counted in ingots.

| Route | Per Galvanic | Once, to seed the arrays |
|---|---|---|
| A, Spagyric breakdown | 16 copper, 4 iron, 2 gold ingots | 1 redstone dust |
| B, Exaltation of copper | 79 copper ingots (79 Copper Sulfur; theurgy-chain counts 26.3 raw copper at 3 each) | 1 iron ingot, 1 raw gold, 1 redstone dust |
| C, Transmutation of iron | 16.4 iron ingots | 1 copper ingot, 1 raw gold, 1 redstone dust |

Redstone is never a per-Galvanic cost: one dust seeds the Galvanic array once. Gold matters only to route A.

## 3. Candidates

**Depth** is how many recycles an item's copper, iron, gold and redstone take to reach ingots, dust or nuggets. "Mixed 1/2" means some of it comes out at the first recycle. Stone, quartz, plastic and the like that come out earlier don't count against an item. **Passes** means a true intermediate 2.

**Passes** now also says **excluded, packing** for an item whose tree has a storage block, and **flagged, axe** for a waxed item. Metals are given as Cu, Fe, Au and Rs. "Full" is per item at full yield. "Tier 1" is per item after its recycles at a quarter each (the decompose not included).

| Item | Mod | Recycle tree (recipe) | Depth | Full | Tier 1 (craft) | Covers | Passes |
|---|---|---|---|---|---|---|---|
| Piston | vanilla | piston → 3 planks, 4 cobblestone, 1 iron, 1 redstone (`NF/piston.json`) | 1 | Fe 1, Rs 1 | Fe 0.25, Rs 0.25 | iron, redstone | **no** |
| Hopper | vanilla | hopper → 5 iron + chest (`NF/hopper.json`); chest → 8 planks (`V/chest.json`) | 1 | Fe 5 | Fe 1.25 | iron | **no** |
| Redstone Comparator | vanilla | comparator → 3 redstone torches, quartz, 3 stone (`V/comparator.json`); torch → redstone + stick (`NF/redstone_torch.json`) | 2 | Rs 3 | Rs 0.19 | redstone | **yes** |
| Bucket | vanilla | bucket → 3 iron (`NF/bucket.json`) | 1 | Fe 3 | Fe 0.75 | iron | **no** |
| Cut Copper | vanilla | cut copper → 1 copper block (`V/cut_copper.json`, 4 → 4); block → 9 copper (`V/copper_block.json`) | 2 | Cu 9 | Cu 0.56 (2.25) | copper | **excluded, packing** |
| Copper Grate | vanilla | grate → 1 copper block (`V/copper_grate.json`); block → 9 copper | 2 | Cu 9 | Cu 0.56 (2.25) | copper | **excluded, packing** |
| Waxed Block of Copper | vanilla | → copper block + honeycomb (`V/waxed_copper_block_from_honeycomb.json`); block → 9 copper. But it crafts straight to 9 copper (`V/copper_ingot_from_waxed_copper_block.json`) | 2 on paper | Cu 9 | Cu 0.56 (9, by crafting) | copper | **no**, refined in effect; excluded, packing |
| Sticky Piston | vanilla | → slime ball + piston (`V/sticky_piston.json`); piston → iron, redstone | 2 | Fe 1, Rs 1 | Fe 0.06, Rs 0.06 | iron, redstone | **yes** |
| Minecart with Hopper | vanilla | → hopper + minecart (`V/hopper_minecart.json`); hopper → 5 iron + chest; minecart → 5 iron (`NF/minecart.json`) | 2 | Fe 10 | Fe 0.63 | iron | **yes** |
| Empty Map | vanilla | → 8 paper + compass (`V/map.json`); compass → 4 iron + redstone (`NF/compass.json`); paper → sugar cane (`V/paper.json`) | 2 | Fe 4, Rs 1 | Fe 0.25, Rs 0.06 | iron, redstone | **yes** |
| Copper Bulb | vanilla | → ¾ copper block, ¼ blaze rod, ¼ redstone (`V/copper_bulb.json`, 3 + 1 + 1 → 4); block → 9 copper | mixed 1/2 | Cu 6.75, Rs 0.25 | Cu 0.42 (1.69), Rs 0.06 | copper, redstone | **no**; excluded, packing |
| Redstone Repeater | vanilla | → 2 torches, redstone, 3 stone (`V/repeater.json`); torch → redstone + stick | mixed 1/2 | Rs 3 | Rs 0.38 | redstone | **no** |
| Lightning Rod | vanilla | → 3 copper (`NF/lightning_rod.json`) | 1 | Cu 3 | Cu 0.75 | copper | **no** |
| Crafter | vanilla | → 5 iron, crafting table, 2 redstone, dropper (`NF/crafter.json`); dropper → 7 cobblestone + redstone (`NF/dropper.json`) | mixed 1/2 | Fe 5, Rs 3 | Fe 1.25, Rs 0.56 | iron, redstone | **no** |
| Copper Chest | vanilla | → 8 copper + chest (`NF/copper_chest.json`) | 1 | Cu 8 | Cu 2 | copper | **no** |
| Steam Turbine | Wireworks | → 4 copper blocks, 2 plastic, 2 pistons, 1 iron block (`WW/steam_turbine.json`); blocks → 9 ingots (`V/copper_block.json`, `V/iron_block.json`); piston → iron, redstone | 2 | Cu 36, Fe 11, Rs 2 | Cu 2.25 (9), Fe 0.69 (2.38), Rs 0.13 | copper, iron, redstone | **excluded, packing** |
| Pump | Pipeworks | → 3 pipes, piston, bucket (`PW/pump.json`); pipe → 1 copper (`PW/pipe.json`, 3 → 3); piston → iron, redstone; bucket → 3 iron | 2 | Cu 3, Fe 4, Rs 1 | Cu 0.19, Fe 0.25, Rs 0.06 | copper, iron, redstone | **yes** |
| Outlet | Pipeworks | → 4 pipes + dispenser (`PW/outlet.json`); pipe → copper; dispenser → 7 cobblestone, bow, redstone (`NF/dispenser.json`) | 2 | Cu 4, Rs 1 | Cu 0.25, Rs 0.06 | copper, redstone | **yes** |
| Railgun | Gearworks | → lightning rod, 2 copper blocks, redstone block, netherite ingot (`GW/railgun.json`); rod → 3 copper; blocks → 9 | 2 | Cu 21, Rs 9 | Cu 1.31 (4.69), Rs 0.56 (2.25) | copper, redstone | **excluded, packing** (blocks and netherite) |
| Steam Engine | Wireworks | → 5 iron, piston, 2 copper, iron block (`WW/steam_engine.json`) | mixed 1/2 | Cu 2, Fe 15, Rs 1 | Cu 0.5, Fe 1.88 (3.56), Rs 0.06 | copper, iron, redstone | **no**; excluded, packing |
| Solar Panel | Wireworks | → 2 glass, daylight detector, 2 copper, plastic, iron block (`WW/solar_panel.json`) | mixed 1/2 | Cu 2, Fe 9 | Cu 0.5, Fe 0.56 (2.25) | copper, iron | **no**; excluded, packing |
| Splitter | Beltworks | → 2 belt tiles, comparator, copper (`BW/splitter.json`); belt tile → 3/16 dried kelp, 3/16 copper (`BW/belt_tile_from_dried_kelp.json`, first by id) | mixed 1/2/3 | Cu 1.38, Rs 3 | Cu 0.27, Rs 0.05 | copper, redstone | **no** |
| Loader | Beltworks | → ½ hopper, ½ copper, ½ belt tile (`BW/loader.json`, makes 2) | mixed 1/2 | Cu 0.59, Fe 2.5 | Cu 0.13, Fe 0.16 | copper, iron | **no** |
| Feeder | Beltworks | → loader + belt tile (`BW/feeder.json`); loader → hopper … | mixed 2/3 | Cu 0.78, Fe 2.5 | Cu 0.04, Fe 0.04 | copper, iron | **no** |
| Storage Tank | Pipeworks | → 4 iron, 4 glass, 1 copper (`PW/storage_tank.json`) | 1 | Cu 1, Fe 4 | Cu 0.25, Fe 1 | copper, iron | **no** |
| Assembler 1 | Craftworks | → crafter, 9 iron, 5 redstone (`CW/assembler_1.json`, assembling) | mixed 1/2/3 | Fe 14, Rs 8 | Fe 2.56, Rs 1.39 | iron, redstone | **no** |
| Refiner | Craftworks | → blast furnace, 4 iron, 5 redstone (`CW/refiner.json`, assembling); blast furnace → 5 iron, furnace, 3 smooth stone (`NF/blast_furnace.json`) | mixed 1/2 | Fe 9, Rs 5 | Fe 1.31, Rs 1.25 | iron, redstone | **no** |
| Kinetic Upgrade | Gearworks | → 4 copper, piston, redstone (`GW/kinetic_upgrade.json`) | mixed 1/2 | Cu 4, Fe 1, Rs 2 | Cu 1, Fe 0.06, Rs 0.31 | copper, iron, redstone | **no** |
| Burner Harvester | Fieldworks | → 3 iron, 4 cobblestone, furnace, iron pickaxe (`FW/harvester_1.json`); pickaxe → 3 iron + 2 sticks (`NF/iron_pickaxe.json`) | mixed 1/2 | Fe 6 | Fe 0.94 | iron | **no** |
| Machine Casing | Refined Storage | → 8 Quartz Enriched Iron + andesite (`RS/machine_casing.json`); QEI → ¾ iron, ¼ quartz (`RS/quartz_enriched_iron.json`) | 2 | Fe 6 | Fe 0.38 | iron | **yes** |
| Vacuum Module | Modular Routers | → blank module, hopper, eye of ender (`MR/vacuum_module.json`); blank module → ⅙ redstone, ½ paper, ½ gold nugget (`MR/blank_module.json`); hopper → 5 iron | 2 | Fe 5, Au ½ nugget, Rs 0.17 | Fe 0.31, Au 0.03 nugget, Rs 0.01 | iron, gold (trace), redstone | **yes**; its gold needs packing |
| Waxed Copper Chest | vanilla | → copper chest + honeycomb (`V/waxed_copper_chest_from_honeycomb.json`); copper chest → 8 copper + chest (`NF/copper_chest.json`) | 2 | Cu 8 | Cu 0.5 | copper | yes; **flagged, axe** |
| Waxed Copper Trapdoor | vanilla | → copper trapdoor + honeycomb (`V/waxed_copper_trapdoor_from_honeycomb.json`); trapdoor → 4 copper (`NF/copper_trapdoor.json`) | 2 | Cu 4 | Cu 0.25 | copper | yes; **flagged, axe** |
| Waxed Lightning Rod | vanilla | → lightning rod + honeycomb (`V/waxed_lightning_rod_from_honeycomb.json`); rod → 3 copper (`NF/lightning_rod.json`) | 2 | Cu 3 | Cu 0.19 | copper | yes; **flagged, axe** |
| Rune of Sacrifice II | Neo Vitae | → 4 netherite scrap, hellforged parts, 3 cobblestone, Rune of Sacrifice (`NV/rune_2_sacrifice.json`); Rune of Sacrifice → 4 deepslate, tabula robur, 2 gold, blank rune, a tier-1 blood orb (`NV/rune_sacrifice.json`) | 2 | Au 2 | Au 0.13 | gold | **yes** |

**Today's placeholders.** Piston, Hopper and Bucket **fail**: each gives its iron (and the piston its redstone) at the first recycle, so they are intermediate 1. Only the **Comparator passes**, and only for redstone.

**What passes with no packing:** Comparator, Sticky Piston, Minecart with Hopper, Empty Map, Pump, Outlet, Machine Casing, Vacuum Module and Rune of Sacrifice II, plus the three waxed items flagged for the axe. Of the unflagged ones, only the Pump and the Outlet carry copper, at 3 and 4. Every richer copper carrier (Cut Copper, Copper Grate, Steam Turbine, Railgun) is excluded: it passed the Recycler's rule only through copper blocks. A rescan of every item in the pack found no other clean copper carrier; the clean iron carriers beyond those above (Chest and TNT Minecarts, Refined Storage's cards, Voidworks' Void Crucible at 7) add nothing a Minecart with Hopper doesn't.

**Gold.** One true intermediate-2 item carries whole gold ingots with no packing: Neo Vitae's **Rune of Sacrifice II**, 2 gold from the Rune of Sacrifice inside it. Its first recycle also returns 4 netherite scrap. Modular Routers' modules and upgrades (Vacuum Module ½ nugget, Camouflage and Stack Upgrades ¾) carry gold only as nuggets, and Theurgy liquefies no nugget (`theurgy-…jar!/data/theurgy/recipe/liquefaction/` has gold `from_ingots`, `from_raw_materials` and `from_ores` only), so their gold reaches it only through 9 nuggets → 1 ingot (`V/gold_ingot_from_nuggets.json`), a compress recipe. Every other gold carrier gives its gold at the first recycle (Beltworks' express and turbo tiers, Functional Storage's upgrades, Craftworks' Assembler 3, Pipez' upgrades, Time in a Bottle) or at depth 3 and more.

## 4. Proposed mixes

Each proposal is a Mix: fixed full amounts. The tier-1 figures apply the decompose's quarter, then two recycles at a quarter each. Essences per Galvanic is the largest of the route's needs divided by what one essence gives. All three are 8 items, so they compare as they stand, and none relies on a packing recipe or a waxed item.

### Pipeworks: 4 Outlets, 2 Pumps, 1 Minecart with Hopper, 1 Comparator (unchanged)

- **Per essence, tier 1**: 0.34 copper, 0.28 iron, 0.14 redstone, no gold. Also 0.56 cobblestone, 0.19 stone, 0.14 planks, 0.06 quartz and 0.05 string.
- **Essences per Galvanic**: A 46.5 (copper-bound, gold not covered), B 230, C 58.
- **Byproducts**: the most cobblestone (36 in the full tree), plus planks and string. The first recycle returns pistons, buckets and dispensers.
- **Suite**: pulls in Pipeworks. Pumps and Outlets are its fluid blocks, usable as decomposed.
- The balanced pick, and the best for route C.

### Outlets: 6 Outlets, 2 Pumps

- **Per essence, tier 1**: 0.47 copper, 0.13 iron, 0.13 redstone, no gold. Also 0.78 cobblestone, 0.10 planks and 0.07 string.
- **Essences per Galvanic**: A 34.1 (copper-bound; iron binds at 32 right behind it), B 169, C 131. Gold not covered.
- **Byproducts**: cobblestone (50 in the full tree), the dispensers' string, planks.
- The most copper a clean mix can carry. Route C suffers: the Pumps are its only iron.

### Runes: 4 Outlets, 2 Pumps, 2 Runes of Sacrifice II

- **Per essence, tier 1**: 0.34 copper, 0.13 iron, 0.09 redstone, **0.06 gold**. Also 0.94 cobblestone, **0.5 netherite scrap**, 0.18 deepslate, 0.13 hellforged parts, and Neo Vitae's tabulae and a blood orb in small amounts.
- **Essences per Galvanic**: A 46.5 (copper-bound; gold binds at 32), B 230, C 131. Every route covered, gold included.
- With one rune and the Minecart with Hopper back, route C returns to 58.3 but A becomes gold-bound at 64.
- **The catch**: netherite scrap at eight times the rate of gold, from the rune's first recycle, before a team has seen the Nether. And Neo Vitae may become a challenge's mod.

### What the exclusion costs

Before, the best figures were A 22.3 / B 110 / C 55 with the blocks recycled, or A 6.6 / B 33 / C 23 with the blocks crafted back to ingots, which is what a player would have done. Now the best are A 34.1 and B 169 (Outlets) and C 58.3 (Pipeworks). Against the crafted figures, that is **about five times the essence for routes A and B and 2.6 times for C**. The cause is copper: the best clean carrier holds 4 (the Outlet), where the Steam Turbine held 36. Route B, the copper grind, is hit hardest, at 169 essences per Galvanic at tier 1.

Two things would win some of it back. The waxed copper items, if the axe is ruled no shortcut: 6 Waxed Copper Chests and 2 Pumps give A 32 (iron-bound) and B 94. And higher-tier cores (#189), which multiply every figure. Neither changes the packing rule.

Every mix covers the one redstone seed within 16 essences.

## 5. Open questions

- **Storage blocks.** Decided: excluded (packing). **Waxed copper** is the new edge. A Waxed Copper Chest passes with no block, but an axe unwaxes a placed one for free. Is it excluded like a block?
- **Gold for route A.** The mix can carry gold only through the Rune of Sacrifice II, with its netherite scrap. Does the mix carry gold (the Runes mix), or does gold come from outside the mix ([gold-sources.md](gold-sources.md))?
- **Netherite.** The Runes mix hands out 0.5 netherite scrap per essence. Is that acceptable, before the Nether?
- **Neo Vitae's role.** If Neo Vitae becomes the life challenge's mod, may a Neo Vitae item sit in Advanced's mix, when recycling it returns Neo Vitae intermediates?
- **Full yield or tier 1.** At full yield every figure is 64 times larger (16 times with crafted blocks). At tier 1, route B costs 110 to 230 essences per Galvanic. Should the mix be sized at tier 1, with higher-tier cores (#189) bringing the cost down, or at a higher core's yield?
- **Bootstrap.** The Recycler's parts (`decomposing.js`) are assembled from pistons, hoppers and a comparator that decomposing by hand gives today. A Recycler is needed before anything can be recycled, so a mix of Pumps and Outlets yields no piston until one is built. The parts' recipes would have to take the new mix's items whole: a Pump, an Outlet, a Comparator.
- **Usable as decomposed.** A Pump, an Outlet or a Minecart with Hopper works as soon as it is decomposed. Is that a reward or a cost? The inert alternative, Cut Copper, is now excluded.
- **Mix size.** Today's mix holds four items. Essences per Galvanic scales inversely with the mix's count; the three proposals are all 8 items.
- **FTB Materials.** Its dust-to-ingot smelting recipes carry an `ftbmaterials:components_available` condition (`ftb-materials-26.1.2.6.jar!/data/ftbmaterials/recipe/copper_ingot_from_smelting_copper_dust.json`). If they load, a copper ingot recycles into FTB's copper dust, not raw copper, because smelting comes first and its id sorts first. That changes nothing above refined. It does make route B's raw copper unreachable from recycled ingots, and recycling ingots is worse anyway: 0.75 sulfur against 1.
- **Checks in game.** The tag order, the NeoForge recipe overrides and whether FTB Materials' recipes load were read from data, not seen in EMI's recycling view.
