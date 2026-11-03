// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.compat.theurgy.mixin;

import io.github._5thlayer.mechanicalmasteryreborn.challenge.ChainMachines;
import io.github._5thlayer.mechanicalmasteryreborn.compat.theurgy.ChainMachine;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * A Theurgy apparatus from a mix does no work while its chain is locked (#214): each tick its crafting behaviour asks
 * {@link ChainMachines#lockedAt} first, and while locked stops, as it does with no input. Theurgy has no hook of its own.
 * {@link Pseudo}, so without Theurgy, whose class it targets by name, it never applies.
 */
@Pseudo
@Mixin(targets = "com.klikli_dev.theurgy.content.behaviour.crafting.CraftingBehaviour")
public abstract class CraftingBehaviourMixin implements ChainMachine {

    @Shadow
    protected BlockEntity blockEntity;

    @Shadow
    public abstract void stopProcessing();

    /** Only with input to work on, the one case it would otherwise go on, so an idle machine asks nothing. */
    @Inject(method = "tickServer(ZZ)V", at = @At("HEAD"), cancellable = true)
    private void mechanicalmasteryreborn$locked(boolean isActive, boolean hasInput, CallbackInfo callback) {
        if (hasInput && mechanicalmasteryreborn$stopIfLocked()) {
            callback.cancel();
        }
    }

    @Override
    public boolean mechanicalmasteryreborn$stopIfLocked() {
        BlockEntity entity = blockEntity;
        if (entity == null || entity.getLevel() == null
                || ChainMachines.lockedAt(entity.getLevel(), entity.getBlockPos(), entity.getBlockState()).isEmpty()) {
            return false;
        }
        stopProcessing();
        return true;
    }
}
