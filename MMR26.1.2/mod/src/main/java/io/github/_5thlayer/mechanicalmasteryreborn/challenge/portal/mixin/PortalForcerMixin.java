// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.challenge.portal.mixin;

import io.github._5thlayer.mechanicalmasteryreborn.challenge.ArrivalPlatform;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.BlockUtil;
import net.minecraft.world.level.portal.PortalForcer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Lays an {@link ArrivalPlatform} around a portal the game makes in mid-air. It makes one there only when it found no
 * ground nearby, and only then picks its height with {@code Mth.clamp}, which is how this knows.
 */
@Mixin(PortalForcer.class)
public abstract class PortalForcerMixin {

    @Shadow
    @Final
    private ServerLevel level;

    @Unique
    private boolean mechanicalmasteryreborn$inMidAir;

    @Inject(method = "createPortal", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;clamp(III)I"))
    private void mechanicalmasteryreborn$foundNoGround(BlockPos origin, Direction.Axis axis,
            CallbackInfoReturnable<Optional<BlockUtil.FoundRectangle>> callback) {
        mechanicalmasteryreborn$inMidAir = true;
    }

    @Inject(method = "createPortal", at = @At("RETURN"))
    private void mechanicalmasteryreborn$laysPlatform(BlockPos origin, Direction.Axis axis,
            CallbackInfoReturnable<Optional<BlockUtil.FoundRectangle>> callback) {
        if (mechanicalmasteryreborn$inMidAir) {
            mechanicalmasteryreborn$inMidAir = false;
            callback.getReturnValue().ifPresent(portal -> ArrivalPlatform.build(level, portal, axis));
        }
    }
}
