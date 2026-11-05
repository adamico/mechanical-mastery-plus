// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.gametest;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import io.github._5thlayer.mechanicalmasteryreborn.challenge.AssaultExplosions;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.Challenges;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.Explosions;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.Handicap;
import io.github._5thlayer.mechanicalmasteryreborn.cod.Cod;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.MinecraftServer;

/**
 * The Assault lets explosions through its team's claims (#241): its {@link Explosions} ({@link #EXPLOSIONS}, in
 * memory, since the dev runs have no FTB Chunks) are on while it runs, put back to the team's own value by a win or a
 * stop, kept on through a reset, which sends a new Assault (#294), and turned back on when a player turns them off
 * meanwhile.
 */
final class AssaultExplosionsTests {

    private static final BlockPos COD = new BlockPos(1, 2, 1);
    private static final int TICKED = 2;

    /** Each team's setting, off until a test sets it, as a team's default is. */
    private static final Map<UUID, Boolean> ALLOWED = new ConcurrentHashMap<>();

    static final Explosions EXPLOSIONS = new Explosions() {
        @Override
        public Optional<Boolean> allowed(MinecraftServer server, UUID team) {
            return Optional.of(ALLOWED.getOrDefault(team, false));
        }

        @Override
        public void set(MinecraftServer server, UUID team, boolean allowed) {
            ALLOWED.put(team, allowed);
        }
    };

    private AssaultExplosionsTests() {
    }

    static void register(MechanicalMasteryRebornGameTests.Registrar tests) {
        tests.test("assault_lets_explosions_through_and_a_stop_restores_them", 20, helper -> restored(helper, false, false));
        tests.test("assault_reset_keeps_explosions_through", 20, AssaultExplosionsTests::keptThroughReset);
        tests.test("assault_win_restores_a_team_that_allowed_explosions", 20, helper -> restored(helper, true, true));
        tests.test("assault_turns_explosions_back_on", 20, AssaultExplosionsTests::turnedBackOn);
    }

    private static void restored(GameTestHelper helper, boolean own, boolean win) {
        UUID team = TestTeams.claim(helper);
        ALLOWED.put(team, own);
        helper.setBlock(COD, Cod.BLOCK.get());
        helper.runAfterDelay(TICKED, () -> {
            MinecraftServer server = server(helper);
            Challenges.begin(server, team, Handicap.PROTECTION);
            helper.assertTrue(ALLOWED.get(team), "explosions aren't on while the Assault runs");
            helper.assertTrue(AssaultExplosions.get(server).lifted(team), "the team's own value isn't kept");
            helper.assertTrue(win ? Challenges.win(server, team) : Challenges.end(server, team), "the Assault didn't end");
            helper.assertValueEqual(ALLOWED.get(team), own, "the team's explosions after the Assault");
            helper.assertFalse(AssaultExplosions.get(server).lifted(team), "the team's own value is still kept");
            Challenges.end(server, team);
            helper.succeed();
        });
    }

    /** A reset sends a new Assault (#294), so explosions stay on through it, and the stop after it restores the team's own value. */
    private static void keptThroughReset(GameTestHelper helper) {
        UUID team = TestTeams.claim(helper);
        helper.setBlock(COD, Cod.BLOCK.get());
        helper.runAfterDelay(TICKED, () -> {
            MinecraftServer server = server(helper);
            Challenges.begin(server, team, Handicap.PROTECTION);
            helper.assertTrue(Challenges.reset(server, team), "the protection challenge didn't reset");
            helper.assertTrue(ALLOWED.get(team), "explosions aren't on after a reset");
            helper.assertTrue(AssaultExplosions.get(server).lifted(team), "the team's own value isn't kept after a reset");
            helper.assertTrue(Challenges.end(server, team), "the protection challenge didn't stop");
            helper.assertFalse(ALLOWED.get(team), "the team's own value wasn't restored after a reset and a stop");
            helper.succeed();
        });
    }

    private static void turnedBackOn(GameTestHelper helper) {
        UUID team = TestTeams.claim(helper);
        helper.setBlock(COD, Cod.BLOCK.get());
        helper.runAfterDelay(TICKED, () -> {
            MinecraftServer server = server(helper);
            Challenges.begin(server, team, Handicap.PROTECTION);
            ALLOWED.put(team, false);
            AssaultExplosions.changed(server, team);
            helper.runAfterDelay(2, () -> {
                helper.assertTrue(ALLOWED.get(team), "explosions turned off during the Assault weren't turned back on");
                Challenges.end(server, team);
                helper.assertFalse(ALLOWED.get(team), "the team's own value wasn't restored");
                helper.succeed();
            });
        });
    }

    private static MinecraftServer server(GameTestHelper helper) {
        return helper.getLevel().getServer();
    }
}
