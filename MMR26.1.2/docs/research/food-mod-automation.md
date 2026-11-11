# How well the profile's food mods automate in Reborn

Research for #346. Sources are the jars in Reborn's CurseForge profile
(`mods/`), read as bytecode (`javap`) and data files, plus NeoForge
26.1.2.109's own sources. Nothing was run in game: every "works" below is
what the code says, and wants an in-game check before a design leans on it.

| Mod | Jar |
| --- | --- |
| Agritech Evolved | `agritechevolved-2.2.0.7-neoforge-mc26.1.2.jar` |
| Animal Pen | `animal_pen-neoforge-26.1-2.5.2.jar` |
| Butchery | `butchery-5.2-neoforge-26.1.2.jar` |
| Butcher's Delight | `butchersdelight-neoforge-26.1.2-2.1.1.jar` |
| Croptopia | `croptopia-neoforge-26.1.2-4.3.2.jar` |
| Farmer's Delight | `FarmersDelight-26.1.2-1.3.3-fix8.jar` |
| Quality Food | `quality_food-26.1.2-2.4.7.jar` |
| Refined Cooking | `refinedcooking-neoforge-26.1.2-7.3.1.jar` |
| Pantry for Blockheads | `pantryforblockheads-neoforge-26.1.2-26.1.2.8.jar` |
| TimberReplant | `TimberReplant-4.4.0.jar` |

## How the pack moves items

NeoForge 26.1 replaced `IItemHandler` with the transfer API:
`Capabilities.Item.BLOCK` hands out a `ResourceHandler<ItemResource>`.

- **Beltworks** is the only one of the pack's four mods that moves items.
  `blocks/Inventories.at` asks `Level.getCapability(Capabilities.Item.BLOCK, pos, side)`
  and nothing else: no fallback to a plain vanilla `Container`. Beltworks
  registers only `Capabilities.Energy` on its own blocks, so other blocks
  cannot push into a belt through a capability.
- **Groundworks** moves nothing. `Footprint.forwardCapabilities` forwards the
  item, fluid and energy capabilities of a multiblock's part blocks to its
  core.
- **Pipeworks** handles fluids only (`Capabilities.Fluid`).
- **Fieldworks**' Harvesters expose `Capabilities.Item.BLOCK` (`Harvesters`,
  `HarvesterBlockEntity`), but they mine ore patches and the Oil Well; its
  recipes are `fieldworks:harvesting` on ores. Nothing in it touches crops,
  animals or food.
- **Vanilla hoppers** go through NeoForge's hopper hook, which tries the
  capability first and then falls back to any vanilla `Container`. So a
  hopper reaches a few blocks a belt cannot.

NeoForge itself registers the item capability only for named vanilla blocks
(chests, furnaces, smoker, barrel, hopper, crafter and so on;
`CapabilityHooks.registerVanillaProviders`). It has no catch-all for modded
`Container` block entities. **Balm** does: `NeoForgeBalm` registers a
fallback `Capabilities.Item.BLOCK` provider for every block entity that
implements `BalmContainerProvider`, wrapping it in `WorldlyContainerWrapper`
or `VanillaContainerWrapper`. That provider is how Butcher's Delight and
Pantry for Blockheads blocks become reachable by belts.

## Per mod

### Agritech Evolved: automates fully

- The Planter registers `Capabilities.Item.BLOCK` (`ATEBlockEntities.registerCapabilities`);
  the Composter, Biomass Burner, Silo, Fertilizer Spreader and Capacitor
  register their own.
- `PlanterBlockEntity.tryOutputItemsBelow` pushes the harvest into whatever
  item handler sits below it. A belt is not a valid target (no item
  capability), so a chest or hopper goes between.
- 627 `agritechevolved:crop` and 215 `agritechevolved:tree` recipes, guarded
  by `neoforge:mod_loaded`. Live in this profile: vanilla, 58 Croptopia crops
  plus Croptopia's fruit and nut saplings, and Farmer's Delight's cabbage,
  onion, rice and tomato.
- **Overlap:** it is a self-contained crop and tree farm, so it competes with
  any farm the pack plans to build, and it makes Croptopia's and Farmer's
  Delight's crops a no-player resource. No overlap with Fieldworks.

### Animal Pen: half automates

- No item capability on the pens, aviaries or aquariums.
- `DispenserInteractionExecutor` lets a dispenser use an item on the pen
  (feed, shear, bucket, milk). Outputs go through `ItemTransferHelperImpl`,
  which inserts into a neighbouring block's `Capabilities.Item.BLOCK`.
- So a dispenser plus a chest automates it; the dispenser's own input has to
  be refilled, which a hopper or a belt into the dispenser can do (vanilla
  dispensers have the capability).
- **Overlap:** with Butchery and Butcher's Delight as the meat source;
  Animal Pen keeps animals alive and yields drops without killing.

### Butchery: partly automates

- Registers `Capabilities.Item.BLOCK` for almost every block: butcher's
  tables, carcasses and corpses, Meat Grinder, Skin Rack, Freezer, Basin,
  Blood Grate, Rotisserie, Pestle and Mortar, displays (`ButcheryModBlockEntities.registerCapabilities`).
  Blood Grate and Basin also expose `Capabilities.Fluid.BLOCK` (blood),
  so Pipeworks could move blood.
- **The core loop is a player's.** Carcasses are hung and cut with a knife
  by hand (MCreator right-click procedures).
- The **Meat Grinder** runs from its block tick
  (`MeatgrinderBlock.tick` → `MeatgrinderrecipeprocedureProcedure`), so it
  automates. The **Basin** collects salt on its tick. The **Freezer**'s
  procedures hang off its menu opening and closing
  (`FreezerguiopenedProcedure`), which suggests it only works with a player
  in the GUI; check in game.
- Its 82 cooked-meat recipes are vanilla `smelting`/`smoking`/`campfire_cooking`,
  so a furnace line automates those. 9 recipes are `farmersdelight:cutting`.
- **Overlap:** Butcher's Delight (same carcass idea), Farmer's Delight
  cutting.

### Butcher's Delight: reachable, not automatable

- No capability of its own, but all 87 of its block entities implement
  `WorldlyContainer` and `BalmContainerProvider`, so Balm's fallback gives
  them `Capabilities.Item.BLOCK`. Belts and hoppers can reach them.
- The processing is player-driven: hooked carcasses, racks and roasting are
  right-click procedures (`ProRackProcedure`, `RoastedAnimalProcessProcedure`).
  Roasters tick (`Roaster*UpdateTickProcedure`), so roasting may finish on
  its own once a player has loaded a carcass.
- Only 5 recipes of its own; its products are mostly drops.
- **Overlap:** Butchery, nearly one to one.

### Croptopia: no machines; automation depends on others

- No block entities. It adds crops, trees and food items.
- Its food is 231 shapeless and 91 shaped crafting recipes, plus 17
  smelting, 17 smoking and 8 campfire. It also ships `thermal:insolator` and
  `immersiveengineering:cloche` recipes, inert here.
- So Croptopia automates exactly as far as the pack's crafting does: the
  furnace recipes in a furnace line, the crafting recipes in a Craftworks
  Assembler (which registers `Capabilities.Item.BLOCK`, `machine/Assemblers`).
  Crops are grown by Agritech's Planter without a player.
- **Overlap:** Farmer's Delight (tomato, onion, cabbage, rice exist in both
  under different ids), Agritech as the farm.

### Farmer's Delight: the Cooking Pot automates, the Cutting Board does not

- Capabilities on the Cooking Pot, Cutting Board, Cabinet and Basket.
  Stove and Skillet have none.
- **Cooking Pot** (`CookingPotItemHandler`): from the top, insert into the
  six ingredient slots; from any other side, insert into the container slot
  (bowls) and extract the meal from the output slot. A heat source below is
  still needed. This is a proper automatable machine: 28 `farmersdelight:cooking`
  recipes.
- **Cutting Board**: an item can be inserted and extracted, but cutting runs
  only in `processStoredItemUsingTool(ItemStack, Player)`, a tool used on
  the board. The profile has no Create Deployer or other block that uses
  tools, so the 105 `farmersdelight:cutting` recipes (and Butchery's 9) stay
  manual.
- **Overlap:** Croptopia (crops), Butchery (cutting), Cooking for
  Blockheads/Pantry (meals).

### Quality Food: see the next section

No blocks, no recipes. Mixins and a loot modifier.

### Refined Cooking: a bridge, not a machine

- Registers `Capabilities.Item.BLOCK` on the Kitchen Access Point and a
  Cooking for Blockheads `KitchenItemProvider` on the Kitchen Station
  (`NeoForgeRefinedCooking.registerCapabilities`), and Refined Storage
  network nodes on both.
- It lets a Cooking for Blockheads kitchen read ingredients from a Refined
  Storage network. The cooking is still a player clicking a recipe in the
  kitchen menu. It saves the player hauling, it does not cook alone.
- Needs Refined Storage and Cooking for Blockheads, both in the profile.

### Pantry for Blockheads: the Artisan Press automates

- The Artisan Press is a `BalmContainerProvider` with input and output
  sub-containers and a `serverTick` that processes on its own; Balm's
  fallback gives it `Capabilities.Item.BLOCK`. Its 25 `pantryforblockheads:artisan_press`
  recipes should therefore run from a belt. Which side reaches which slot is
  Balm's default wrapper; check in game.
- Its other recipes (`farmingforblockheads:shipping_bin`, `:market`,
  `littlejoys:drop_rush`) belong to mods not in the profile, so they are inert.

### TimberReplant: nothing to automate

- No blocks or recipes. `BlockBreakEventHandler` and `TreeFeller` fell a
  whole tree and replant it when a player breaks a log.
- **Overlap:** Agritech's tree recipes do the same job without a player.

## Does Quality Food's quality survive automation?

Quality is a data component on the stack. Moving it is safe: belts, hoppers
and `ResourceHandler`s carry `ItemResource`, which keeps components, so a
quality stack arrives as itself (and does not stack with plain items).

Applying quality on processing is narrower. Quality Food hooks only these
(`quality_food.mixins.json`):

- the vanilla **crafting table, inventory grid and Crafter** (`CraftingMenuMixin`,
  `InventoryMenuMixin`, `CrafterBlockMixin`, `ResultSlotMixin`), plus FastBench,
  Sophisticated Core's crafting upgrade and Tom's Storage terminal;
- **furnaces, smokers, blast furnaces and campfires** (`AbstractFurnaceBlockEntityMixin`,
  `CampFireBlockEntityMixin`, `FurnaceResultSlotMixin`). Quality is kept for
  automated furnaces too: `HopperBlockEntityMixin` and
  `WorldlyContainerWrapperMixin` hook the NeoForge wrapper that belts and
  hoppers use, so pulling a smoked steak out with a belt keeps its quality;
- **harvest and drops**: block breaking, stems, sugar cane, cake, eggs,
  animal breeding and a global loot modifier.

Which recipes it recognises is decided by recipe id and item tags, not by mod:

- Any recipe made in the hooked stations rolls quality from its ingredients.
  Server config `no_quality_recipes` turns that off per recipe;
  `retain_quality_recipes` copies quality through when all ingredients
  share it; storage-block recipes are detected and kept automatically.
- Items that can carry quality: `#quality_food:material_whitelist`, which
  holds `#c:crops`, `#c:seeds`, `#c:foods`, `#c:eggs`, `#c:mushrooms`, flours,
  doughs, sugar and so on, plus Farmer's Delight wild crops, pies, mushroom
  colonies and rice panicle. Croptopia's and Butchery's foods count as long
  as they sit in those `c:` tags.
- Blocks that drop quality: `#quality_food:quality_blocks`, which names
  vanilla crops and Farmer's Delight wild crops, rice and crates.
- `Compat$Mod` names Farmer's Delight among its known mods, but only for
  those tags; there is no Farmer's Delight mixin.

So **quality is lost** in every machine it does not hook: Farmer's Delight's
Cooking Pot and Cutting Board, Butchery's Meat Grinder, Pantry's Artisan
Press, Cooking for Blockheads kitchens, Agritech's Planter output (it rolls
its own drops, not a block break), and Craftworks' Assemblers. Those take
quality inputs and give plain outputs. Only the vanilla furnace family and
vanilla crafting keep it.

## Summary

| Mod | Belt-reachable | Runs without a player | Quality kept |
| --- | --- | --- | --- |
| Agritech Evolved | yes | yes | no (own drops) |
| Animal Pen | outputs only, via dispenser + chest | with a dispenser | drops only |
| Butchery | yes | Meat Grinder, Basin, furnace recipes; butchering no | no, except furnace recipes |
| Butcher's Delight | yes (Balm) | no | no |
| Croptopia | n/a | via Assembler / furnace / Agritech | furnace only |
| Farmer's Delight | yes | Cooking Pot yes, Cutting Board no | no |
| Refined Cooking | yes | no | no |
| Pantry for Blockheads | yes (Balm) | Artisan Press yes | no |
| TimberReplant | n/a | no | n/a |

Open checks in game: the Butchery Freezer without a player, the Artisan
Press's side mapping, and Agritech planter output into a hopper then a belt.
