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
import net.minecraft.core.GlobalPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

/**
 * Where each team's C.O.D. stands, as it noted on its first tick, and the {@link Assault} it is sent while it plays
 * the protection challenge: the blocks it built and the gate it opened, so its end removes exactly those, and the gates
 * of Assaults removed. Saved with
 * the world, in the overworld's data, apart from the team's own state.
 */
public final class AssaultState extends SavedData {

    /** One Assault: what it built in {@code dimension}, and its gate's entity, if one opened. */
    public record Built(ResourceKey<Level> dimension, List<BlockPos> blocks, Optional<UUID> gate) {

        static final Codec<Built> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Level.RESOURCE_KEY_CODEC.fieldOf("dimension").forGetter(Built::dimension),
                BlockPos.CODEC.listOf().fieldOf("blocks").forGetter(Built::blocks),
                UUIDUtil.STRING_CODEC.optionalFieldOf("gate").forGetter(Built::gate))
                .apply(instance, Built::new));

        public Built {
            blocks = List.copyOf(blocks);
        }
    }

    private static final Codec<AssaultState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.unboundedMap(UUIDUtil.STRING_CODEC, GlobalPos.CODEC).optionalFieldOf("cods", Map.of()).forGetter(state -> state.cods),
            Codec.unboundedMap(UUIDUtil.STRING_CODEC, Built.CODEC).optionalFieldOf("assaults", Map.of()).forGetter(state -> state.assaults),
            UUIDUtil.STRING_CODEC.listOf().optionalFieldOf("closed", List.of()).forGetter(state -> List.copyOf(state.closed)),
            Codec.unboundedMap(UUIDUtil.STRING_CODEC, Codec.INT).optionalFieldOf("paused", Map.of()).forGetter(state -> state.paused),
            Codec.unboundedMap(UUIDUtil.STRING_CODEC, UUIDUtil.STRING_CODEC).optionalFieldOf("shut", Map.of()).forGetter(state -> state.shut))
            .apply(instance, AssaultState::new));

    public static final SavedDataType<AssaultState> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath(MechanicalMasteryReborn.MOD_ID, "assault_state"), AssaultState::new, CODEC);

    private final Map<UUID, GlobalPos> cods;
    private final Map<UUID, Built> assaults;
    /** The gates of Assaults removed, whose gate or mobs may still load with a chunk no one was near as they were removed. */
    private final Set<UUID> closed;

    /** The wave each team's paused Assault had reached as its gate closed for no one being home (#240). */
    private final Map<UUID, Integer> paused;
    /** The gates closed for no one being home, by their team: kept out if they load again, their mobs not. */
    private final Map<UUID, UUID> shut;

    private AssaultState() {
        this(Map.of(), Map.of(), List.of(), Map.of(), Map.of());
    }

    private AssaultState(Map<UUID, GlobalPos> cods, Map<UUID, Built> assaults, List<UUID> closed, Map<UUID, Integer> paused,
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

    /** Every team's Assault. */
    Map<UUID, Built> assaults() {
        return assaults;
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
            built.gate().ifPresent(closed::add);
            setDirty();
        }
        return Optional.ofNullable(built);
    }

    /** Whether {@code gate} was the gate of an Assault since removed. */
    public boolean closed(UUID gate) {
        return closed.contains(gate);
    }

    /** The wave {@code team}'s Assault had reached as it paused, while it is paused. */
    public Optional<Integer> paused(UUID team) {
        return Optional.ofNullable(paused.get(team));
    }

    /** Pauses {@code team}'s Assault at {@code wave}, its gate {@code gate} shut, or only notes the wave if it already is. */
    void pause(UUID team, Optional<UUID> gate, int wave) {
        paused.put(team, wave);
        gate.ifPresent(shut -> this.shut.put(shut, team));
        setDirty();
    }

    /** Ends {@code team}'s pause, saying the wave it had reached, its gate now {@code gate}. */
    Optional<Integer> unpause(UUID team, Optional<UUID> gate) {
        Integer wave = paused.remove(team);
        assault(team).ifPresent(built -> assaults.put(team, new Built(built.dimension(), built.blocks(), gate)));
        setDirty();
        return Optional.ofNullable(wave);
    }

    /** The team whose gate {@code gate} was shut for no one being home, if it was. */
    public Optional<UUID> shut(UUID gate) {
        return Optional.ofNullable(shut.get(gate));
    }
}
