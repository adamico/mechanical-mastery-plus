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
import io.github._5thlayer.mechanicalmasteryreborn.challenge.OverproductionBar;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.RiftShards;
import io.github._5thlayer.mechanicalmasteryreborn.cod.Cod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityProcessor;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.Vec3;

/**
 * The Assault's gates and bridges (#237, #239): starting the protection challenge builds a platform on each side of the
 * team's C.O.D. and a bridge to the first block toward it, and opens a gate on each ({@link #GATES}, an armor stand, since the
 * dev runs have no Gateways); a reset and an end remove the gate, what was built and the gate's mobs, but no Rift
 * Shard, and a reset sends a new Assault from wave 1 (#294); a failed gate reopens at its wave; and only the playing team's island is sent one. C.O.D. notes where it stands on its first tick.
 */
final class AssaultTests {

    private static final BlockPos COD = new BlockPos(1, 2, 1);
    private static final BlockPos OTHER_COD = new BlockPos(7, 2, 7);
    /** Ticks for a placed C.O.D. to have ticked. */
    private static final int TICKED = 2;
    /** The table Gateways gives the slimes the Assault spawns; not in the game tests' data, nor needed to be. */
    private static final ResourceKey<LootTable> ASSAULT_SLIME = ResourceKey.create(Registries.LOOT_TABLE,
            Identifier.fromNamespaceAndPath("mechanicalmasteryreborn", "assault/slime"));

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
        tests.test("assault_reset_sends_a_new_one_from_wave_1_and_leaves_the_rift_shards", 20, AssaultTests::newOnReset);
        tests.test("assault_win_removes_it", 20, AssaultTests::removedOnWin);
        tests.test("assault_keeps_out_a_removed_gates_mob_that_loads_later", 20, AssaultTests::keepsOutLaterMobs);
        tests.test("assault_is_sent_at_its_island_only", 20, AssaultTests::itsIslandOnly);
        tests.test("assault_builds_all_four_sides", 20, AssaultTests::fourSides);
        tests.test("assault_reopens_a_failed_gate_at_its_wave", 20, AssaultTests::reopensFailed);
        tests.test("assault_gate_moves_side_at_a_step", 20, AssaultTests::movesAtStep);
        tests.test("assault_shows_its_wave_and_side_on_the_challenge_bar", 20, AssaultTests::barShowsWaveAndSide);
        tests.test("assault_wave_command_sets_the_open_gates_wave", 20, AssaultTests::waveCommand);
        tests.test("assault_slime_s_split_offs_drop_no_rift_shard", 1, AssaultTests::splitOffsDropNoShard);
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

    /** A reset removes the Assault as an end does and sends a new one from wave 1, as though the challenge had just begun (#294). */
    private static void newOnReset(GameTestHelper helper) {
        UUID team = TestTeams.claim(helper);
        helper.setBlock(COD, Cod.BLOCK.get());
        helper.runAfterDelay(TICKED, () -> {
            Challenges.begin(server(helper), team, Handicap.PROTECTION);
            Removal removal = Removal.of(helper, team);
            helper.assertTrue(Assault.setWave(server(helper), team, 7), "the gate's wave wasn't set");
            UUID gate = AssaultState.get(server(helper)).assault(team).orElseThrow().gates().getFirst();
            helper.assertTrue(Challenges.reset(server(helper), team), "the protection challenge didn't reset");
            removal.expectGone(helper, "a reset");
            AssaultState state = AssaultState.get(server(helper));
            helper.assertTrue(state.closed(gate), "the gate at wave 7 isn't kept out after a reset");
            helper.assertTrue(state.assault(team).isPresent(), "no new Assault was sent after a reset");
            AssaultState.Built sent = state.assault(team).get();
            helper.assertValueEqual(sent.gates().size(), 1, "the gates the new Assault opened");
            UUID opened = sent.gates().getFirst();
            helper.assertFalse(opened.equals(removal.gate()) || opened.equals(gate), "the new Assault's gate is an old one");
            helper.assertValueEqual(WAVES.get(opened), 0, "the waves behind the new Assault's gate");
            helper.assertValueEqual(sent.wave(), 0, "the new Assault's wave");
            expectStone(helper, helper.absolutePos(COD).offset(Assault.OFFSET, -1, 0), "the new Assault's platform");
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
            var bar = OverproductionBar.of(team);
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
            expectGone(helper, after);
            for (BlockPos pos : built.blocks()) {
                helper.assertTrue(helper.getLevel().getBlockState(pos).isAir(), "the Assault's block at " + pos + " is still there after " + after);
            }
            helper.assertTrue(AssaultState.get(server(helper)).assault(team).isEmpty(), "the team is still sent an Assault after " + after);
        }

        /** The gate kept out and closed, its mob gone and the Rift Shard left, as after any end; what was built may stand again. */
        void expectGone(GameTestHelper helper, String after) {
            AssaultState state = AssaultState.get(server(helper));
            helper.assertTrue(state.closed(gate), "the gate isn't kept out after " + after);
            Entity open = helper.getLevel().getEntity(gate);
            helper.assertTrue(open == null || open.isRemoved(), "the gate is still open after " + after);
            helper.assertTrue(mob.isRemoved(), "the gate's mob is still around after " + after);
            helper.assertFalse(shard.isRemoved(), "a Rift Shard was taken by " + after);
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

    /** {@code /mmr assault wave <n>} reopens the team's gate with the waves before {@code n} behind it (#247). */
    private static void waveCommand(GameTestHelper helper) {
        UUID team = TestTeams.claim(helper);
        helper.setBlock(COD, Cod.BLOCK.get());
        helper.runAfterDelay(TICKED, () -> {
            Challenges.begin(server(helper), team, Handicap.PROTECTION);
            AssaultState state = AssaultState.get(server(helper));
            UUID old = state.assault(team).orElseThrow().gates().getFirst();
            var op = server(helper).createCommandSourceStack().withLevel(helper.getLevel())
                    .withPosition(helper.absolutePos(COD).getCenter()).withSuppressedOutput();
            server(helper).getCommands().performPrefixedCommand(op, "mmr assault wave 10");
            var built = state.assault(team).orElseThrow();
            helper.assertValueEqual(built.gates().size(), 1, "the open gates after the wave command");
            UUID gate = built.gates().getFirst();
            helper.assertFalse(gate.equals(old), "the gate wasn't reopened");
            helper.assertTrue(state.closed(old), "the old gate isn't kept out");
            helper.assertValueEqual(WAVES.get(gate), 9, "the waves behind the reopened gate");
            helper.assertValueEqual(built.wave(), 10, "the wave the team's bar shows");
            Challenges.end(server(helper), team);
            helper.succeed();
        });
    }

    /**
     * A slime the gate spawned carries the Assault's loot table, as Gateways' loot-table modifier sets it; its split-offs
     * fall back to a slime's own, with no Rift Shard pool (#251).
     */
    private static void splitOffsDropNoShard(GameTestHelper helper) {
        var tag = new CompoundTag();
        tag.putString("id", "minecraft:slime");
        tag.putInt("Size", 1);
        tag.putString("DeathLootTable", ASSAULT_SLIME.identifier().toString());
        Mob slime = (Mob) EntityType.loadEntityRecursive(tag, helper.getLevel(), EntitySpawnReason.COMMAND, EntityProcessor.NOP);
        slime.setPos(helper.absoluteVec(COD.above().getBottomCenter()));
        slime.setNoAi(true);
        helper.getLevel().addFreshEntity(slime);
        helper.assertValueEqual(slime.getLootTable(), Optional.of(ASSAULT_SLIME), "the gate's slime's loot table");
        slime.setHealth(0);
        slime.remove(Entity.RemovalReason.KILLED);
        List<Slime> children = helper.getLevel().getEntitiesOfClass(Slime.class, slime.getBoundingBox().inflate(2), child -> child != slime);
        helper.assertFalse(children.isEmpty(), "the slime split into nothing");
        for (Slime child : children) {
            helper.assertValueEqual(child.getLootTable(), EntityType.SLIME.getDefaultLootTable(), "a split-off's loot table");
            child.discard();
        }
        helper.succeed();
    }

    private static MinecraftServer server(GameTestHelper helper) {
        return helper.getLevel().getServer();
    }
}
