// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.compat.food.mixin;

import java.util.Optional;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.neoforged.neoforge.capabilities.BlockCapability;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Cooking for Blockheads' fridge with an ice unit keeps food for good (#363). Spoiled's config slows a fridge by its
 * block entity type alone, a quarter of the normal rate, and the ice unit is a state of the fridge's block, so Spoiled's
 * world tick finds no item handler on a fridge with one, in its own block or in the fridge it stacks with, and leaves
 * its food as it is. Each of a two-high fridge's blocks is a fridge, of any colour, and either may hold the unit.
 * {@link Pseudo}, so without Spoiled it never applies.
 */
@Pseudo
@Mixin(targets = "com.mrbysco.spoiled.handler.SpoilHandler")
public abstract class SpoilHandlerMixin {

    @Redirect(method = "onWorldTick", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/level/ServerLevel;getCapability(Lnet/neoforged/neoforge/capabilities/BlockCapability;Lnet/minecraft/core/BlockPos;Ljava/lang/Object;)Ljava/lang/Object;"))
    private Object mechanicalmasteryreborn$iceUnitKeepsFood(ServerLevel level, BlockCapability<Object, Object> capability,
                                                            BlockPos pos, Object side) {
        if (iceUnit(level.getBlockState(pos)).isPresent()
                && (hasIceUnit(level, pos) || hasIceUnit(level, pos.below()) || hasIceUnit(level, pos.above()))) {
            return null;
        }
        return level.getCapability(capability, pos, side);
    }

    private static boolean hasIceUnit(ServerLevel level, BlockPos pos) {
        return iceUnit(level.getBlockState(pos)).orElse(false);
    }

    /** Whether a fridge has its ice unit, or empty for any other block: only the fridge has the property. */
    private static Optional<Boolean> iceUnit(BlockState state) {
        for (Property<?> property : state.getProperties()) {
            if (property instanceof BooleanProperty ice && "ice_unit".equals(ice.getName())) {
                return Optional.of(state.getValue(ice));
            }
        }
        return Optional.empty();
    }
}
