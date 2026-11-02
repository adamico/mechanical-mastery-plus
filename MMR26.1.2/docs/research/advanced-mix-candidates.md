# Advanced Mechanical Essence — Depth ≥ 2 Candidate Items

All items across all pack mods (excluding `mechanicalmasteryreborn`) at **recycle depth ≥ 2**, after filtering out:
- **Decorative blocks** (Connected Glass, stairs/slabs/walls/fences/gates/doors/trapdoors/panes/bars/chains/lanterns/torches/carpets/candles/banners/signs/beds/shelves/statues, waxed/cut/chiseled/grate/bulb copper, stone/dungeon/polished/chiseled bricks/tiles/pillars, wool/terracotta/concrete/stained glass/dyes, bookshelves, paintings, frames, pots, etc.)
- **Tools, weapons, and armors** (handheld tools, wrenches, rods, wands, sticks, bows, arrows, suits, lances, railgun, guide books/tomes, Shrinking Device, Time in a Bottle)
- **Items/blocks not stackable to 64** (`stacksTo(1)` or `stacksTo(16)`: AdminShop tablet & permit, Construction Stick upgrade templates, Fieldworks buckets, Functional Storage tools, LaserIO cards/holder/cloner/wrench, Neo Vitae orbs/sigils/crystals/tomes/flasks/daggers/anointments/keys/ledger, Pipez wrench & filter tool, Refined Storage disks/storage blocks/controllers/wireless grids/cards/wrench, vanilla minecarts/boats/beds/shulker boxes/banners/signs/stews/buckets/potions)

*(Items marked **`[in current mix]`** are the 29 items currently in `MMR26.1.2/kubejs/server_scripts/decomposing.js`; items marked `(packing)` are depth 2 via a storage block ingredient such as `iron_block`, `copper_block`, `redstone_block`, `coal_block`, `hay_block`, or `resin_block`.)*

---

## 1. Suite Mods (54 items)

### `beltworks` (15)
- [x] `beltworks:improved_belt_tile` (d=2) → `1x beltworks:belt_tile, 0.125x iron_ingot`
- [x] `beltworks:express_belt_tile` (d=3) → `1x beltworks:improved_belt_tile, 0.125x gold_ingot`
- [x] `beltworks:turbo_belt_tile` (d=4) → `1x beltworks:express_belt_tile, 0.125x diamond`
- [ ] `beltworks:loader` (d=3) → `0.5x hopper, 0.5x copper_ingot, 0.5x beltworks:belt_tile`
- [x] `beltworks:improved_loader` (d=4) → `1x beltworks:loader, 0.5x iron_ingot`
- [x] `beltworks:express_loader` (d=5) → `1x beltworks:improved_loader, 0.5x gold_ingot`
- [x] `beltworks:turbo_loader` (d=6) → `1x beltworks:express_loader, 0.5x diamond`
- [ ] `beltworks:feeder` (d=4) → `1x beltworks:loader, 1x beltworks:belt_tile`
- [x] `beltworks:improved_feeder` (d=5) → `1x beltworks:improved_loader, 1x beltworks:improved_belt_tile`
- [x] `beltworks:express_feeder` (d=6) → `1x beltworks:express_loader, 1x beltworks:express_belt_tile`
- [x] `beltworks:turbo_feeder` (d=7) → `1x beltworks:turbo_loader, 1x beltworks:turbo_belt_tile`
- [ ] `beltworks:splitter` (d=4) **`[in current mix]`** → `2x beltworks:belt_tile, 1x comparator, 1x copper_ingot`
- [x] `beltworks:improved_splitter` (d=5) → `1x beltworks:splitter, 2x iron_ingot`
- [x] `beltworks:express_splitter` (d=6) → `1x beltworks:improved_splitter, 2x gold_ingot`
- [x] `beltworks:turbo_splitter` (d=7) → `1x beltworks:express_splitter, 2x diamond`

### `craftworks` (4)
- [ ] `craftworks:refiner` (d=3) → `1x blast_furnace, 4x iron_ingot, 5x redstone`
- [ ] `craftworks:assembler_1` (d=3) → `1x crafter, 9x iron_ingot, 5x redstone`
- [x] `craftworks:assembler_2` (d=4) **`[in current mix]`** → `1x craftworks:assembler_1, 6x gold_ingot, 10x redstone`
- [x] `craftworks:assembler_3` (d=5) → `1x craftworks:assembler_2, 4x diamond, 2x redstone_block`

### `fieldworks` (2)
- [ ] `fieldworks:harvester_1` (d=3) → `3x iron_ingot, 4x cobblestone, 1x furnace, 1x iron_pickaxe`
- [x] `fieldworks:harvester_2` (d=4) **`[in current mix]`** → `4x copper_ingot, 2x redstone, 2x iron_ingot, 1x fieldworks:harvester_1`

### `gearworks` (12)
- [ ] `gearworks:gear_bench` (d=2) → `6x iron_ingot, 2x redstone, 1x smithing_table`
- [x] `gearworks:jump_upgrade` (d=2) → `4x iron_ingot, 3x slime_ball, 1x piston, 1x redstone`
- [x] `gearworks:kinetic_upgrade` (d=2) **`[in current mix]`** → `4x copper_ingot, 1x piston, 1x redstone`
- [x] `gearworks:metabolic_upgrade` (d=2) **`[in current mix]`** → `4x copper_ingot, 1x golden_carrot, 1x redstone`
- [x] `gearworks:nuclear_upgrade` (d=2, packing) → `5x iron_ingot, 1x redstone_block, 2x netherite_ingot, 1x nether_star`
- [x] `gearworks:reach_upgrade` (d=2) → `4x iron_ingot, 3x ender_pearl, 1x piston, 1x redstone`
- [x] `gearworks:shield_upgrade` (d=2) → `4x iron_ingot, 2x redstone, 2x obsidian, 1x shield`
- [x] `gearworks:combustion_upgrade` (d=3) → `4x iron_ingot, 1x blast_furnace, 1x redstone`
- [x] `gearworks:boost_upgrade` (d=4) → `4x gold_ingot, 4x firework_rocket, 1x dispenser`
- [x] `gearworks:speed_upgrade` (d=4) → `4x iron_ingot, 1x sugar, 2x piston, 1x redstone`
- [x] `gearworks:night_vision_upgrade` (d=5) → `4x iron_ingot, 3x glass_pane, 1x golden_carrot, 1x redstone`
- [x] `gearworks:solar_upgrade` (d=5) → `3x glass_pane, 4x copper_ingot, 1x daylight_detector, 1x redstone`

### `pipeworks` (2)
- [ ] `pipeworks:pump` (d=2) **`[in current mix]`** → `3x pipeworks:pipe, 1x piston, 1x bucket`
- [ ] `pipeworks:outlet` (d=4) **`[in current mix]`** → `4x pipeworks:pipe, 1x dispenser`

### `voidworks` (10)
- [ ] `voidworks:void_crucible` (d=2) **`[in current mix]`** → `6x amethyst_shard, 2x crying_obsidian, 1x cauldron`
- [x] `voidworks:mutation_chamber` (d=2) → `6x amethyst_shard, 2x crying_obsidian, 1x smithing_table`
- [x] `voidworks:unmaking_chamber` (d=3) → `6x amethyst_shard, 2x crying_obsidian, 1x grindstone`
- [x] `voidworks:overworld_void_displacer` (d=3) → `4x crying_obsidian, 4x ender_pearl, 1x sticky_piston`
- [x] `voidworks:nether_void_displacer` (d=3) → `4x polished_blackstone_bricks, 2x magma_cream, 2x blaze_rod, 1x sticky_piston`
- [x] `voidworks:end_void_displacer` (d=3) → `4x end_stone_bricks, 2x popped_chorus_fruit, 2x end_rod, 1x sticky_piston`
- [x] `voidworks:void_well` (d=3) → `4x shulker_shell, 4x crying_obsidian, 1x ender_chest`
- [x] `voidworks:void_well_casing` (d=2) → `0.5x obsidian, 0.5x purpur_block, 0.125x ender_pearl`
- [x] `voidworks:void_well_rim` (d=4) → `0.5x purpur_pillar, 0.5x obsidian`
- [x] `voidworks:well_hatch` (d=3) → `4x voidworks:void_well_casing, 1x shulker_box`

### `wireworks` (9)
- [ ] `wireworks:small_pole` (d=2) → `0.5x stick, 1x copper_ingot`
- [x] `wireworks:transmission_pole` (d=2) **`[in current mix]`** → `2x copper_ingot, 1x iron_bars, 2x wireworks:plastic, 2x iron_ingot`
- [x] `wireworks:transformer` (d=2, packing) **`[in current mix]`** → `4x iron_ingot, 3x copper_ingot, 1x iron_block, 1x wireworks:plastic`
- [ ] `wireworks:boiler` (d=2) → `7x iron_ingot, 1x bucket, 1x furnace`
- [ ] `wireworks:steam_engine` (d=2) **`[in current mix]`** → `5x iron_ingot, 1x piston, 2x copper_ingot, 1x iron_block`
- [x] `wireworks:steam_turbine` (d=2) **`[in current mix]`** → `4x copper_block, 2x wireworks:plastic, 2x piston, 1x iron_block`
- [x] `wireworks:accumulator` (d=2) **`[in current mix]`** → `4x iron_block, 4x wireworks:battery, 1x copper_ingot`
- [x] `wireworks:solar_panel` (d=3) → `2x glass, 1x daylight_detector, 2x copper_ingot, 1x wireworks:plastic, 1x iron_block`
- [x] `wireworks:reactor` (d=3) → `4x iron_block, 2x wireworks:plastic, 2x netherite_block, 1x blast_furnace`

---

## 2. AdminShop (9 items)
- [x] `adminshop:shop` (d=3) **`[in current mix]`** → `7x iron_ingot, 1x crafting_table, 1x barrel`
- [x] `adminshop:seller` (d=3) **`[in current mix]`** → `7x iron_ingot, 1x hopper, 1x barrel`
- [x] `adminshop:fluid_seller` (d=3) **`[in current mix]`** → `7x iron_ingot, 1x bucket, 1x barrel`
- [x] `adminshop:buyer_1` (d=3) **`[in current mix]`** → `7x iron_ingot, 1x barrel, 1x hopper`
- [x] `adminshop:buyer_2` (d=4) **`[in current mix]`** → `2x gold_ingot, 1x adminshop:buyer_1, 4x iron_ingot, 1x hopper, 1x barrel`
- [x] `adminshop:buyer_3` (d=5) **`[in current mix]`** → `2x diamond, 1x adminshop:buyer_2, 4x gold_ingot, 1x hopper, 1x barrel`
- [x] `adminshop:fluid_buyer` (d=3) **`[in current mix]`** → `7x iron_ingot, 1x barrel, 1x bucket`
- [x] `adminshop:detector` (d=2) **`[in current mix]`** → `1x emerald, 1x stick`
- [x] `adminshop:adv_detector` (d=3) **`[in current mix]`** → `3x adminshop:detector, 1x emerald, 3x stone`

---

## 3. Vanilla `minecraft` (35 items)
- [ ] `minecraft:anvil` (d=2, packing) **`[in current mix]`** → `3x iron_block, 4x iron_ingot`
- [ ] `minecraft:blast_furnace` (d=2) **`[in current mix]`** → `5x iron_ingot, 1x furnace, 3x smooth_stone`
- [ ] `minecraft:smoker` (d=2) → `4x dark_oak_log, 1x furnace`
- [ ] `minecraft:hopper` (d=2) **`[in current mix]`** → `5x iron_ingot, 1x chest`
- [ ] `minecraft:copper_chest` (d=2) **`[in current mix]`** → `8x copper_ingot, 1x chest`
- [x] `minecraft:trapped_chest` (d=3) → `1x chest, 1x tripwire_hook`
- [x] `minecraft:barrel` (d=2) → `6x oak_planks, 2x oak_slab`
- [x] `minecraft:composter` (d=2) → `7x oak_slab`
- [x] `minecraft:grindstone` (d=2) → `2x stick, 1x stone_slab, 2x oak_planks`
- [ ] `minecraft:lectern` (d=3) → `4x oak_slab, 1x bookshelf`
- [ ] `minecraft:crafter` (d=2) → `5x iron_ingot, 1x crafting_table, 2x redstone, 1x dropper`
- [ ] `minecraft:dispenser` (d=3) → `7x cobblestone, 1x bow, 1x redstone`
- [ ] `minecraft:sticky_piston` (d=2) → `1x slime_ball, 1x piston`
- [ ] `minecraft:comparator` (d=3) **`[in current mix]`** → `3x redstone_torch, 1x quartz, 3x stone`
- [ ] `minecraft:repeater` (d=3) → `2x redstone_torch, 1x redstone, 3x stone`
- [x] `minecraft:redstone_torch` (d=2) → `1x redstone, 1x stick`
- [ ] `minecraft:daylight_detector` (d=2) → `3x glass, 3x quartz, 3x oak_slab`
- [ ] `minecraft:target` (d=2, packing) → `4x redstone, 1x hay_block`
- [ ] `minecraft:lever` (d=2) → `1x stick, 1x cobblestone`
- [x] `minecraft:tripwire_hook` (d=2) → `0.5x iron_ingot, 0.5x stick, 0.5x oak_planks`
- [ ] `minecraft:rail` (d=2) → `0.375x iron_ingot, 0.0625x stick`
- [ ] `minecraft:powered_rail` (d=2) → `1x gold_ingot, 0.1667x stick, 0.1667x redstone`
- [x] `minecraft:detector_rail` (d=2) → `1x iron_ingot, 0.1667x stone_pressure_plate, 0.1667x redstone`
- [x] `minecraft:activator_rail` (d=3) → `1x iron_ingot, 0.3333x stick, 0.1667x redstone_torch`
- [x] `minecraft:enchanting_table` (d=2) → `1x book, 2x diamond, 4x obsidian`
- [x] `minecraft:ender_chest` (d=2) → `8x obsidian, 1x ender_eye`
- [x] `minecraft:end_crystal` (d=2) → `7x glass, 1x ender_eye, 1x ghast_tear`
- [x] `minecraft:lodestone` (d=4) → `8x chiseled_stone_bricks, 1x iron_ingot`
- [ ] `minecraft:creaking_heart` (d=2, packing) → `2x pale_oak_log, 1x resin_block`
- [x] `minecraft:magma_block` (d=2) → `4x magma_cream`
- [ ] `minecraft:map` (d=2) → `8x paper, 1x compass`
- [x] `minecraft:filled_map` (d=3) → `1x filled_map, 1x map`
- [x] `minecraft:sugar` (d=3) → `0.3333x honey_bottle`
- [x] `minecraft:fermented_spider_eye` (d=4) → `1x spider_eye, 1x brown_mushroom, 1x sugar`
- [x] `minecraft:pumpkin_pie` (d=4) → `1x pumpkin, 1x sugar, 1x egg`

---

## 4. Challenge Mods (`theurgy` & `neovitae`)

### `theurgy` (20)
- [ ] `theurgy:liquefaction_cauldron` (d=3) **`[in current mix]`** → `5x copper_ingot, 1x cauldron, 3x andesite`
- [ ] `theurgy:calcination_oven` (d=2, packing) → `4x iron_ingot, 1x copper_block`
- [ ] `theurgy:distiller` (d=3) → `3x iron_ingot, 1x copper_block, 3x andesite`
- [ ] `theurgy:pyromantic_brazier` (d=3) → `5x copper_ingot, 4x andesite`
- [ ] `theurgy:sal_ammoniac_accumulator` (d=3) → `3x andesite, 3x iron_ingot, 2x stick`
- [ ] `theurgy:sal_ammoniac_tank` (d=2) → `4x iron_ingot, 3x copper_ingot, 2x stick`
- [ ] `theurgy:incubator` (d=3) → `2x oak_planks, 3x andesite, 3x gold_ingot, 1x copper_ingot`
- [ ] `theurgy:incubator_mercury_vessel` (d=3) → `4x copper_ingot, 1x theurgy:mercury_shard, 3x andesite`
- [ ] `theurgy:incubator_salt_vessel` (d=3) → `4x copper_ingot, 1x theurgy:alchemical_salt_strata, 3x andesite`
- [ ] `theurgy:incubator_sulfur_vessel` (d=3) → `4x copper_ingot, 1x theurgy:alchemical_sulfur_rotten_flesh, 3x andesite`
- [ ] `theurgy:fermentation_vat` (d=3) → `6x copper_ingot, 1x theurgy:alchemical_sulfur_rotten_flesh, 1x barrel, 1x copper_block`
- [ ] `theurgy:digestion_vat` (d=2) → `1x theurgy:sal_ammoniac_crystal, 2x gold_ingot, 1x decorated_pot, 3x red_sandstone`
- [ ] `theurgy:mercury_catalyst` (d=2) → `4x iron_ingot, 1x theurgy:mercury_shard, 3x gold_ingot, 1x quartz_block`
- [ ] `theurgy:mercury_capacitor` (d=3) → `4x gold_ingot, 2x theurgy:mercury_shard, 2x quartz_block, 1x theurgy:mercury_catalyst`
- [ ] `theurgy:caloric_flux_emitter` (d=3) → `1x campfire, 2x gold_ingot, 1x theurgy:mercury_shard, 3x andesite`
- [ ] `theurgy:mercury_flux_emitter` (d=3) → `2x theurgy:mercury_shard, 2x gold_ingot, 3x andesite`
- [ ] `theurgy:sulfuric_flux_emitter` (d=3) → `1x theurgy:sal_ammoniac_crystal, 2x gold_ingot, 1x theurgy:alchemical_sulfur_rotten_flesh, 3x andesite`
- [ ] `theurgy:logistics_fluid_inserter` (d=2) → `1x blue_dye, 1x theurgy:mercury_shard, 1x copper_ingot`
- [ ] `theurgy:logistics_fluid_extractor` (d=2) → `1x blue_dye, 1x copper_ingot, 1x theurgy:mercury_shard`
- [ ] `theurgy:logistics_mercury_flux_connector` (d=2) → `1x blue_dye, 1x theurgy:mercury_shard`

### `neovitae` (34)
*(Note: Assessed during grilling for #212. A challenge mod's item goes into the mix only if recycling it yields nothing above the Chain's second tier, and no reusable catalyst such as a Blood Orb. Thus, only `ara_vitae` and `hellfire_forge` are included; all runes, slates, casings, and blood orb-yielding items are excluded.)*
- [ ] `neovitae:ara_vitae` (d=2) → `4x deepslate, 1x furnace, 3x gold_ingot`
- [ ] `neovitae:hellfire_forge` (d=2, packing) → `2x iron_ingot, 4x deepslate, 1x neovitae:tabula_rasa, 1x iron_block`
- [x] `neovitae:athanor` (d=2) → `3x deepslate, 2x neovitae:tabula_animata, 1x neovitae:weak_blood_orb, 2x iron_block, 1x furnace`
- [x] `neovitae:sanguine_casing` (d=2) → `1x neovitae:bloodstone, 0.25x diamond, 0.75x iron_ingot, 0.25x neovitae:tabula_animata`
- [x] `neovitae:hellforged_sanguine_casing` (d=3) → `4x neovitae:ingot_hellforged, 1x neovitae:sanguine_casing`
- [x] `neovitae:swift_sanguine_casing` (d=4) → `2x neovitae:tabula_animata, 2x sugar, 1x neovitae:sanguine_casing`
- [x] `neovitae:sanguine_crafter` (d=3) → `7x neovitae:sanguine_casing, 1x neovitae:tabula_spiritus, 1x crafter`
- [x] `neovitae:master_ritual_stone` (d=2) → `4x obsidian, 4x neovitae:ritual_stone, 1x neovitae:weak_blood_orb`
- [x] `neovitae:ritual_anchor` (d=2) → `1x neovitae:weak_blood_orb, 2x neovitae:ritual_stone, 1x neovitae:tabula_spiritus, 3x obsidian`
- [x] `neovitae:lava_crystal` (d=3) → `2x glass, 3x lava_bucket, 1x neovitae:weak_blood_orb, 2x obsidian, 1x diamond`
- [x] `neovitae:furnacecell_primitive` (d=2, packing) → `6x cobblestone, 1x neovitae:tabula_rasa, 1x coal_block, 1x neovitae:weak_blood_orb`
- [x] `neovitae:primitive_hydration_cell` (d=3) → `6x cobblestone, 1x neovitae:tabula_rasa, 1x water_bucket, 1x neovitae:weak_blood_orb`
- [x] `neovitae:input_routing_node` (d=2) → `1x neovitae:output_routing_node`
- [x] `neovitae:spike_trap` (d=4) → `7x neovitae:dungeon_stone, 1x neovitae:spikes, 1x dispenser`
- **Tier 1 Runes (10)**:
  - [x] `neovitae:rune_capacity` (d=2) → `4x deepslate, 3x bucket, 1x neovitae:rune_blank, 1x neovitae:tabula_animata`
  - [x] `neovitae:rune_capacity_augmented` (d=3) → `4x obsidian, 1x neovitae:tabula_spiritus, 2x bucket, 1x neovitae:rune_capacity, 1x neovitae:weak_blood_orb`
  - [x] `neovitae:rune_charging` (d=2) → `4x redstone, 1x neovitae:tabula_spiritus, 2x glowstone_dust, 1x neovitae:rune_blank, 1x neovitae:weak_blood_orb`
  - [x] `neovitae:rune_dislocation` (d=3) → `4x deepslate, 3x water_bucket, 1x neovitae:rune_blank, 1x neovitae:tabula_animata`
  - [x] `neovitae:rune_efficiency` (d=2) → `4x redstone, 1x neovitae:tabula_aetherea, 2x neovitae:hellforged_parts, 1x neovitae:rune_blank, 1x neovitae:weak_blood_orb`
  - [x] `neovitae:rune_orb` (d=2) → `4x deepslate, 3x neovitae:weak_blood_orb, 2x neovitae:rune_blank`
  - [x] `neovitae:rune_sacrifice` (d=2) → `4x deepslate, 1x neovitae:tabula_robur, 2x gold_ingot, 1x neovitae:rune_blank, 1x neovitae:weak_blood_orb`
  - [x] `neovitae:rune_sacrifice_self` (d=2) → `4x deepslate, 1x neovitae:tabula_robur, 2x glowstone_dust, 1x neovitae:rune_blank, 1x neovitae:weak_blood_orb`
  - [x] `neovitae:rune_speed` (d=4) → `4x deepslate, 2x neovitae:tabula_rasa, 2x sugar, 1x neovitae:rune_blank`
  - [x] `neovitae:rune_acceleration` (d=5) → `4x bucket, 1x neovitae:tabula_spiritus, 2x gold_ingot, 1x neovitae:rune_speed, 1x neovitae:weak_blood_orb`
- **Tier 2 Reinforced Runes (10)**:
  - [x] `neovitae:rune_2_acceleration` (d=6) → `4x netherite_scrap, 1x neovitae:hellforged_parts, 3x cobblestone, 1x neovitae:rune_acceleration`
  - [x] `neovitae:rune_2_capacity` (d=3) → `4x netherite_scrap, 1x neovitae:hellforged_parts, 3x cobblestone, 1x neovitae:rune_capacity`
  - [x] `neovitae:rune_2_capacity_augmented` (d=4) → `4x netherite_scrap, 1x neovitae:hellforged_parts, 3x cobblestone, 1x neovitae:rune_capacity_augmented`
  - [x] `neovitae:rune_2_charging` (d=3) → `4x netherite_scrap, 1x neovitae:hellforged_parts, 3x cobblestone, 1x neovitae:rune_charging`
  - [x] `neovitae:rune_2_dislocation` (d=4) → `4x netherite_scrap, 1x neovitae:hellforged_parts, 3x cobblestone, 1x neovitae:rune_dislocation`
  - [x] `neovitae:rune_2_efficiency` (d=3) → `6x netherite_scrap, 2x neovitae:rune_efficiency, 1x cobblestone`
  - [x] `neovitae:rune_2_orb` (d=3) → `4x netherite_scrap, 1x neovitae:hellforged_parts, 3x cobblestone, 1x neovitae:rune_orb`
  - [x] `neovitae:rune_2_sacrifice` (d=3) → `4x netherite_scrap, 1x neovitae:hellforged_parts, 3x cobblestone, 1x neovitae:rune_sacrifice`
  - [x] `neovitae:rune_2_sacrifice_self` (d=3) → `4x netherite_scrap, 1x neovitae:hellforged_parts, 3x cobblestone, 1x neovitae:rune_sacrifice_self`
  - [x] `neovitae:rune_2_speed` (d=5) → `4x netherite_scrap, 1x neovitae:hellforged_parts, 3x cobblestone, 1x neovitae:rune_speed`

---

## 5. Storage, Logistics & Utility Mods

### `functionalstorage` (34 base items + 30 non-oak wood drawer variants)
- **Upgrades (12)**:
  - [ ] `functionalstorage:copper_upgrade` (d=3) → `4x copper_ingot, 2x copper_block, 2x chest, 1x functionalstorage:oak_2`
  - [ ] `functionalstorage:gold_upgrade` (d=4) → `3x gold_ingot, 3x gold_block, 2x chest, 1x functionalstorage:copper_upgrade`
  - [x] `functionalstorage:diamond_upgrade` (d=5) → `4x diamond, 2x diamond_block, 2x chest, 1x functionalstorage:gold_upgrade`
  - [ ] `functionalstorage:iron_downgrade` (d=3) → `8x iron_ingot, 1x functionalstorage:oak_2`
  - [ ] `functionalstorage:void_upgrade` (d=3) → `8x obsidian, 1x functionalstorage:oak_2`
  - [ ] `functionalstorage:puller_upgrade` (d=3) → `6x andesite, 1x hopper, 1x functionalstorage:oak_2, 1x redstone`
  - [ ] `functionalstorage:pusher_upgrade` (d=3) → `6x andesite, 1x redstone, 1x functionalstorage:oak_2, 1x hopper`
  - [ ] `functionalstorage:collector_upgrade` (d=3) → `4x andesite, 2x hopper, 2x redstone, 1x functionalstorage:oak_2`
  - [ ] `functionalstorage:redstone_upgrade` (d=4) → `4x redstone, 2x redstone_block, 2x comparator, 1x functionalstorage:oak_2`
  - [ ] `functionalstorage:water_generator_upgrade` (d=3) → `6x andesite, 2x water_bucket, 1x bucket`
  - [ ] `functionalstorage:dripping_upgrade` (d=3) → `6x andesite, 1x pointed_dripstone, 1x cauldron, 1x lava_bucket`
  - [ ] `functionalstorage:obsidian_upgrade` (d=4) → `4x functionalstorage:dripping_upgrade, 1x functionalstorage:water_generator_upgrade`
- **Controllers & Special Drawers (9)**:
  - [ ] `functionalstorage:storage_controller` (d=4) → `4x andesite, 2x quartz_block, 2x functionalstorage:oak_2, 1x comparator`
  - [x] `functionalstorage:controller_extension` (d=4) → `4x andesite, 2x quartz_block, 2x functionalstorage:oak_2, 1x repeater`
  - [x] `functionalstorage:armory_cabinet` (d=4) → `4x andesite, 3x functionalstorage:oak_2, 1x comparator, 1x netherite_ingot`
  - [ ] `functionalstorage:compacting_drawer` (d=3) → `5x andesite, 2x piston, 1x functionalstorage:oak_2, 1x iron_ingot`
  - [ ] `functionalstorage:simple_compacting_drawer` (d=3) → `6x andesite, 1x functionalstorage:oak_2, 1x piston, 1x iron_ingot`
  - [x] `functionalstorage:ender_drawer` (d=3) → `6x oak_planks, 2x functionalstorage:oak_2, 1x ender_chest`
  - [ ] `functionalstorage:fluid_1` (d=2) → `8x oak_planks, 1x bucket`
  - [ ] `functionalstorage:fluid_2` (d=2) → `3.5x oak_planks, 1x bucket`
  - [ ] `functionalstorage:fluid_4` (d=2) → `1x bucket, 1.25x oak_planks`
- **Wooden & Framed Drawers (13 base + 30 non-oak wood variants)**:
  - [ ] `functionalstorage:oak_1` (d=2) → `8x oak_planks, 1x chest`
  - [ ] `functionalstorage:spruce_1` (d=2) → `8x spruce_planks, 1x chest`
  - [ ] `functionalstorage:birch_1` (d=2) → `8x birch_planks, 1x chest`
  - [ ] `functionalstorage:jungle_1` (d=2) → `8x jungle_planks, 1x chest`
  - [ ] `functionalstorage:acacia_1` (d=2) → `8x acacia_planks, 1x chest`
  - [ ] `functionalstorage:dark_oak_1` (d=2) → `8x dark_oak_planks, 1x chest`
  - [ ] `functionalstorage:mangrove_1` (d=2) → `8x mangrove_planks, 1x chest`
  - [ ] `functionalstorage:cherry_1` (d=2) → `8x cherry_planks, 1x chest`
  - [ ] `functionalstorage:pale_oak_1` (d=2) → `8x pale_oak_planks, 1x chest`
  - [ ] `functionalstorage:crimson_1` (d=2) → `8x crimson_planks, 1x chest`
  - [ ] `functionalstorage:warped_1` (d=2) → `8x warped_planks, 1x chest`
  - [ ] `functionalstorage:oak_2` (d=2) → `3.5x oak_planks, 1x chest`
  - [ ] `functionalstorage:spruce_2` (d=2) → `3.5x spruce_planks, 1x chest`
  - [ ] `functionalstorage:birch_2` (d=2) → `3.5x birch_planks, 1x chest`
  - [ ] `functionalstorage:jungle_2` (d=2) → `3.5x jungle_planks, 1x chest`
  - [ ] `functionalstorage:acacia_2` (d=2) → `3.5x acacia_planks, 1x chest`
  - [ ] `functionalstorage:dark_oak_2` (d=2) → `3.5x dark_oak_planks, 1x chest`
  - [ ] `functionalstorage:mangrove_2` (d=2) → `3.5x mangrove_planks, 1x chest`
  - [ ] `functionalstorage:cherry_2` (d=2) → `3.5x cherry_planks, 1x chest`
  - [ ] `functionalstorage:pale_oak_2` (d=2) → `3.5x pale_oak_planks, 1x chest`
  - [ ] `functionalstorage:crimson_2` (d=2) → `3.5x crimson_planks, 1x chest`
  - [ ] `functionalstorage:warped_2` (d=2) → `3.5x warped_planks, 1x chest`
  - [ ] `functionalstorage:oak_4` (d=2) → `1x chest, 1.25x oak_planks`
  - [ ] `functionalstorage:spruce_4` (d=2) → `1x chest, 1.25x spruce_planks`
  - [ ] `functionalstorage:birch_4` (d=2) → `1x chest, 1.25x birch_planks`
  - [ ] `functionalstorage:jungle_4` (d=2) → `1x chest, 1.25x jungle_planks`
  - [ ] `functionalstorage:acacia_4` (d=2) → `1x chest, 1.25x acacia_planks`
  - [ ] `functionalstorage:dark_oak_4` (d=2) → `1x chest, 1.25x dark_oak_planks`
  - [ ] `functionalstorage:mangrove_4` (d=2) → `1x chest, 1.25x mangrove_planks`
  - [ ] `functionalstorage:cherry_4` (d=2) → `1x chest, 1.25x cherry_planks`
  - [ ] `functionalstorage:pale_oak_4` (d=2) → `1x chest, 1.25x pale_oak_planks`
  - [ ] `functionalstorage:crimson_4` (d=2) → `1x chest, 1.25x crimson_planks`
  - [ ] `functionalstorage:warped_4` (d=2) → `1x chest, 1.25x warped_planks`
  - [x] `functionalstorage:framed_1` (d=2) → `8x iron_nugget, 1x chest`
  - [x] `functionalstorage:framed_2` (d=2) → `3.5x iron_nugget, 1x chest`
  - [x] `functionalstorage:framed_4` (d=2) → `1x chest, 1.25x iron_nugget`
  - [x] `functionalstorage:framed_fluid_1` (d=2) → `8x iron_nugget, 1x bucket`
  - [x] `functionalstorage:framed_fluid_2` (d=2) → `3.5x iron_nugget, 1x bucket`
  - [x] `functionalstorage:framed_fluid_4` (d=2) → `1x bucket, 1.25x iron_nugget`
  - [x] `functionalstorage:compacting_framed_drawer` (d=3) → `5x iron_nugget, 2x piston, 1x functionalstorage:oak_2, 1x iron_ingot`
  - [x] `functionalstorage:framed_simple_compacting_drawer` (d=3) → `6x iron_nugget, 1x functionalstorage:oak_2, 1x piston, 1x iron_ingot`
  - [x] `functionalstorage:framed_storage_controller` (d=4) → `4x iron_nugget, 2x quartz_block, 2x functionalstorage:oak_2, 1x comparator`
  - [x] `functionalstorage:framed_controller_extension` (d=4) → `4x iron_nugget, 2x quartz_block, 2x functionalstorage:oak_2, 1x repeater`

### `pipez` (7)
- [ ] `pipez:item_pipe` (d=2) → `0.375x iron_ingot, 0.125x dropper, 0.0625x redstone`
- [ ] `pipez:fluid_pipe` (d=2) → `0.375x iron_ingot, 0.125x bucket, 0.0625x redstone`
- [ ] `pipez:energy_pipe` (d=2, packing) → `0.375x iron_ingot, 0.125x redstone_block, 0.0625x redstone`
- [ ] `pipez:universal_pipe` (d=3) → `0.3333x pipez:item_pipe, 0.3333x pipez:energy_pipe, 0.3333x pipez:fluid_pipe, 0.3333x iron_ingot, 0.1667x redstone_block`
- [ ] `pipez:improved_upgrade` (d=2) **`[in current mix]`** → `4x gold_ingot, 4x redstone, 1x pipez:basic_upgrade`
- [x] `pipez:advanced_upgrade` (d=3) → `4x diamond, 4x redstone_block, 1x pipez:improved_upgrade`
- [x] `pipez:ultimate_upgrade` (d=4) → `4x netherite_ingot, 4x redstone_block, 1x pipez:advanced_upgrade`

### `laserio` (8)
*(Note: `laser_wrench`, `card_holder`, `card_cloner`, `card_item`, `card_fluid`, `card_energy`, and `card_redstone` are all `stacksTo(1)`, and `laser_connector` / `overclocker_card` / `overclocker_node` are depth 1.)*
- [ ] `laserio:laser_node` (d=5) → `4x iron_ingot, 4x glass_pane, 1x laserio:laser_connector`
- [x] `laserio:laser_connector_advanced` (d=2) → `2x ender_pearl, 1x diamond, 2x redstone, 1x laserio:laser_connector, 3x gold_ingot`
- [ ] `laserio:logic_chip_raw` (d=2) → `1x redstone, 0.5x gold_nugget, 0.5x clay_ball, 0.25x quartz_block`
- [x] `laserio:filter_basic` (d=5) → `1x iron_bars, 1x glass_pane, 0.25x laserio:logic_chip`
- [x] `laserio:filter_count` (d=6) → `1x laserio:filter_basic, 1x observer`
- [x] `laserio:filter_mod` (d=6) → `1x laserio:filter_basic, 1x book`
- [x] `laserio:filter_nbt` (d=6) → `1x laserio:filter_basic, 1x white_wool`
- [x] `laserio:filter_tag` (d=6) → `1x laserio:filter_basic, 1x paper`

### `modularrouters` (46)
- **Router Block (1)**:
  - [ ] `modularrouters:modular_router` (d=2) → `1x iron_ingot, 1x iron_bars, 0.25x modularrouters:blank_module`
- **Modules (19)**:
  - [ ] `modularrouters:activator_module` (d=4) → `4x redstone, 1x lever, 2x dispenser, 1x modularrouters:blank_module, 1x quartz`
  - [ ] `modularrouters:detector_module` (d=4) → `1x modularrouters:blank_module, 1x comparator`
  - [x] `modularrouters:distributor_module` (d=5) → `3x modularrouters:sender_module_2, 1x modularrouters:blank_module`
  - [ ] `modularrouters:dropper_module` (d=2) → `1x modularrouters:blank_module, 1x dropper`
  - [x] `modularrouters:energy_distributor_module` (d=6) → `1x modularrouters:energy_output_module, 1x modularrouters:distributor_module`
  - [ ] `modularrouters:energy_output_module` (d=2) → `1x redstone_block, 2x gold_ingot, 1x modularrouters:blank_module, 1x quartz`
  - [ ] `modularrouters:extruder_module_2` (d=2) → `1x modularrouters:blank_module, 1x chest`
  - [ ] `modularrouters:flinger_module` (d=3) → `1x modularrouters:dropper_module, 1x gunpowder`
  - [ ] `modularrouters:fluid_module` (d=2) → `1x cauldron, 2x glass, 1x modularrouters:blank_module`
  - [ ] `modularrouters:fluid_module_2` (d=3) → `1x modularrouters:fluid_module, 1x prismarine_shard`
  - [ ] `modularrouters:placer_module` (d=4) → `1x modularrouters:blank_module, 1x dispenser, 1x dirt`
  - [x] `modularrouters:player_module` (d=6) → `1x diamond_helmet, 1x modularrouters:sender_module_3, 1x wither_skeleton_skull, 1x modularrouters:puller_module_2, 1x diamond_chestplate`
  - [ ] `modularrouters:puller_module_1` (d=3) → `1x modularrouters:blank_module, 1x sticky_piston`
  - [x] `modularrouters:puller_module_2` (d=4) → `1x modularrouters:puller_module_1, 1x ender_pearl`
  - [ ] `modularrouters:sender_module_1` (d=3) → `1x modularrouters:blank_module, 1x bow, 1x arrow`
  - [x] `modularrouters:sender_module_2` (d=4) → `1x modularrouters:sender_module_1, 1x ender_pearl`
  - [x] `modularrouters:sender_module_3` (d=5) → `1x modularrouters:sender_module_2, 1x end_stone, 1x ender_chest`
  - [x] `modularrouters:vacuum_module` (d=3) → `1x modularrouters:blank_module, 1x hopper, 1x ender_eye`
  - [ ] `modularrouters:void_module` (d=3) → `1x modularrouters:blank_module, 1x lava_bucket`
- **Upgrades (9)**:
  - [ ] `modularrouters:blast_upgrade` (d=2) → `4x iron_bars, 4x obsidian, 1x modularrouters:blank_upgrade`
  - [ ] `modularrouters:camouflage_upgrade` (d=2) → `1x modularrouters:blank_upgrade, 1x red_dye, 1x green_dye, 1x blue_dye`
  - [ ] `modularrouters:energy_upgrade` (d=2) → `4x quartz, 1x redstone_block, 1x modularrouters:blank_upgrade, 1x gold_ingot`
  - [ ] `modularrouters:fluid_upgrade` (d=2) → `0.3333x bucket, 0.6667x glass, 0.3333x modularrouters:blank_upgrade`
  - [ ] `modularrouters:muffler_upgrade` (d=5) → `1x white_wool, 0.25x modularrouters:blank_upgrade`
  - [ ] `modularrouters:security_upgrade` (d=2) → `1x quartz, 2x gold_nugget, 1x modularrouters:blank_upgrade, 1x redstone`
  - [x] `modularrouters:speed_upgrade` (d=2) → `0.6667x redstone, 0.3333x gold_ingot, 0.6667x gold_nugget, 0.3333x modularrouters:blank_upgrade, 0.6667x gunpowder, 0.3333x blaze_rod`
  - [ ] `modularrouters:stack_upgrade` (d=2) → `1x modularrouters:blank_upgrade, 1x stone_bricks, 1x brick`
  - [ ] `modularrouters:sync_upgrade` (d=3) → `0.1875x redstone, 0.0625x andesite, 0.0625x redstone_torch, 0.0625x modularrouters:blank_upgrade`
- **Augments (12)**:
  - [ ] `modularrouters:augment_core` (d=2) → `0.25x modularrouters:blank_module, 0.25x modularrouters:blank_upgrade`
  - [ ] `modularrouters:fast_pickup_augment` (d=3) → `1x modularrouters:augment_core, 1x fishing_rod`
  - [ ] `modularrouters:filter_round_robin_augment` (d=3) → `1x modularrouters:augment_core, 1x clock`
  - [ ] `modularrouters:mimic_augment` (d=3) → `1x modularrouters:augment_core, 1x obsidian, 1x redstone, 1x glowstone_dust`
  - [ ] `modularrouters:pickup_delay_augment` (d=3) → `1x modularrouters:augment_core, 1x slime_ball`
  - [ ] `modularrouters:pushing_augment` (d=3) → `2x piston, 1x modularrouters:augment_core`
  - [ ] `modularrouters:range_down_augment` (d=3) → `0.25x stick, 0.75x quartz, 0.25x modularrouters:augment_core`
  - [ ] `modularrouters:range_up_augment` (d=3) → `0.75x quartz, 0.25x modularrouters:augment_core, 0.25x stick`
  - [ ] `modularrouters:redstone_augment` (d=3) → `2x redstone_torch, 2x redstone, 1x modularrouters:augment_core`
  - [ ] `modularrouters:regulator_augment` (d=4) → `2x quartz, 2x comparator, 1x modularrouters:augment_core`
  - [ ] `modularrouters:stack_augment` (d=3) → `1x modularrouters:augment_core, 1x modularrouters:stack_upgrade`
  - [ ] `modularrouters:xp_vacuum_augment` (d=3) → `1x modularrouters:augment_core, 1x soul_sand`
- **Filters (5)**:
  - [ ] `modularrouters:bulk_item_filter` (d=2) → `4x iron_ingot, 2x glass, 2x modularrouters:blank_module, 1x diamond`
  - [ ] `modularrouters:inspection_filter` (d=3) → `2x spider_eye, 1x modularrouters:bulk_item_filter, 1x paper`
  - [ ] `modularrouters:mod_filter` (d=4) → `1x modularrouters:bulk_item_filter, 1x repeater, 1x redstone_torch`
  - [ ] `modularrouters:regex_filter` (d=4) → `1x modularrouters:bulk_item_filter, 1x comparator`
  - [ ] `modularrouters:tag_filter` (d=3) → `1x modularrouters:bulk_item_filter, 1x paper, 1x black_dye`

### `refinedstorage` (41 base items; dyed variants excluded)
*(Note: disks, storage blocks, controllers, portable/wireless grids, security/network/config cards, and wrench are `stacksTo(1)`.)*
- **Parts, Housings & Raw Processors (14)**:
  - [ ] `refinedstorage:machine_casing` (d=3) → `8x refinedstorage:quartz_enriched_iron, 1x andesite`
  - [ ] `refinedstorage:storage_housing` (d=2) → `2x glass, 3x redstone, 3x refinedstorage:quartz_enriched_iron`
  - [ ] `refinedstorage:pattern` (d=2) → `3x glass, 3x redstone, 3x refinedstorage:quartz_enriched_iron`
  - [ ] `refinedstorage:raw_basic_processor` (d=2) → `1x refinedstorage:processor_binding, 1x iron_ingot, 1x ftbmaterials:silicon_gem, 1x redstone`
  - [x] `refinedstorage:raw_improved_processor` (d=2) → `1x refinedstorage:processor_binding, 1x gold_ingot, 1x ftbmaterials:silicon_gem, 1x redstone`
  - [x] `refinedstorage:raw_advanced_processor` (d=2) → `1x refinedstorage:processor_binding, 1x diamond, 1x ftbmaterials:silicon_gem, 1x redstone`
  - [ ] `refinedstorage:1k_storage_part` (d=2) → `4x ftbmaterials:silicon_gem, 1x refinedstorage:quartz_enriched_iron, 3x glass, 1x redstone`
  - [x] `refinedstorage:4k_storage_part` (d=3) → `4x refinedstorage:basic_processor, 1x refinedstorage:quartz_enriched_iron, 3x refinedstorage:1k_storage_part, 1x redstone`
  - [x] `refinedstorage:16k_storage_part` (d=4) → `4x refinedstorage:improved_processor, 1x refinedstorage:quartz_enriched_iron, 3x refinedstorage:4k_storage_part, 1x redstone`
  - [x] `refinedstorage:64k_storage_part` (d=5) → `4x refinedstorage:advanced_processor, 1x refinedstorage:quartz_enriched_iron, 3x refinedstorage:16k_storage_part, 1x redstone`
  - [ ] `refinedstorage:64b_fluid_storage_part` (d=2) → `4x ftbmaterials:silicon_gem, 1x refinedstorage:quartz_enriched_iron, 3x glass, 1x bucket`
  - [x] `refinedstorage:256b_fluid_storage_part` (d=3) → `4x refinedstorage:basic_processor, 1x refinedstorage:quartz_enriched_iron, 3x refinedstorage:64b_fluid_storage_part, 1x bucket`
  - [x] `refinedstorage:1024b_fluid_storage_part` (d=4) → `4x refinedstorage:improved_processor, 1x refinedstorage:quartz_enriched_iron, 3x refinedstorage:256b_fluid_storage_part, 1x bucket`
  - [x] `refinedstorage:4096b_fluid_storage_part` (d=5) → `4x refinedstorage:advanced_processor, 1x refinedstorage:quartz_enriched_iron, 3x refinedstorage:1024b_fluid_storage_part, 1x bucket`
- **Upgrades (6)**:
  - [ ] `refinedstorage:upgrade` (d=2) → `6x refinedstorage:quartz_enriched_iron, 2x glass, 1x refinedstorage:improved_processor`
  - [x] `refinedstorage:speed_upgrade` (d=4) → `5x refinedstorage:quartz_enriched_iron, 3x sugar, 1x refinedstorage:upgrade`
  - [x] `refinedstorage:stack_upgrade` (d=5) → `5x sugar, 4x refinedstorage:speed_upgrade`
  - [x] `refinedstorage:range_upgrade` (d=3) → `5x refinedstorage:quartz_enriched_iron, 3x ender_pearl, 1x refinedstorage:upgrade`
  - [x] `refinedstorage:autocrafting_upgrade` (d=3) → `5x refinedstorage:quartz_enriched_iron, 1x refinedstorage:construction_core, 2x crafting_table, 1x refinedstorage:upgrade`
  - [x] `refinedstorage:regulator_upgrade` (d=4) → `5x refinedstorage:quartz_enriched_iron, 1x comparator, 2x redstone, 1x refinedstorage:upgrade`
- **Network Blocks & Cables (21)**:
  - [ ] `refinedstorage:cable` (d=2) → `0.5x refinedstorage:quartz_enriched_copper, 0.1667x glass, 0.0833x redstone`
  - [ ] `refinedstorage:importer` (d=3) → `1x refinedstorage:cable, 1x refinedstorage:destruction_core, 1x refinedstorage:improved_processor`
  - [ ] `refinedstorage:exporter` (d=3) → `1x refinedstorage:cable, 1x refinedstorage:construction_core, 1x refinedstorage:improved_processor`
  - [ ] `refinedstorage:external_storage` (d=3) → `1x refinedstorage:construction_core, 3x refinedstorage:quartz_enriched_iron, 1x refinedstorage:destruction_core, 2x copper_chest, 1x refinedstorage:cable, 1x refinedstorage:improved_processor`
  - [ ] `refinedstorage:constructor` (d=3) → `4x refinedstorage:quartz_enriched_iron, 1x refinedstorage:construction_core, 2x diamond, 1x refinedstorage:cable, 1x refinedstorage:improved_processor`
  - [ ] `refinedstorage:destructor` (d=3) → `4x refinedstorage:quartz_enriched_iron, 1x refinedstorage:destruction_core, 2x diamond, 1x refinedstorage:cable, 1x refinedstorage:improved_processor`
  - [ ] `refinedstorage:grid` (d=4) → `2x refinedstorage:improved_processor, 1x refinedstorage:construction_core, 3x glass, 1x refinedstorage:quartz_enriched_iron, 1x refinedstorage:machine_casing, 1x refinedstorage:destruction_core`
  - [ ] `refinedstorage:crafting_grid` (d=5) → `1x refinedstorage:grid, 1x refinedstorage:advanced_processor, 1x crafting_table`
  - [ ] `refinedstorage:pattern_grid` (d=5) → `1x refinedstorage:grid, 1x refinedstorage:advanced_processor, 1x refinedstorage:pattern`
  - [ ] `refinedstorage:storage_monitor` (d=4) → `2x refinedstorage:basic_processor, 1x refinedstorage:construction_core, 3x glass, 1x refinedstorage:quartz_enriched_iron, 1x refinedstorage:machine_casing, 1x refinedstorage:destruction_core`
  - [ ] `refinedstorage:autocrafting_monitor` (d=4) → `2x refinedstorage:improved_processor, 2x refinedstorage:pattern, 3x glass, 1x refinedstorage:quartz_enriched_iron, 1x refinedstorage:machine_casing`
  - [ ] `refinedstorage:disk_drive` (d=4) → `6x refinedstorage:quartz_enriched_iron, 1x copper_chest, 1x refinedstorage:machine_casing, 1x refinedstorage:advanced_processor`
  - [ ] `refinedstorage:disk_interface` (d=4) → `4x refinedstorage:quartz_enriched_iron, 2x refinedstorage:storage_housing, 1x refinedstorage:construction_core, 1x refinedstorage:machine_casing, 1x refinedstorage:destruction_core`
  - [ ] `refinedstorage:interface` (d=4) → `4x refinedstorage:quartz_enriched_iron, 1x refinedstorage:importer, 2x redstone, 1x refinedstorage:machine_casing, 1x refinedstorage:exporter`
  - [ ] `refinedstorage:autocrafter` (d=4) → `4x refinedstorage:quartz_enriched_iron, 1x refinedstorage:construction_core, 2x refinedstorage:advanced_processor, 1x refinedstorage:machine_casing, 1x refinedstorage:destruction_core`
  - [ ] `refinedstorage:autocrafter_manager` (d=4) → `2x refinedstorage:advanced_processor, 2x refinedstorage:white_autocrafter, 3x glass, 1x refinedstorage:quartz_enriched_iron, 1x refinedstorage:machine_casing`
  - [ ] `refinedstorage:detector` (d=4) → `4x refinedstorage:quartz_enriched_iron, 1x redstone_torch, 2x comparator, 1x refinedstorage:machine_casing, 1x refinedstorage:improved_processor`
  - [ ] `refinedstorage:relay` (d=4) → `1x refinedstorage:machine_casing, 1x refinedstorage:cable, 1x refinedstorage:advanced_processor, 1x redstone_torch`
  - [ ] `refinedstorage:security_manager` (d=5) → `4x refinedstorage:quartz_enriched_iron, 1x copper_chest, 2x refinedstorage:security_card, 1x refinedstorage:machine_casing, 1x refinedstorage:fallback_security_card`
  - [x] `refinedstorage:network_transmitter` (d=4) → `3x ender_pearl, 1x refinedstorage:construction_core, 1x refinedstorage:machine_casing, 1x refinedstorage:destruction_core, 2x refinedstorage:advanced_processor, 1x netherite_ingot`
  - [x] `refinedstorage:network_receiver` (d=4) → `2x refinedstorage:advanced_processor, 1x netherite_ingot, 1x refinedstorage:construction_core, 1x refinedstorage:machine_casing, 1x refinedstorage:destruction_core, 3x ender_pearl`

### Other Mods (4 base items)
- [ ] `constructionstick:stick_template` (d=2) → `4x stick, 1x oak_button`
- [x] `elevatorid:elevator_white` (d=5, +15 other wool colors) → `8x white_wool, 1x ender_pearl`
- [x] `gateways:gate_pearl` (d=2) → `5x blaze_rod, 1x ender_pearl, 3x nether_bricks`
- [x] `globalxp:xp_block` (d=2) → `8x iron_bars, 1x emerald`
