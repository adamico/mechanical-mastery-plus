// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.record;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github._5thlayer.mechanicalmasteryreborn.MechanicalMasteryReborn;
import io.github._5thlayer.mechanicalmasteryreborn.essence.Essence;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

/**
 * How much essence each team has decomposed, by tier, and how: on a Recycler or by hand, since the world began. Both
 * count against the team's {@link Inflation} the same; only a decompose on a Recycler wakes C.O.D. ({@link TeamState}).
 * Saved with the world, in the overworld's data.
 */
public final class DecomposeLog extends SavedData {

    /** How an essence was decomposed. */
    public enum Way implements StringRepresentable {
        RECYCLER("recycler"), HAND("hand");

        static final Codec<Way> CODEC = StringRepresentable.fromEnum(Way::values);

        private final String name;

        Way(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }

    private static final Codec<Essence> ESSENCE_CODEC = Codec.STRING.comapFlatMap(
            path -> Essence.byPath(path).map(DataResult::success).orElseGet(() -> DataResult.error(() -> "no essence " + path)),
            Essence::path);

    /** One team's counts, per essence and way. */
    private record Tally(Map<Essence, Map<Way, Long>> counts) {

        static final Codec<Tally> CODEC = Codec.unboundedMap(ESSENCE_CODEC, Codec.unboundedMap(Way.CODEC, Codec.LONG))
                .xmap(Tally::copied, Tally::counts);

        static Tally copied(Map<Essence, Map<Way, Long>> counts) {
            var tally = new Tally(new EnumMap<>(Essence.class));
            counts.forEach((essence, ways) -> tally.counts.put(essence, new EnumMap<>(ways)));
            return tally;
        }
    }

    private static final Codec<DecomposeLog> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.unboundedMap(UUIDUtil.STRING_CODEC, Tally.CODEC).fieldOf("teams").forGetter(log -> log.teams))
            .apply(instance, DecomposeLog::new));

    private static final SavedDataType<DecomposeLog> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath(MechanicalMasteryReborn.MOD_ID, "decompose_log"), DecomposeLog::new, CODEC);

    private final Map<UUID, Tally> teams;

    private DecomposeLog() {
        this(Map.of());
    }

    private DecomposeLog(Map<UUID, Tally> teams) {
        this.teams = new HashMap<>(teams);
    }

    public static DecomposeLog get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(TYPE);
    }

    /** Records {@code count} of {@code essence} decomposed by {@code team}, one way, and counts it against its Inflation. */
    public static void record(MinecraftServer server, UUID team, Essence essence, int count, Way way) {
        get(server).record(team, essence, count, way);
        TeamState.get(server).countDecomposed(team, essence, count);
    }

    private void record(UUID team, Essence essence, int count, Way way) {
        teams.computeIfAbsent(team, ignored -> new Tally(new EnumMap<>(Essence.class))).counts
                .computeIfAbsent(essence, ignored -> new EnumMap<>(Way.class))
                .merge(way, (long) count, Long::sum);
        setDirty();
    }

    /** How much of {@code essence} {@code team} has decomposed, one way. */
    public long count(UUID team, Essence essence, Way way) {
        Tally tally = teams.get(team);
        if (tally == null) {
            return 0;
        }
        return tally.counts.getOrDefault(essence, Map.of()).getOrDefault(way, 0L);
    }
}
