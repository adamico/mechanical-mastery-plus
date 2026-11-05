// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.gametest;

import java.util.Optional;
import java.util.UUID;

import io.github._5thlayer.mechanicalmasteryreborn.ServerConfig;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.Chains;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.Handicap;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.LastStands;
import io.github._5thlayer.mechanicalmasteryreborn.cod.Cod;
import io.github._5thlayer.mechanicalmasteryreborn.cod.CodBlock;
import io.github._5thlayer.mechanicalmasteryreborn.cod.CodBlockEntity;
import io.github._5thlayer.mechanicalmasteryreborn.essence.Essence;
import io.github._5thlayer.mechanicalmasteryreborn.essence.Essences;
import io.github._5thlayer.mechanicalmasteryreborn.lab.Lab;
import io.github._5thlayer.mechanicalmasteryreborn.lab.LabMenu;
import io.github._5thlayer.mechanicalmasteryreborn.record.LastStand;
import io.github._5thlayer.mechanicalmasteryreborn.record.TeamState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * C.O.D.'s last stand (#261, ADR 0014): the lab shows and offers it only once the team has won all four challenges; started, C.O.D.
 * makes Nullessence at its floor, each one against the team's Lead, which never falls below minus the margin; Quintessence
 * put into C.O.D., by a hopper or by hand, raises the Lead and, a lag later, C.O.D.'s rate, never past its ceiling; and
 * once the Lead reaches the margin C.O.D. shuts down for good. The tests play at the default config. A test notes the
 * team's wins in its state directly, and sets its Lead as {@code /cod last_stand lead} does.
 */
final class LastStandTests {

    private static final BlockPos COD = new BlockPos(1, 2, 1);
    private static final BlockPos HOPPER = COD.above();
    private static final BlockPos LAB = new BlockPos(4, 2, 4);
    /** The default {@code last_stand.floor} the tests play at: 16 a minute, one each 75 ticks. */
    private static final double FLOOR = 16.0;
    private static final int FLOOR_INTERVAL = 75;
    /** Ticks past an interval's end, so a check never lands on the tick a Nullessence is made. */
    private static final int MARGIN = 2;
    /** Ample time for a hopper to push a few items, 8 ticks each. */
    private static final int HOPPER_TICKS = 40;

    private LastStandTests() {
    }

    static void register(MechanicalMasteryRebornGameTests.Registrar tests) {
        tests.test("last_stand_lab_offers_it_only_after_the_fourth_win", 1, LastStandTests::offeredAfterTheFourthWin);
        tests.test("last_stand_cod_makes_nullessence_at_its_floor", FLOOR_INTERVAL + MARGIN + 1, LastStandTests::makesAtTheFloor);
        tests.test("last_stand_lead_never_falls_below_minus_the_margin", 2 * FLOOR_INTERVAL + MARGIN + 1, LastStandTests::leadCapped);
        tests.test("last_stand_nullessence_kept_voided_or_decomposed_leaves_the_lead", FLOOR_INTERVAL - 10, LastStandTests::nullessenceLeavesTheLead);
        tests.test("last_stand_hopper_quintessence_raises_the_lead", HOPPER_TICKS + 1, LastStandTests::hopperRaisesTheLead);
        tests.test("last_stand_rate_rises_after_a_lag_and_never_past_the_ceiling", 41, LastStandTests::rateRisesAfterALag);
        tests.test("last_stand_won_at_the_margin_shuts_cod_down_for_good", FLOOR_INTERVAL + MARGIN + 1, LastStandTests::wonAtTheMargin);
        tests.test("last_stand_cod_takes_no_quintessence_before_it", 1, LastStandTests::noQuintessenceBefore);
    }

    /** Before the fourth win the lab shows it locked and its button starts nothing; after it, the lab starts it, once. */
    private static void offeredAfterTheFourthWin(GameTestHelper helper) {
        UUID team = TestTeams.claim(helper);
        var player = member(helper, team);
        win(helper, team, Handicap.ENERGY, Handicap.PROTECTION, Handicap.LIFE);
        LabMenu menu = open(helper, player);
        helper.assertValueEqual(menu.lastStand(), LabMenu.LastStandPhase.LOCKED, "the last stand after three wins");
        helper.assertFalse(menu.showsLastStand(), "the lab shows the last stand's row after three wins");
        helper.assertFalse(menu.clickMenuButton(player, LabMenu.START_LAST_STAND), "the lab started the last stand after three wins");
        helper.assertTrue(state(helper).lastStand(team).isEmpty(), "a last stand after three wins");
        win(helper, team, Handicap.MONEY);
        helper.assertTrue(menu.showsLastStand(), "a lab open through the fourth win still hides the last stand's row");
        menu = open(helper, player);
        helper.assertValueEqual(menu.lastStand(), LabMenu.LastStandPhase.OFFERED, "the last stand after the fourth win");
        helper.assertTrue(menu.showsLastStand(), "the lab hides the last stand's row after the fourth win");
        helper.assertTrue(menu.clickMenuButton(player, LabMenu.START_LAST_STAND), "the lab refused the last stand after the fourth win");
        helper.assertValueEqual(state(helper).lastStand(team), Optional.of(LastStand.starting(FLOOR)), "the last stand as it starts");
        helper.assertValueEqual(open(helper, player).lastStand(), LabMenu.LastStandPhase.RUNNING, "the lab once the last stand started");
        helper.assertFalse(open(helper, player).clickMenuButton(player, LabMenu.START_LAST_STAND), "the lab started the last stand twice");
        Chains.unlock(helper.getLevel().getServer(), player);
        helper.assertTrue(TestStages.holds(player, LastStands.STAGE), "no last stand's stage once it started");
        helper.succeed();
    }

    /** With no Quintessence going in, C.O.D. stays at its floor: one Nullessence each 75 ticks, each one off the Lead. */
    private static void makesAtTheFloor(GameTestHelper helper) {
        UUID team = started(helper);
        helper.setBlock(COD, Cod.BLOCK.get());
        helper.runAtTickTime(1, () -> helper.assertValueEqual(cod(helper).interval(), FLOOR_INTERVAL, "C.O.D.'s interval at its floor"));
        helper.runAtTickTime(FLOOR_INTERVAL + MARGIN, () -> {
            LastStand stand = state(helper).lastStand(team).orElseThrow();
            helper.assertValueEqual(stand.lead(), -1L, "the Lead after one Nullessence");
            helper.assertValueEqual(stand.rate(), FLOOR, "C.O.D.'s rate with no Quintessence going in");
            // Its reserve still holds the Advanced essence it was placed with, so the Nullessence goes onto its top.
            helper.assertItemEntityPresent(Essences.item(Essence.NULLESSENCE), COD.above(), 1.5);
            helper.succeed();
        });
    }

    private static void leadCapped(GameTestHelper helper) {
        UUID team = started(helper);
        long margin = ServerConfig.lastStandMargin();
        helper.assertTrue(state(helper).setLead(team, -margin + 1, margin), "the Lead wasn't set");
        helper.setBlock(COD, Cod.BLOCK.get());
        helper.runAtTickTime(2 * FLOOR_INTERVAL + MARGIN, () -> {
            helper.assertValueEqual(state(helper).lastStand(team).orElseThrow().lead(), -margin, "the Lead after two Nullessence");
            helper.succeed();
        });
    }

    /** What becomes of a Nullessence after it is made never moves the Lead: kept in hand, decomposed by hand, or voided. */
    private static void nullessenceLeavesTheLead(GameTestHelper helper) {
        UUID team = started(helper);
        var player = member(helper, team);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Essences.item(Essence.NULLESSENCE), 3));
        player.getMainHandItem().use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
        helper.assertValueEqual(player.getMainHandItem().getCount(), 2, "the Nullessence left after a decompose by hand");
        helper.assertTrue(player.getInventory().countItem(Blocks.COBBLESTONE.asItem()) > 0, "decomposing Nullessence gave nothing");
        var dropped = new ItemEntity(helper.getLevel(), 0, 0, 0, new ItemStack(Essences.item(Essence.NULLESSENCE)));
        helper.getLevel().addFreshEntity(dropped);
        dropped.discard();
        helper.assertValueEqual(state(helper).lastStand(team).orElseThrow().lead(), 0L, "the Lead");
        helper.succeed();
    }

    /**
     * Three Quintessence from a hopper above C.O.D. raise the Lead by three times what one cancels, less what C.O.D. made
     * meanwhile, and the hopper is never pushed what C.O.D. makes.
     */
    private static void hopperRaisesTheLead(GameTestHelper helper) {
        UUID team = started(helper);
        helper.setBlock(COD, Cod.BLOCK.get());
        helper.setBlock(HOPPER, Blocks.HOPPER.defaultBlockState().setValue(HopperBlock.FACING, Direction.DOWN));
        helper.getBlockEntity(HOPPER, HopperBlockEntity.class).setItem(0, new ItemStack(Essences.item(Essence.QUINTESSENCE), 3));
        helper.runAtTickTime(HOPPER_TICKS, () -> {
            var hopper = helper.getBlockEntity(HOPPER, HopperBlockEntity.class);
            helper.assertTrue(hopper.isEmpty(), "the hopper kept " + hopper.getItem(0));
            // Fewer ticks than an interval at the floor: C.O.D. has made nothing yet.
            helper.assertValueEqual(state(helper).lastStand(team).orElseThrow().lead(), 3L * ServerConfig.lastStandQuintessenceCancels(),
                    "the Lead after three Quintessence");
            helper.succeed();
        });
    }

    /** A handful of Quintessence lifts C.O.D.'s rate a little at a time, far short of the ceiling 40 ticks on. */
    private static void rateRisesAfterALag(GameTestHelper helper) {
        UUID team = started(helper);
        long margin = ServerConfig.lastStandMargin();
        state(helper).setLead(team, -margin, margin);
        state(helper).cancel(team, margin, ServerConfig.lastStandLag(), margin);
        helper.runAtTickTime(2, () -> {
            double rate = state(helper).lastStand(team).orElseThrow().rate();
            helper.assertTrue(rate > FLOOR, "C.O.D.'s rate didn't rise a tick after the Quintessence: " + rate);
        });
        helper.runAtTickTime(40, () -> {
            double rate = state(helper).lastStand(team).orElseThrow().rate();
            helper.assertTrue(rate < FLOOR + 2, "C.O.D.'s rate rose with no lag: " + rate);
            helper.assertTrue(rate <= ServerConfig.lastStandCeiling(), "C.O.D.'s rate past its ceiling: " + rate);
            helper.succeed();
        });
    }

    /**
     * One Quintessence short of the margin, a hand of two puts one in and wins: C.O.D. stands dark, makes nothing more,
     * takes nothing more, and its team's state says so after a load.
     */
    private static void wonAtTheMargin(GameTestHelper helper) {
        UUID team = started(helper);
        long margin = ServerConfig.lastStandMargin();
        state(helper).setLead(team, margin - ServerConfig.lastStandQuintessenceCancels(), margin);
        helper.setBlock(COD, Cod.BLOCK.get());
        var player = member(helper, team);
        helper.runAtTickTime(1, () -> {
            helper.assertValueEqual(use(helper, player, 2), 1, "the Quintessence a hand keeps past the margin");
            helper.assertTrue(state(helper).wonThePack(team), "the margin didn't win the pack");
            helper.assertTrue(helper.getBlockState(COD).getValue(CodBlock.SHUT_DOWN), "C.O.D. doesn't stand dark");
        });
        helper.runAtTickTime(FLOOR_INTERVAL + MARGIN, () -> {
            var cod = cod(helper);
            helper.assertFalse(cod.isMaking(), "C.O.D. makes after it shut down");
            helper.assertValueEqual(state(helper).lastStand(team).orElseThrow().lead(), margin, "the Lead after C.O.D. shut down");
            helper.assertValueEqual(use(helper, player, 1), 1, "the Quintessence a hand keeps once C.O.D. shut down");
            helper.assertTrue(helper.getBlockState(COD).getValue(CodBlock.SHUT_DOWN), "C.O.D. lit again");
            var ops = helper.getLevel().registryAccess().createSerializationContext(NbtOps.INSTANCE);
            var codec = TeamState.TYPE.codec();
            TeamState loaded = codec.parse(ops, codec.encodeStart(ops, state(helper)).getOrThrow()).getOrThrow();
            helper.assertTrue(loaded.wonThePack(team), "the win after a load");
            helper.assertValueEqual(open(helper, player).lastStand(), LabMenu.LastStandPhase.WON, "the lab once the pack is won");
            helper.succeed();
        });
    }

    /** Before its last stand C.O.D. takes no Quintessence, by hand or otherwise. */
    private static void noQuintessenceBefore(GameTestHelper helper) {
        UUID team = TestTeams.claim(helper);
        win(helper, team, Handicap.values());
        helper.setBlock(COD, Cod.BLOCK.get());
        helper.assertValueEqual(use(helper, member(helper, team), 3), 3, "the Quintessence a hand keeps before the last stand");
        helper.succeed();
    }

    /** A team of the test's island that has won all four challenges and started its last stand. */
    private static UUID started(GameTestHelper helper) {
        UUID team = TestTeams.claim(helper);
        win(helper, team, Handicap.values());
        helper.assertTrue(LastStands.begin(helper.getLevel().getServer(), team), "the last stand didn't start");
        return team;
    }

    /** Notes {@code handicaps}' challenges won for {@code team}, as winning each does, with none of a win's other effects. */
    private static void win(GameTestHelper helper, UUID team, Handicap... handicaps) {
        for (Handicap handicap : handicaps) {
            helper.assertTrue(state(helper).blockTier(team, handicap), "the " + handicap.id() + " challenge wasn't noted won");
        }
    }

    /** Right-clicks C.O.D. with {@code count} Quintessence, and says how many are left in hand. */
    private static int use(GameTestHelper helper, Player player, int count) {
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Essences.item(Essence.QUINTESSENCE), count));
        BlockPos pos = helper.absolutePos(COD);
        helper.getBlockState(COD).useItemOn(player.getMainHandItem(), helper.getLevel(), player, InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false));
        return player.getMainHandItem().getCount();
    }

    private static LabMenu open(GameTestHelper helper, Player player) {
        if (!helper.getBlockState(LAB).is(Lab.BLOCK.get())) {
            helper.setBlock(LAB, Lab.BLOCK.get());
        }
        var provider = helper.getBlockState(LAB).getMenuProvider(helper.getLevel(), helper.absolutePos(LAB));
        helper.assertTrue(provider != null, "the lab opens nothing");
        return (LabMenu) provider.createMenu(0, player.getInventory(), player);
    }

    private static Player member(GameTestHelper helper, UUID team) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        TestTeams.join(player, team);
        return player;
    }

    private static CodBlockEntity cod(GameTestHelper helper) {
        return helper.getBlockEntity(COD, CodBlockEntity.class);
    }

    private static TeamState state(GameTestHelper helper) {
        return TeamState.get(helper.getLevel().getServer());
    }
}
