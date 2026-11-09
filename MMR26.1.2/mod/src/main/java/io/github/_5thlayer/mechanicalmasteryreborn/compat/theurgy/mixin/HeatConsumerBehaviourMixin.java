// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.compat.theurgy.mixin;

import io.github._5thlayer.mechanicalmasteryreborn.compat.theurgy.BrazierHeat;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * A chain machine finds no heat in a Pyromantic Brazier below it while {@link BrazierHeat#cold} (#329). Theurgy's heat
 * check asks the block below for heat first and then its own heat receiver, which a Caloric Flux Emitter fills, so only
 * the first is cut. {@link Pseudo}, as the crafting mixins are.
 */
@Pseudo
@Mixin(targets = "com.klikli_dev.theurgy.content.behaviour.heat.HeatConsumerBehaviour")
public abstract class HeatConsumerBehaviourMixin {

    @Unique
    private static final Identifier mechanicalmasteryreborn$BRAZIER = Identifier.fromNamespaceAndPath("theurgy", "pyromantic_brazier");

    @Shadow
    @Final
    private BlockEntity blockEntity;

    @Inject(method = "hasHeatProvider()Z", at = @At("HEAD"), cancellable = true)
    private void mechanicalmasteryreborn$coldBrazier(CallbackInfoReturnable<Boolean> callback) {
        BlockEntity entity = blockEntity;
        if (entity == null || entity.getLevel() == null) {
            return;
        }
        var below = entity.getBlockPos().below();
        var block = entity.getLevel().getBlockState(below).getBlock();
        if (mechanicalmasteryreborn$BRAZIER.equals(BuiltInRegistries.BLOCK.getKey(block)) && BrazierHeat.cold(entity.getLevel(), below)) {
            callback.setReturnValue(false);
        }
    }
}
