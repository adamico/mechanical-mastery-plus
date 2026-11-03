// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.gametest;

import java.util.Optional;
import java.util.UUID;

import io.github._5thlayer.mechanicalmasteryreborn.challenge.Assault;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.AssaultState;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.Challenges;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.Gates;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.Handicap;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.RiftShards;
import io.github._5thlayer.mechanicalmasteryreborn.cod.Cod;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;

/**
 * The Assault's one gate and bridge (#237): starting the protection challenge builds a platform east of the team's
 * C.O.D. and a bridge to the first block toward it, and opens a gate there ({@link #GATES}, an armor stand, since the
 * dev runs have no Gateways); a reset and an end remove the gate, what was built and the gate's mobs, but no Rift
 * Shard; and only the playing team's island is sent one. C.O.D. notes where it stands on its first tick.
 */
final class AssaultTests {

    private static final BlockPos COD = new BlockPos(1, 2, 1);
    private static final BlockPos OTHER_COD = new BlockPos(7, 2, 7);
    /** Ticks for a placed C.O.D. to have ticked. */
    private static final int TICKED = 2;

    /** A gate that is only an armor stand, standing where the Assault opens one. */
    static final Gates GATES = (level, at, summoner) -> {
        var stand = EntityType.ARMOR_STAND.create(level, EntitySpawnReason.COMMAND);
        stand.setPos(at);
        stand.setNoGravity(true);
        level.addFreshEntity(stand);
        return Optional.of(stand.getUUID());
    };

    private AssaultTests() {
    }

    static void register(MechanicalMasteryRebornGameTests.Registrar tests) {
        tests.test("assault_start_builds_a_platform_a_bridge_and_a_gate", 20, AssaultTests::builtOnStart);
        tests.test("assault_reset_removes_it_and_leaves_the_rift_shards", 20, AssaultTests::removedOnReset);
        tests.test("assault_win_removes_it", 20, AssaultTests::removedOnWin);
        tests.test("assault_keeps_out_a_removed_gates_mob_that_loads_later", 20, AssaultTests::keepsOutLaterMobs);
        tests.test("assault_is_sent_at_its_island_only", 20, AssaultTests::itsIslandOnly);
    }

    private static void builtOnStart(GameTestHelper helper) {
        UUID team = TestTeams.claim(helper);
        helper.setBlock(COD, Cod.BLOCK.get());
        helper.runAfterDelay(TICKED, () -> {
            helper.assertTrue(Challenges.begin(server(helper), team, Handicap.PROTECTION), "the protection challenge didn't start");
            BlockPos cod = helper.absolutePos(COD);
            BlockPos middle = cod.offset(Assault.OFFSET, -1, 0);
            expectStone(helper, middle, "the platform's middle");
            expectStone(helper, middle.offset(Assault.PLATFORM_RADIUS, 0, Assault.PLATFORM_RADIUS), "the platform's corner");
            BlockPos end = firstSolidWestOf(helper, middle.offset(-Assault.PLATFORM_RADIUS - 1, 0, 0), cod.getX());
            helper.assertFalse(helper.getLevel().getBlockState(end).is(Assault.STONE.get()), "the bridge ran past the first block toward C.O.D.");
            expectStone(helper, end.east(), "the bridge's last block, by the island");
            expectStone(helper, end.east().north(), "the bridge's side");
            helper.assertTrue(AssaultState.get(server(helper)).assault(team).orElseThrow().gate().isPresent(), "no gate opened on the platform");
            Challenges.end(server(helper), team);
            helper.succeed();
        });
    }

    private static void removedOnReset(GameTestHelper helper) {
        UUID team = TestTeams.claim(helper);
        helper.setBlock(COD, Cod.BLOCK.get());
        helper.runAfterDelay(TICKED, () -> {
            Challenges.begin(server(helper), team, Handicap.PROTECTION);
            Removal removal = Removal.of(helper, team);
            helper.assertTrue(Challenges.reset(server(helper), team), "the protection challenge didn't reset");
            removal.expectRemoved(helper, "a reset");
            Challenges.end(server(helper), team);
            helper.succeed();
        });
    }

    private static void removedOnWin(GameTestHelper helper) {
        UUID team = TestTeams.claim(helper);
        helper.setBlock(COD, Cod.BLOCK.get());
        helper.runAfterDelay(TICKED, () -> {
            Challenges.begin(server(helper), team, Handicap.PROTECTION);
            Removal removal = Removal.of(helper, team);
            helper.assertTrue(Challenges.win(server(helper), team), "the protection challenge wasn't won");
            removal.expectRemoved(helper, "a win");
            helper.succeed();
        });
    }

    private static void itsIslandOnly(GameTestHelper helper) {
        UUID team = TestTeams.claim(helper);
        UUID other = TestTeams.claim(helper, OTHER_COD, OTHER_COD);
        helper.setBlock(COD, Cod.BLOCK.get());
        helper.setBlock(OTHER_COD, Cod.BLOCK.get());
        helper.runAfterDelay(TICKED, () -> {
            Challenges.begin(server(helper), team, Handicap.PROTECTION);
            AssaultState state = AssaultState.get(server(helper));
            helper.assertTrue(state.assault(team).isPresent(), "the playing team was sent no Assault");
            helper.assertTrue(state.assault(other).isEmpty(), "another team was sent an Assault");
            helper.assertFalse(helper.getLevel().getBlockState(helper.absolutePos(OTHER_COD).offset(Assault.OFFSET, -1, 0))
                    .is(Assault.STONE.get()), "a platform was built off another team's island");
            Challenges.end(server(helper), team);
            helper.succeed();
        });
    }

    /** What a gate or mob of a removed Assault does when its chunk loads later: it is kept out. */
    private static void keepsOutLaterMobs(GameTestHelper helper) {
        UUID team = TestTeams.claim(helper);
        helper.setBlock(COD, Cod.BLOCK.get());
        helper.runAfterDelay(TICKED, () -> {
            Challenges.begin(server(helper), team, Handicap.PROTECTION);
            UUID gate = AssaultState.get(server(helper)).assault(team).orElseThrow().gate().orElseThrow();
            Challenges.end(server(helper), team);
            var mob = EntityType.ZOMBIE.create(helper.getLevel(), EntitySpawnReason.COMMAND);
            mob.setPos(helper.absolutePos(COD.above()).getBottomCenter());
            Assault.markOwner(mob, gate);
            helper.assertFalse(helper.getLevel().addFreshEntity(mob), "a removed Assault's mob joined the level");
            helper.succeed();
        });
    }

    /**
     * A started Assault with one of its gate's mobs and a Rift Shard dropped beside it, to see what an end removes. They
     * stand on the test, whose chunk is loaded, not on the platform {@link Assault#OFFSET} blocks off, whose chunk may
     * not be yet: an end can't see a gate there, so the test doesn't look for it either, only that it is kept out.
     */
    private record Removal(UUID team, AssaultState.Built built, UUID gate, Entity mob, ItemEntity shard) {

        static Removal of(GameTestHelper helper, UUID team) {
            var built = AssaultState.get(server(helper)).assault(team).orElseThrow();
            UUID gate = built.gate().orElseThrow();
            BlockPos above = helper.absolutePos(COD.above().east());
            var mob = EntityType.ZOMBIE.create(helper.getLevel(), EntitySpawnReason.COMMAND);
            mob.setPos(above.getBottomCenter());
            mob.setNoAi(true);
            Assault.markOwner(mob, gate);
            helper.getLevel().addFreshEntity(mob);
            var shard = new ItemEntity(helper.getLevel(), above.getX() + 0.5, above.getY(), above.getZ() + 0.5,
                    new ItemStack(RiftShards.ITEM.get()));
            shard.setNoGravity(true);
            shard.setNeverPickUp();
            helper.getLevel().addFreshEntity(shard);
            return new Removal(team, built, gate, mob, shard);
        }

        void expectRemoved(GameTestHelper helper, String after) {
            AssaultState state = AssaultState.get(server(helper));
            helper.assertTrue(state.closed(gate), "the gate isn't kept out after " + after);
            Entity open = helper.getLevel().getEntity(gate);
            helper.assertTrue(open == null || open.isRemoved(), "the gate is still open after " + after);
            helper.assertTrue(mob.isRemoved(), "the gate's mob is still around after " + after);
            helper.assertFalse(shard.isRemoved(), "a Rift Shard was taken by " + after);
            for (BlockPos pos : built.blocks()) {
                helper.assertTrue(helper.getLevel().getBlockState(pos).isAir(), "the Assault's block at " + pos + " is still there after " + after);
            }
            helper.assertTrue(state.assault(team).isEmpty(), "the team is still sent an Assault after " + after);
        }
    }

    private static BlockPos firstSolidWestOf(GameTestHelper helper, BlockPos from, int stopX) {
        BlockPos pos = from;
        while (pos.getX() > stopX && helper.getLevel().getBlockState(pos).is(Assault.STONE.get())) {
            pos = pos.west();
        }
        return pos;
    }

    private static void expectStone(GameTestHelper helper, BlockPos pos, String what) {
        helper.assertTrue(helper.getLevel().getBlockState(pos).is(Assault.STONE.get()), what + " isn't Assault Stone");
    }

    private static MinecraftServer server(GameTestHelper helper) {
        return helper.getLevel().getServer();
    }
}
