# Spoiled and Cooking for Blockheads in Reborn

Research for #347. Sources are the jars in Reborn's CurseForge profile, read with `javap` and by unpacking their data (nothing was run):

- Spoiled `Spoiled-neoforge-26.1.2-12.1.0.jar` (mrbysco, `com.mrbysco.spoiled`)
- Cooking for Blockheads `cookingforblockheads-neoforge-26.1.2-26.1.2.6.jar` (BlayTheNinth, `net.blay09.mods.cookingforblockheads`)
- Pantry for Blockheads `pantryforblockheads-neoforge-26.1.2-26.1.2.8.jar`
- Refined Cooking `refinedcooking-neoforge-26.1.2-7.3.1.jar` (`sebastrn.refinedcooking`)
- Quality Food `quality_food-26.1.2-2.4.7.jar` (`de.cadentem.quality_food`)

The profile has no config file for any of them yet (`config/` and `saves/New World/serverconfig/` hold none), so everything below is their defaults. The pack repo sets none either.

Claims marked **(inferred)** are read from class shapes and call sites, not from a test in game.

## How Spoiled spoils

- Freshness is a data component on the stack, `SpoilTimer(timer, maxTime)` (`component/SpoilTimer`). When it reaches its max, the stack is replaced by the spoil recipe's result (`SpoilHelper.updateSpoilingStack`, `isSpoiled`).
- What spoils comes from `spoiled:spoil_recipe` recipes. The only one shipped is `data/spoiled/recipe/spoiling/initial_to_rotten_flesh.json`: `#spoiled:foods` (= `#c:foods`) into `minecraft:rotten_flesh`, active while `initializeSpoiling` is on. `#spoiled:foods_blacklist` exempts rotten flesh and both golden apples.
- Common config (`config/SpoiledConfig$Common`), defaults:
  - `spoilRate` 30: seconds between spoil updates (`SpoiledConfigCache` multiplies by 20 ticks).
  - `defaultSpoilTime` 40: updates before default food spoils, so 30 s x 40 = 20 minutes.
  - `initializeSpoiling` true, `mergeSpoilingFood` false, `spoilEverything` false, `defaultSpoilItem` `minecraft:rotten_flesh`.
  - `spoilBlacklist`: item ids that never spoil.
  - `containerModifier`, default `["minecraft:shulker_box,0"]`: per block entity type, `id,rate`.
  - `itemContainerModifier`: the same for item containers (backpacks, bundles), keyed by item id.
  - `ignoredComponents`: any stack carrying one of these component ids never spoils.
- Where updates happen (`handler/SpoilHandler`):
  - **Players** (`onPlayerTick`): every slot of the inventory, and the contents of item containers held there (via the `Item.ITEM` capability), in any dimension. Creative players are skipped.
  - **Block entities** (`onWorldTick`): every loaded block entity that answers `Capabilities.Item.BLOCK` with a null side. Unopened loot chests are skipped. **Only in the Overworld**: the handler returns for any other dimension.
  - **Entities that are a `Container`** (chest minecarts and the like), plus item frames (`ItemFrameMixin`). Dropped item entities are not a `Container`, so loose items on the ground do not spoil (inferred).
- The container modifier is a chance, not a slower clock: on each update a block entity whose type has rate `r` is updated only if `random < r` (inferred from the `nextDouble` compare). `0` stops spoiling, `0.2` spoils at a fifth of the speed on average.

## Which Cooking for Blockheads blocks slow or stop spoilage

None of them, by themselves. Cooking for Blockheads has no spoilage code: no class or data references Spoiled, and its "Preservation Chamber" upgrade (`UpgradeablePreservation`) only "prevents the last item of a slot from being used up" (en_us lang), so a recipe can't take the last of an ingredient.

Its storage blocks are ordinary item containers to Spoiled, so food in them spoils at full speed. These block entities implement Balm's `BalmContainerProvider` and so expose an item handler: `fridge`, `counter`, `cabinet`, `spice_rack`, `fruit_basket`, `cookie_jar`, `oven`, `tool_rack`, `chicken_sink`. The `cooking_table`, `cutting_board`, `toaster`, `sink` and `milk_jar` hold no item inventory Spoiled can see (inferred; the toaster keeps its two slots internal).

To make a fridge keep food, the pack sets Spoiled's `containerModifier`, which Spoiled's own config comment uses as its example: `"cookingforblockheads:fridge,0.2"`. The keys are block entity type ids, so one entry covers every dyed fridge, and `cookingforblockheads:counter` covers counters (cabinets have their own `cabinet` type). The config is Spoiled's common config (its file appears in `config/` on first launch); for Reborn it would ship through the pack's `config/` or `defaultconfigs/`.

## Can the kitchen be fed and emptied automatically

- **Feeding storage: yes.** Fridges, counters, cabinets, spice racks and fruit baskets expose item handlers, so pipes and belts can insert and extract.
- **The oven: yes, unless `disallowOvenAutomation` is set** (Cooking for Blockheads config, default false). Its cook time and fuel time scale with `ovenCookTimeMultiplier` and `ovenFuelTimeMultiplier`, and the Heating Unit runs it on energy.
- **Meal preparation: player only.** The cooking table has no inventory (`CookingTableBlockEntity` is only a menu provider). Crafting happens in `KitchenMenu`, which scans the multiblock (`KitchenMultiblockScanner`) and pulls ingredients from every `KitchenItemProvider` next to it. Any block entity with an item capability counts as one (`registerFallbackBlockEntityProvider`), so a vanilla chest or a modded crate joined to the kitchen supplies ingredients too. Nothing outputs a meal without a player clicking it.
- **Refined Cooking** adds the Kitchen Station, a Refined Storage node that makes a whole RS network a kitchen ingredient source (items and fluids such as water and milk), and the Kitchen Access Point with its Kitchen Network Card to reach a kitchen from a distance. It feeds the table from storage; it does not craft on its own.
- **Pantry for Blockheads** adds crops, trees, foods and recipes; its jar holds no spoilage or automation code.

## Pipes, belts, machines and Refined Storage

Spoiled only sees block entities that expose an item handler with no side, in the Overworld:

- **Chests, crates, fridges, machine inventories:** spoil, at full speed unless `containerModifier` names their type. A machine that exposes its slots that way spoils its inputs and outputs too.
- **Items moving through a pipe or belt:** spoil only if that block entity exposes its in-transit contents through the null-side item handler. Many transport blocks don't, so food in transit usually stops ageing until it lands (inferred; depends on each mod).
- **Refined Storage disks: don't spoil (inferred).** RS keeps network contents in its own storage, not in a block entity item handler, so Spoiled never walks them. Refined Storage's external storage reading a fridge does not change that fridge's own spoiling.
- **Other dimensions:** block entities and container entities don't spoil there at all; only what players carry does.

Stacks with different `SpoilTimer` values are different components, so they don't merge into one stack in inventories, storage or kitchen slots unless `mergeSpoilingFood` is on (which adds a crafting recipe to combine them; inferred from `StackFoodRecipe` and `MergeRecipeCondition`).

## Freshness and Quality Food's quality

- Quality is its own component (`quality_food:quality`, `registry/QFComponents`), independent of Spoiled's timer. A stack carries both, so freshness neither raises nor lowers quality.
- When food spoils, Spoiled replaces it with the recipe's result (rotten flesh), a new stack, so the quality is gone with it.
- Quality on cooked or crafted output is rolled by Quality Food's mixins on vanilla crafting (`CraftingMenuMixin`, `ResultSlotMixin`, `InventoryMenuMixin`, `CrafterBlockMixin`), furnaces (`AbstractFurnaceBlockEntityMixin`, `FurnaceResultSlotMixin`), campfires, and some other mods' crafting (Sophisticated Core, Tom's Storage, FastBench). There is no mixin into Cooking for Blockheads' `KitchenMenu`, so meals made at the cooking table come out without quality, whatever their ingredients' quality (inferred). The oven is its own block entity, not a vanilla furnace, so it likely drops quality too (inferred).
- A crafted meal starts a fresh spoil timer; it does not inherit its ingredients' freshness.
- If quality food should never rot, Spoiled's `ignoredComponents` can list `quality_food:quality`. That's an option, not a recommendation.

## Open in game

- Whether each Reborn pipe and belt exposes its in-transit items to Spoiled.
- Whether cooking table meals and oven output really lose quality.
- Confirm the fridge modifier with a short `spoilRate`.
