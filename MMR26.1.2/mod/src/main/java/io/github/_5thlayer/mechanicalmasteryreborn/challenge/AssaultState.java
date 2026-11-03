// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.challenge;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github._5thlayer.mechanicalmasteryreborn.MechanicalMasteryReborn;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.phys.Vec3;

/**
 * Where each team's C.O.D. stands, as it noted on its first tick, and the {@link Assault} it is sent while it plays
 * the protection challenge: the blocks it built and the gate it opened, so its end removes exactly those, and the gates
 * of Assaults removed. Saved with
 * the world, in the overworld's data, apart from the team's own state.
 */
public final class AssaultState extends SavedData {

    /**
     * One Assault: what it built in {@code dimension}, the entity of its one gate while open, and the side of C.O.D. the
     * gate is on now, where it opens again after a failure or an absence (#239).
     */
    public record Built(ResourceKey<Level> dimension, List<BlockPos> blocks, List<UUID> gates, Optional<Direction> side) {

        static final Codec<Built> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Level.RESOURCE_KEY_CODEC.fieldOf("dimension").forGetter(Built::dimension),
                BlockPos.CODEC.listOf().fieldOf("blocks").forGetter(Built::blocks),
                UUIDUtil.STRING_CODEC.listOf().optionalFieldOf("gates", List.of()).forGetter(Built::gates),
                Direction.CODEC.optionalFieldOf("side").forGetter(Built::side))
                .apply(instance, Built::new));

        public Built {
            blocks = List.copyOf(blocks);
            gates = List.copyOf(gates);
        }

        /** This Assault with {@code gates} open instead. */
        Built withGates(List<UUID> gates) {
            return new Built(dimension, blocks, gates, side);
        }

    }

    /** A gate closed for no one being home (#240): where it stood, if seen, and the wave it had reached. */
    public record Waiting(UUID gate, Optional<Vec3> at, int wave) {

        static final Codec<Waiting> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                UUIDUtil.STRING_CODEC.fieldOf("gate").forGetter(Waiting::gate),
                Vec3.CODEC.optionalFieldOf("at").forGetter(Waiting::at),
                Codec.INT.fieldOf("wave").forGetter(Waiting::wave))
                .apply(instance, Waiting::new));
    }

    private static final Codec<AssaultState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.unboundedMap(UUIDUtil.STRING_CODEC, GlobalPos.CODEC).optionalFieldOf("cods", Map.of()).forGetter(state -> state.cods),
            Codec.unboundedMap(UUIDUtil.STRING_CODEC, Built.CODEC).optionalFieldOf("assaults", Map.of()).forGetter(state -> state.assaults),
            UUIDUtil.STRING_CODEC.listOf().optionalFieldOf("closed", List.of()).forGetter(state -> List.copyOf(state.closed)),
            Codec.unboundedMap(UUIDUtil.STRING_CODEC, Waiting.CODEC.listOf()).optionalFieldOf("paused", Map.of()).forGetter(state -> state.paused),
            Codec.unboundedMap(UUIDUtil.STRING_CODEC, UUIDUtil.STRING_CODEC).optionalFieldOf("shut", Map.of()).forGetter(state -> state.shut))
            .apply(instance, AssaultState::new));

    public static final SavedDataType<AssaultState> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath(MechanicalMasteryReborn.MOD_ID, "assault_state"), AssaultState::new, CODEC);

    private final Map<UUID, GlobalPos> cods;
    private final Map<UUID, Built> assaults;
    /** The gates of Assaults removed, whose gate or mobs may still load with a chunk no one was near as they were removed. */
    private final Set<UUID> closed;

    /** Each team's Assault paused for no one being home (#240): its gates closed, each to open again at its wave. */
    private final Map<UUID, List<Waiting>> paused;
    /** The gates closed for no one being home, by their team: kept out if they load again, their mobs not. */
    private final Map<UUID, UUID> shut;

    private AssaultState() {
        this(Map.of(), Map.of(), List.of(), Map.of(), Map.of());
    }

    private AssaultState(Map<UUID, GlobalPos> cods, Map<UUID, Built> assaults, List<UUID> closed, Map<UUID, List<Waiting>> paused,
            Map<UUID, UUID> shut) {
        this.cods = new HashMap<>(cods);
        this.assaults = new HashMap<>(assaults);
        this.closed = new HashSet<>(closed);
        this.paused = new HashMap<>(paused);
        this.shut = new HashMap<>(shut);
    }

    public static AssaultState get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(TYPE);
    }

    /** Where {@code team}'s C.O.D. stands, once it has ticked. */
    public Optional<GlobalPos> cod(UUID team) {
        return Optional.ofNullable(cods.get(team));
    }

    /** Notes that {@code team}'s C.O.D. stands at {@code pos}. */
    public void noteCod(UUID team, GlobalPos pos) {
        if (!pos.equals(cods.put(team, pos))) {
            setDirty();
        }
    }

    /** The Assault {@code team} is sent, if any. */
    public Optional<Built> assault(UUID team) {
        return Optional.ofNullable(assaults.get(team));
    }

    void putAssault(UUID team, Built built) {
        assaults.put(team, built);
        setDirty();
    }

    Optional<Built> takeAssault(UUID team) {
        Built built = assaults.remove(team);
        paused.remove(team);
        shut.entrySet().removeIf(entry -> {
            if (entry.getValue().equals(team)) {
                closed.add(entry.getKey());
                return true;
            }
            return false;
        });
        if (built != null) {
            closed.addAll(built.gates());
            setDirty();
        }
        return Optional.ofNullable(built);
    }

    /** The team whose Assault has the open gate {@code gate}, if any. */
    public Optional<UUID> teamOf(UUID gate) {
        return assaults.entrySet().stream().filter(entry -> entry.getValue().gates().contains(gate)).map(Map.Entry::getKey).findFirst();
    }

    /** Puts {@code reopened} in place of {@code failed} in {@code team}'s Assault, and keeps {@code failed} out. */
    void replaceGate(UUID team, UUID failed, Optional<UUID> reopened) {
        Built built = assaults.get(team);
        List<UUID> gates = new java.util.ArrayList<>(built.gates());
        gates.remove(failed);
        reopened.ifPresent(gates::add);
        assaults.put(team, built.withGates(gates));
        closed.add(failed);
        setDirty();
    }

    /** Moves {@code team}'s gate {@code from} to {@code side}, open there as {@code to}, and keeps {@code from} out. */
    void moveGate(UUID team, UUID from, Optional<UUID> to, Direction side) {
        Built built = assaults.get(team);
        assaults.put(team, new Built(built.dimension(), built.blocks(), to.stream().toList(), Optional.of(side)));
        closed.add(from);
        setDirty();
    }

    /** Whether {@code gate} was the gate of an Assault since removed. */
    public boolean closed(UUID gate) {
        return closed.contains(gate);
    }

    /** Every team's Assault. */
    Map<UUID, Built> assaults() {
        return assaults;
    }

    /** The gates of {@code team}'s Assault closed for no one being home, while it is paused. */
    public Optional<List<Waiting>> paused(UUID team) {
        return Optional.ofNullable(paused.get(team));
    }

    /** Pauses {@code team}'s Assault, its gates closed as {@code waiting}: they are no longer its open gates. */
    void pause(UUID team, List<Waiting> waiting) {
        paused.put(team, List.copyOf(waiting));
        waiting.forEach(gate -> shut.put(gate.gate(), team));
        Built built = assaults.get(team);
        if (built != null) {
            assaults.put(team, built.withGates(List.of()));
        }
        setDirty();
    }

    /** Notes where the closed gate {@code gate} stood and the wave it had reached, as it loads again, while its team is away. */
    void noteWaiting(UUID gate, Vec3 at, int wave) {
        shut(gate).flatMap(this::paused).ifPresent(waiting -> {
            List<Waiting> noted = waiting.stream()
                    .map(each -> each.gate().equals(gate) ? new Waiting(gate, Optional.of(at), wave) : each).toList();
            paused.put(shut.get(gate), noted);
            setDirty();
        });
    }

    /** Ends {@code team}'s pause, its gates open again as {@code gates}. */
    void unpause(UUID team, List<UUID> gates) {
        paused.remove(team);
        Built built = assaults.get(team);
        if (built != null) {
            assaults.put(team, built.withGates(gates));
        }
        setDirty();
    }

    /** The team whose gate {@code gate} was closed for no one being home, if it was. */
    public Optional<UUID> shut(UUID gate) {
        return Optional.ofNullable(shut.get(gate));
    }
}
