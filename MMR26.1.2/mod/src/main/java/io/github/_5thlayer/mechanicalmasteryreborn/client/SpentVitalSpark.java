// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.client;

import com.mojang.serialization.MapCodec;
import io.github._5thlayer.mechanicalmasteryreborn.life.Homunculus;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.conditional.ConditionalItemModelProperty;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

/**
 * The item model condition {@code mechanicalmasteryreborn:spent}: whether a Vital Spark is spent ({@link Homunculus#spent}),
 * so its item model greys it (#375).
 */
public record SpentVitalSpark() implements ConditionalItemModelProperty {

    public static final MapCodec<SpentVitalSpark> MAP_CODEC = MapCodec.unit(new SpentVitalSpark());

    @Override
    public boolean get(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity owner, int seed, ItemDisplayContext context) {
        return Homunculus.spent(stack);
    }

    @Override
    public MapCodec<SpentVitalSpark> type() {
        return MAP_CODEC;
    }
}
