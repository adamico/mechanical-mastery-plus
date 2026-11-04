// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.gametest;

import java.util.Optional;
import java.util.UUID;

import io.github._5thlayer.mechanicalmasteryreborn.ServerConfig;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.Challenges;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.Handicap;
import io.github._5thlayer.mechanicalmasteryreborn.cod.Cod;
import io.github._5thlayer.mechanicalmasteryreborn.cod.CodBlockEntity;
import io.github._5thlayer.mechanicalmasteryreborn.essence.Essence;
import io.github._5thlayer.mechanicalmasteryreborn.essence.Essences;
import io.github._5thlayer.mechanicalmasteryreborn.lab.Lab;
import io.github._5thlayer.mechanicalmasteryreborn.lab.LabMenu;
import io.github._5thlayer.mechanicalmasteryreborn.port.Port;
import io.github._5thlayer.mechanicalmasteryreborn.port.PortBlock;
import io.github._5thlayer.mechanicalmasteryreborn.port.PortPhase;
import io.github._5thlayer.mechanicalmasteryreborn.record.TeamState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * A challenge's Port (#224): it takes its Transmuted Essence, by hand or from a hopper, only while its team plays its
 * challenge, up to the quota and never more, and at the quota wins it: the tier C.O.D. makes is blocked for that team,
 * the challenge ends, and the lab won't start it again. Each test is an island of its own, with another team's in a corner.
 */
final class PortTests {

    private static final BlockPos PORT = new BlockPos(4, 2, 4);
    private static final BlockPos HOPPER = PORT.above();
    private static final BlockPos COD = new BlockPos(2, 2, 2);
    private static final BlockPos LAB = new BlockPos(6, 2, 2);
    private static final BlockPos GENERATOR_AT = new BlockPos(2, 2, 6);
    /** Another team's island, in a corner of the test's. */
    private static final BlockPos OTHER_ISLAND = new BlockPos(7, 2, 7);
    /** Ample time for a hopper to push a few items, 8 ticks each. */
    private static final int HOPPER_TICKS = 100;

    private PortTests() {
    }

    static void register(MechanicalMasteryRebornGameTests.Registrar tests) {
        tests.test("port_closed_takes_nothing_by_hand", 1, PortTests::closedTakesNothingByHand);
        tests.test("port_closed_takes_nothing_from_a_hopper", HOPPER_TICKS + 10, PortTests::closedTakesNothingFromAHopper);
        tests.test("port_refuses_another_transmuted_essence", HOPPER_TICKS + 10, PortTests::refusesAnotherEssence);
        tests.test("port_short_of_the_quota_blocks_nothing", 1, PortTests::shortOfTheQuota);
        tests.test("port_hopper_keeps_what_is_past_the_quota", HOPPER_TICKS + 10, PortTests::hopperKeepsTheRest);
        tests.test("port_quota_wins_the_challenge_for_its_team_only", 1, PortTests::quotaWins);
        tests.test("port_galvanic_quota_gives_the_team_s_generators_fe_again", 1, PortTests::galvanicQuotaGivesFeAgain);
        tests.test("port_energy_is_won_against_a_tier_below_advanced", 1, PortTests::wonBelowAdvanced);
        tests.test("port_after_a_win_cod_makes_the_tier_below_at_its_waking_pace", 3, PortTests::afterAWin);
        tests.test("port_reset_clears_the_count_and_a_load_keeps_it", 1, PortTests::resetClearsLoadKeeps);
        tests.test("port_lab_refuses_a_won_challenge", 1, PortTests::labRefusesAWin);
        tests.test("port_shows_closed_open_and_won", 3, PortTests::showsItsPhase);
        tests.test("port_revert_forgets_the_win_and_unblocks_its_tier", 1, PortTests::revertForgetsTheWin);
    }

    /** No challenge played, then another challenge played: a hand's essence stays in it. */
    private static void closedTakesNothingByHand(GameTestHelper helper) {
        UUID team = TestTeams.claim(helper);
        place(helper, Handicap.ENERGY);
        var player = member(helper, team);
        helper.assertValueEqual(use(helper, player, Essence.GALVANIC, 4), 4, "the essence a hand keeps with no challenge played");
        // The money challenge without its Account, which the game tests have none of.
        helper.assertTrue(state(helper).begin(team, Handicap.MONEY, 0), "the money challenge didn't start");
        helper.assertValueEqual(use(helper, player, Essence.GALVANIC, 4), 4, "the essence a hand keeps while another challenge is played");
        helper.assertValueEqual(state(helper).delivered(team), 0L, "the essence counted");
        helper.succeed();
    }

    private static void closedTakesNothingFromAHopper(GameTestHelper helper) {
        TestTeams.claim(helper);
        place(helper, Handicap.ENERGY);
        hopper(helper, Essence.GALVANIC, 3);
        helper.runAtTickTime(HOPPER_TICKS, () -> {
            expectHopperHolds(helper, 3, "a hopper above a closed Port");
            helper.succeed();
        });
    }

    /** Open, it takes neither another Transmuted Essence by hand nor from a hopper. */
    private static void refusesAnotherEssence(GameTestHelper helper) {
        UUID team = TestTeams.claim(helper);
        place(helper, Handicap.ENERGY);
        start(helper, team);
        helper.assertValueEqual(use(helper, member(helper, team), Essence.VITAL, 2), 2, "the Vital Essence a hand keeps");
        hopper(helper, Essence.AUREATE, 3);
        helper.runAtTickTime(HOPPER_TICKS, () -> {
            expectHopperHolds(helper, 3, "a hopper of Aureate Essence above the Galvanic Port");
            helper.assertValueEqual(state(helper).delivered(team), 0L, "the essence counted");
            helper.succeed();
        });
    }

    private static void shortOfTheQuota(GameTestHelper helper) {
        UUID team = TestTeams.claim(helper);
        place(helper, Handicap.ENERGY);
        start(helper, team);
        int quota = ServerConfig.quota(Essence.ADVANCED);
        helper.assertValueEqual(use(helper, member(helper, team), Essence.GALVANIC, quota - 1), 0, "the essence a hand keeps short of the quota");
        helper.assertValueEqual(state(helper).delivered(team), (long) quota - 1, "the essence counted");
        helper.assertValueEqual(state(helper).tier(team), Optional.of(Essence.ADVANCED), "the tier short of the quota");
        helper.assertTrue(state(helper).plays(team, Handicap.ENERGY), "the challenge ended short of the quota");
        helper.succeed();
    }

    private static void hopperKeepsTheRest(GameTestHelper helper) {
        UUID team = TestTeams.claim(helper);
        place(helper, Handicap.ENERGY);
        start(helper, team);
        int quota = ServerConfig.quota(Essence.ADVANCED);
        hopper(helper, Essence.GALVANIC, quota + 2);
        helper.runAtTickTime(HOPPER_TICKS, () -> {
            expectHopperHolds(helper, 2, "a hopper pushing past the quota");
            helper.assertTrue(state(helper).won(team, Handicap.ENERGY), "the quota didn't win the challenge");
            helper.succeed();
        });
    }

    /** At the quota: the tier is blocked for the team only, and the challenge, Inflation and the handicap end. */
    private static void quotaWins(GameTestHelper helper) {
        UUID team = TestTeams.claim(helper);
        UUID other = TestTeams.claim(helper, OTHER_ISLAND, OTHER_ISLAND);
        place(helper, Handicap.ENERGY);
        start(helper, team);
        helper.assertTrue(Challenges.begin(server(helper), other, Handicap.ENERGY), "the other team's challenge didn't start");
        int quota = ServerConfig.quota(Essence.ADVANCED);
        helper.assertValueEqual(use(helper, member(helper, team), Essence.GALVANIC, quota + 3), 3, "the essence a hand keeps past the quota");
        helper.assertValueEqual(state(helper).tier(team), Optional.of(Essence.IMPROVED), "the tier after the win");
        helper.assertTrue(state(helper).won(team, Handicap.ENERGY), "the win wasn't noted");
        helper.assertValueEqual(state(helper).handicap(team), Optional.<Handicap>empty(), "the handicap after the win");
        helper.assertTrue(state(helper).inflation(team).isEmpty(), "Inflation goes on after the win");
        helper.assertValueEqual(state(helper).delivered(team), 0L, "the count after the win");
        helper.assertValueEqual(state(helper).tier(other), Optional.of(Essence.ADVANCED), "the other team's tier");
        helper.assertTrue(state(helper).plays(other, Handicap.ENERGY), "the other team's challenge ended");
        helper.succeed();
    }

    /**
     * The energy handicap's cost goes with the challenge, for the team that won it only: its generator, stopped while
     * it plays, gives FE again, and another team's, still playing, stays stopped.
     */
    private static void galvanicQuotaGivesFeAgain(GameTestHelper helper) {
        UUID team = TestTeams.claim(helper);
        UUID other = TestTeams.claim(helper, OTHER_ISLAND, OTHER_ISLAND);
        place(helper, Handicap.ENERGY);
        helper.setBlock(GENERATOR_AT, GeneratorTests.GENERATOR);
        helper.setBlock(OTHER_ISLAND, GeneratorTests.GENERATOR);
        start(helper, team);
        helper.assertTrue(Challenges.begin(server(helper), other, Handicap.ENERGY), "the other team's challenge didn't start");
        helper.assertValueEqual(GeneratorTests.draw(helper, GENERATOR_AT), 0, "FE drawn while the team plays energy");
        use(helper, member(helper, team), Essence.GALVANIC, ServerConfig.quota(Essence.ADVANCED));
        helper.assertTrue(state(helper).won(team, Handicap.ENERGY), "the quota didn't win the challenge");
        helper.assertValueEqual(GeneratorTests.draw(helper, GENERATOR_AT), GeneratorTests.DRAW, "FE drawn after the win");
        helper.assertValueEqual(GeneratorTests.draw(helper, OTHER_ISLAND), 0, "FE drawn from the other team's generator");
        helper.succeed();
    }

    /**
     * The energy challenge isn't tied to Advanced: against Improved, started at the lab, it takes Improved's quota,
     * and a win drops C.O.D. to Regular and ends the handicap.
     */
    private static void wonBelowAdvanced(GameTestHelper helper) {
        UUID team = TestTeams.claim(helper);
        state(helper).setTier(team, Optional.of(Essence.IMPROVED));
        place(helper, Handicap.ENERGY);
        helper.setBlock(LAB, Lab.BLOCK.get());
        var player = member(helper, team);
        var menu = openLab(helper, player);
        helper.assertTrue(menu.clickMenuButton(player, Handicap.ENERGY.ordinal()), "the lab refused energy against Improved");
        int quota = ServerConfig.quota(Essence.IMPROVED);
        helper.assertTrue(quota > ServerConfig.quota(Essence.ADVANCED), "Improved's quota isn't above Advanced's");
        use(helper, player, Essence.GALVANIC, quota - 1);
        helper.assertTrue(state(helper).plays(team, Handicap.ENERGY), "the challenge ended short of Improved's quota");
        helper.assertValueEqual(state(helper).tier(team), Optional.of(Essence.IMPROVED), "the tier short of the quota");
        helper.assertValueEqual(use(helper, player, Essence.GALVANIC, 2), 1, "the essence a hand keeps past the quota");
        helper.assertTrue(state(helper).won(team, Handicap.ENERGY), "the quota didn't win the challenge");
        helper.assertValueEqual(state(helper).tier(team), Optional.of(Essence.REGULAR), "the tier after the win");
        helper.assertValueEqual(state(helper).handicap(team), Optional.<Handicap>empty(), "the handicap after the win");
        helper.assertTrue(state(helper).inflation(team).isEmpty(), "Inflation goes on after the win");
        helper.succeed();
    }

    /** Won, an awake C.O.D. makes Improved at its waking pace, past a reset and a load. */
    private static void afterAWin(GameTestHelper helper) {
        UUID team = TestTeams.claim(helper);
        place(helper, Handicap.ENERGY);
        start(helper, team);
        use(helper, member(helper, team), Essence.GALVANIC, ServerConfig.quota(Essence.ADVANCED));
        state(helper).wake(team);
        helper.setBlock(COD, Cod.BLOCK.get());
        var cod = helper.getBlockEntity(COD, CodBlockEntity.class);
        helper.assertFalse(Challenges.reset(server(helper), team), "a reset found a challenge after the win");
        var ops = helper.getLevel().registryAccess().createSerializationContext(NbtOps.INSTANCE);
        var codec = TeamState.TYPE.codec();
        TeamState loaded = codec.parse(ops, codec.encodeStart(ops, state(helper)).getOrThrow()).getOrThrow();
        helper.assertValueEqual(loaded.tier(team), Optional.of(Essence.IMPROVED), "the tier after a load");
        helper.assertTrue(loaded.inflation(team).isEmpty(), "Inflation after a load");
        helper.runAtTickTime(2, () -> {
            helper.assertValueEqual(cod.tier(), Optional.of(Essence.IMPROVED), "C.O.D.'s tier after the win");
            helper.assertValueEqual(cod.interval(), ServerConfig.codWakingInterval(), "C.O.D.'s interval after the win");
            helper.succeed();
        });
    }

    private static void resetClearsLoadKeeps(GameTestHelper helper) {
        UUID team = TestTeams.claim(helper);
        place(helper, Handicap.ENERGY);
        start(helper, team);
        var player = member(helper, team);
        use(helper, player, Essence.GALVANIC, 2);
        var ops = helper.getLevel().registryAccess().createSerializationContext(NbtOps.INSTANCE);
        var codec = TeamState.TYPE.codec();
        TeamState loaded = codec.parse(ops, codec.encodeStart(ops, state(helper)).getOrThrow()).getOrThrow();
        helper.assertValueEqual(loaded.delivered(team), 2L, "the count after a load");
        helper.assertTrue(Challenges.reset(server(helper), team), "the challenge didn't reset");
        helper.assertValueEqual(state(helper).delivered(team), 0L, "the count after a reset");
        helper.succeed();
    }

    /** Both the lab's button and {@link Challenges#begin} refuse a challenge the team has won. */
    private static void labRefusesAWin(GameTestHelper helper) {
        UUID team = TestTeams.claim(helper);
        place(helper, Handicap.ENERGY);
        start(helper, team);
        var player = member(helper, team);
        use(helper, player, Essence.GALVANIC, ServerConfig.quota(Essence.ADVANCED));
        helper.setBlock(LAB, Lab.BLOCK.get());
        var menu = openLab(helper, player);
        helper.assertTrue(menu.won(Handicap.ENERGY), "the lab doesn't show the challenge won");
        helper.assertFalse(menu.canChoose(Handicap.ENERGY), "the lab offers a won challenge");
        helper.assertFalse(menu.clickMenuButton(player, Handicap.ENERGY.ordinal()), "the lab started a won challenge");
        helper.assertFalse(Challenges.begin(server(helper), team, Handicap.ENERGY), "a won challenge started");
        helper.assertValueEqual(state(helper).handicap(team), Optional.<Handicap>empty(), "the handicap after starting a won challenge");
        helper.succeed();
    }

    private static void showsItsPhase(GameTestHelper helper) {
        UUID team = TestTeams.claim(helper);
        place(helper, Handicap.ENERGY);
        helper.runAtTickTime(1, () -> {
            expectPhase(helper, PortPhase.CLOSED);
            start(helper, team);
        });
        helper.runAtTickTime(2, () -> {
            expectPhase(helper, PortPhase.OPEN);
            use(helper, member(helper, team), Essence.GALVANIC, ServerConfig.quota(Essence.ADVANCED));
            expectPhase(helper, PortPhase.WON);
            helper.succeed();
        });
    }

    /** {@code /cod challenge revert} on the island forgets the win, unblocks its tier, and lets the challenge start again. */
    private static void revertForgetsTheWin(GameTestHelper helper) {
        UUID team = TestTeams.claim(helper);
        place(helper, Handicap.ENERGY);
        start(helper, team);
        use(helper, member(helper, team), Essence.GALVANIC, ServerConfig.quota(Essence.ADVANCED));
        var op = server(helper).createCommandSourceStack().withLevel(helper.getLevel())
                .withPosition(helper.absolutePos(PORT).getCenter()).withSuppressedOutput();
        server(helper).getCommands().performPrefixedCommand(op, "cod challenge revert energy");
        helper.assertFalse(state(helper).won(team, Handicap.ENERGY), "the win outlasted a revert");
        helper.assertValueEqual(state(helper).tier(team), Optional.of(Essence.ADVANCED), "the tier after a revert");
        helper.assertFalse(state(helper).revertWin(team, Handicap.ENERGY), "a challenge not won was reverted");
        start(helper, team);
        helper.succeed();
    }

    private static void place(GameTestHelper helper, Handicap handicap) {
        helper.setBlock(PORT, Port.BLOCKS_BY_HANDICAP.get(handicap).get());
    }

    private static void start(GameTestHelper helper, UUID team) {
        helper.assertTrue(Challenges.begin(server(helper), team, Handicap.ENERGY), "the energy challenge didn't start");
    }

    private static Player member(GameTestHelper helper, UUID team) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        TestTeams.join(player, team);
        return player;
    }

    /** Right-clicks the Port with {@code count} of {@code essence}, and says how many are left in hand. */
    private static int use(GameTestHelper helper, Player player, Essence essence, int count) {
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Essences.item(essence), count));
        BlockPos pos = helper.absolutePos(PORT);
        helper.getBlockState(PORT).useItemOn(player.getMainHandItem(), helper.getLevel(), player, InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false));
        return player.getMainHandItem().getCount();
    }

    private static LabMenu openLab(GameTestHelper helper, Player player) {
        return (LabMenu) helper.getBlockState(LAB).getMenuProvider(helper.getLevel(), helper.absolutePos(LAB))
                .createMenu(0, player.getInventory(), player);
    }

    private static void hopper(GameTestHelper helper, Essence essence, int count) {
        helper.setBlock(HOPPER, Blocks.HOPPER.defaultBlockState().setValue(HopperBlock.FACING, Direction.DOWN));
        helper.getBlockEntity(HOPPER, HopperBlockEntity.class).setItem(0, new ItemStack(Essences.item(essence), count));
    }

    private static void expectHopperHolds(GameTestHelper helper, int count, String what) {
        helper.assertValueEqual(helper.getBlockEntity(HOPPER, HopperBlockEntity.class).getItem(0).getCount(), count, what);
    }

    private static void expectPhase(GameTestHelper helper, PortPhase phase) {
        helper.assertValueEqual(helper.getBlockState(PORT).getValue(PortBlock.PHASE), phase, "the Port's look");
    }

    private static TeamState state(GameTestHelper helper) {
        return TeamState.get(server(helper));
    }

    private static MinecraftServer server(GameTestHelper helper) {
        return helper.getLevel().getServer();
    }
}
