// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.record;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github._5thlayer.mechanicalmasteryreborn.MechanicalMasteryReborn;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.Handicap;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

/**
 * Each team's state, keyed on its id ({@link Teams}): whether its C.O.D. is awake, how much essence an awake
 * C.O.D. has made for it, spawned or not, which Inflation (#168) reads, and the handicap it chose at the lab, which
 * says which challenge it plays. Its members share it, and a player who joins
 * a team shares it from then on. Saved with the world, in the overworld's data.
 */
public final class TeamState extends SavedData {

    /** A handicap by its id, which never changes once shipped. */
    private static final Codec<Handicap> HANDICAP_CODEC = Codec.STRING.comapFlatMap(
            id -> Handicap.byId(id).map(DataResult::success).orElseGet(() -> DataResult.error(() -> "No handicap " + id)),
            Handicap::id);

    /** One team's state. */
    private static final class Entry {

        static final Codec<Entry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.BOOL.optionalFieldOf("awake", false).forGetter(entry -> entry.awake),
                Codec.LONG.optionalFieldOf("made", 0L).forGetter(entry -> entry.made),
                HANDICAP_CODEC.optionalFieldOf("handicap").forGetter(entry -> entry.handicap))
                .apply(instance, Entry::new));

        boolean awake;
        long made;
        Optional<Handicap> handicap;

        Entry(boolean awake, long made, Optional<Handicap> handicap) {
            this.awake = awake;
            this.made = made;
            this.handicap = handicap;
        }
    }

    private static final Codec<TeamState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.unboundedMap(UUIDUtil.STRING_CODEC, Entry.CODEC).fieldOf("teams").forGetter(state -> state.teams))
            .apply(instance, TeamState::new));

    /** How it is saved, which the game tests read back as a world load would. */
    public static final SavedDataType<TeamState> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath(MechanicalMasteryReborn.MOD_ID, "team_state"), TeamState::new, CODEC);

    private final Map<UUID, Entry> teams;

    private TeamState() {
        this(Map.of());
    }

    private TeamState(Map<UUID, Entry> teams) {
        this.teams = new HashMap<>(teams);
    }

    public static TeamState get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(TYPE);
    }

    /** Whether {@code team}'s C.O.D. is awake. */
    public boolean isAwake(UUID team) {
        Entry entry = teams.get(team);
        return entry != null && entry.awake;
    }

    /** Wakes {@code team}'s C.O.D., for good, and says whether it was dormant until now. */
    public boolean wake(UUID team) {
        Entry entry = entry(team);
        if (entry.awake) {
            return false;
        }
        entry.awake = true;
        setDirty();
        return true;
    }

    /**
     * Puts {@code team}'s C.O.D. back to dormant, as a new island's is, its count of what it made forgotten. The
     * challenge the team plays, if any, goes on.
     */
    public void makeDormant(UUID team) {
        Entry entry = teams.get(team);
        if (entry == null) {
            return;
        }
        if (entry.handicap.isPresent()) {
            entry.awake = false;
            entry.made = 0;
        } else {
            teams.remove(team);
        }
        setDirty();
    }

    /** The handicap {@code team} chose at the lab, while it plays that handicap's challenge. */
    public Optional<Handicap> handicap(UUID team) {
        Entry entry = teams.get(team);
        return entry == null ? Optional.empty() : entry.handicap;
    }

    /**
     * Starts {@code handicap}'s challenge for {@code team}, and says whether it did: not while the team plays one
     * already, and not for a handicap the lab doesn't offer yet.
     */
    public boolean choose(UUID team, Handicap handicap) {
        if (!handicap.choosable() || handicap(team).isPresent()) {
            return false;
        }
        entry(team).handicap = Optional.of(handicap);
        setDirty();
        return true;
    }

    /** How much essence an awake C.O.D. has made for {@code team}, whether it spawned or not. */
    public long made(UUID team) {
        Entry entry = teams.get(team);
        return entry == null ? 0 : entry.made;
    }

    /** Counts one essence an awake C.O.D. made for {@code team}. */
    public void countMade(UUID team) {
        entry(team).made++;
        setDirty();
    }

    private Entry entry(UUID team) {
        return teams.computeIfAbsent(team, ignored -> new Entry(false, 0, Optional.empty()));
    }
}
