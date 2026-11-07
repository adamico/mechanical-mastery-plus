// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.essence.mixin;

import io.github._5thlayer.mechanicalmasteryreborn.essence.Making;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * A block entity's tick in a server level, as every machine does its work, counts as that block making things
 * ({@link Making}), so a Transmuted Essence it makes is stamped with its island's team's attempt (#322).
 */
@Mixin(targets = "net.minecraft.world.level.chunk.LevelChunk$BoundTickingBlockEntity")
public abstract class BoundTickingBlockEntityMixin {

    @Shadow
    @Final
    private BlockEntity blockEntity;

    @Inject(method = "tick", at = @At("HEAD"))
    private void mechanicalmasteryreborn$making(CallbackInfo callback) {
        if (blockEntity.getLevel() instanceof ServerLevel level) {
            Making.atBlock(level, blockEntity.getBlockPos());
        }
    }

    @Inject(method = "tick", at = @At("RETURN"))
    private void mechanicalmasteryreborn$made(CallbackInfo callback) {
        Making.done();
    }
}
