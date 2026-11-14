// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.life;

import io.github._5thlayer.mechanicalmasteryreborn.MechanicalMasteryReborn;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.ChainMachines;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LightningRodBlock;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/**
 * A Homunculus kindled as vanilla's carved pumpkin builds a golem: a Vital Spark used on a lightning rod standing on a clay
 * block calls down a strike that harms nothing, the clay and the rod become a Homunculus Stand, and the Homunculus rises on
 * the free space around the stand's base nearest the player. Used on the rod of a stand with no Homunculus, it raises a new
 * one there. The spark is used up. Without the right to build there, without life's chain unlocked for the island's team,
 * or with no free space by the base, the player is told why and nothing is used up.
 */
final class Kindling {

    private Kindling() {
    }

    static void kindle(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getLevel() instanceof ServerLevel level) || !event.getItemStack().is(Homunculus.VITAL_SPARK.get())) {
            return;
        }
        var rod = event.getPos();
        var state = level.getBlockState(rod);
        var base = rod.below();
        boolean fresh = state.getBlock() instanceof LightningRodBlock && level.getBlockState(base).is(Blocks.CLAY);
        boolean freeStand = state.is(Homunculus.STAND.get()) && state.getValue(HomunculusStandBlock.HALF) == DoubleBlockHalf.UPPER
                && level.getBlockEntity(base) instanceof HomunculusStandBlockEntity stand && !stand.seated();
        if (!fresh && !freeStand) {
            return;
        }
        event.setCanceled(true);
        Player player = event.getEntity();
        var space = HomunculusEntity.spaceBeside(level, base, player.position());
        String refusal = !player.mayBuild() || !level.mayInteract(player, rod) ? "not_allowed"
                : ChainMachines.lockedAt(level, base, Homunculus.STAND.get().defaultBlockState()).isPresent() ? "locked"
                : space.isEmpty() ? "no_room" : null;
        if (refusal != null) {
            if (player instanceof ServerPlayer serverPlayer) {
                serverPlayer.sendOverlayMessage(Component.translatable(MechanicalMasteryReborn.MOD_ID + ".homunculus.kindle." + refusal));
            }
            event.setCancellationResult(InteractionResult.FAIL);
            return;
        }
        ItemStack spark = event.getItemStack();
        if (!raise(level, base, space.get(), fresh, Homunculus.spent(spark))) {
            event.setCancellationResult(InteractionResult.FAIL);
            return;
        }
        spark.consume(1, player);
        event.setCancellationResult(InteractionResult.SUCCESS_SERVER);
    }

    /** The strike, the stand if it is new, and its Homunculus on {@code space}; false if none could be made. */
    private static boolean raise(ServerLevel level, BlockPos base, BlockPos space, boolean fresh, boolean spent) {
        var homunculus = Homunculus.HOMUNCULUS.get().create(level, EntitySpawnReason.TRIGGERED);
        if (homunculus == null) {
            return false;
        }
        var bolt = EntityType.LIGHTNING_BOLT.create(level, EntitySpawnReason.TRIGGERED);
        if (bolt != null) {
            bolt.setVisualOnly(true);
            bolt.snapTo(base.getX() + 0.5, base.getY() + 1, base.getZ() + 0.5, 0F, 0F);
            level.addFreshEntity(bolt);
        }
        if (fresh) {
            var lower = Homunculus.STAND.get().defaultBlockState();
            level.setBlock(base, lower, Block.UPDATE_ALL);
            level.setBlock(base.above(), lower.setValue(HomunculusStandBlock.HALF, DoubleBlockHalf.UPPER), Block.UPDATE_ALL);
        }
        if (!(level.getBlockEntity(base) instanceof HomunculusStandBlockEntity stand)) {
            return false;
        }
        homunculus.snapTo(space.getX() + 0.5, space.getY(), space.getZ() + 0.5, 0F, 0F);
        homunculus.boundTo(base);
        level.addFreshEntity(homunculus);
        stand.bind(homunculus.getUUID(), spent);
        return true;
    }
}
