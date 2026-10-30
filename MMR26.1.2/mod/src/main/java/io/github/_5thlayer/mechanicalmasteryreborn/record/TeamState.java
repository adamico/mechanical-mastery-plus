// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.record;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github._5thlayer.mechanicalmasteryreborn.MechanicalMasteryReborn;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.Handicap;
import io.github._5thlayer.mechanicalmasteryreborn.essence.Essence;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

/**
 * Each team's state, keyed on its id ({@link Teams}): whether its C.O.D. is awake, how much essence an awake
 * C.O.D. has made for it, spawned or not, the handicap it chose at the lab, which says which challenge it plays, and
 * its {@link Inflation} while it plays one, with whether it was warned that Inflation nears its cap. Its members share
 * it, and a player who joins a team shares it from then on. Saved with the world, in the overworld's data.
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
                HANDICAP_CODEC.optionalFieldOf("handicap").forGetter(entry -> entry.handicap),
                Inflation.CODEC.optionalFieldOf("inflation").forGetter(entry -> entry.inflation),
                Codec.BOOL.optionalFieldOf("warned", false).forGetter(entry -> entry.warned))
                .apply(instance, Entry::new));

        boolean awake;
        long made;
        Optional<Handicap> handicap;
        /** Present exactly while {@link #handicap} is. */
        Optional<Inflation> inflation;
        /** Whether the team was warned that its Inflation nears its cap, since it last fell back below; only while it plays a challenge. */
        boolean warned;

        Entry(boolean awake, long made, Optional<Handicap> handicap, Optional<Inflation> inflation, boolean warned) {
            this.awake = awake;
            this.made = made;
            this.handicap = handicap;
            // A challenge saved before Inflation was counted counts from the world's start.
            this.inflation = handicap.isPresent() ? inflation.or(() -> Optional.of(Inflation.startingAt(0))) : Optional.empty();
            this.warned = handicap.isPresent() && warned;
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

    /** Whether {@code team} plays {@code handicap}'s challenge. */
    public boolean plays(UUID team, Handicap handicap) {
        return handicap(team).filter(handicap::equals).isPresent();
    }

    /** Whether any team plays {@code handicap}'s challenge, which is cheaper to ask than which team a block is on. */
    public boolean anyPlays(Handicap handicap) {
        for (Entry entry : teams.values()) {
            if (entry.handicap.filter(handicap::equals).isPresent()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Starts {@code handicap}'s challenge for {@code team} at {@code gameTime}, the overworld's, its Inflation from
     * nothing, and says whether it did: not while the team plays one already, and not for a handicap the lab doesn't
     * offer yet.
     */
    public boolean choose(UUID team, Handicap handicap, long gameTime) {
        if (!handicap.choosable() || handicap(team).isPresent()) {
            return false;
        }
        Entry entry = entry(team);
        entry.handicap = Optional.of(handicap);
        entry.inflation = Optional.of(Inflation.startingAt(gameTime));
        setDirty();
        return true;
    }

    /** Ends the challenge {@code team} plays, its handicap and Inflation with it, and says whether it played one. */
    public boolean endChallenge(UUID team) {
        Entry entry = teams.get(team);
        if (entry == null || entry.handicap.isEmpty()) {
            return false;
        }
        entry.handicap = Optional.empty();
        entry.inflation = Optional.empty();
        entry.warned = false;
        setDirty();
        return true;
    }

    /**
     * Resets the challenge {@code team} plays at {@code gameTime}, as though it had just begun: its Inflation from
     * nothing, and so C.O.D. back to its waking pace, and no warning given. Its handicap stays. Says whether it played
     * one.
     */
    public boolean resetChallenge(UUID team, long gameTime) {
        Entry entry = teams.get(team);
        if (entry == null || entry.handicap.isEmpty()) {
            return false;
        }
        entry.inflation = Optional.of(Inflation.startingAt(gameTime));
        entry.warned = false;
        setDirty();
        return true;
    }

    /** The teams that play a challenge. */
    public List<UUID> playing() {
        return teams.entrySet().stream().filter(entry -> entry.getValue().handicap.isPresent()).map(Map.Entry::getKey).toList();
    }

    /** Whether {@code team} was warned that its Inflation nears its cap, since it last fell back below. */
    public boolean warned(UUID team) {
        Entry entry = teams.get(team);
        return entry != null && entry.warned;
    }

    /**
     * Notes whether {@code team} was warned that its Inflation nears its cap, while it plays a challenge, and says
     * whether that changed.
     */
    public boolean setWarned(UUID team, boolean warned) {
        Entry entry = teams.get(team);
        if (entry == null || entry.handicap.isEmpty() || entry.warned == warned) {
            return false;
        }
        entry.warned = warned;
        setDirty();
        return true;
    }

    /** {@code team}'s Inflation, while it plays a challenge. */
    public Optional<Inflation> inflation(UUID team) {
        Entry entry = teams.get(team);
        return entry == null ? Optional.empty() : entry.inflation;
    }

    /** How much essence an awake C.O.D. has made for {@code team}, whether it spawned or not. */
    public long made(UUID team) {
        Entry entry = teams.get(team);
        return entry == null ? 0 : entry.made;
    }

    /** Counts one essence an awake C.O.D. made for {@code team}, and toward its Inflation while it plays a challenge. */
    public void countMade(UUID team) {
        Entry entry = entry(team);
        entry.made++;
        entry.inflation = entry.inflation.map(Inflation::plusOneMade);
        setDirty();
    }

    /**
     * Counts {@code count} of {@code essence} {@code team} decomposed, by hand or on a Recycler, against its Inflation
     * while it plays a challenge: only Advanced essence, the tier C.O.D. makes.
     */
    void countDecomposed(UUID team, Essence essence, long count) {
        Entry entry = teams.get(team);
        if (entry == null || entry.inflation.isEmpty() || essence != Essence.ADVANCED) {
            return;
        }
        entry.inflation = entry.inflation.map(inflation -> inflation.plusDecomposed(count));
        setDirty();
    }

    private Entry entry(UUID team) {
        return teams.computeIfAbsent(team, ignored -> new Entry(false, 0, Optional.empty(), Optional.empty(), false));
    }
}
