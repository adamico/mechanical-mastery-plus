// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.gametest;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import io.github._5thlayer.mechanicalmasteryreborn.ServerConfig;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.Challenges;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.Handicap;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.OverproductionBar;
import io.github._5thlayer.mechanicalmasteryreborn.cod.Cod;
import io.github._5thlayer.mechanicalmasteryreborn.essence.Essence;
import io.github._5thlayer.mechanicalmasteryreborn.essence.Essences;
import io.github._5thlayer.mechanicalmasteryreborn.record.Overproduction;
import io.github._5thlayer.mechanicalmasteryreborn.record.TeamState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.transaction.Transaction;

/**
 * Overproduction (#204): from the moment a team chooses a handicap, the Advanced essence its awake C.O.D. makes minus the
 * Advanced essence the team decomposes, by hand or on a Recycler, never below zero. With no challenge played nothing
 * counts. Each test is an island of its own.
 */
final class OverproductionTests {

    /** Clear of the standard Recycler's 3x3, which stands at x 3 to 5 and z 2 to 4. */
    private static final BlockPos COD = new BlockPos(1, 2, 1);
    /** Another team's C.O.D., on an island of its own in a corner of the test's. */
    private static final BlockPos OTHER_COD = new BlockPos(7, 2, 7);
    /** Ticks past an interval's end, so a check never lands on the tick the essence is made. */
    private static final int MARGIN = 2;

    private OverproductionTests() {
    }

    static void register(MechanicalMasteryRebornGameTests.Registrar tests) {
        tests.test("overproduction_counts_nothing_with_no_challenge", waking() + 10, OverproductionTests::nothingWithNoChallenge);
        tests.test("overproduction_rises_with_each_made_and_falls_with_each_decompose", 3 * waking() + 10, OverproductionTests::risesAndFalls);
        tests.test("overproduction_is_unchanged_by_voiding_essence", waking() + 10, OverproductionTests::unchangedByVoiding);
        tests.test("overproduction_counts_a_hand_decompose_as_a_recycler_one", 10, OverproductionTests::handCountsAsRecycler);
        tests.test("overproduction_is_untouched_by_another_team_s_cod", waking() + 10, OverproductionTests::untouchedByAnotherTeam);
        tests.test("overproduction_survives_a_save_and_load", waking() + 10, OverproductionTests::survivesASaveAndLoad);
        tests.test("overproduction_carries_over_from_its_old_name", waking() + 10, OverproductionTests::carriesOverFromItsOldName);
        tests.test("overproduction_bar_shows_the_excess_against_the_cap_to_its_team", waking() + 10, OverproductionTests::barShowsTheExcess);
        tests.test("overproduction_bar_shows_only_while_a_challenge_is_played", 10, OverproductionTests::barOnlyWhilePlayed);
    }

    private static void nothingWithNoChallenge(GameTestHelper helper) {
        UUID team = TestTeams.claim(helper);
        helper.setBlock(COD, Cod.BLOCK.get());
        state(helper).wake(team);
        decomposeByHand(helper, team);
        helper.runAtTickTime(waking() + MARGIN, () -> {
            helper.assertTrue(state(helper).made(team) > 0, "the awake C.O.D. made nothing");
            helper.assertTrue(state(helper).overproduction(team).isEmpty(), "a team that plays no challenge has Overproduction");
            long now = helper.getLevel().getServer().overworld().getGameTime();
            helper.assertTrue(Challenges.begin(helper.getLevel().getServer(), team, Handicap.ENERGY), "the challenge didn't start");
            Overproduction overproduction = state(helper).overproduction(team).orElseThrow();
            helper.assertValueEqual(overproduction.began(), now, "when the challenge began");
            helper.assertValueEqual(overproduction.made(), 0L, "the essence made since the challenge began, as it begins");
            helper.assertValueEqual(overproduction.decomposed(), 0L, "the essence decomposed since the challenge began, as it begins");
            helper.assertValueEqual(overproduction.excess(), 0L, "the excess as the challenge begins");
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
        });
        // The clamp is on the totals: the one decomposed beyond what was made is credit the next one made uses up.
        helper.runAtTickTime(3 * waking() + MARGIN, () -> {
            expect(helper, team, 3, 3, 0, "after one more made");
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
        build.turn(ServerConfig.turnsOf(1));
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
            Overproduction before = state(helper).overproduction(team).orElseThrow();
            var ops = helper.getLevel().registryAccess().createSerializationContext(NbtOps.INSTANCE);
            var codec = TeamState.TYPE.codec();
            Tag saved = codec.encodeStart(ops, state(helper)).getOrThrow();
            TeamState loaded = codec.parse(ops, saved).getOrThrow();
            helper.assertValueEqual(loaded.overproduction(team), Optional.of(before), "the team's Overproduction after a load");
            helper.assertValueEqual(before.made(), 1L, "the essence made before the save");
            helper.assertValueEqual(before.decomposed(), 2L, "the essence decomposed before the save");
            helper.assertTrue(loaded.overproduction(UUID.randomUUID()).isEmpty(), "a team that plays no challenge has Overproduction after a load");
            helper.succeed();
        });
    }

    /** A world saved when Overproduction was named Inflation keeps the challenge it plays, and its count, on load. */
    private static void carriesOverFromItsOldName(GameTestHelper helper) {
        UUID team = TestTeams.claim(helper);
        helper.setBlock(COD, Cod.BLOCK.get());
        start(helper, team);
        state(helper).wake(team);
        helper.runAtTickTime(waking() + MARGIN, () -> {
            decomposeByHand(helper, team);
            Overproduction before = state(helper).overproduction(team).orElseThrow();
            var ops = helper.getLevel().registryAccess().createSerializationContext(NbtOps.INSTANCE);
            var codec = TeamState.TYPE.codec();
            Tag saved = codec.encodeStart(ops, state(helper)).getOrThrow();
            helper.assertTrue(renamed(saved, "overproduction", "inflation"), "nothing was saved as Overproduction");
            TeamState loaded = codec.parse(ops, saved).getOrThrow();
            helper.assertValueEqual(loaded.overproduction(team), Optional.of(before), "the team's Overproduction, saved as Inflation, after a load");
            helper.succeed();
        });
    }

    /** Renames every {@code from} key under {@code tag} to {@code to}, as a world saved before a rename holds it; says whether any was. */
    private static boolean renamed(Tag tag, String from, String to) {
        boolean any = false;
        if (tag instanceof CompoundTag compound) {
            for (String key : Set.copyOf(compound.keySet())) {
                any |= renamed(compound.get(key), from, to);
            }
            Tag value = compound.get(from);
            if (value != null) {
                compound.remove(from);
                compound.put(to, value);
                any = true;
            }
        } else if (tag instanceof ListTag list) {
            for (Tag element : list) {
                any |= renamed(element, from, to);
            }
        }
        return any;
    }

    /** Its team's player sees the bar fill toward the cap; another team's player, playing no challenge, sees none. */
    private static void barShowsTheExcess(GameTestHelper helper) {
        UUID team = TestTeams.claim(helper);
        UUID other = TestTeams.claim(helper, OTHER_COD, OTHER_COD);
        ServerPlayer player = onlinePlayer(helper, team);
        ServerPlayer stranger = onlinePlayer(helper, other);
        helper.setBlock(COD, Cod.BLOCK.get());
        start(helper, team);
        state(helper).wake(team);
        helper.runAtTickTime(waking() + MARGIN, () -> {
            var bar = OverproductionBar.of(team);
            helper.assertTrue(bar.isPresent(), "a team that plays a challenge has no bar");
            helper.assertValueEqual(bar.get().getProgress(), 1.0F / ServerConfig.overproductionCap(), "the bar's fill, one made");
            helper.assertValueEqual(bar.get().getName(), Component.translatable("mechanicalmasteryreborn.bar.challenge",
                    Component.translatable("mechanicalmasteryreborn.challenge.title", Component.translatable("mechanicalmasteryreborn.handicap.energy")),
                    Component.translatable("mechanicalmasteryreborn.bar.resets",
                            Component.translatable("mechanicalmasteryreborn.bar.overproduction", 1L, ServerConfig.overproductionCap()))), "the bar's title");
            String read = bar.get().getName().getString();
            helper.assertTrue(read.contains("resets") && read.contains("essence"), "the bar's title doesn't name the reset and what it loses: " + read);
            helper.assertTrue(bar.get().getPlayers().contains(player), "the team's player doesn't see its bar");
            helper.assertFalse(bar.get().getPlayers().contains(stranger), "another team's player sees the bar");
            helper.assertTrue(OverproductionBar.of(other).isEmpty(), "a team that plays no challenge has a bar");
            decomposeByHand(helper, team);
        });
        helper.runAtTickTime(waking() + MARGIN + 1, () -> {
            helper.assertValueEqual(OverproductionBar.of(team).orElseThrow().getProgress(), 0.0F, "the bar's fill after a decompose");
            leave(helper, player, stranger);
            helper.succeed();
        });
    }

    private static void barOnlyWhilePlayed(GameTestHelper helper) {
        UUID team = TestTeams.claim(helper);
        ServerPlayer player = onlinePlayer(helper, team);
        helper.runAtTickTime(1, () -> {
            helper.assertTrue(OverproductionBar.of(team).isEmpty(), "a team that plays no challenge has a bar");
            start(helper, team);
        });
        helper.runAtTickTime(2, () -> {
            helper.assertTrue(OverproductionBar.of(team).orElseThrow().getPlayers().contains(player), "the bar didn't show as the challenge began");
            helper.assertTrue(state(helper).endChallenge(team), "the challenge didn't end");
        });
        helper.runAtTickTime(3, () -> {
            helper.assertTrue(OverproductionBar.of(team).isEmpty(), "the bar stayed after the challenge ended");
            leave(helper, player);
            helper.succeed();
        });
    }

    /** A player of {@code team}, online, as the bar finds its players. */
    @SuppressWarnings("removal")
    private static ServerPlayer onlinePlayer(GameTestHelper helper, UUID team) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        TestTeams.join(player, team);
        return player;
    }

    private static void leave(GameTestHelper helper, ServerPlayer... players) {
        for (ServerPlayer player : players) {
            helper.getLevel().getServer().getPlayerList().remove(player);
        }
    }

    private static void start(GameTestHelper helper, UUID team) {
        helper.assertTrue(Challenges.begin(helper.getLevel().getServer(), team, Handicap.ENERGY), "the challenge didn't start");
    }

    private static void expect(GameTestHelper helper, UUID team, long made, long decomposed, long excess, String when) {
        var found = state(helper).overproduction(team);
        helper.assertTrue(found.isPresent(), "no Overproduction " + when);
        Overproduction overproduction = found.get();
        helper.assertValueEqual(overproduction.made(), made, "the essence made since the challenge began, " + when);
        helper.assertValueEqual(overproduction.decomposed(), decomposed, "the essence decomposed since the challenge began, " + when);
        helper.assertValueEqual(overproduction.excess(), excess, "the excess, " + when);
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
