// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.gametest;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import io.github._5thlayer.mechanicalmasteryreborn.challenge.Assault;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.AssaultAbsence;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.AssaultState;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.Challenges;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.Gates;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.Handicap;
import io.github._5thlayer.mechanicalmasteryreborn.cod.Cod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * The Assault waits for someone at home (#240): with no player of the team on its island, off it or offline, its gate
 * closes, each gate at the wave it had reached, its mobs staying; with one back, it opens again at that wave. The gates
 * ({@link #GATES}) are {@link AssaultTests#GATES}' armor stands; a gate summoned by one of these tests'
 * players stands by that player, on the test, whose chunk is loaded, not on the platform {@link Assault#OFFSET} off.
 */
final class AssaultAbsenceTests {

    private static final BlockPos COD = new BlockPos(1, 2, 1);
    private static final BlockPos HOME = new BlockPos(3, 2, 3);
    private static final int TICKED = 2;
    private static final int WAVE = 3;

    /** These tests' players, whose gates stand by them. */
    private static final Set<UUID> SUMMONERS = ConcurrentHashMap.newKeySet();

    /** {@link AssaultTests#GATES}, but a gate summoned by one of these tests' players stands by that player, noted where it was asked to. */
    static final Gates GATES = new Gates() {
        @Override
        public Optional<UUID> open(ServerLevel level, Vec3 at, @Nullable ServerPlayer summoner, int wave) {
            Vec3 where = summoner != null && SUMMONERS.contains(summoner.getUUID()) ? summoner.position() : at;
            Optional<UUID> gate = AssaultTests.GATES.open(level, where, summoner, wave);
            gate.ifPresent(id -> AssaultTests.AT.put(id, at));
            return gate;
        }

        @Override
        public int wave(Entity gate) {
            return AssaultTests.WAVES.getOrDefault(gate.getUUID(), 0);
        }

        @Override
        public boolean call(ServerLevel level, UUID gate) {
            WaveCallerTests.CALLS.merge(gate, 1, Integer::sum);
            return true;
        }

        @Override
        public int called(ServerLevel level, UUID gate) {
            return WaveCallerTests.CALLS.getOrDefault(gate, 0);
        }

        @Override
        public boolean callHome(ServerLevel level, UUID team) {
            return WaveCallerTests.HOME_GATES.contains(team) && WaveCallerTests.HOME_CALLS.add(team);
        }
    };

    private AssaultAbsenceTests() {
    }

    static void register(MechanicalMasteryRebornGameTests.Registrar tests) {
        tests.test("assault_closes_off_the_island_and_reopens_at_its_wave", 20, AssaultAbsenceTests::closesAway);
        tests.test("assault_closes_with_the_team_offline", 20, AssaultAbsenceTests::closesOffline);
    }

    private static void closesAway(GameTestHelper helper) {
        UUID team = TestTeams.claim(helper);
        ServerPlayer player = player(helper, team);
        helper.setBlock(COD, Cod.BLOCK.get());
        helper.runAfterDelay(TICKED, () -> {
            MinecraftServer server = server(helper);
            Challenges.begin(server, team, Handicap.PROTECTION);
            AssaultState state = AssaultState.get(server);
            AssaultAbsence.update(server);
            helper.assertTrue(state.paused(team).isEmpty(), "the Assault paused with a player at home");
            List<UUID> gates = state.assault(team).orElseThrow().gates();
            helper.assertTrue(gates.size() == 1, "the Assault opened " + gates.size() + " gates, not 1");
            AssaultTests.WAVES.put(gates.getFirst(), WAVE);
            Direction side = state.assault(team).orElseThrow().side().orElseThrow();
            UUID gate = gates.get(0);
            var mob = EntityType.ZOMBIE.create(helper.getLevel(), EntitySpawnReason.COMMAND);
            mob.setPos(helper.absolutePos(COD.above().east()).getBottomCenter());
            mob.setNoAi(true);
            Assault.markOwner(mob, gate);
            helper.getLevel().addFreshEntity(mob);

            player.setPos(player.position().add(1000, 0, 0));
            AssaultAbsence.update(server);
            helper.assertTrue(waves(state.paused(team).orElse(List.of())).equals(List.of(WAVE)),
                    "the gate didn't close at their waves with no one home");
            for (UUID each : gates) {
                Entity closed = helper.getLevel().getEntity(each);
                helper.assertTrue(closed == null || closed.isRemoved(), "a gate is still open with no one home");
            }
            helper.assertFalse(mob.isRemoved(), "a mob already out went with its gate");
            Assault.reopen(helper.getLevel(), gate, player.position(), WAVE);
            helper.assertTrue(state.assault(team).orElseThrow().gates().isEmpty(), "a gate closed for no one home reopened as a failed one");

            player.setPos(helper.absolutePos(HOME).getBottomCenter());
            AssaultAbsence.update(server);
            helper.assertTrue(state.paused(team).isEmpty(), "the Assault is still paused with a player back home");
            List<UUID> reopened = state.assault(team).orElseThrow().gates();
            helper.assertTrue(reopened.stream().noneMatch(gates::contains), "a closed gate came back instead of opening again");
            List<Integer> waves = reopened.stream().map(AssaultTests.WAVES::get).sorted().toList();
            helper.assertTrue(waves.equals(List.of(WAVE)), "the gate opened again at waves " + waves);
            helper.assertTrue(AssaultTests.AT.get(reopened.getFirst()).equals(Assault.gateAt(helper.absolutePos(COD), side)),
                    "the gate didn't open again on its side");
            Challenges.end(server, team);
            helper.assertTrue(mob.isRemoved(), "the closed gate's mob outlived the Assault's end");
            leave(player);
            helper.succeed();
        });
    }

    private static void closesOffline(GameTestHelper helper) {
        UUID team = TestTeams.claim(helper);
        ServerPlayer player = player(helper, team);
        helper.setBlock(COD, Cod.BLOCK.get());
        helper.runAfterDelay(TICKED, () -> {
            MinecraftServer server = server(helper);
            Challenges.begin(server, team, Handicap.PROTECTION);
            AssaultState state = AssaultState.get(server);
            AssaultAbsence.update(server);
            helper.assertTrue(state.paused(team).isEmpty(), "the Assault paused with a player at home");
            leave(player);
            AssaultAbsence.update(server);
            helper.assertTrue(state.paused(team).isPresent(), "the gate didn't close with the team offline");
            Challenges.end(server, team);
            helper.succeed();
        });
    }

    /** A player of {@code team}, online and at home on its island. */
    static ServerPlayer player(GameTestHelper helper, UUID team) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.setGameMode(GameType.SPECTATOR);
        SUMMONERS.add(player.getUUID());
        TestTeams.join(player, team);
        player.setPos(helper.absolutePos(HOME).getBottomCenter());
        return player;
    }

    private static List<Integer> waves(List<AssaultState.Waiting> waiting) {
        return waiting.stream().map(AssaultState.Waiting::wave).sorted().toList();
    }

    static void leave(ServerPlayer player) {
        player.level().getServer().getPlayerList().remove(player);
    }

    private static MinecraftServer server(GameTestHelper helper) {
        return helper.getLevel().getServer();
    }
}
