// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.compat.voidworks.mixin;

import io.github._5thlayer.mechanicalmasteryreborn.challenge.QuintessenceRung;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * A Mutation Chamber climbs no Quartessence into Quintessence until its island's team has started its last stand (#262):
 * each upgrade asks {@link QuintessenceRung#lockedAt} first and while locked skips, spending nothing, as when it holds too
 * few, and Jade asks it the same ({@link QuintessenceRung.Chamber}). Voidworks has no hook of its own, and the mod
 * compiles against no suite mod, so this names only NeoForge's types.
 * {@link Pseudo}, so without Voidworks, whose class it targets by name, it never applies.
 */
@Pseudo
@Mixin(targets = "io.github._5thlayer.voidworks.mutation.MutationChamberBlockEntity")
public abstract class MutationChamberBlockEntityMixin implements QuintessenceRung.Chamber {

    /** Voidworks' {@code MutationItems.INPUT}: the slot of what waits to upgrade. */
    private static final int INPUT = 0;

    @Shadow
    @Final
    private ItemStacksResourceHandler inventory;

    @Inject(method = "upgrade", at = @At("HEAD"), cancellable = true)
    private void mechanicalmasteryreborn$waitForTheLastStand(ServerLevel level, CallbackInfo callback) {
        if (mechanicalmasteryreborn$waitsForTheLastStand()) {
            callback.cancel();
        }
    }

    @Override
    public boolean mechanicalmasteryreborn$waitsForTheLastStand() {
        BlockEntity chamber = (BlockEntity) (Object) this;
        return chamber.getLevel() != null
                && QuintessenceRung.lockedAt(chamber.getLevel(), chamber.getBlockPos(), inventory.getResource(INPUT).getItem());
    }
}
