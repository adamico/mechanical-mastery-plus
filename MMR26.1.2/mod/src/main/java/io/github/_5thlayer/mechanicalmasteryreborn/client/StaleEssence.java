// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.client;

import com.mojang.serialization.MapCodec;
import io.github._5thlayer.mechanicalmasteryreborn.essence.Attempts;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.conditional.ConditionalItemModelProperty;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

/**
 * The item model condition {@code mechanicalmasteryreborn:stale}: whether a Transmuted Essence is stale to its viewer
 * ({@link Attempts#staleToViewer}), so its item model dims it (#322).
 */
public record StaleEssence() implements ConditionalItemModelProperty {

    public static final MapCodec<StaleEssence> MAP_CODEC = MapCodec.unit(new StaleEssence());

    @Override
    public boolean get(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity owner, int seed, ItemDisplayContext context) {
        return Attempts.staleToViewer(stack);
    }

    @Override
    public MapCodec<StaleEssence> type() {
        return MAP_CODEC;
    }
}
