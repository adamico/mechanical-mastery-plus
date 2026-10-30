// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.gametest;

import java.util.Optional;
import java.util.UUID;

import io.github._5thlayer.mechanicalmasteryreborn.ServerConfig;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.Challenges;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.Handicap;
import io.github._5thlayer.mechanicalmasteryreborn.cod.Cod;
import io.github._5thlayer.mechanicalmasteryreborn.essence.Essence;
import io.github._5thlayer.mechanicalmasteryreborn.essence.Essences;
import io.github._5thlayer.mechanicalmasteryreborn.record.Inflation;
import io.github._5thlayer.mechanicalmasteryreborn.record.TeamState;
import io.github._5thlayer.mechanicalmasteryreborn.recycler.RecyclerCoreBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.transaction.Transaction;

/**
 * Inflation (#204): from the moment a team chooses a handicap, the Advanced essence its awake C.O.D. makes minus the
 * Advanced essence the team decomposes, by hand or on a Recycler, never below zero. With no challenge played nothing
 * counts. Each test is an island of its own.
 */
final class InflationTests {

    /** Clear of the standard Recycler's 3x3, which stands at x 3 to 5 and z 2 to 4. */
    private static final BlockPos COD = new BlockPos(1, 2, 1);
    /** Another team's C.O.D., on an island of its own in a corner of the test's. */
    private static final BlockPos OTHER_COD = new BlockPos(7, 2, 7);
    /** Ticks past an interval's end, so a check never lands on the tick the essence is made. */
    private static final int MARGIN = 2;

    private InflationTests() {
    }

    static void register(MechanicalMasteryRebornGameTests.Registrar tests) {
        tests.test("inflation_counts_nothing_with_no_challenge", waking() + 10, InflationTests::nothingWithNoChallenge);
        tests.test("inflation_rises_with_each_made_and_falls_with_each_decompose", 2 * waking() + 10, InflationTests::risesAndFalls);
        tests.test("inflation_is_unchanged_by_voiding_essence", waking() + 10, InflationTests::unchangedByVoiding);
        tests.test("inflation_counts_a_hand_decompose_as_a_recycler_one", 10, InflationTests::handCountsAsRecycler);
        tests.test("inflation_is_untouched_by_another_team_s_cod", waking() + 10, InflationTests::untouchedByAnotherTeam);
        tests.test("inflation_survives_a_save_and_load", waking() + 10, InflationTests::survivesASaveAndLoad);
    }

    private static void nothingWithNoChallenge(GameTestHelper helper) {
        UUID team = TestTeams.claim(helper);
        helper.setBlock(COD, Cod.BLOCK.get());
        state(helper).wake(team);
        decomposeByHand(helper, team);
        helper.runAtTickTime(waking() + MARGIN, () -> {
            helper.assertTrue(state(helper).made(team) > 0, "the awake C.O.D. made nothing");
            helper.assertTrue(state(helper).inflation(team).isEmpty(), "a team that plays no challenge has Inflation");
            long now = helper.getLevel().getServer().overworld().getGameTime();
            helper.assertTrue(Challenges.start(helper.getLevel().getServer(), team, Handicap.ENERGY), "the challenge didn't start");
            Inflation inflation = state(helper).inflation(team).orElseThrow();
            helper.assertValueEqual(inflation.began(), now, "when the challenge began");
            helper.assertValueEqual(inflation.made(), 0L, "the essence made since the challenge began, as it begins");
            helper.assertValueEqual(inflation.decomposed(), 0L, "the essence decomposed since the challenge began, as it begins");
            helper.assertValueEqual(inflation.excess(), 0L, "the excess as the challenge begins");
            helper.succeed();
        });
    }

    private static void risesAndFalls(GameTestHelper helper) {
        UUID team = TestTeams.claim(helper);
        helper.setBlock(COD, Cod.BLOCK.get());
        start(helper, team);
        state(helper).wake(team);
        helper.runAtTickTime(waking() + MARGIN, () -> expect(helper, team, 1, 0, 1, "an interval into the challenge"));
        helper.runAtTickTime(2 * waking() + MARGIN, () -> {
            expect(helper, team, 2, 0, 2, "two intervals into the challenge");
            decomposeByHand(helper, team);
            expect(helper, team, 2, 1, 1, "after a decompose");
            decomposeByHand(helper, team);
            decomposeByHand(helper, team);
            expect(helper, team, 2, 3, 0, "after decomposing more than was made");
            helper.succeed();
        });
    }

    /** A full C.O.D. spills what it makes; taking it all out of its slot and off its pile to nowhere voids it. */
    private static void unchangedByVoiding(GameTestHelper helper) {
        UUID team = TestTeams.claim(helper);
        helper.setBlock(COD, Cod.BLOCK.get());
        start(helper, team);
        state(helper).wake(team);
        helper.runAtTickTime(waking() + MARGIN, () -> {
            expect(helper, team, 1, 0, 1, "an interval into the challenge");
            var face = helper.getLevel().getCapability(Capabilities.Item.BLOCK, helper.absolutePos(COD), Direction.NORTH);
            helper.assertTrue(face != null, "C.O.D. has no item face");
            try (var transaction = Transaction.openRoot()) {
                face.extract(0, face.getResource(0), face.getAmountAsInt(0), transaction);
                transaction.commit();
            }
            var pile = helper.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(helper.absolutePos(COD.above())).inflate(1, 0.5, 1));
            helper.assertFalse(pile.isEmpty(), "the full C.O.D. spilled nothing");
            pile.forEach(ItemEntity::discard);
            expect(helper, team, 1, 0, 1, "after voiding all it held and spilled");
            helper.succeed();
        });
    }

    private static void handCountsAsRecycler(GameTestHelper helper) {
        UUID team = TestTeams.claim(helper);
        start(helper, team);
        var build = RecyclerBuild.standard(helper);
        build.feed(1);
        build.turn(RecyclerCoreBlockEntity.TURNS);
        expect(helper, team, 0, 1, 0, "after a decompose on a Recycler");
        decomposeByHand(helper, team);
        expect(helper, team, 0, 2, 0, "after a decompose by hand too");
        helper.succeed();
    }

    /** Two teams play a challenge, awake; only the other one has a C.O.D., and decomposes. */
    private static void untouchedByAnotherTeam(GameTestHelper helper) {
        UUID team = TestTeams.claim(helper);
        UUID other = TestTeams.claim(helper, OTHER_COD, OTHER_COD);
        helper.setBlock(OTHER_COD, Cod.BLOCK.get());
        start(helper, team);
        start(helper, other);
        state(helper).wake(team);
        state(helper).wake(other);
        helper.runAtTickTime(waking() + MARGIN, () -> {
            decomposeByHand(helper, other);
            expect(helper, other, 1, 1, 0, "of the team whose C.O.D. made one and who decomposed one");
            expect(helper, team, 0, 0, 0, "of the team with no C.O.D. of its own");
            helper.succeed();
        });
    }

    private static void survivesASaveAndLoad(GameTestHelper helper) {
        UUID team = TestTeams.claim(helper);
        helper.setBlock(COD, Cod.BLOCK.get());
        start(helper, team);
        state(helper).wake(team);
        helper.runAtTickTime(waking() + MARGIN, () -> {
            decomposeByHand(helper, team);
            decomposeByHand(helper, team);
            Inflation before = state(helper).inflation(team).orElseThrow();
            var ops = helper.getLevel().registryAccess().createSerializationContext(NbtOps.INSTANCE);
            var codec = TeamState.TYPE.codec();
            Tag saved = codec.encodeStart(ops, state(helper)).getOrThrow();
            TeamState loaded = codec.parse(ops, saved).getOrThrow();
            helper.assertValueEqual(loaded.inflation(team), Optional.of(before), "the team's Inflation after a load");
            helper.assertValueEqual(before.made(), 1L, "the essence made before the save");
            helper.assertValueEqual(before.decomposed(), 2L, "the essence decomposed before the save");
            helper.assertTrue(loaded.inflation(UUID.randomUUID()).isEmpty(), "a team that plays no challenge has Inflation after a load");
            helper.succeed();
        });
    }

    private static void start(GameTestHelper helper, UUID team) {
        helper.assertTrue(Challenges.start(helper.getLevel().getServer(), team, Handicap.ENERGY), "the challenge didn't start");
    }

    private static void expect(GameTestHelper helper, UUID team, long made, long decomposed, long excess, String when) {
        var found = state(helper).inflation(team);
        helper.assertTrue(found.isPresent(), "no Inflation " + when);
        Inflation inflation = found.get();
        helper.assertValueEqual(inflation.made(), made, "the essence made since the challenge began, " + when);
        helper.assertValueEqual(inflation.decomposed(), decomposed, "the essence decomposed since the challenge began, " + when);
        helper.assertValueEqual(inflation.excess(), excess, "the excess, " + when);
    }

    /** Decomposes one Advanced essence by hand, as a player of {@code team}. */
    private static void decomposeByHand(GameTestHelper helper, UUID team) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        TestTeams.join(player, team);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Essences.item(Essence.ADVANCED)));
        player.getMainHandItem().use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
    }

    private static TeamState state(GameTestHelper helper) {
        return TeamState.get(helper.getLevel().getServer());
    }

    private static int waking() {
        return ServerConfig.codWakingInterval();
    }
}
