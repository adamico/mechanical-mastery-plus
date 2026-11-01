// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.record;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github._5thlayer.mechanicalmasteryreborn.MechanicalMasteryReborn;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

/**
 * The FTB party each island owns, by the island's team id ({@link Teams}), as {@link PartySync} keeps them (#225), so
 * the rest of the mod finds a team's FTB team, and with it its AdminShop Account, whoever of it is online. Saved with
 * the world, in the overworld's data.
 */
public final class IslandParties extends SavedData {

    private static final Codec<IslandParties> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.unboundedMap(UUIDUtil.STRING_CODEC, UUIDUtil.STRING_CODEC).fieldOf("parties").forGetter(links -> links.parties))
            .apply(instance, IslandParties::new));

    public static final SavedDataType<IslandParties> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath(MechanicalMasteryReborn.MOD_ID, "island_parties"), IslandParties::new, CODEC);

    private final Map<UUID, UUID> parties;

    private IslandParties() {
        this(Map.of());
    }

    private IslandParties(Map<UUID, UUID> parties) {
        this.parties = new HashMap<>(parties);
    }

    public static IslandParties get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(TYPE);
    }

    /** The party {@code island} owns, if it has one yet. */
    public Optional<UUID> party(UUID island) {
        return Optional.ofNullable(parties.get(island));
    }

    /** The island that owns {@code party}, if any. */
    public Optional<UUID> islandOf(UUID party) {
        return parties.entrySet().stream().filter(link -> link.getValue().equals(party)).map(Map.Entry::getKey).findFirst();
    }

    /** Every island's party, by the island's team id. */
    public Map<UUID, UUID> all() {
        return Map.copyOf(parties);
    }

    public void link(UUID island, UUID party) {
        if (!party.equals(parties.put(island, party))) {
            setDirty();
        }
    }

    public void forget(UUID island) {
        if (parties.remove(island) != null) {
            setDirty();
        }
    }
}
