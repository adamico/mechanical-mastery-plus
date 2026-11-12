# Does a fake player work FD's Cutting Board and Butchery's butchering?

Research for #350 (part of #345). Question: can the companion mod's Homunculus, right-clicking the block in front of it through a NeoForge `FakePlayer`, run the manual food steps of the profile's kept food mods on 26.1.2?

## Answer

Yes, for all three mods read, with one condition: the Homunculus must click **through `ServerPlayerGameMode.useItemOn`** (the same path a real player's click takes), not by calling `Block#useItemOn` / `useWithoutItem` directly. Butchery and Butcher's Delight do most of their work in `RightClickBlock` event handlers, which only fire on that path. None of the steps checks for a real player, a sneak, or the client; all read the tool from the **main hand**.

## Sources

Jars decompiled with Vineflower 1.11.2 from the CurseForge profile `~/CF/Instances/Mechanical Mastery Reborn/mods`:

- `FarmersDelight-26.1.2-1.3.3-fix8.jar`
- `butchery-5.2-neoforge-26.1.2.jar`
- `butchersdelight-neoforge-26.1.2-2.1.1.jar`
- NeoForge `neoforge-26.1.2.77-universal.jar` (Gradle cache) for `FakePlayer` and `CommonHooks`.

These mods are in the profile but not yet in `MMR26.1.2/mods/` (no `.pw.toml`); the versions above are the ones read.

## NeoForge's FakePlayer

`net.neoforged.neoforge.common.util.FakePlayer extends ServerPlayer`, so any `instanceof ServerPlayer` / `instanceof Player` check passes. It overrides `sendSystemMessage`, `awardStat`, `openMenu`, `die`, `tick` and a few others into no-ops, and `isFakePlayer()` returns true. `CommonHooks.onRightClickBlock(Player, InteractionHand, BlockPos, BlockHitResult)` posts `PlayerInteractEvent.RightClickBlock`; it is called from the server's `ServerPlayerGameMode.useItemOn`, not from the block methods. The companion mod already gets one with `FakePlayerFactory.getMinecraft(level)` in `compat/gateways/GatewaysGates.java`; the Homunculus will want its own `GameProfile` (so it doesn't share the shared Minecraft fake player) and to put the tool in its main hand before each click.

## Farmer's Delight: Cutting Board

`vectorwing.farmersdelight.common.block.CuttingBoardBlock#useItemOn` and `CuttingBoardBlockEntity#processStoredItemUsingTool`:

- Uses `player.getMainHandItem()` regardless of the hand passed.
- Empty main hand: takes the stored item back into `player.getInventory()`.
- Main hand item the board accepts: places it, and sets the main hand to the remainder.
- Otherwise: treats the main hand item as the tool and, if a `cutting` recipe matches, **spawns the results as item entities** beside the board (direction = facing rotated counter-clockwise), damages the tool, takes one item off the board.
- No sneak, client, creative-only or real-player check. `player` may even be `null` in `processStoredItemUsingTool` (FD's own dispenser path), so the tool step does not need a player at all.
- Side effects for a fake player: `awardStat` (no-op on FakePlayer), `sendOverlayMessage` with remaining count / invalid-item text, and the `USE_CUTTING_BOARD` advancement trigger because a FakePlayer is a `ServerPlayer`. Check in game that the advancement trigger doesn't log or write a stray advancements file for the fake UUID.

So a Homunculus works a board in two clicks: one holding the ingredient (place), then one per cut holding the knife. The outputs land on the floor, so something must collect them.

## Butchery: carcass butchering

MCreator code in `net.mcreator.butchery`. The carcass is dropped by `*CarcassdropProcedure` on `LivingDeathEvent` (killing the mob, not a right-click). Then, on the placed carcass block (e.g. `CowcarcassBlock`):

1. **Bleeding**: `CowcarcassBlock#useWithoutItem` calls `CowcarcassbleedingProcedure`, which needs a main-hand item tagged `c:cleaver` (or `forge:cleaver`). It sets `isBleeding` on the block entity and runs a timed loop until drained (or instantly if the `INSTANT_BLEED` config is on). `useWithoutItem` is reached because the block doesn't override `useItemOn`, so the default `TRY_WITH_EMPTY_HAND` falls through, as long as the clicker is **not** sneaking with an item.
2. **Cutting up**: `CowcutupProcedure` is an `@EventBusSubscriber` on `PlayerInteractEvent.RightClickBlock` (main hand only). It needs a main-hand item tagged `c:skinning_knives` (and *not* a cleaver), the drained carcass block, and its `blockstate` stage; each click advances one stage, damages the knife, swings, and **spawns the drops as item entities** (29 `new ItemEntity` sites in the cow procedure). No sneak, creative, client or real-player checks.

Each animal has its own `*cutupProcedure`/`*bleedingProcedure` copy of the same shape. Because cutting up is event-driven, a Homunculus that calls the block's methods directly would bleed the carcass but never cut it up.

## Butcher's Delight: hooks and roasters

`net.mcreator.butchersdelight.procedures.Prohooked*Procedure.onRightClickBlock` (wired from `ButchersDelight` to the right-click-block event) checks `event_hand == player.getUsedItemHand()` (MAIN_HAND when not using an item, fine for a fake player), an empty block above a sturdy top face, and the matching dead-animal item (e.g. `DEAD_COW`) in the main hand. It places the hooked block and removes one from `player.getInventory()` via `clearOrCountMatchingItems`, so the fake player must hold the carcass item in its inventory/main hand. No sneak or real-player check. Roaster interactions (`ProRoaster*Procedure`) are event-driven the same way.

## Implications for the Homunculus

- Click through `fakePlayer.gameMode.useItemOn(fakePlayer, level, mainHandStack, InteractionHand.MAIN_HAND, hitResult)` so `RightClickBlock` fires; aim the `BlockHitResult` at the block in front.
- Keep the fake player standing (never `setShiftKeyDown(true)`), else held-item clicks skip the block's `use*` methods.
- Copy the Homunculus's held tool into the fake player's main hand before the click and copy it back after (tools take durability damage; placement steps consume the stack).
- All three mods drop their outputs as item entities, and FD's empty-hand take-back goes to the fake player's inventory; the Homunculus needs to collect from the floor and drain the fake inventory.
- Not covered by right-clicking: Butchery's carcass comes from killing the mob (`LivingDeathEvent`), a separate step.
- Not read here: Cooking for Blockheads, Croptopia, Quality Food, Refined Cooking (also in the profile). FD's Cooking Pot, Skillet, Stove and feasts were not read either; the question only named the Cutting Board.
