// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn;

import java.util.List;
import java.util.stream.IntStream;

import io.github._5thlayer.mechanicalmasteryreborn.recycler.HandDriveBlock;
import io.github._5thlayer.mechanicalmasteryreborn.recycler.Recycler;
import net.minecraft.resources.Identifier;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * The mod's server config. The Recycler's settings: the yield (the line's glossary: Yield), the share of a mix, or of what
 * made an item, that one decompose or recycle returns, on every core and by hand; the turns each core tier takes; and the FE
 * drive's pace, cost and store; and the food a hand drive's turn costs.
 *
 * <p>And the recipe types recycling reads as assembling. The mod names no suite mod (libworks ADR 0013), so the pack names
 * Craftworks' assembling type there.
 * NeoForge sends a server config to each client, so EMI shows the yield a server plays with.
 *
 * <p>And C.O.D.'s pace while dormant, once awake, and how it grows while a challenge is played, and the cap of a team's Inflation.
 *
 * <p>And the share of a sale a team is paid while it plays the money challenge.
 */
public final class ServerConfig {

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    /** Every core's yield, and decomposing by hand's: a quarter, as Factorio's recycler returns. */
    public static final ModConfigSpec.DoubleValue YIELD = BUILDER
            .comment("The share of a mix, or of what made an item, that a core of any tier returns per decompose or recycle,",
                    "each part of an item left over as the chance of one more. Decomposing by hand returns the same.")
            .defineInRange("recycler.yield", 0.25, 0.0, 1.0);

    /** The turns a decompose or a recycle takes on each core tier, the tier-1 one first: a higher core runs faster (#189). */
    private static final List<ModConfigSpec.IntValue> TURNS = IntStream.rangeClosed(1, Recycler.TIERS)
            .mapToObj(tier -> BUILDER
                    .comment("The turns of its drives a decompose or a recycle takes on a tier-" + tier + " core.")
                    .defineInRange("recycler.turns.tier_" + tier, 10 - 2 * tier, 1, 72000))
            .toList();

    /** What a hand drive's turn that counts costs the player turning it: about a hunger point an essence (#222). */
    public static final ModConfigSpec.DoubleValue HAND_DRIVE_EXHAUSTION = BUILDER
            .comment("The food exhaustion each turn of a hand drive that counts adds to the player who turned it, as sprinting does.",
                    "0.5 is about one hunger point per essence on a tier-1 core (8 turns a decompose, 4 exhaustion a point).")
            .defineInRange("recycler.hand_drive.exhaustion", 0.5, 0.0, 40.0);

    /** How often an FE drive turns: the hand drive's cap, so one matches one player cranking without a pause. */
    public static final ModConfigSpec.IntValue FE_DRIVE_INTERVAL = BUILDER
            .comment("The ticks between two turns of one FE drive. 5 is as fast as a hand drive turns, one player cranking without a pause.")
            .defineInRange("recycler.fe_drive.interval", HandDriveBlock.CAP, 1, 72000);

    /** What an FE drive spends on a turn that counts: cheap enough for an early engine. */
    public static final ModConfigSpec.IntValue FE_DRIVE_FE_PER_TURN = BUILDER
            .comment("The FE an FE drive spends on each turn that counts. It spends nothing on a turn its Recycler can't use.")
            .defineInRange("recycler.fe_drive.fe_per_turn", 40, 1, 1_000_000);

    /** How much an FE drive stores, in turns. */
    public static final ModConfigSpec.IntValue FE_DRIVE_STORE_TURNS = BUILDER
            .comment("How many turns' worth of FE an FE drive stores. Full, it takes no more until it spends some.")
            .defineInRange("recycler.fe_drive.store_turns", 10, 1, 1_000_000);

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

    /** How much faster an awake C.O.D. grows each minute of a challenge, from its waking pace: a placeholder until the balance pass (#207). */
    public static final ModConfigSpec.DoubleValue COD_PACE_GROWTH = BUILDER
            .comment("The share of its waking pace an awake C.O.D.'s pace grows by each minute since its team's challenge began,",
                    "without end: 0.1 is twice the waking pace ten minutes in, three times twenty minutes in. 0 keeps the waking pace.",
                    "It never makes more than one essence a tick.")
            .defineInRange("cod.pace_growth", 0.1, 0.0, 100.0);

    /** The excess Inflation may reach while a challenge is played: a placeholder until the balance pass (#207). */
    public static final ModConfigSpec.IntValue COD_INFLATION_CAP = BUILDER
            .comment("The excess of Advanced essence C.O.D. may make beyond what a team decomposes while it plays a challenge:",
                    "its Inflation's cap, which the team's bar fills toward. 256 is four stacks, a placeholder.")
            .defineInRange("cod.inflation_cap", 256, 1, 1_000_000);

    /** The share of a sale the money handicap pays (#213). */
    public static final ModConfigSpec.DoubleValue MONEY_SALE_SHARE = BUILDER
            .comment("The share of a sale's price AdminShop pays a team while it plays the money challenge, by hand or by a Seller,",
                    "rounded down to the cent. 0.5 is half. Buying is unaffected.")
            .defineInRange("money.sale_share", 0.5, 0.0, 1.0);

    static final ModConfigSpec SPEC = BUILDER.build();

    private ServerConfig() {
    }

    public static void register(ModContainer container) {
        container.registerConfig(ModConfig.Type.SERVER, SPEC);
    }

    /** The yield of every core, and of decomposing by hand. */
    public static double yield() {
        return SPEC.isLoaded() ? YIELD.getAsDouble() : YIELD.getDefault();
    }

    /** The turns a decompose or a recycle takes on a core of {@code tier}, from 1 to {@link Recycler#TIERS}. */
    public static int turnsOf(int tier) {
        var turns = TURNS.get(tier - 1);
        return SPEC.isLoaded() ? turns.getAsInt() : turns.getDefault();
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

    /** The share of its waking pace an awake C.O.D.'s pace grows by each minute of a challenge. */
    public static double codPaceGrowth() {
        return SPEC.isLoaded() ? COD_PACE_GROWTH.getAsDouble() : COD_PACE_GROWTH.getDefault();
    }

    /** The excess Inflation may reach while a challenge is played. */
    public static int inflationCap() {
        return SPEC.isLoaded() ? COD_INFLATION_CAP.getAsInt() : COD_INFLATION_CAP.getDefault();
    }

    /** The share of a sale the money handicap pays. */
    public static double moneySaleShare() {
        return SPEC.isLoaded() ? MONEY_SALE_SHARE.getAsDouble() : MONEY_SALE_SHARE.getDefault();
    }

    /** The food exhaustion a hand drive's turn that counts adds. */
    public static double handDriveExhaustion() {
        return SPEC.isLoaded() ? HAND_DRIVE_EXHAUSTION.getAsDouble() : HAND_DRIVE_EXHAUSTION.getDefault();
    }

    /** The ticks between two turns of one FE drive. */
    public static int feDriveInterval() {
        return SPEC.isLoaded() ? FE_DRIVE_INTERVAL.getAsInt() : FE_DRIVE_INTERVAL.getDefault();
    }

    /** The FE an FE drive spends on a turn that counts. */
    public static int feDriveFePerTurn() {
        return SPEC.isLoaded() ? FE_DRIVE_FE_PER_TURN.getAsInt() : FE_DRIVE_FE_PER_TURN.getDefault();
    }

    /** How many turns' worth of FE an FE drive stores. */
    public static int feDriveStoreTurns() {
        return SPEC.isLoaded() ? FE_DRIVE_STORE_TURNS.getAsInt() : FE_DRIVE_STORE_TURNS.getDefault();
    }
}
