// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.gametest;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import io.github._5thlayer.mechanicalmasteryreborn.challenge.Assault;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.AssaultAbsence;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.AssaultState;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.Challenges;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.Handicap;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.WaveCaller;
import io.github._5thlayer.mechanicalmasteryreborn.cod.Cod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

/**
 * The Wave Caller (#341): a call brings on the Assault's next wave at a platform drawn from all four, the one in use
 * included, calling the gate standing there or opening one with the waves reached behind it; it calls nothing while the
 * Assault is paused; a step moves a gate to a side no other stands on; a gate behind never takes the bar back; and with
 * no Assault it calls the team's open home gates. The gates are {@link AssaultAbsenceTests#GATES}', whose calls are noted.
 */
final class WaveCallerTests {

    private static final BlockPos COD = new BlockPos(1, 2, 1);
    private static final int TICKED = 2;
    /** Calls enough that each of four sides is drawn, but for a chance far below a flaky test's. */
    private static final int CALLS_MADE = 60;

    /** How many waves each gate had called. */
    static final Map<UUID, Integer> CALLS = new ConcurrentHashMap<>();
    /** The teams standing in for ones with a home gate open. */
    static final Set<UUID> HOME_GATES = ConcurrentHashMap.newKeySet();
    /** The teams whose home gates had a wave called. */
    static final Set<UUID> HOME_CALLS = ConcurrentHashMap.newKeySet();

    private WaveCallerTests() {
    }

    static void register(MechanicalMasteryRebornGameTests.Registrar tests) {
        tests.test("wave_caller_calls_at_a_platform_drawn_from_all_four", 20, WaveCallerTests::drawsFromAllFour);
        tests.test("wave_caller_calls_nothing_while_the_assault_is_paused", 20, WaveCallerTests::nothingWhilePaused);
        tests.test("wave_caller_step_moves_a_gate_to_a_free_side", 20, WaveCallerTests::stepToFreeSide);
        tests.test("wave_caller_gates_reopen_on_their_own_sides_after_a_pause", 20, WaveCallerTests::reopenOwnSides);
        tests.test("wave_caller_gate_behind_keeps_the_bar", 20, WaveCallerTests::barKept);
        tests.test("wave_caller_calls_home_gates_with_no_assault", 1, WaveCallerTests::callsHomeGates);
    }

    /**
     * Each call lands on one of the four sides: the one in use has its gate's wave called, another has a gate opened
     * there with the waves reached behind it, its first wave called; no side ever has two gates.
     */
    private static void drawsFromAllFour(GameTestHelper helper) {
        UUID team = TestTeams.claim(helper);
        helper.setBlock(COD, Cod.BLOCK.get());
        helper.runAfterDelay(TICKED, () -> {
            Challenges.begin(server(helper), team, Handicap.PROTECTION);
            AssaultState state = AssaultState.get(server(helper));
            UUID first = state.assault(team).orElseThrow().gates().getFirst();
            Assault.noteWave(helper.getLevel(), first, 4);
            for (int i = 0; i < CALLS_MADE; i++) {
                helper.assertTrue(WaveCaller.call(helper.getLevel(), team), "a call to a running Assault called nothing");
            }
            List<UUID> gates = state.assault(team).orElseThrow().gates();
            helper.assertValueEqual(gates.size(), 4, "the gates open after calls on every side");
            Set<Direction> sides = new HashSet<>();
            for (UUID gate : gates) {
                Direction side = state.side(gate).orElseThrow();
                helper.assertTrue(sides.add(side), "two gates stand on the " + side + " side");
                helper.assertTrue(CALLS.getOrDefault(gate, 0) > 0, "a gate on the " + side + " side had no wave called");
                helper.assertTrue(AssaultTests.AT.get(gate).equals(Assault.gateAt(helper.absolutePos(COD), side)),
                        "a gate isn't on its side's platform");
                if (!gate.equals(first)) {
                    helper.assertValueEqual(AssaultTests.WAVES.get(gate), 4, "the waves behind a called gate");
                }
            }
            Challenges.end(server(helper), team);
            helper.assertTrue(gates.stream().allMatch(state::closed), "a called gate outlived the Assault");
            helper.succeed();
        });
    }

    private static void nothingWhilePaused(GameTestHelper helper) {
        UUID team = TestTeams.claim(helper);
        ServerPlayer player = AssaultAbsenceTests.player(helper, team);
        helper.setBlock(COD, Cod.BLOCK.get());
        helper.runAfterDelay(TICKED, () -> {
            MinecraftServer server = server(helper);
            Challenges.begin(server, team, Handicap.PROTECTION);
            AssaultAbsenceTests.leave(player);
            AssaultAbsence.update(server);
            helper.assertTrue(AssaultState.get(server).paused(team).isPresent(), "the Assault didn't pause with no one home");
            helper.assertFalse(WaveCaller.call(helper.getLevel(), team), "a call to a paused Assault called a wave");
            helper.assertTrue(AssaultState.get(server).assault(team).orElseThrow().gates().isEmpty(), "a call opened a gate while paused");
            Challenges.end(server, team);
            helper.succeed();
        });
    }

    /** With two gates open, a step moves one to a side neither stands on, and leaves the other where it is. */
    private static void stepToFreeSide(GameTestHelper helper) {
        UUID team = TestTeams.claim(helper);
        helper.setBlock(COD, Cod.BLOCK.get());
        helper.runAfterDelay(TICKED, () -> {
            Challenges.begin(server(helper), team, Handicap.PROTECTION);
            AssaultState state = AssaultState.get(server(helper));
            UUID first = state.assault(team).orElseThrow().gates().getFirst();
            for (int i = 0; i < CALLS_MADE && state.assault(team).orElseThrow().gates().size() < 2; i++) {
                WaveCaller.call(helper.getLevel(), team);
            }
            List<UUID> two = state.assault(team).orElseThrow().gates();
            helper.assertValueEqual(two.size(), 2, "the gates open after a call on another side");
            UUID other = two.get(1);
            Direction firstSide = state.side(first).orElseThrow();
            Direction otherSide = state.side(other).orElseThrow();
            Assault.step(helper.getLevel(), first, Assault.STEP);
            List<UUID> gates = state.assault(team).orElseThrow().gates();
            helper.assertValueEqual(gates.size(), 2, "the gates open after a step");
            helper.assertTrue(gates.contains(other), "the other gate moved with the step");
            UUID moved = gates.stream().filter(gate -> !gate.equals(other)).findFirst().orElseThrow();
            Direction now = state.side(moved).orElseThrow();
            helper.assertTrue(now != firstSide && now != otherSide, "the gate moved to a side in use: " + now);
            helper.assertValueEqual(state.side(other), Optional.of(otherSide), "the other gate's side");
            helper.assertValueEqual(CALLS.getOrDefault(moved, 0), CALLS.getOrDefault(first, 0), "the waves called on the moved gate");
            Challenges.end(server(helper), team);
            helper.succeed();
        });
    }

    /** With two gates open, a pause closes both, and each opens again on its own side as a member comes home. */
    private static void reopenOwnSides(GameTestHelper helper) {
        UUID team = TestTeams.claim(helper);
        ServerPlayer player = AssaultAbsenceTests.player(helper, team);
        helper.setBlock(COD, Cod.BLOCK.get());
        helper.runAfterDelay(TICKED, () -> {
            MinecraftServer server = server(helper);
            Challenges.begin(server, team, Handicap.PROTECTION);
            AssaultState state = AssaultState.get(server);
            for (int i = 0; i < CALLS_MADE && state.assault(team).orElseThrow().gates().size() < 2; i++) {
                WaveCaller.call(helper.getLevel(), team);
            }
            Set<Direction> before = new HashSet<>();
            state.assault(team).orElseThrow().gates().forEach(gate -> before.add(state.side(gate).orElseThrow()));
            helper.assertValueEqual(before.size(), 2, "the sides two gates stand on");
            Vec3 home = player.position();
            player.setPos(home.add(1000, 0, 0));
            AssaultAbsence.update(server);
            helper.assertTrue(state.assault(team).orElseThrow().gates().isEmpty(), "a gate is still open with no one home");
            player.setPos(home);
            AssaultAbsence.update(server);
            Set<Direction> after = new HashSet<>();
            for (UUID gate : state.assault(team).orElseThrow().gates()) {
                Direction side = state.side(gate).orElseThrow();
                after.add(side);
                helper.assertTrue(AssaultTests.AT.get(gate).equals(Assault.gateAt(helper.absolutePos(COD), side)),
                        "a gate didn't open again on its side's platform");
            }
            helper.assertValueEqual(after, before, "the sides the gates opened again on");
            Challenges.end(server, team);
            AssaultAbsenceTests.leave(player);
            helper.succeed();
        });
    }

    /** A called gate behind another starting a wave the bar has passed leaves the bar where it is. */
    private static void barKept(GameTestHelper helper) {
        UUID team = TestTeams.claim(helper);
        helper.setBlock(COD, Cod.BLOCK.get());
        helper.runAfterDelay(TICKED, () -> {
            Challenges.begin(server(helper), team, Handicap.PROTECTION);
            AssaultState state = AssaultState.get(server(helper));
            UUID first = state.assault(team).orElseThrow().gates().getFirst();
            Assault.noteWave(helper.getLevel(), first, 9);
            Assault.noteWave(helper.getLevel(), first, 6);
            helper.assertValueEqual(state.assault(team).orElseThrow().wave(), 9, "the bar's wave after a gate behind started one");
            Challenges.end(server(helper), team);
            helper.succeed();
        });
    }

    /** With no Assault, a call brings on the team's open home gates' next wave, and with none open, calls nothing. */
    private static void callsHomeGates(GameTestHelper helper) {
        UUID team = TestTeams.claim(helper);
        helper.assertFalse(WaveCaller.call(helper.getLevel(), team), "a call with no gate open called a wave");
        HOME_GATES.add(team);
        helper.assertTrue(WaveCaller.call(helper.getLevel(), team), "a call didn't reach the open home gate");
        helper.assertTrue(HOME_CALLS.contains(team), "the home gate had no wave called");
        HOME_GATES.remove(team);
        helper.succeed();
    }

    private static MinecraftServer server(GameTestHelper helper) {
        return helper.getLevel().getServer();
    }
}
