// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.record;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;

/**
 * Keeping each island's players in one FTB party of its own (#225), so that AdminShop's Account and FTB Chunks' claims,
 * which go by FTB Teams, mean the same team as {@link Teams}, which goes by Skyblock Builder's. A reconciliation rather
 * than event bookkeeping: from who is on which island and who is in which party, it works out what to change, and
 * asks for nothing once the two agree, so it can run as often as it likes. Plain data, so a JUnit test reads it
 * without Minecraft.
 *
 * <p>Each move changes one thing, and the next run picks up from there: a player in another party leaves it before
 * joining their island's, and a party is created before anyone joins it. Only a player online is moved, as FTB Teams
 * moves players, so one who is offline waits for their next login.
 */
public final class PartySync {

    /** A Skyblock Builder team, by its name and its players. */
    public record Island(String name, Set<UUID> members) {
    }

    /** An FTB party, by its name and its players. */
    public record Party(String name, Set<UUID> members) {
    }

    /** One change to FTB Teams, or to which party an island owns. */
    public sealed interface Move {

        /** Create {@code island}'s party, named {@code name}, owned by {@code owner}, who must be online. */
        record Create(UUID island, String name, UUID owner) implements Move {
        }

        /** {@code player} joins {@code party}. */
        record Join(UUID player, UUID party) implements Move {
        }

        /** {@code player} leaves {@code party}, back to a team of their own. */
        record Leave(UUID player, UUID party) implements Move {
        }

        /** {@code party} takes {@code name}, its island's. */
        record Rename(UUID party, String name) implements Move {
        }

        /** {@code island} no longer owns a party, since the one it owned is gone. */
        record Forget(UUID island) implements Move {
        }
    }

    private PartySync() {
    }

    /**
     * What to change so each island's players are in its party and nobody else is: {@code islands} by their team id,
     * {@code owned} the party each island owns, {@code parties} every FTB party by its id, and {@code online} the
     * players who are online. In a fixed order, by id, so a run is the same however the maps iterate.
     */
    public static List<Move> moves(Map<UUID, Island> islands, Map<UUID, UUID> owned, Map<UUID, Party> parties, Set<UUID> online) {
        List<Move> moves = new ArrayList<>();
        Map<UUID, UUID> partyOf = new TreeMap<>();
        new TreeMap<>(parties).forEach((id, party) -> party.members().forEach(player -> partyOf.putIfAbsent(player, id)));

        new TreeMap<>(owned).forEach((islandId, partyId) -> {
            Party party = parties.get(partyId);
            Island island = islands.get(islandId);
            if (party == null) {
                moves.add(new Move.Forget(islandId));
                return;
            }
            for (UUID player : sorted(party.members())) {
                if (!online.contains(player)) {
                    continue;
                }
                if (island == null || !island.members().contains(player)) {
                    moves.add(new Move.Leave(player, partyId));
                }
            }
            if (island != null && !party.name().equals(island.name())) {
                moves.add(new Move.Rename(partyId, island.name()));
            }
        });

        new TreeMap<>(islands).forEach((islandId, island) -> {
            UUID partyId = owned.get(islandId);
            if (partyId != null && !parties.containsKey(partyId)) {
                return;
            }
            for (UUID player : sorted(island.members())) {
                if (!online.contains(player)) {
                    continue;
                }
                UUID current = partyOf.get(player);
                // A player in another island's party is taken out by that island's own check above.
                if (current != null && !current.equals(partyId) && !owned.containsValue(current)) {
                    moves.add(new Move.Leave(player, current));
                } else if (current == null && partyId != null) {
                    moves.add(new Move.Join(player, partyId));
                }
            }
            if (partyId == null) {
                // FTB Teams makes a party only for an online player in no party.
                sorted(island.members()).stream()
                        .filter(player -> online.contains(player) && !partyOf.containsKey(player))
                        .findFirst()
                        .ifPresent(owner -> moves.add(new Move.Create(islandId, island.name(), owner)));
            }
        });
        return moves;
    }

    private static List<UUID> sorted(Set<UUID> players) {
        return players.stream().sorted().toList();
    }
}
