// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.life;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import io.github._5thlayer.mechanicalmasteryreborn.MechanicalMasteryReborn;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import org.jspecify.annotations.Nullable;

/**
 * A player's assigning of a Homunculus's Supply or its Table (#368): sneak-clicked with an empty hand by a player who may
 * build at its stand, the Homunculus waits 30 seconds for the player's next right-click on a block, within its range of its
 * stand, where the player may build too. A storage is then its Supply; a block ringed by hoppers puts its Table above it, and
 * a meal on such a block puts it where the meal is. Each is kept on the stand. One out of range, one the player may not build
 * at, or any other block is refused and the assigning waits on. A sneak-click on the same Homunculus again cancels it. The
 * action bar says each step. A fake player's click, such as a Homunculus's own as its kindler, assigns nothing.
 */
final class SupplyAssignment {

    /** Ticks an assigning waits for its click. */
    private static final int WAIT = 30 * 20;
    private static final String TOLD = MechanicalMasteryReborn.MOD_ID + ".homunculus.supply.";

    private record Assigning(UUID homunculus, int until) {
    }

    /** Each assigning player's assigning, by the player's id. */
    private static final Map<UUID, Assigning> PENDING = new HashMap<>();

    private SupplyAssignment() {
    }

    /** {@code player} sneak-clicked {@code homunculus} empty-handed: starts assigning its Supply, or cancels it. */
    static synchronized void toggle(ServerPlayer player, HomunculusEntity homunculus) {
        var stand = homunculus.stand();
        if (stand == null || homunculus.standEntity() == null) {
            return;
        }
        if (!player.mayBuild() || !player.level().mayInteract(player, stand)) {
            tell(player, "not_allowed");
            return;
        }
        int now = player.level().getServer().getTickCount();
        var pending = PENDING.remove(player.getUUID());
        if (pending != null && pending.homunculus().equals(homunculus.getUUID()) && now <= pending.until()) {
            tell(player, "cancelled");
            return;
        }
        PENDING.put(player.getUUID(), new Assigning(homunculus.getUUID(), now + WAIT));
        tell(player, "assigning");
    }

    /** An assigning player's right-click on a block: the storage they assign, instead of what the click would do. */
    static synchronized void click(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getLevel() instanceof ServerLevel level) || !(event.getEntity() instanceof ServerPlayer player) || player.isFakePlayer()) {
            return;
        }
        var pending = PENDING.get(player.getUUID());
        if (pending == null) {
            return;
        }
        if (level.getServer().getTickCount() > pending.until() || !(level.getEntity(pending.homunculus()) instanceof HomunculusEntity homunculus)
                || homunculus.standEntity() == null) {
            PENDING.remove(player.getUUID());
            return;
        }
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS_SERVER);
        var stand = homunculus.standEntity();
        var pos = event.getPos();
        var face = event.getFace() == null ? Direction.UP : event.getFace();
        if (!level.mayInteract(player, pos)) {
            tell(player, "not_allowed");
        } else if (level.getCapability(Capabilities.Item.BLOCK, pos, face) != null) {
            if (!stand.inRange(pos)) {
                tell(player, "too_far");
            } else {
                PENDING.remove(player.getUUID());
                stand.assignSupply(pos, face);
                tell(player, "set");
            }
        } else if (tableAt(level, pos) instanceof BlockPos table) {
            if (!stand.inRange(table)) {
                tell(player, "too_far");
            } else {
                PENDING.remove(player.getUUID());
                stand.assignTable(table);
                player.sendOverlayMessage(Component.translatable(MechanicalMasteryReborn.MOD_ID + ".homunculus.table.set"));
            }
        } else {
            tell(player, "not_storage");
        }
    }

    /** The Table a click on {@code pos} assigns: above a block ringed by hoppers, or where a meal stands on one; else null. */
    private static @Nullable BlockPos tableAt(ServerLevel level, BlockPos pos) {
        if (level.getBlockState(pos).is(Homunculus.PLACES) && Homunculus.ringed(level, pos.below())) {
            return pos;
        }
        return Homunculus.ringed(level, pos) ? pos.above() : null;
    }

    private static void tell(ServerPlayer player, String what) {
        player.sendOverlayMessage(Component.translatable(TOLD + what));
    }
}
