// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.compat.food;

import java.util.function.Supplier;

import com.mojang.serialization.Codec;
import io.github._5thlayer.mechanicalmasteryreborn.MechanicalMasteryReborn;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * An Animal Pen's feed quality: the quality of what its animals were last fed, kept on the cage the pen holds them in, so its products roll
 * their quality at the odds that feed gives (#365): the better the feed, the better the eggs, milk and wool. A pen never
 * fed, or fed plain food, rolls at Quality Food's own odds.
 */
public final class PenFeed {

    private static final DeferredRegister.DataComponents COMPONENTS =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, MechanicalMasteryReborn.MOD_ID);

    /** The quality level of the feed a cage's animals were last given. */
    public static final Supplier<DataComponentType<Integer>> FEED_QUALITY = COMPONENTS.registerComponentType("feed_quality",
            builder -> builder.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT));

    private PenFeed() {
    }

    public static void register(IEventBus modBus) {
        COMPONENTS.register(modBus);
    }

    /** Remembers on {@code cage} that its animals were fed {@code feed}. */
    public static void fed(ItemStack cage, ItemStack feed) {
        if (cage.isEmpty()) {
            return;
        }
        int level = FoodQuality.level(feed);
        if (level > 0) {
            cage.set(FEED_QUALITY.get(), level);
        } else {
            cage.remove(FEED_QUALITY.get());
        }
    }

    /** Rolls the quality of {@code product}, of the animals in {@code cage}, at the odds their last feed gives. */
    public static void roll(ItemStack cage, ItemStack product, RandomSource random, HolderLookup.Provider registries) {
        int feed = cage.getOrDefault(FEED_QUALITY.get(), 0);
        FoodQuality.roll(product, 1, FoodQuality.weight(feed, registries), random, registries);
    }
}
