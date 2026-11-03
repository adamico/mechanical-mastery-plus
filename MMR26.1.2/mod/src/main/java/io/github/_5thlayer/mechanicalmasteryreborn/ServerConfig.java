// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import io.github._5thlayer.mechanicalmasteryreborn.essence.Essence;
import io.github._5thlayer.mechanicalmasteryreborn.recycler.HandDriveBlock;
import io.github._5thlayer.mechanicalmasteryreborn.recycler.Recycler;
import net.minecraft.resources.Identifier;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * The mod's server config. The Recycler's settings: the yield (the line's glossary: Yield), the share of a mix, or of what
 * made an item, that one decompose or recycle returns, on every core and by hand; the turns each core tier takes; the hand cooldown; the FE
 * drive's pace, cost and store; and the food a hand drive's turn costs.
 *
 * <p>And the recipe types recycling reads as assembling. The mod names no suite mod (libworks ADR 0013), so the pack names
 * Craftworks' assembling type there.
 * NeoForge sends a server config to each client, so EMI shows the yield a server plays with.
 *
 * <p>And C.O.D.'s pace while dormant, once awake, and how it grows while a challenge is played, and the cap of a team's Inflation.
 *
 * <p>And the share of a sale a team is paid while it plays the money challenge, and the quota a challenge's Port takes to win it.
 *
 * <p>And the life handicap's faster hunger and Diet, and whether it pauses in Neo Vitae's Demon Realm.
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
                    .comment("The turns of its drives a decompose or a recycle takes on a tier-" + tier + " core. The tier-1 core's 8",
                            "turns of 5 ticks is 2 seconds on one hand drive cranked without a pause, C.O.D.'s waking pace",
                            "(cod.waking_interval), and at the hand drive's 0.5 exhaustion a turn about one hunger point an essence.")
                    .defineInRange("recycler.core.tier_" + tier + ".turns", 10 - 2 * tier, 1, 1_000_000))
            .toList();

    /** Ticks an essence rests after a decompose by hand: four times a hand drive's 8 turns, so a Recycler is much the faster way. */
    public static final ModConfigSpec.IntValue HAND_COOLDOWN = BUILDER
            .comment("The ticks an essence rests after a decompose by hand, shown on the hotbar. 160 is 8 seconds, four times what",
                    "one hand drive takes on a tier-1 core, and slower than a dormant C.O.D. (cod.dormant_interval).")
            .defineInRange("recycler.hand.cooldown", 160, 0, 72000);

    /** What a hand drive's turn that counts costs the player turning it: about a hunger point an essence (#222). */
    public static final ModConfigSpec.DoubleValue HAND_DRIVE_EXHAUSTION = BUILDER
            .comment("The food exhaustion each turn of a hand drive that counts adds to the player who turned it, as sprinting does.",
                    "0.5 is about one hunger point per essence on a tier-1 core (8 turns a decompose, 4 exhaustion a point).")
            .defineInRange("recycler.hand_drive.exhaustion", 0.5, 0.0, 40.0);

    /** How often an FE drive turns: the hand drive's cap, so one matches one player cranking without a pause. */
    public static final ModConfigSpec.IntValue FE_DRIVE_INTERVAL = BUILDER
            .comment("The ticks between two turns of one FE drive. 5 is as fast as a hand drive turns, one player cranking without a pause.")
            .defineInRange("recycler.fe_drive.interval", HandDriveBlock.CAP, 1, 72000);

    /**
     * What an FE drive spends on a turn that counts (#207): 60 FE, 12 FE/t a drive, so one Steam Engine (450 FE/t) runs a
     * bank of drives, a Solar Panel (30 FE/t at peak) two, and a 30-minute energy challenge, run on banked FE alone, takes
     * about nine Accumulators (50,000 FE each).
     */
    public static final ModConfigSpec.IntValue FE_DRIVE_FE_PER_TURN = BUILDER
            .comment("The FE an FE drive spends on each turn that counts. It spends nothing on a turn its Recycler can't use.",
                    "60 is 12 FE/t a drive at its default pace: a Steam Engine runs 37, a Solar Panel at peak 2, and under the",
                    "energy handicap, which runs drives on banked FE alone, a 30-minute race takes about 9 Accumulators.")
            .defineInRange("recycler.fe_drive.fe_per_turn", 60, 1, 1_000_000);

    /** How much an FE drive stores, in turns. */
    public static final ModConfigSpec.IntValue FE_DRIVE_STORE_TURNS = BUILDER
            .comment("How many turns' worth of FE an FE drive stores. Full, it takes no more until it spends some.")
            .defineInRange("recycler.fe_drive.store_turns", 10, 1, 1_000_000);

    /** How far an FE defence reaches, in blocks (#236, a first guess for #207). */
    public static final ModConfigSpec.IntValue FE_DEFENCE_RANGE = BUILDER
            .comment("How far an FE defence shoots, in blocks, at the nearest hostile mob it can see.")
            .defineInRange("defence.fe.range", 12, 1, 64);

    /** The damage of an FE defence's shot: a zombie takes four. */
    public static final ModConfigSpec.DoubleValue FE_DEFENCE_DAMAGE = BUILDER
            .comment("The damage of one shot of an FE defence. 6 takes a zombie (20 health) in four shots.")
            .defineInRange("defence.fe.damage", 6.0, 0.5, 1000.0);

    /** The ticks between two shots of an FE defence. */
    public static final ModConfigSpec.IntValue FE_DEFENCE_INTERVAL = BUILDER
            .comment("The ticks between two shots of one FE defence. 10 is as fast as a mob can be hurt again.")
            .defineInRange("defence.fe.interval", 10, 1, 72000);

    /** What an FE defence spends on a shot: 10 FE/t while it fires, nothing while it has no target. */
    public static final ModConfigSpec.IntValue FE_DEFENCE_FE_PER_SHOT = BUILDER
            .comment("The FE an FE defence spends on each shot. It spends nothing while no hostile mob is in reach.",
                    "100 is 10 FE/t while it fires at its default pace.")
            .defineInRange("defence.fe.fe_per_shot", 100, 1, 1_000_000);

    /** How much an FE defence stores, in shots. */
    public static final ModConfigSpec.IntValue FE_DEFENCE_STORE_SHOTS = BUILDER
            .comment("How many shots' worth of FE an FE defence stores. Full, it takes no more until it spends some.")
            .defineInRange("defence.fe.store_shots", 20, 1, 1_000_000);

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

    /**
     * How much faster an awake C.O.D. grows each minute of a challenge, from its waking pace (#207): a twentieth, so a team
     * that keeps level needs one more full-time drive every twenty minutes.
     */
    public static final ModConfigSpec.DoubleValue COD_PACE_GROWTH = BUILDER
            .comment("The share of its waking pace an awake C.O.D.'s pace grows by each minute since its team's challenge began,",
                    "without end: 0.05 is twice the waking pace twenty minutes in, three times forty minutes in, so a team that",
                    "keeps level needs one more full-time drive every twenty minutes. 0 keeps the waking pace.",
                    "It never makes more than one essence a tick.")
            .defineInRange("cod.pace_growth", 0.05, 0.0, 100.0);

    /**
     * The excess Inflation may reach while a challenge is played (#207): ten stacks, which a team that decomposes nothing
     * reaches about fifteen minutes in at the default waking pace and growth.
     */
    public static final ModConfigSpec.IntValue COD_INFLATION_CAP = BUILDER
            .comment("The excess of Advanced essence C.O.D. may make beyond what a team decomposes while it plays a challenge:",
                    "its Inflation's cap, which the team's bar fills toward. 640 is ten stacks: a team that decomposes nothing",
                    "reaches it about fifteen minutes in at the default waking pace and growth.")
            .defineInRange("cod.inflation_cap", 640, 1, 1_000_000);

    /** The share of a sale the money handicap pays (#213). */
    public static final ModConfigSpec.DoubleValue MONEY_SALE_SHARE = BUILDER
            .comment("The share of a sale's price AdminShop pays a team while it plays the money challenge, by hand or by a Seller,",
                    "rounded down to the cent. 0.5 is half. Buying is unaffected.")
            .defineInRange("money.sale_share", 0.5, 0.0, 1.0);

    /** How much faster hunger drains under the life handicap (#244, a placeholder for #207). */
    public static final ModConfigSpec.DoubleValue LIFE_HUNGER_RATE = BUILDER
            .comment("How many times faster a player's hunger drains while their team plays the life challenge: every exhaustion,",
                    "from sprinting, healing, a hand drive's turn or anything else, is multiplied by it. 2 is twice vanilla's.")
            .defineInRange("life.hunger_rate", 2.0, 1.0, 100.0);

    /** How many foods a Diet holds. */
    public static final ModConfigSpec.IntValue LIFE_DIET_SIZE = BUILDER
            .comment("How many of the foods a player ate last their Diet holds while their team plays the life challenge.",
                    "A food already in it feeds less each time it is there.")
            .defineInRange("life.diet_size", 8, 0, 256);

    /** How much less a food feeds for each time it is in the Diet. */
    public static final ModConfigSpec.DoubleValue LIFE_REPEAT_FALLOFF = BUILDER
            .comment("The share of its hunger and saturation a food feeds less for each time it is in the eater's Diet:",
                    "0.25 feeds 75% once there, 50% twice, down to life.floor.")
            .defineInRange("life.repeat_falloff", 0.25, 0.0, 1.0);

    /** The least share a food in the Diet feeds. */
    public static final ModConfigSpec.DoubleValue LIFE_FLOOR = BUILDER
            .comment("The least share of its hunger and saturation a food feeds, however often it is in the eater's Diet.")
            .defineInRange("life.floor", 0.25, 0.0, 1.0);

    /** Whether the life handicap pauses for a player in Neo Vitae's Demon Realm. */
    public static final ModConfigSpec.BooleanValue LIFE_PAUSE_IN_DEMON_REALM = BUILDER
            .comment("Whether a player in Neo Vitae's Demon Realm (neovitae:dungeon) hungers and eats as in vanilla while their team",
                    "plays the life challenge. Off, the handicap goes on there as anywhere else. Inflation goes on either way.")
            .define("life.pause_in_demon_realm", false);

    /**
     * The Transmuted Essence a challenge's Port takes to win it, by the tier C.O.D. makes, highest first (#224). Each
     * challenge must take longer than the last, even with the better core a win unlocks (#189).
     */
    private static final Map<Essence, ModConfigSpec.IntValue> QUOTAS = quotas(Map.of(
            Essence.ADVANCED, 4, Essence.IMPROVED, 8, Essence.REGULAR, 16, Essence.BASIC, 48));

    static final ModConfigSpec SPEC = BUILDER.build();

    private ServerConfig() {
    }

    public static void register(ModContainer container) {
        container.registerConfig(ModConfig.Type.SERVER, SPEC);
    }

    private static Map<Essence, ModConfigSpec.IntValue> quotas(Map<Essence, Integer> defaults) {
        Map<Essence, ModConfigSpec.IntValue> quotas = new EnumMap<>(Essence.class);
        for (Essence tier : Essence.TIERS) {
            String name = tier.path().substring(0, tier.path().indexOf('_'));
            quotas.put(tier, BUILDER
                    .comment("The Transmuted Essence a challenge's Port takes to win it while C.O.D. makes " + name + " essence.",
                            "Each challenge must take longer than the last, even with the better Recycler core a win unlocks.")
                    .defineInRange("quota." + name, defaults.get(tier), 1, 1_000_000));
        }
        return quotas;
    }

    /** The Transmuted Essence a Port takes to win its challenge while C.O.D. makes {@code tier}. */
    public static int quota(Essence tier) {
        var quota = QUOTAS.get(tier);
        return SPEC.isLoaded() ? quota.getAsInt() : quota.getDefault();
    }

    /** The ticks an essence rests after a decompose by hand. */
    public static int handCooldown() {
        return SPEC.isLoaded() ? HAND_COOLDOWN.getAsInt() : HAND_COOLDOWN.getDefault();
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

    /** How many times faster hunger drains under the life handicap. */
    public static double lifeHungerRate() {
        return SPEC.isLoaded() ? LIFE_HUNGER_RATE.getAsDouble() : LIFE_HUNGER_RATE.getDefault();
    }

    /** How many foods a Diet holds. */
    public static int lifeDietSize() {
        return SPEC.isLoaded() ? LIFE_DIET_SIZE.getAsInt() : LIFE_DIET_SIZE.getDefault();
    }

    /** How much less a food feeds for each time it is in the Diet. */
    public static double lifeRepeatFalloff() {
        return SPEC.isLoaded() ? LIFE_REPEAT_FALLOFF.getAsDouble() : LIFE_REPEAT_FALLOFF.getDefault();
    }

    /** The least share a food in the Diet feeds. */
    public static double lifeFloor() {
        return SPEC.isLoaded() ? LIFE_FLOOR.getAsDouble() : LIFE_FLOOR.getDefault();
    }

    /** Whether the life handicap pauses for a player in the Demon Realm. */
    public static boolean lifePauseInDemonRealm() {
        return SPEC.isLoaded() ? LIFE_PAUSE_IN_DEMON_REALM.getAsBoolean() : LIFE_PAUSE_IN_DEMON_REALM.getDefault();
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

    /** How far an FE defence shoots, in blocks. */
    public static int feDefenceRange() {
        return SPEC.isLoaded() ? FE_DEFENCE_RANGE.getAsInt() : FE_DEFENCE_RANGE.getDefault();
    }

    /** The damage of one shot of an FE defence. */
    public static float feDefenceDamage() {
        return (float) (SPEC.isLoaded() ? FE_DEFENCE_DAMAGE.getAsDouble() : FE_DEFENCE_DAMAGE.getDefault());
    }

    /** The ticks between two shots of one FE defence. */
    public static int feDefenceInterval() {
        return SPEC.isLoaded() ? FE_DEFENCE_INTERVAL.getAsInt() : FE_DEFENCE_INTERVAL.getDefault();
    }

    /** The FE an FE defence spends on a shot. */
    public static int feDefenceFePerShot() {
        return SPEC.isLoaded() ? FE_DEFENCE_FE_PER_SHOT.getAsInt() : FE_DEFENCE_FE_PER_SHOT.getDefault();
    }

    /** How many shots' worth of FE an FE defence stores. */
    public static int feDefenceStoreShots() {
        return SPEC.isLoaded() ? FE_DEFENCE_STORE_SHOTS.getAsInt() : FE_DEFENCE_STORE_SHOTS.getDefault();
    }
}
