// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.record;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import io.github._5thlayer.mechanicalmasteryreborn.record.PartySync.Island;
import io.github._5thlayer.mechanicalmasteryreborn.record.PartySync.Move;
import io.github._5thlayer.mechanicalmasteryreborn.record.PartySync.Party;
import org.junit.jupiter.api.Test;

/**
 * Keeping each island's players in one FTB party of its own (#225): what {@link PartySync#moves} asks for, from who
 * is on which island and who is in which party.
 */
class PartySyncTest {

    private static final UUID ISLAND = id(1);
    private static final UUID OTHER_ISLAND = id(2);
    private static final UUID PARTY = id(10);
    private static final UUID OTHER_PARTY = id(11);
    private static final UUID ALICE = id(100);
    private static final UUID BOB = id(101);

    @Test
    void anIslandWithNoPartyGetsOneOwnedByAMemberOnline() {
        var moves = PartySync.moves(Map.of(ISLAND, new Island("Alice", Set.of(ALICE))), Map.of(), Map.of(), Set.of(ALICE));
        assertEquals(List.of(new Move.Create(ISLAND, "Alice", ALICE)), moves);
    }

    @Test
    void anIslandWithNoMemberOnlineWaitsForOne() {
        var moves = PartySync.moves(Map.of(ISLAND, new Island("Alice", Set.of(ALICE))), Map.of(), Map.of(), Set.of());
        assertEquals(List.of(), moves);
    }

    @Test
    void aMemberInAnotherPartyLeavesItBeforeTheIslandsPartyIsCreated() {
        var moves = PartySync.moves(
                Map.of(ISLAND, new Island("Alice", Set.of(ALICE))),
                Map.of(),
                Map.of(OTHER_PARTY, new Party("Old friends", Set.of(ALICE))),
                Set.of(ALICE));
        assertEquals(List.of(new Move.Leave(ALICE, OTHER_PARTY)), moves);
    }

    @Test
    void aPlayerWhoJoinedAnIslandJoinsItsParty() {
        var moves = PartySync.moves(
                Map.of(ISLAND, new Island("Alice", Set.of(ALICE, BOB))),
                Map.of(ISLAND, PARTY),
                Map.of(PARTY, new Party("Alice", Set.of(ALICE))),
                Set.of(BOB));
        assertEquals(List.of(new Move.Join(BOB, PARTY)), moves);
    }

    @Test
    void aPlayerInAnotherPartyLeavesItFirst() {
        var moves = PartySync.moves(
                Map.of(ISLAND, new Island("Alice", Set.of(ALICE, BOB))),
                Map.of(ISLAND, PARTY),
                Map.of(PARTY, new Party("Alice", Set.of(ALICE)), OTHER_PARTY, new Party("Bob's friends", Set.of(BOB))),
                Set.of(BOB));
        assertEquals(List.of(new Move.Leave(BOB, OTHER_PARTY)), moves);
    }

    @Test
    void aPlayerWhoLeftAnIslandLeavesItsParty() {
        var moves = PartySync.moves(
                Map.of(ISLAND, new Island("Alice", Set.of(ALICE)), OTHER_ISLAND, new Island("Bob", Set.of(BOB))),
                Map.of(ISLAND, PARTY),
                Map.of(PARTY, new Party("Alice", Set.of(ALICE, BOB))),
                Set.of(BOB));
        assertEquals(List.of(new Move.Leave(BOB, PARTY)), moves);
    }

    @Test
    void aPlayerOnNoIslandLeavesAnIslandsParty() {
        var moves = PartySync.moves(
                Map.of(ISLAND, new Island("Alice", Set.of(ALICE))),
                Map.of(ISLAND, PARTY),
                Map.of(PARTY, new Party("Alice", Set.of(ALICE, BOB))),
                Set.of(BOB));
        assertEquals(List.of(new Move.Leave(BOB, PARTY)), moves);
    }

    @Test
    void anOfflinePlayerWaitsToBeMoved() {
        var moves = PartySync.moves(
                Map.of(ISLAND, new Island("Alice", Set.of(ALICE, BOB))),
                Map.of(ISLAND, PARTY),
                Map.of(PARTY, new Party("Alice", Set.of(ALICE)), OTHER_PARTY, new Party("Bob's friends", Set.of(BOB))),
                Set.of(ALICE));
        assertEquals(List.of(), moves);
    }

    @Test
    void aPlayerOnNoIslandKeepsAPartyOfTheirOwn() {
        var moves = PartySync.moves(
                Map.of(),
                Map.of(),
                Map.of(OTHER_PARTY, new Party("Bob's friends", Set.of(BOB))),
                Set.of(BOB));
        assertEquals(List.of(), moves);
    }

    @Test
    void aRenamedIslandRenamesItsParty() {
        var moves = PartySync.moves(
                Map.of(ISLAND, new Island("Sky Farm", Set.of(ALICE))),
                Map.of(ISLAND, PARTY),
                Map.of(PARTY, new Party("Alice", Set.of(ALICE))),
                Set.of());
        assertEquals(List.of(new Move.Rename(PARTY, "Sky Farm")), moves);
    }

    @Test
    void anIslandWhosePartyIsGoneForgetsIt() {
        var moves = PartySync.moves(
                Map.of(ISLAND, new Island("Alice", Set.of(ALICE))),
                Map.of(ISLAND, PARTY),
                Map.of(),
                Set.of(ALICE));
        assertEquals(List.of(new Move.Forget(ISLAND)), moves);
    }

    @Test
    void aPartyOfAnIslandThatIsGoneIsForgottenOnceEmpty() {
        var moves = PartySync.moves(
                Map.of(),
                Map.of(ISLAND, PARTY),
                Map.of(PARTY, new Party("Alice", Set.of(ALICE))),
                Set.of(ALICE));
        assertEquals(List.of(new Move.Leave(ALICE, PARTY)), moves);
    }

    @Test
    void islandsInStepAskForNothing() {
        var moves = PartySync.moves(
                Map.of(ISLAND, new Island("Alice", Set.of(ALICE, BOB))),
                Map.of(ISLAND, PARTY),
                Map.of(PARTY, new Party("Alice", Set.of(ALICE, BOB))),
                Set.of(ALICE, BOB));
        assertEquals(List.of(), moves);
    }

    private static UUID id(long n) {
        return new UUID(0L, n);
    }
}
