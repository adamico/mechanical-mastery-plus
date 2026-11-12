# Which idle hook each of life's chain machines offers (#356)

Question: for each block #353 puts in `chain_machines/life`, what lets the companion mod stop its work while the team
hasn't started life, the way `compat/theurgy/mixin/CraftingBehaviourMixin` idles Theurgy's apparatus? Where none
exists, which method would a mixin target?

Sources: the jars in the Reborn CurseForge profile (`~/CF/Instances/Mechanical Mastery Reborn/mods`), inspected with
`javap -p -c`. Versions: Agritech Evolved 2.2.0.7, Animal Pen 2.5.2, Butchery 5.2, Pantry for Blockheads 26.1.2.8,
Farmer's Delight 26.1.2-1.3.3-fix8.

## Answer

None of the six mods fires an event or exposes a capability that gates work. NeoForge itself has no
"block entity may tick" event. Every machine needs a `@Pseudo` mixin, as Theurgy and Neo Vitae already have, each
asking `ChainMachines.lockedAt(level, pos, state)` at the head of the method below and cancelling.

| Machine | Registry ids | Work happens in | Mixin target |
|---|---|---|---|
| Agritech planters (basic tier) | `agritechevolved:basic_planter`, `basic_<wood>_planter` (acacia, bamboo, birch, cherry, crimson, dark_oak, jungle, mangrove, pale_oak, spruce, warped), `terracotta_planter`, `<colour>_terracotta_planter` (16 colours) | block entity ticker | `com.misterd.agritechevolved.blockentity.custom.PlanterBlockEntity#tick(Level, BlockPos, BlockState, PlanterBlockEntity)` (public static): grows, harvests and pushes output below |
| Agritech Advanced Planter | `agritechevolved:advanced_planter` | block entity ticker (FE-powered) | `...blockentity.custom.AdvancedPlanterBlockEntity#tick(Level, BlockPos, BlockState, AdvancedPlanterBlockEntity)` (public static) |
| Animal Pen | `animal_pen:animal_pen_<wood>` (oak, spruce, birch, jungle, acacia, dark_oak, mangrove, cherry, bamboo, crimson, warped, pale_oak); the same entity base also backs `aquarium_block` and the `*aviary` blocks | not the ticker: `AbstractAnimalPenBlockEntity#tick()` only counts ticks and plays death animations. Products (milk, wool, eggs, breeding...) come from interactions, by a player's use or by a dispenser (Animal Pen's own `DispenserMixin`) | `lv.id.bonne.animalpen.blocks.entities.AbstractAnimalPenBlockEntity#performInteraction(AnimalInteractionExecutor, ServerLevel, ItemStack)` (private, returns `AbstractAnimalPenBlockEntity$InteractionResult`); both `interactWithPen` overloads funnel into it. A player-only alternative is cancelling `PlayerInteractEvent.RightClickBlock`, but that misses dispensers |
| Butchery Meat Grinder | `butchery:meat_grinder` | no ticker (the block entity has none): `MeatgrinderBlock#tick(BlockState, ServerLevel, BlockPos, RandomSource)` is a self-rescheduling scheduled tick that runs `MeatgrinderrecipeprocedureProcedure.execute` then `scheduleTick` | `net.mcreator.butchery.procedures.MeatgrinderrecipeprocedureProcedure#execute(LevelAccessor, double, double, double)` (public static). Cancelling here keeps the reschedule; cancelling `MeatgrinderBlock#tick` instead would break the loop for good |
| Butchery Basin | `butchery:basin` | same shape: `BasinBlock#tick` runs `BasinupdatetickProcedure.execute` then reschedules | `net.mcreator.butchery.procedures.BasinupdatetickProcedure#execute(LevelAccessor, double, double, double, BlockState)` (public static) |
| Pantry's Artisan Press | `pantryforblockheads:artisan_press` | block entity server ticker | `net.blay09.mods.pantryforblockheads.block.entity.ArtisanPressBlockEntity#serverTick(Level, BlockPos, BlockState, ArtisanPressBlockEntity)` (public static); call its private `setProcessing(false)` (via `@Invoker`/`@Shadow`) on cancel so it stops animating, as Theurgy's mixin calls `stopProcessing()` |
| FD Cooking Pot | `farmersdelight:cooking_pot` | block entity server ticker | `vectorwing.farmersdelight.common.block.entity.CookingPotBlockEntity#cookingTick(Level, BlockPos, BlockState, CookingPotBlockEntity)` (public static). Narrower: `canCook(CookingPotRecipe)` (protected, boolean) returning false stops only cooking and leaves meal-to-output moving, which is harmless |

## Notes

- The Butchery procedures take coordinates, not a block entity: the mixin rebuilds the `BlockPos` and reads the state
  from the `LevelAccessor` (cast to `Level`) before asking `lockedAt`.
- For the static tickers the block entity arrives as the last argument, so the inject reads it from there.
- Only the ids above are the machines' items; the pack's `chain_machines/life` tag lists them (#353) and the mixins
  stay generic, asking the tag through `lockedAt`.
