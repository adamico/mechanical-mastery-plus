// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.record;

import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
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
 * its {@link Inflation} while it plays one, with whether it was warned that Inflation nears its cap, and the money set
 * aside from its Account while it plays the money challenge, the challenges it has ever started, the essence its Port has taken toward the quota, and the tiers it has
 * blocked and the challenges it has won, which no reset touches. Its members share
 * it, and a player who joins a team shares it from then on. Saved with the world, in the overworld's data.
 */
public final class TeamState extends SavedData {

    /** A handicap by its id, which never changes once shipped. */
    private static final Codec<Handicap> HANDICAP_CODEC = Codec.STRING.comapFlatMap(
            id -> Handicap.byId(id).map(DataResult::success).orElseGet(() -> DataResult.error(() -> "No handicap " + id)),
            Handicap::id);
    private static final Codec<Set<Handicap>> HANDICAPS_CODEC = HANDICAP_CODEC.listOf().xmap(TeamState::handicaps, List::copyOf);
    /** A Mechanical Essence by its id, which never changes once shipped. */
    private static final Codec<Essence> TIER_CODEC = Codec.STRING.comapFlatMap(
            path -> Essence.byPath(path).filter(Essence.TIERS::contains).map(DataResult::success)
                    .orElseGet(() -> DataResult.error(() -> "No tier " + path)),
            Essence::path);
    private static final Codec<Set<Essence>> TIERS_CODEC = TIER_CODEC.listOf().xmap(TeamState::tiers, List::copyOf);

    /** One team's state. */
    private static final class Entry {

        static final Codec<Entry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.BOOL.optionalFieldOf("awake", false).forGetter(entry -> entry.awake),
                Codec.LONG.optionalFieldOf("made", 0L).forGetter(entry -> entry.made),
                HANDICAP_CODEC.optionalFieldOf("handicap").forGetter(entry -> entry.handicap),
                Inflation.CODEC.optionalFieldOf("inflation").forGetter(entry -> entry.inflation),
                Codec.BOOL.optionalFieldOf("warned", false).forGetter(entry -> entry.warned),
                Codec.LONG.optionalFieldOf("set_aside", 0L).forGetter(entry -> entry.setAside),
                HANDICAPS_CODEC.optionalFieldOf("unlocked", Set.of()).forGetter(entry -> entry.unlocked),
                TIERS_CODEC.optionalFieldOf("blocked", Set.of()).forGetter(entry -> entry.blocked),
                HANDICAPS_CODEC.optionalFieldOf("won", Set.of()).forGetter(entry -> entry.won),
                Codec.LONG.optionalFieldOf("delivered", 0L).forGetter(entry -> entry.delivered))
                .apply(instance, Entry::new));

        boolean awake;
        long made;
        Optional<Handicap> handicap;
        /** Present exactly while {@link #handicap} is. */
        Optional<Inflation> inflation;
        /** Whether the team was warned that its Inflation nears its cap, since it last fell back below; only while it plays a challenge. */
        boolean warned;
        /** The money set aside from the team's Account while it plays the money challenge, in AdminShop's cents. */
        long setAside;
        /** The challenges the team has started, whose chains stay unlocked for it from then on (ADR 0007). */
        Set<Handicap> unlocked;
        /** The tiers the team has blocked, for good: C.O.D. makes the highest of the rest. */
        Set<Essence> blocked;
        /** The challenges the team has won, for good. */
        Set<Handicap> won;
        /** The Transmuted Essence the team's Port has taken toward the quota of the challenge it plays; only while it plays one. */
        long delivered;

        Entry(boolean awake, long made, Optional<Handicap> handicap, Optional<Inflation> inflation, boolean warned, long setAside,
                Set<Handicap> unlocked, Set<Essence> blocked, Set<Handicap> won, long delivered) {
            this.awake = awake;
            this.made = made;
            this.handicap = handicap;
            // A challenge saved before Inflation was counted counts from the world's start.
            this.inflation = handicap.isPresent() ? inflation.or(() -> Optional.of(Inflation.startingAt(0))) : Optional.empty();
            this.warned = handicap.isPresent() && warned;
            this.setAside = setAside;
            this.unlocked = handicaps(unlocked);
            handicap.ifPresent(this.unlocked::add);
            this.blocked = tiers(blocked);
            this.won = handicaps(won);
            this.delivered = handicap.isPresent() ? delivered : 0;
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
     * challenge the team plays, if any, goes on, and so do the tiers it blocked and the challenges it won.
     */
    public void makeDormant(UUID team) {
        Entry entry = teams.get(team);
        if (entry == null) {
            return;
        }
        if (entry.handicap.isPresent() || !entry.blocked.isEmpty() || !entry.won.isEmpty()) {
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
     * offer yet, nor for a challenge it has won.
     */
    public boolean choose(UUID team, Handicap handicap, long gameTime) {
        return handicap.choosable() && begin(team, handicap, gameTime);
    }

    /**
     * Starts {@code handicap}'s challenge for {@code team} as {@link #choose} does, though the lab can't offer it yet,
     * as an op's command may (#235); false while the team plays one or has won it.
     */
    public boolean begin(UUID team, Handicap handicap, long gameTime) {
        if (handicap(team).isPresent() || won(team, handicap)) {
            return false;
        }
        Entry entry = entry(team);
        entry.handicap = Optional.of(handicap);
        entry.inflation = Optional.of(Inflation.startingAt(gameTime));
        entry.unlocked.add(handicap);
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
        entry.delivered = 0;
        setDirty();
        return true;
    }

    /**
     * Resets the challenge {@code team} plays at {@code gameTime}, as though it had just begun: its Inflation from
     * nothing, and so C.O.D. back to its waking pace, no warning given and nothing delivered to its Port. Its handicap stays. Says whether it played
     * one.
     */
    public boolean resetChallenge(UUID team, long gameTime) {
        Entry entry = teams.get(team);
        if (entry == null || entry.handicap.isEmpty()) {
            return false;
        }
        entry.inflation = Optional.of(Inflation.startingAt(gameTime));
        entry.warned = false;
        entry.delivered = 0;
        setDirty();
        return true;
    }

    /** How much Transmuted Essence {@code team}'s Port has taken toward the quota of the challenge it plays. */
    public long delivered(UUID team) {
        Entry entry = teams.get(team);
        return entry == null ? 0 : entry.delivered;
    }

    /** Counts {@code count} more essence {@code team}'s Port took, while it plays a challenge. */
    public void deliver(UUID team, long count) {
        Entry entry = teams.get(team);
        if (entry == null || entry.handicap.isEmpty() || count == 0) {
            return;
        }
        entry.delivered += count;
        setDirty();
    }

    /** The tier {@code team}'s C.O.D. makes: the highest it hasn't blocked, Advanced first, and none once all four are. */
    public Optional<Essence> tier(UUID team) {
        Entry entry = teams.get(team);
        return Essence.TIERS.stream().filter(tier -> entry == null || !entry.blocked.contains(tier)).findFirst();
    }

    /**
     * Sets the tier {@code team}'s C.O.D. makes, by blocking every tier above it and none below, or all four for
     * none, as {@code /cod tier} does. The challenges it has won stay as they are.
     */
    public void setTier(UUID team, Optional<Essence> tier) {
        Entry entry = entry(team);
        int index = tier.map(Essence.TIERS::indexOf).orElse(Essence.TIERS.size());
        entry.blocked = tiers(Essence.TIERS.subList(0, index));
        setDirty();
    }

    /**
     * Blocks the tier {@code team}'s C.O.D. makes, for good, as winning {@code won}'s challenge does, and notes the
     * challenge won. Says whether it did: not once all four are blocked, and not for a challenge won already.
     */
    public boolean blockTier(UUID team, Handicap won) {
        Optional<Essence> tier = tier(team);
        Entry entry = entry(team);
        if (tier.isEmpty() || entry.won.contains(won)) {
            return false;
        }
        entry.blocked.add(tier.get());
        entry.won.add(won);
        setDirty();
        return true;
    }

    /**
     * Forgets that {@code team} won {@code handicap}'s challenge, so it can be played again, and unblocks the lowest
     * tier blocked, the one the last win blocked, as {@code /cod challenge revert} does. Says whether it had won it.
     */
    public boolean revertWin(UUID team, Handicap handicap) {
        Entry entry = teams.get(team);
        if (entry == null || !entry.won.remove(handicap)) {
            return false;
        }
        Essence.TIERS.reversed().stream().filter(entry.blocked::contains).findFirst().ifPresent(entry.blocked::remove);
        setDirty();
        return true;
    }

    /** Whether {@code team} has won {@code handicap}'s challenge. */
    public boolean won(UUID team, Handicap handicap) {
        Entry entry = teams.get(team);
        return entry != null && entry.won.contains(handicap);
    }

    /** The money set aside from {@code team}'s Account while it plays the money challenge. */
    public long setAside(UUID team) {
        Entry entry = teams.get(team);
        return entry == null ? 0 : entry.setAside;
    }

    /** Whether {@code team} has ever started {@code handicap}'s challenge, which keeps its chain unlocked (ADR 0007). */
    public boolean unlocked(UUID team, Handicap handicap) {
        Entry entry = teams.get(team);
        return entry != null && entry.unlocked.contains(handicap);
    }

    /** Puts {@code amount} more of {@code team}'s money aside. */
    public void putAside(UUID team, long amount) {
        if (amount != 0) {
            entry(team).setAside += amount;
            setDirty();
        }
    }

    /** Takes back everything set aside of {@code team}'s money, and says how much. */
    public long takeBackSetAside(UUID team) {
        Entry entry = teams.get(team);
        if (entry == null || entry.setAside == 0) {
            return 0;
        }
        long amount = entry.setAside;
        entry.setAside = 0;
        setDirty();
        return amount;
    }

    /** The teams that play a challenge. */
    public List<UUID> playing() {
        return teams.entrySet().stream().filter(entry -> entry.getValue().handicap.isPresent()).map(Map.Entry::getKey).toList();
    }

    /** The teams with a state of their own: woken, playing or once played a challenge, or with a tier blocked. */
    public Set<UUID> known() {
        return Set.copyOf(teams.keySet());
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
     * while it plays a challenge: only essence of the tier its C.O.D. makes.
     */
    void countDecomposed(UUID team, Essence essence, long count) {
        Entry entry = teams.get(team);
        if (entry == null || entry.inflation.isEmpty() || tier(team).filter(essence::equals).isEmpty()) {
            return;
        }
        entry.inflation = entry.inflation.map(inflation -> inflation.plusDecomposed(count));
        setDirty();
    }

    /** A set of its own of {@code handicaps}, which may be empty, as {@link EnumSet#copyOf} can't take. */
    private static EnumSet<Handicap> handicaps(Collection<Handicap> handicaps) {
        return handicaps.isEmpty() ? EnumSet.noneOf(Handicap.class) : EnumSet.copyOf(handicaps);
    }

    /** A set of its own of {@code tiers}, which may be empty. */
    private static EnumSet<Essence> tiers(Collection<Essence> tiers) {
        return tiers.isEmpty() ? EnumSet.noneOf(Essence.class) : EnumSet.copyOf(tiers);
    }

    private Entry entry(UUID team) {
        return teams.computeIfAbsent(team, ignored -> new Entry(false, 0, Optional.empty(), Optional.empty(), false, 0, Set.of(), Set.of(),
                Set.of(), 0));
    }
}
