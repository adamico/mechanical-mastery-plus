// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn;

import java.util.List;

import io.github._5thlayer.mechanicalmasteryreborn.recycler.RecyclerCoreBlockEntity;
import net.minecraft.resources.Identifier;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * The mod's server config. The Recycler's settings: each core tier's yield (the line's glossary: Yield), the share of a mix,
 * or of what made an item, that one decompose or recycle returns, and decomposing by hand the lowest core's.
 *
 * <p>And the recipe types recycling reads as assembling. The mod names no suite mod (libworks ADR 0013), so the pack names
 * Craftworks' assembling type there.
 * NeoForge sends a server config to each client, so EMI shows the yield a server plays with.
 *
 * <p>And C.O.D.'s pace while dormant, and once awake.
 */
public final class ServerConfig {

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    /** A tier-1 core's yield, and decomposing by hand's: a quarter, as Factorio's recycler returns. */
    public static final ModConfigSpec.DoubleValue TIER_1_YIELD = BUILDER
            .comment("The share of a mix, or of what made an item, that a tier-1 core returns per decompose or recycle,",
                    "each part of an item left over as the chance of one more. Decomposing by hand returns the same.")
            .defineInRange("recycler.yield.tier_1", 0.25, 0.0, 1.0);

    /** Recipe types of sized ingredients and results, run backwards after smelting and before crafting. */
    public static final ModConfigSpec.ConfigValue<List<? extends String>> ASSEMBLING_TYPES = BUILDER
            .comment("Recipe types recycling reads as assembling, after smelting and before crafting: each a list of sized",
                    "\"ingredients\" and of \"results\", the first what it makes. The pack names Craftworks' assembling type.")
            .defineListAllowEmpty("recycler.assembling_types", List.of(), () -> "", entry -> entry instanceof String id
                    && Identifier.tryParse(id) != null);

    /** How often a dormant C.O.D. makes one Advanced essence: a little faster than decomposing by hand rests an essence. */
    public static final ModConfigSpec.IntValue COD_DORMANT_INTERVAL = BUILDER
            .comment("The ticks a dormant C.O.D. takes to make one Advanced essence while its slot holds less than a stack.",
                    "120 is 6 seconds, a little faster than decomposing by hand (8 seconds an essence).")
            .defineInRange("cod.dormant_interval", 120, 1, 72000);

    /** How often an awake C.O.D. makes one Advanced essence between challenges: about one hand drive's throughput. */
    public static final ModConfigSpec.IntValue COD_WAKING_INTERVAL = BUILDER
            .comment("The ticks an awake C.O.D. takes to make one Advanced essence while no challenge is played.",
                    "40 is 2 seconds, about what one hand drive turned without a pause decomposes (8 turns, 5 ticks apart).")
            .defineInRange("cod.waking_interval", 40, 1, 72000);

    static final ModConfigSpec SPEC = BUILDER.build();

    private ServerConfig() {
    }

    public static void register(ModContainer container) {
        container.registerConfig(ModConfig.Type.SERVER, SPEC);
    }

    /** The yield of a core of {@code tier}; tier 1 is the only one yet. */
    public static double yieldOf(int tier) {
        return SPEC.isLoaded() ? TIER_1_YIELD.getAsDouble() : TIER_1_YIELD.getDefault();
    }

    /** The recipe types recycling reads as assembling. */
    public static List<Identifier> assemblingTypes() {
        return (SPEC.isLoaded() ? ASSEMBLING_TYPES.get() : ASSEMBLING_TYPES.getDefault()).stream().map(Identifier::parse).toList();
    }

    /** The ticks a dormant C.O.D. takes to make one essence. */
    public static int codDormantInterval() {
        return SPEC.isLoaded() ? COD_DORMANT_INTERVAL.getAsInt() : COD_DORMANT_INTERVAL.getDefault();
    }

    /** The ticks an awake C.O.D. takes to make one essence. */
    public static int codWakingInterval() {
        return SPEC.isLoaded() ? COD_WAKING_INTERVAL.getAsInt() : COD_WAKING_INTERVAL.getDefault();
    }

    /** Decomposing by hand's yield: the lowest core's, so a Recycler never does worse. */
    public static double byHand() {
        return yieldOf(RecyclerCoreBlockEntity.TIER);
    }
}
