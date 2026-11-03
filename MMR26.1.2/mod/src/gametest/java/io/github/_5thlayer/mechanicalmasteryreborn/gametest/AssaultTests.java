// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.gametest;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import io.github._5thlayer.mechanicalmasteryreborn.challenge.Assault;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.AssaultState;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.Challenges;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.Gates;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.Handicap;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.InflationBar;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.RiftShards;
import io.github._5thlayer.mechanicalmasteryreborn.cod.Cod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

/**
 * The Assault's gates and bridges (#237, #239): starting the protection challenge builds a platform on each side of the
 * team's C.O.D. and a bridge to the first block toward it, and opens a gate on each ({@link #GATES}, an armor stand, since the
 * dev runs have no Gateways); a reset and an end remove the gate, what was built and the gate's mobs, but no Rift
 * Shard; a failed gate reopens at its wave; and only the playing team's island is sent one. C.O.D. notes where it stands on its first tick.
 */
final class AssaultTests {

    private static final BlockPos COD = new BlockPos(1, 2, 1);
    private static final BlockPos OTHER_COD = new BlockPos(7, 2, 7);
    /** Ticks for a placed C.O.D. to have ticked. */
    private static final int TICKED = 2;

    /** The wave each of {@link #GATES}' gates opened at. */
    static final Map<UUID, Integer> WAVES = new ConcurrentHashMap<>();
    /** Where each of {@link #GATES}' gates opened, so no test looks for a gate whose chunk may not be loaded. */
    static final Map<UUID, Vec3> AT = new ConcurrentHashMap<>();

    /** A gate that is only an armor stand, standing where the Assault opens one. */
    static final Gates GATES = (level, at, summoner, wave) -> {
        var stand = EntityType.ARMOR_STAND.create(level, EntitySpawnReason.COMMAND);
        stand.setPos(at);
        stand.setNoGravity(true);
        level.addFreshEntity(stand);
        WAVES.put(stand.getUUID(), wave);
        AT.put(stand.getUUID(), at);
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
        tests.test("assault_builds_all_four_sides", 20, AssaultTests::fourSides);
        tests.test("assault_reopens_a_failed_gate_at_its_wave", 20, AssaultTests::reopensFailed);
        tests.test("assault_gate_moves_side_at_a_step", 20, AssaultTests::movesAtStep);
        tests.test("assault_shows_its_wave_and_side_on_the_challenge_bar", 20, AssaultTests::barShowsWaveAndSide);
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
            helper.assertTrue(AssaultState.get(server(helper)).assault(team).orElseThrow().gates().size() == 1, "not one gate opened");
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

    /** A platform and a bridge on each side of C.O.D., each with its gate (#239). */
    private static void fourSides(GameTestHelper helper) {
        UUID team = TestTeams.claim(helper);
        helper.setBlock(COD, Cod.BLOCK.get());
        helper.runAfterDelay(TICKED, () -> {
            Challenges.begin(server(helper), team, Handicap.PROTECTION);
            BlockPos cod = helper.absolutePos(COD);
            List<BlockPos> blocks = AssaultState.get(server(helper)).assault(team).orElseThrow().blocks();
            for (Direction side : Direction.Plane.HORIZONTAL) {
                // Other tests' structures stand around this one and the Assault builds only into air, so a side is
                // seen by what it built out there, not by one block.
                int far = Assault.OFFSET - Assault.PLATFORM_RADIUS;
                helper.assertTrue(blocks.stream().anyMatch(pos -> distanceToward(cod, pos, side) >= far),
                        "no " + side + " platform was built");
                helper.assertTrue(blocks.stream().anyMatch(pos -> distanceToward(cod, pos, side) > 0 && distanceToward(cod, pos, side) < far),
                        "no " + side + " bridge was built");
            }
            AssaultState.Built built = AssaultState.get(server(helper)).assault(team).orElseThrow();
            helper.assertTrue(built.gates().size() == 1, "not one gate opened for four sides");
            Direction side = built.side().orElseThrow();
            helper.assertTrue(AT.get(built.gates().getFirst()).equals(Assault.gateAt(cod, side)), "the gate isn't on its side's platform");
            Challenges.end(server(helper), team);
            helper.succeed();
        });
    }

    /** A gate that fails opens again at once at the wave it had reached, on the side it is on; the failed one is kept out (#239). */
    private static void reopensFailed(GameTestHelper helper) {
        UUID team = TestTeams.claim(helper);
        helper.setBlock(COD, Cod.BLOCK.get());
        helper.runAfterDelay(TICKED, () -> {
            Challenges.begin(server(helper), team, Handicap.PROTECTION);
            AssaultState state = AssaultState.get(server(helper));
            Direction side = state.assault(team).orElseThrow().side().orElseThrow();
            UUID failed = state.assault(team).orElseThrow().gates().getFirst();
            Assault.reopen(helper.getLevel(), failed, helper.absolutePos(COD.above()).getBottomCenter(), 7);
            List<UUID> gates = state.assault(team).orElseThrow().gates();
            helper.assertTrue(gates.size() == 1, "the Assault has " + gates.size() + " gates after one failed, not 1");
            helper.assertFalse(gates.contains(failed), "the failed gate is still the Assault's");
            helper.assertTrue(state.closed(failed), "the failed gate isn't kept out");
            UUID reopened = gates.getFirst();
            helper.assertTrue(Integer.valueOf(7).equals(WAVES.get(reopened)), "the gate didn't reopen at its wave");
            helper.assertTrue(AT.get(reopened).equals(Assault.gateAt(helper.absolutePos(COD), side)), "the gate didn't reopen on its side");
            Challenges.end(server(helper), team);
            helper.assertTrue(state.closed(reopened), "the reopened gate isn't removed with the Assault");
            helper.succeed();
        });
    }

    /** At a step's end the gate moves to another side, at the wave reached; between steps it stays (#239). */
    private static void movesAtStep(GameTestHelper helper) {
        UUID team = TestTeams.claim(helper);
        helper.setBlock(COD, Cod.BLOCK.get());
        helper.runAfterDelay(TICKED, () -> {
            Challenges.begin(server(helper), team, Handicap.PROTECTION);
            AssaultState state = AssaultState.get(server(helper));
            BlockPos cod = helper.absolutePos(COD);
            Direction side = state.assault(team).orElseThrow().side().orElseThrow();
            UUID gate = state.assault(team).orElseThrow().gates().getFirst();
            Assault.step(helper.getLevel(), gate, Assault.STEP - 1);
            helper.assertTrue(state.assault(team).orElseThrow().gates().equals(List.of(gate)), "the gate moved between steps");
            Assault.step(helper.getLevel(), gate, Assault.STEP);
            AssaultState.Built built = state.assault(team).orElseThrow();
            helper.assertTrue(built.gates().size() == 1, "the Assault has " + built.gates().size() + " gates after a step, not 1");
            UUID moved = built.gates().getFirst();
            Direction now = built.side().orElseThrow();
            helper.assertTrue(now != side, "the gate stayed on the " + side + " side at a step");
            helper.assertTrue(AT.get(moved).equals(Assault.gateAt(cod, now)), "the moved gate isn't on its new side's platform");
            helper.assertTrue(Integer.valueOf(Assault.STEP).equals(WAVES.get(moved)), "the moved gate didn't keep its wave");
            helper.assertTrue(state.closed(gate), "the old gate isn't kept out");
            Challenges.end(server(helper), team);
            helper.succeed();
        });
    }

    /** The challenge's bar names the Assault's wave and side, kept in step as the gate starts a wave (#239). */
    private static void barShowsWaveAndSide(GameTestHelper helper) {
        UUID team = TestTeams.claim(helper);
        ServerPlayer player = AssaultAbsenceTests.player(helper, team);
        helper.setBlock(COD, Cod.BLOCK.get());
        helper.runAfterDelay(TICKED, () -> {
            Challenges.begin(server(helper), team, Handicap.PROTECTION);
            AssaultState.Built built = AssaultState.get(server(helper)).assault(team).orElseThrow();
            Assault.noteWave(helper.getLevel(), built.gates().getFirst(), 7);
        });
        helper.runAfterDelay(TICKED + 2, () -> {
            Direction side = AssaultState.get(server(helper)).assault(team).orElseThrow().side().orElseThrow();
            var bar = InflationBar.of(team);
            helper.assertTrue(bar.isPresent(), "the team has no challenge bar during its Assault");
            Component expected = Component.translatable("mechanicalmasteryreborn.bar.assault",
                    Challenges.title(Handicap.PROTECTION), 7, Assault.sideName(side));
            Object top = ((TranslatableContents) bar.get().getName().getContents()).getArgs()[0];
            helper.assertValueEqual(top, expected, "the bar's top line");
            Challenges.end(server(helper), team);
            AssaultAbsenceTests.leave(player);
            helper.succeed();
        });
    }

    /** What a gate or mob of a removed Assault does when its chunk loads later: it is kept out. */
    private static void keepsOutLaterMobs(GameTestHelper helper) {
        UUID team = TestTeams.claim(helper);
        helper.setBlock(COD, Cod.BLOCK.get());
        helper.runAfterDelay(TICKED, () -> {
            Challenges.begin(server(helper), team, Handicap.PROTECTION);
            UUID gate = AssaultState.get(server(helper)).assault(team).orElseThrow().gates().getFirst();
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
            UUID gate = built.gates().getFirst();
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

    /** How far {@code pos} is from {@code cod} toward {@code side}, in a line 3 blocks either way of that side's axis. */
    private static int distanceToward(BlockPos cod, BlockPos pos, Direction side) {
        BlockPos off = pos.subtract(cod);
        int across = side.getAxis() == Direction.Axis.X ? off.getZ() : off.getX();
        if (Math.abs(across) > Assault.PLATFORM_RADIUS) {
            return -1;
        }
        return off.getX() * side.getStepX() + off.getZ() * side.getStepZ();
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
