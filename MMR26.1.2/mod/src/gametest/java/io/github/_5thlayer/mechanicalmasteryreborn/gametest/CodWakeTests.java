// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.gametest;

import java.util.UUID;

import io.github._5thlayer.mechanicalmasteryreborn.ServerConfig;
import io.github._5thlayer.mechanicalmasteryreborn.cod.Cod;
import io.github._5thlayer.mechanicalmasteryreborn.essence.Essence;
import io.github._5thlayer.mechanicalmasteryreborn.essence.Essences;
import io.github._5thlayer.mechanicalmasteryreborn.record.DecomposeLog;
import io.github._5thlayer.mechanicalmasteryreborn.record.TeamState;
import io.github._5thlayer.mechanicalmasteryreborn.record.Teams;
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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

/**
 * C.O.D. waking (#167): a team's first decompose on a Recycler wakes its C.O.D., and only that team's, and decomposing
 * by hand never does. Awake, it makes essence at its waking pace without stopping, into its slot, which pushes into
 * what is beside it; once the slot is full and nothing takes from it, onto a pile on its top of up to a stack, and
 * past that only into the team's count. Each test is an island of its own.
 */
final class CodWakeTests {

    /** Clear of the standard Recycler's 3x3, which stands at x 3 to 5 and z 2 to 4. */
    private static final BlockPos COD = new BlockPos(1, 2, 1);
    /** Another team's C.O.D., on an island of its own in a corner of the test's. */
    private static final BlockPos OTHER_COD = new BlockPos(7, 2, 7);
    /** Ticks either side of an interval's end, so a check never lands on the tick the essence is made. */
    private static final int MARGIN = 2;
    private static final int STACK = 64;

    private CodWakeTests() {
    }

    static void register(MechanicalMasteryRebornGameTests.Registrar tests) {
        tests.test("cod_stays_dormant_however_many_decompose_by_hand", dormant() + 10, CodWakeTests::staysDormantByHand);
        tests.test("cod_wakes_on_its_team_s_first_recycler_decompose", waking() + 10, CodWakeTests::wakesOnARecyclerDecompose);
        tests.test("cod_awake_holds_its_waking_pace", 5 * waking() + 10, CodWakeTests::holdsItsWakingPace);
        tests.test("cod_awake_pushes_into_an_adjacent_inventory", waking() + 10, CodWakeTests::pushesIntoAnAdjacentInventory);
        tests.test("cod_awake_spills_out_of_its_top_once_full", 2 * waking() + 10, CodWakeTests::spillsOnceFull);
        tests.test("cod_awake_pile_stops_at_a_stack_but_still_counts", 2 * waking() + 10, CodWakeTests::pileStopsAtAStack);
        tests.test("cod_team_state_survives_a_save_and_load", waking() + 10, CodWakeTests::survivesASaveAndLoad);
        tests.test("cod_team_state_is_shared_by_a_joining_player", 1, CodWakeTests::sharedByAJoiningPlayer);
        tests.test("cod_awake_feeds_a_hopper_below_only_what_it_makes", 3 * waking() + 20, CodWakeTests::feedsAHopperOnlyWhatItMakes);
    }

    private static void staysDormantByHand(GameTestHelper helper) {
        UUID team = TestTeams.claim(helper);
        place(helper, COD);
        empty(helper, COD);
        for (int i = 0; i < 3; i++) {
            var player = helper.makeMockPlayer(GameType.SURVIVAL);
            TestTeams.join(player, team);
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Essences.item(Essence.ADVANCED)));
            player.getMainHandItem().use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
        }
        helper.assertValueEqual(DecomposeLog.get(helper.getLevel().getServer()).count(team, Essence.ADVANCED, DecomposeLog.Way.HAND), 3L,
                "the Advanced essence the team decomposed by hand");
        helper.assertFalse(state(helper).isAwake(team), "the team woke by decomposing by hand");
        helper.runAtTickTime(waking() + MARGIN, () -> expectHolds(helper, COD, 0, "a dormant C.O.D. a waking interval after it was emptied"));
        helper.runAtTickTime(dormant() + MARGIN, () -> {
            expectHolds(helper, COD, 1, "a dormant C.O.D. a dormant interval after it was emptied");
            helper.succeed();
        });
    }

    private static void wakesOnARecyclerDecompose(GameTestHelper helper) {
        UUID team = TestTeams.claim(helper);
        UUID other = TestTeams.claim(helper, OTHER_COD, OTHER_COD);
        place(helper, COD);
        place(helper, OTHER_COD);
        empty(helper, COD);
        empty(helper, OTHER_COD);
        var build = RecyclerBuild.standard(helper);
        build.feed(1);
        build.turn(RecyclerCoreBlockEntity.TURNS);
        helper.assertTrue(state(helper).isAwake(team), "the team whose Recycler decomposed is still dormant");
        helper.assertFalse(state(helper).isAwake(other), "another team woke");
        helper.runAtTickTime(waking() + MARGIN, () -> {
            expectHolds(helper, COD, 1, "the woken C.O.D. a waking interval later");
            expectHolds(helper, OTHER_COD, 0, "the other team's C.O.D. a waking interval later");
            helper.succeed();
        });
    }

    private static void holdsItsWakingPace(GameTestHelper helper) {
        UUID team = TestTeams.claim(helper);
        place(helper, COD);
        empty(helper, COD);
        state(helper).wake(team);
        int interval = waking();
        for (int made = 0; made < 5; made++) {
            int before = made;
            helper.runAtTickTime((made + 1) * interval - MARGIN,
                    () -> expectHolds(helper, COD, before, "an awake C.O.D. just before interval " + (before + 1)));
        }
        helper.runAtTickTime(5 * interval + MARGIN, () -> {
            expectHolds(helper, COD, 5, "an awake C.O.D. just after five intervals");
            helper.succeed();
        });
    }

    private static void pushesIntoAnAdjacentInventory(GameTestHelper helper) {
        UUID team = TestTeams.claim(helper);
        place(helper, COD);
        helper.setBlock(COD.east(), Blocks.CHEST);
        state(helper).wake(team);
        helper.runAtTickTime(MARGIN, () -> {
            expectHolds(helper, COD, 0, "an awake C.O.D. beside a chest");
            helper.assertValueEqual(inChest(helper, COD.east()), STACK, "the essence pushed into the chest at once");
        });
        helper.runAtTickTime(waking() + MARGIN, () -> {
            expectHolds(helper, COD, 0, "an awake C.O.D. beside a chest an interval later");
            helper.assertValueEqual(inChest(helper, COD.east()), STACK + 1, "the essence pushed into the chest an interval later");
            expectPile(helper, 0, "with a chest beside it");
            helper.succeed();
        });
    }

    private static void spillsOnceFull(GameTestHelper helper) {
        UUID team = TestTeams.claim(helper);
        place(helper, COD);
        state(helper).wake(team);
        helper.runAtTickTime(waking() - MARGIN, () -> expectPile(helper, 0, "just before an interval"));
        helper.runAtTickTime(waking() + MARGIN, () -> {
            expectHolds(helper, COD, STACK, "a full C.O.D. that nothing takes from");
            expectPile(helper, 1, "an interval after it woke full");
        });
        helper.runAtTickTime(2 * waking() + MARGIN, () -> {
            expectPile(helper, 2, "two intervals after it woke full");
            helper.succeed();
        });
    }

    private static void pileStopsAtAStack(GameTestHelper helper) {
        UUID team = TestTeams.claim(helper);
        place(helper, COD);
        BlockPos top = helper.absolutePos(COD.above());
        var pile = new ItemEntity(helper.getLevel(), top.getX() + 0.5, top.getY(), top.getZ() + 0.5,
                new ItemStack(Essences.item(Essence.ADVANCED), STACK), 0, 0, 0);
        helper.getLevel().addFreshEntity(pile);
        state(helper).wake(team);
        long before = state(helper).made(team);
        helper.runAtTickTime(2 * waking() + MARGIN, () -> {
            expectPile(helper, STACK, "two intervals after it woke under a stack's pile");
            helper.assertValueEqual(state(helper).made(team), before + 2, "the essence the team's C.O.D. counts as made");
            helper.succeed();
        });
    }

    private static void feedsAHopperOnlyWhatItMakes(GameTestHelper helper) {
        UUID team = TestTeams.claim(helper);
        place(helper, COD);
        empty(helper, COD);
        helper.setBlock(COD.below(), Blocks.HOPPER);
        state(helper).wake(team);
        helper.runAtTickTime(3 * waking() + 10 + MARGIN, () -> {
            helper.assertValueEqual(inChest(helper, COD.below()), 3, "the essence in the hopper after three intervals");
            expectHolds(helper, COD, 0, "an awake C.O.D. over a hopper");
            expectPile(helper, 0, "over a hopper");
            helper.succeed();
        });
    }

    private static void survivesASaveAndLoad(GameTestHelper helper) {
        UUID team = TestTeams.claim(helper);
        place(helper, COD);
        state(helper).wake(team);
        helper.runAtTickTime(waking() + MARGIN, () -> {
            var ops = helper.getLevel().registryAccess().createSerializationContext(NbtOps.INSTANCE);
            var codec = TeamState.TYPE.codec();
            Tag saved = codec.encodeStart(ops, state(helper)).getOrThrow();
            TeamState loaded = codec.parse(ops, saved).getOrThrow();
            helper.assertTrue(loaded.isAwake(team), "the team is dormant after a load");
            helper.assertValueEqual(loaded.made(team), 1L, "the essence the team's C.O.D. made, after a load");
            helper.assertFalse(loaded.isAwake(UUID.randomUUID()), "a team that never woke is awake after a load");
            helper.succeed();
        });
    }

    private static void sharedByAJoiningPlayer(GameTestHelper helper) {
        UUID team = TestTeams.claim(helper);
        state(helper).wake(team);
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        TestTeams.join(player, team);
        helper.assertTrue(state(helper).isAwake(Teams.of(player)), "the team a player joined is dormant to them");
        helper.succeed();
    }

    /** Fails unless the pile on top of C.O.D. holds {@code count} essence. */
    private static void expectPile(GameTestHelper helper, int count, String when) {
        int piled = 0;
        AABB above = new AABB(helper.absolutePos(COD.above())).inflate(1, 0.5, 1);
        for (ItemEntity item : helper.getLevel().getEntitiesOfClass(ItemEntity.class, above)) {
            piled += item.getItem().is(Essences.item(Essence.ADVANCED)) ? item.getItem().getCount() : 0;
        }
        helper.assertValueEqual(piled, count, "the essence piled on C.O.D. " + when);
    }

    private static int inChest(GameTestHelper helper, BlockPos pos) {
        var face = helper.getLevel().getCapability(Capabilities.Item.BLOCK, helper.absolutePos(pos), null);
        helper.assertTrue(face != null, "no item face at " + pos);
        int count = 0;
        for (int slot = 0; slot < face.size(); slot++) {
            count += face.getAmountAsInt(slot);
        }
        return count;
    }

    private static void place(GameTestHelper helper, BlockPos pos) {
        helper.setBlock(pos, Cod.BLOCK.get());
    }

    /** Takes all C.O.D. at {@code pos} holds, to nowhere. */
    private static void empty(GameTestHelper helper, BlockPos pos) {
        var face = face(helper, pos);
        try (var transaction = Transaction.openRoot()) {
            face.extract(0, face.getResource(0), face.getAmountAsInt(0), transaction);
            transaction.commit();
        }
    }

    private static void expectHolds(GameTestHelper helper, BlockPos pos, int count, String what) {
        var face = face(helper, pos);
        if (count == 0) {
            helper.assertTrue(ResourceHandlerUtil.isEmpty(face), what + " isn't empty");
            return;
        }
        ItemStack held = face.getResource(0).toStack(face.getAmountAsInt(0));
        helper.assertTrue(held.is(Essences.item(Essence.ADVANCED)), what + " holds " + held + ", not Advanced essence");
        helper.assertValueEqual(held.getCount(), count, what + "'s count");
    }

    private static ResourceHandler<ItemResource> face(GameTestHelper helper, BlockPos pos) {
        var face = helper.getLevel().getCapability(Capabilities.Item.BLOCK, helper.absolutePos(pos), Direction.NORTH);
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
