// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.gametest;

import java.util.Map;
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
 * closes at the wave it had reached, its mobs staying; with one back, it opens again at that wave. The gates
 * ({@link #GATES}) are {@link AssaultTests#GATES}' armor stands that keep a wave; a gate summoned by one of these tests'
 * players stands by that player, on the test, whose chunk is loaded, not on the platform {@link Assault#OFFSET} off.
 */
final class AssaultAbsenceTests {

    private static final BlockPos COD = new BlockPos(1, 2, 1);
    private static final BlockPos HOME = new BlockPos(3, 2, 3);
    private static final int TICKED = 2;
    private static final int WAVE = 3;

    /** The wave each test gate has reached. */
    private static final Map<UUID, Integer> WAVES = new ConcurrentHashMap<>();
    /** These tests' players, whose gates stand by them. */
    private static final Set<UUID> SUMMONERS = ConcurrentHashMap.newKeySet();

    static final Gates GATES = new Gates() {
        @Override
        public Optional<UUID> open(ServerLevel level, Vec3 at, @Nullable ServerPlayer summoner) {
            return open(level, at, summoner, 0);
        }

        @Override
        public Optional<UUID> open(ServerLevel level, Vec3 at, @Nullable ServerPlayer summoner, int wave) {
            Vec3 where = summoner != null && SUMMONERS.contains(summoner.getUUID()) ? summoner.position() : at;
            Optional<UUID> gate = AssaultTests.GATES.open(level, where, summoner);
            gate.ifPresent(id -> WAVES.put(id, wave));
            return gate;
        }

        @Override
        public int wave(Entity gate) {
            return WAVES.getOrDefault(gate.getUUID(), 0);
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
            UUID gate = state.assault(team).orElseThrow().gate().orElseThrow();
            WAVES.put(gate, WAVE);
            var mob = EntityType.ZOMBIE.create(helper.getLevel(), EntitySpawnReason.COMMAND);
            mob.setPos(helper.absolutePos(COD.above().east()).getBottomCenter());
            mob.setNoAi(true);
            Assault.markOwner(mob, gate);
            helper.getLevel().addFreshEntity(mob);

            player.setPos(player.position().add(1000, 0, 0));
            AssaultAbsence.update(server);
            helper.assertTrue(state.paused(team).equals(Optional.of(WAVE)), "the gate didn't close at its wave with no one home");
            Entity closed = helper.getLevel().getEntity(gate);
            helper.assertTrue(closed == null || closed.isRemoved(), "the gate is still open with no one home");
            helper.assertFalse(mob.isRemoved(), "a mob already out went with its gate");

            player.setPos(helper.absolutePos(HOME).getBottomCenter());
            AssaultAbsence.update(server);
            helper.assertTrue(state.paused(team).isEmpty(), "the Assault is still paused with a player back home");
            UUID reopened = state.assault(team).orElseThrow().gate().orElseThrow();
            helper.assertFalse(reopened.equals(gate), "no gate opened again");
            helper.assertTrue(WAVES.get(reopened) == WAVE, "the gate opened again at wave " + WAVES.get(reopened) + ", not " + WAVE);
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
    private static ServerPlayer player(GameTestHelper helper, UUID team) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.setGameMode(GameType.SPECTATOR);
        SUMMONERS.add(player.getUUID());
        TestTeams.join(player, team);
        player.setPos(helper.absolutePos(HOME).getBottomCenter());
        return player;
    }

    private static void leave(ServerPlayer player) {
        player.level().getServer().getPlayerList().remove(player);
    }

    private static MinecraftServer server(GameTestHelper helper) {
        return helper.getLevel().getServer();
    }
}
