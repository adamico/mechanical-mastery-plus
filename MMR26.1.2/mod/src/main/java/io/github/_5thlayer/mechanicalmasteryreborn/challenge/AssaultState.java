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
            UUIDUtil.STRING_CODEC.listOf().optionalFieldOf("closed", List.of()).forGetter(state -> List.copyOf(state.closed)))
            .apply(instance, AssaultState::new));

    public static final SavedDataType<AssaultState> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath(MechanicalMasteryReborn.MOD_ID, "assault_state"), AssaultState::new, CODEC);

    private final Map<UUID, GlobalPos> cods;
    private final Map<UUID, Built> assaults;
    /** The gates of Assaults removed, whose gate or mobs may still load with a chunk no one was near as they were removed. */
    private final Set<UUID> closed;

    private AssaultState() {
        this(Map.of(), Map.of(), List.of());
    }

    private AssaultState(Map<UUID, GlobalPos> cods, Map<UUID, Built> assaults, List<UUID> closed) {
        this.cods = new HashMap<>(cods);
        this.assaults = new HashMap<>(assaults);
        this.closed = new HashSet<>(closed);
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
}
