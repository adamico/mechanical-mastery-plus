// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.gametest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import io.github._5thlayer.mechanicalmasteryreborn.ServerConfig;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.Challenges;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.Handicap;
import io.github._5thlayer.mechanicalmasteryreborn.cod.Cod;
import io.github._5thlayer.mechanicalmasteryreborn.cod.CodBlockEntity;
import io.github._5thlayer.mechanicalmasteryreborn.essence.Essence;
import io.github._5thlayer.mechanicalmasteryreborn.essence.Essences;
import io.github._5thlayer.mechanicalmasteryreborn.record.TeamState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

/**
 * C.O.D.'s tier (#231): it makes the highest tier its team hasn't blocked, and nothing once all four are; the blocked
 * tiers and won challenges outlast a reset and a load; Inflation counts only decomposing of that tier; and on a drop,
 * the reserve keeps the old essence while the new spills on top. Each test is an island of its own, one team.
 */
final class CodTierTests {

    private static final BlockPos COD = new BlockPos(4, 2, 4);
    private static final int STACK = 64;
    /** Ticks past an interval's end, so a check never lands on the tick the essence is made. */
    private static final int MARGIN = 2;

    private CodTierTests() {
    }

    static void register(MechanicalMasteryRebornGameTests.Registrar tests) {
        tests.test("cod_tier_is_advanced_until_a_tier_is_blocked", 1, CodTierTests::advancedFirst);
        tests.test("cod_tier_makes_the_highest_tier_not_blocked", dormant() + 10, CodTierTests::makesTheHighestNotBlocked);
        tests.test("cod_tier_makes_nothing_once_all_four_are_blocked", 2 * waking() + 10, CodTierTests::nothingOnceAllBlocked);
        tests.test("cod_tier_blocked_tiers_and_won_challenges_survive_a_reset_and_a_load", 1, CodTierTests::surviveResetAndLoad);
        tests.test("cod_tier_inflation_counts_only_decomposing_of_its_tier", 1, CodTierTests::inflationCountsOnlyItsTier);
        tests.test("cod_tier_drop_keeps_the_old_essence_and_spills_the_new", 3 * waking() + 10, CodTierTests::dropKeepsOldAndSpillsNew);
        tests.test("cod_tier_dormant_drop_spills_the_new", dormant() + 10, CodTierTests::dormantDropSpillsTheNew);
        tests.test("cod_tier_command_suggests_teams", 1, CodTierTests::commandSuggestsTeams);
        tests.test("cod_tier_command_sets_a_team_s_tier", 1, CodTierTests::commandSetsTheTier);
    }

    /** Each win blocks the tier made, Advanced first, and a challenge counts once. */
    private static void advancedFirst(GameTestHelper helper) {
        UUID team = TestTeams.claim(helper);
        helper.assertValueEqual(state(helper).tier(team), Optional.of(Essence.ADVANCED), "a new team's tier");
        helper.assertTrue(state(helper).blockTier(team, Handicap.ENERGY), "the first win blocked nothing");
        helper.assertValueEqual(state(helper).tier(team), Optional.of(Essence.IMPROVED), "the tier after one win");
        helper.assertFalse(state(helper).blockTier(team, Handicap.ENERGY), "a challenge won twice blocked a tier");
        helper.assertTrue(state(helper).blockTier(team, Handicap.PROTECTION), "the second win blocked nothing");
        helper.assertTrue(state(helper).blockTier(team, Handicap.LIFE), "the third win blocked nothing");
        helper.assertValueEqual(state(helper).tier(team), Optional.of(Essence.BASIC), "the tier after three wins");
        helper.assertTrue(state(helper).blockTier(team, Handicap.MONEY), "the fourth win blocked nothing");
        helper.assertValueEqual(state(helper).tier(team), Optional.empty(), "the tier after four wins");
        helper.succeed();
    }

    /** A dormant C.O.D., emptied, makes Improved once Advanced is blocked. */
    private static void makesTheHighestNotBlocked(GameTestHelper helper) {
        UUID team = TestTeams.claim(helper);
        state(helper).setTier(team, Optional.of(Essence.IMPROVED));
        var cod = place(helper);
        takeAll(helper);
        helper.runAtTickTime(dormant() + MARGIN, () -> {
            helper.assertValueEqual(cod.tier(), Optional.of(Essence.IMPROVED), "C.O.D.'s tier");
            expectHolds(helper, Essence.IMPROVED, 1, "C.O.D. an interval after it was emptied");
            helper.succeed();
        });
    }

    /** Awake and emptied, with all four tiers blocked, it makes nothing, counts nothing and isn't making. */
    private static void nothingOnceAllBlocked(GameTestHelper helper) {
        UUID team = TestTeams.claim(helper);
        state(helper).setTier(team, Optional.empty());
        state(helper).wake(team);
        var cod = place(helper);
        takeAll(helper);
        helper.runAtTickTime(2 * waking() + MARGIN, () -> {
            helper.assertTrue(face(helper).getResource(0).isEmpty(), "C.O.D. made essence with every tier blocked");
            helper.assertTrue(pile(helper).isEmpty(), "C.O.D. spilled essence with every tier blocked");
            helper.assertValueEqual(state(helper).made(team), 0L, "the essence counted made with every tier blocked");
            helper.assertValueEqual(cod.tier(), Optional.empty(), "C.O.D.'s tier with every tier blocked");
            helper.assertFalse(cod.isMaking(), "C.O.D. reports making essence with every tier blocked");
            helper.assertValueEqual(cod.ticksTowardNext(), 0, "C.O.D.'s ticks toward the next with every tier blocked");
            helper.succeed();
        });
    }

    /** A won challenge's blocked tier outlasts a reset, an end, going dormant and a load. */
    private static void surviveResetAndLoad(GameTestHelper helper) {
        UUID team = TestTeams.claim(helper);
        var server = helper.getLevel().getServer();
        helper.assertTrue(Challenges.begin(server, team, Handicap.ENERGY), "the challenge didn't start");
        helper.assertTrue(state(helper).blockTier(team, Handicap.ENERGY), "the win blocked nothing");
        helper.assertTrue(state(helper).resetChallenge(team, server.overworld().getGameTime()), "the challenge didn't reset");
        state(helper).makeDormant(team);
        helper.assertTrue(state(helper).endChallenge(team), "the challenge didn't end");
        state(helper).makeDormant(team);
        var ops = helper.getLevel().registryAccess().createSerializationContext(NbtOps.INSTANCE);
        var codec = TeamState.TYPE.codec();
        TeamState loaded = codec.parse(ops, codec.encodeStart(ops, state(helper)).getOrThrow()).getOrThrow();
        helper.assertValueEqual(loaded.tier(team), Optional.of(Essence.IMPROVED), "the tier after a reset, an end and a load");
        helper.assertTrue(loaded.won(team, Handicap.ENERGY), "the won challenge was forgotten");
        helper.assertFalse(loaded.won(team, Handicap.LIFE), "a challenge never won reads as won");
        helper.assertValueEqual(loaded.tier(UUID.randomUUID()), Optional.of(Essence.ADVANCED), "another team's tier after a load");
        helper.succeed();
    }

    /** At Regular, since the game tests' pack decomposes only Advanced and Regular essence. */
    private static void inflationCountsOnlyItsTier(GameTestHelper helper) {
        UUID team = TestTeams.claim(helper);
        state(helper).setTier(team, Optional.of(Essence.REGULAR));
        helper.assertTrue(Challenges.begin(helper.getLevel().getServer(), team, Handicap.ENERGY), "the challenge didn't start");
        decomposeByHand(helper, team, Essence.ADVANCED);
        helper.assertValueEqual(state(helper).inflation(team).orElseThrow().decomposed(), 0L, "Advanced essence decomposed, a tier blocked");
        decomposeByHand(helper, team, Essence.REGULAR);
        helper.assertValueEqual(state(helper).inflation(team).orElseThrow().decomposed(), 1L, "Regular essence decomposed, C.O.D.'s tier");
        helper.succeed();
    }

    /** A full, awake C.O.D. drops to Improved: its Advanced stays, and Improved lands on its top. */
    private static void dropKeepsOldAndSpillsNew(GameTestHelper helper) {
        UUID team = TestTeams.claim(helper);
        state(helper).wake(team);
        place(helper);
        take(helper, STACK - 10);
        state(helper).setTier(team, Optional.of(Essence.IMPROVED));
        helper.runAtTickTime(2 * waking() + MARGIN, () -> {
            expectHolds(helper, Essence.ADVANCED, 10, "C.O.D. after its tier dropped");
            int improved = 0;
            for (ItemEntity item : pile(helper)) {
                helper.assertTrue(item.getItem().is(Essences.item(Essence.IMPROVED)), "C.O.D. spilled " + item.getItem());
                improved += item.getItem().getCount();
            }
            helper.assertValueEqual(improved, 2, "the Improved essence spilled in two intervals");
            take(helper, 10);
        });
        helper.runAtTickTime(2 * waking() + MARGIN + waking(), () -> {
            expectHolds(helper, Essence.IMPROVED, 1, "C.O.D. an interval after its Advanced was taken");
            helper.succeed();
        });
    }

    /** A full, dormant C.O.D. drops to Improved: its Advanced stays, and Improved spills at its dormant pace. */
    private static void dormantDropSpillsTheNew(GameTestHelper helper) {
        UUID team = TestTeams.claim(helper);
        place(helper);
        state(helper).setTier(team, Optional.of(Essence.IMPROVED));
        helper.runAtTickTime(dormant() + MARGIN, () -> {
            expectHolds(helper, Essence.ADVANCED, STACK, "a dormant C.O.D. after its tier dropped");
            var spilled = pile(helper);
            helper.assertValueEqual(spilled.size(), 1, "the essences a dormant C.O.D. spilled in an interval");
            helper.assertTrue(spilled.get(0).getItem().is(Essences.item(Essence.IMPROVED)), "a dormant C.O.D. spilled " + spilled.get(0).getItem());
            helper.succeed();
        });
    }

    /** {@code /cod tier} suggests the id of the team whose island it is run on, and of every team with a state. */
    private static void commandSuggestsTeams(GameTestHelper helper) {
        UUID team = TestTeams.claim(helper);
        UUID other = UUID.randomUUID();
        state(helper).wake(other);
        var server = helper.getLevel().getServer();
        var source = server.createCommandSourceStack().withPosition(helper.absoluteVec(COD.getCenter())).withLevel(helper.getLevel());
        var dispatcher = server.getCommands().getDispatcher();
        var suggestions = dispatcher.getCompletionSuggestions(dispatcher.parse("cod tier ", source)).join().getList().stream()
                .map(suggestion -> suggestion.getText()).toList();
        helper.assertTrue(suggestions.contains(team.toString()), "/cod tier didn't suggest the island's team: " + suggestions);
        helper.assertTrue(suggestions.contains(other.toString()), "/cod tier didn't suggest a team with a state: " + suggestions);
        helper.succeed();
    }

    /** {@code /cod tier} sets each tier, and none, and refuses one it doesn't know. */
    private static void commandSetsTheTier(GameTestHelper helper) {
        UUID team = TestTeams.claim(helper);
        var server = helper.getLevel().getServer();
        var source = server.createCommandSourceStack().withSuppressedOutput();
        server.getCommands().performPrefixedCommand(source, "cod tier " + team + " regular");
        helper.assertValueEqual(state(helper).tier(team), Optional.of(Essence.REGULAR), "the tier /cod tier regular set");
        server.getCommands().performPrefixedCommand(source, "cod tier " + team + " none");
        helper.assertValueEqual(state(helper).tier(team), Optional.empty(), "the tier /cod tier none set");
        server.getCommands().performPrefixedCommand(source, "cod tier " + team + " advanced");
        helper.assertValueEqual(state(helper).tier(team), Optional.of(Essence.ADVANCED), "the tier /cod tier advanced set");
        server.getCommands().performPrefixedCommand(source, "cod tier " + team + " golden");
        helper.assertValueEqual(state(helper).tier(team), Optional.of(Essence.ADVANCED), "the tier after an unknown one");
        helper.succeed();
    }

    private static CodBlockEntity place(GameTestHelper helper) {
        helper.setBlock(COD, Cod.BLOCK.get());
        return helper.getBlockEntity(COD, CodBlockEntity.class);
    }

    private static void takeAll(GameTestHelper helper) {
        take(helper, STACK);
    }

    private static void take(GameTestHelper helper, int count) {
        var face = face(helper);
        try (var transaction = Transaction.openRoot()) {
            face.extract(0, face.getResource(0), count, transaction);
            transaction.commit();
        }
    }

    private static void expectHolds(GameTestHelper helper, Essence essence, int count, String what) {
        var face = face(helper);
        ItemStack stack = face.getResource(0).toStack(face.getAmountAsInt(0));
        helper.assertTrue(stack.is(Essences.item(essence)), what + " holds " + stack + ", not " + essence.path());
        helper.assertValueEqual(stack.getCount(), count, what + "'s count");
    }

    private static List<ItemEntity> pile(GameTestHelper helper) {
        return helper.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(helper.absolutePos(COD.above())).inflate(1, 0.5, 1));
    }

    private static void decomposeByHand(GameTestHelper helper, UUID team, Essence essence) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        TestTeams.join(player, team);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Essences.item(essence)));
        player.getMainHandItem().use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
    }

    private static ResourceHandler<ItemResource> face(GameTestHelper helper) {
        var face = helper.getLevel().getCapability(Capabilities.Item.BLOCK, helper.absolutePos(COD), Direction.NORTH);
        helper.assertTrue(face != null, "C.O.D. has no item face");
        return face;
    }

    private static TeamState state(GameTestHelper helper) {
        return TeamState.get(helper.getLevel().getServer());
    }

    private static int dormant() {
        return ServerConfig.codDormantInterval();
    }

    private static int waking() {
        return ServerConfig.codWakingInterval();
    }
}
