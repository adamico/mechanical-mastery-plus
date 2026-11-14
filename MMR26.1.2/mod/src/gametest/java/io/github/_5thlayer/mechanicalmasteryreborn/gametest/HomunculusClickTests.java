// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.gametest;

import java.util.List;

import io.github._5thlayer.mechanicalmasteryreborn.life.Homunculus;
import io.github._5thlayer.mechanicalmasteryreborn.life.HomunculusEntity;
import io.github._5thlayer.mechanicalmasteryreborn.recycler.HandDriveBlock;
import io.github._5thlayer.mechanicalmasteryreborn.recycler.Recycler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.DecoratedPotBlockEntity;
import net.minecraft.world.phys.Vec3;

/**
 * The Homunculus clicks (#368): a woken Homunculus takes one item at a time from the Supply a player assigned it and
 * walks to right-click the nearest block it works that it can reach, through the player's own click path, as the player who
 * kindled it, never sneaking. The tests play at {@link HomunculusTests}' pace, and its stand at {@link #BASE} has stews
 * enough to last them. Each raises its Homunculus by the stand's base on the south side, where the player stands, with
 * nothing to cage it.
 */
final class HomunculusClickTests {

    private static final String TOLD = "mechanicalmasteryreborn.homunculus.supply.";

    private static final BlockPos BASE = new BlockPos(4, 1, 4);
    private static final BlockPos PLAYER = new BlockPos(4, 1, 7);
    private static final BlockPos CHEST = new BlockPos(1, 1, 1);
    private static final BlockPos WORKED = new BlockPos(7, 1, 4);

    private HomunculusClickTests() {
    }

    static void register(MechanicalMasteryRebornGameTests.Registrar tests) {
        tests.test("homunculus_clicks_a_block_with_an_item_from_its_supply", 400, HomunculusClickTests::clicks);
        tests.test("homunculus_clicks_a_hand_drive_round", 400, HomunculusClickTests::handDrive);
        tests.test("homunculus_clicks_nothing_empty_handed", 100, HomunculusClickTests::emptyHanded);
        tests.test("homunculus_clicks_the_nearest_block_it_can_reach", 400, HomunculusClickTests::nearestReachable);
        tests.test("homunculus_clicks_holds_what_its_click_hands_it", 400, HomunculusClickTests::handedBack);
        tests.test("homunculus_clicks_drops_what_it_holds_when_it_dies", 400, HomunculusClickTests::dropsHeld);
        tests.test("homunculus_clicks_spends_its_works_on_a_supply_it_cant_reach", 200, HomunculusClickTests::unreachableSupply);
        tests.test("homunculus_clicks_from_a_supply_a_player_assigns", 40, HomunculusClickTests::assigns);
        tests.test("homunculus_clicks_takes_from_beside_a_supply_it_cant_stand_on", 400, HomunculusClickTests::besideSupply);
        tests.test("homunculus_clicks_skips_a_block_whose_storage_is_empty", 600, HomunculusClickTests::skipsEmpty);
    }

    /**
     * With three sticks in its Supply, it takes one and clicks the block on the face toward it, at the face's centre, as its
     * kindler and never sneaking, again and again with the same stick, which the block doesn't use up.
     */
    private static void clicks(GameTestHelper helper) {
        var player = woken(helper);
        var homunculus = HomunculusTests.homunculus(helper, BASE);
        helper.setBlock(WORKED, ClickedBlock.CLICKED.get());
        chest(helper, CHEST, new ItemStack(Items.STICK, 3));
        assign(helper, player, homunculus, CHEST);
        var worked = helper.absolutePos(WORKED);
        helper.succeedWhen(() -> {
            var clicks = ClickedBlock.clicks(worked);
            helper.assertTrue(clicks.size() >= 2, "clicks so far: " + clicks.size());
            for (var click : clicks) {
                helper.assertTrue(click.fake(), "a click by a real player");
                helper.assertValueEqual(click.by(), player.getUUID(), "who clicked");
                helper.assertFalse(click.sneaking(), "a sneaking click");
                helper.assertValueEqual(click.hand(), InteractionHand.MAIN_HAND, "the hand it clicked with");
                helper.assertTrue(click.held().is(Items.STICK) && click.held().getCount() == 1, "it clicked with " + click.held());
                helper.assertTrue(click.face().getAxis().isHorizontal(), "it clicked the " + click.face() + " face");
                var centre = Vec3.atCenterOf(worked).relative(click.face(), 0.5);
                helper.assertTrue(click.at().distanceTo(centre) < 1e-6, "it clicked at " + click.at() + ", not the face's centre " + centre);
            }
            helper.assertValueEqual(count(helper, CHEST, Items.STICK), 2, "the sticks left in its Supply");
            helper.assertTrue(homunculus.getMainHandItem().is(Items.STICK), "it holds " + homunculus.getMainHandItem());
            player.leave(helper);
        });
    }

    /** Its Supply under a block, so no mob stands on it: it takes from beside it, and clicks. */
    private static void besideSupply(GameTestHelper helper) {
        var player = woken(helper);
        var homunculus = HomunculusTests.homunculus(helper, BASE);
        helper.setBlock(WORKED, ClickedBlock.CLICKED.get());
        chest(helper, CHEST, new ItemStack(Items.STICK, 3));
        helper.setBlock(CHEST.above(), Blocks.STONE);
        assign(helper, player, homunculus, CHEST);
        var worked = helper.absolutePos(WORKED);
        helper.succeedWhen(() -> {
            helper.assertFalse(ClickedBlock.clicks(worked).isEmpty(), "no click yet");
            helper.assertValueEqual(count(helper, CHEST, Items.STICK), 2, "the sticks left in its Supply");
            player.leave(helper);
        });
    }

    /**
     * A decorated pot, in the game-test pack's tag, takes whatever an empty one is clicked with, as a Cutting Board does: it
     * skips the pot while it is empty, and fetches and clicks once it holds a stick.
     */
    private static void skipsEmpty(GameTestHelper helper) {
        var player = woken(helper);
        var homunculus = HomunculusTests.homunculus(helper, BASE);
        helper.setBlock(WORKED, Blocks.DECORATED_POT);
        chest(helper, CHEST, new ItemStack(Items.STICK, 2));
        assign(helper, player, homunculus, CHEST);
        var pot = helper.getBlockEntity(WORKED, DecoratedPotBlockEntity.class);
        helper.runAtTickTime(200, () -> {
            helper.assertTrue(pot.getTheItem().isEmpty(), "the empty pot holds " + pot.getTheItem());
            helper.assertValueEqual(count(helper, CHEST, Items.STICK), 2, "the sticks left in its Supply");
            pot.setTheItem(new ItemStack(Items.STICK));
        });
        helper.succeedWhen(() -> {
            helper.assertTrue(helper.getTick() > 200 && pot.getTheItem().getCount() >= 2, "the pot holds " + pot.getTheItem());
            player.leave(helper);
        });
    }

    /** With a stick from its Supply it turns a hand drive, which a click turns whatever is held. */
    private static void handDrive(GameTestHelper helper) {
        var player = woken(helper);
        helper.setBlock(WORKED, Recycler.HAND_DRIVE.get());
        chest(helper, CHEST, new ItemStack(Items.STICK));
        assign(helper, player, HomunculusTests.homunculus(helper, BASE), CHEST);
        helper.succeedWhen(() -> {
            helper.assertTrue(helper.getBlockState(WORKED).getValue(HandDriveBlock.TURNING), "the hand drive doesn't turn");
            player.leave(helper);
        });
    }

    /** With an empty Supply it never clicks, even with its hand empty, and its works are spent all the same. */
    private static void emptyHanded(GameTestHelper helper) {
        var player = woken(helper);
        var homunculus = HomunculusTests.homunculus(helper, BASE);
        helper.setBlock(WORKED, ClickedBlock.CLICKED.get());
        chest(helper, CHEST, ItemStack.EMPTY);
        assign(helper, player, homunculus, CHEST);
        helper.runAtTickTime(80, () -> {
            helper.assertValueEqual(ClickedBlock.clicks(helper.absolutePos(WORKED)), List.of(), "the clicks with nothing to hold");
            helper.assertTrue(HomunculusTests.stand(helper, BASE).works() > 0, "no works spent with nothing to hold");
            player.leave(helper);
            helper.succeed();
        });
    }

    /** Its Supply walled in, it never fetches from it: it clicks nothing and its works are spent. */
    private static void unreachableSupply(GameTestHelper helper) {
        var player = woken(helper);
        var homunculus = HomunculusTests.homunculus(helper, BASE);
        helper.setBlock(WORKED, ClickedBlock.CLICKED.get());
        chest(helper, CHEST, new ItemStack(Items.STICK));
        assign(helper, player, homunculus, CHEST);
        for (var side : Direction.Plane.HORIZONTAL) {
            helper.setBlock(CHEST.relative(side), Blocks.GLASS);
            helper.setBlock(CHEST.relative(side).above(), Blocks.GLASS);
        }
        helper.setBlock(CHEST.above(), Blocks.GLASS);
        helper.runAtTickTime(150, () -> {
            helper.assertValueEqual(ClickedBlock.clicks(helper.absolutePos(WORKED)), List.of(), "the clicks with a Supply out of reach");
            helper.assertTrue(homunculus.getMainHandItem().isEmpty(), "it holds " + homunculus.getMainHandItem());
            int works = HomunculusTests.stand(helper, BASE).works();
            helper.assertTrue(works > 5, "only " + works + " works spent with a Supply out of reach");
            player.leave(helper);
            helper.succeed();
        });
    }

    /** The block nearest its stand walled in, it works the next nearest, and only that. */
    private static void nearestReachable(GameTestHelper helper) {
        var player = woken(helper);
        var near = new BlockPos(4, 1, 1);
        helper.setBlock(near, ClickedBlock.CLICKED.get());
        for (var side : Direction.Plane.HORIZONTAL) {
            helper.setBlock(near.relative(side), Blocks.GLASS);
            helper.setBlock(near.relative(side).above(), Blocks.GLASS);
        }
        var far = new BlockPos(8, 1, 4);
        helper.setBlock(far, ClickedBlock.CLICKED.get());
        helper.setBlock(new BlockPos(0, 1, 8), ClickedBlock.CLICKED.get());
        chest(helper, CHEST, new ItemStack(Items.STICK));
        assign(helper, player, HomunculusTests.homunculus(helper, BASE), CHEST);
        helper.succeedWhen(() -> {
            helper.assertTrue(ClickedBlock.clicks(helper.absolutePos(far)).size() >= 2, "the next nearest block isn't worked");
            helper.assertValueEqual(ClickedBlock.clicks(helper.absolutePos(near)), List.of(), "the clicks on the walled-in block");
            helper.assertValueEqual(ClickedBlock.clicks(helper.absolutePos(new BlockPos(0, 1, 8))), List.of(), "the clicks on a farther block");
            player.leave(helper);
        });
    }

    /** A click that swaps what it holds leaves it holding the new item; what the click puts anywhere else falls by the block. */
    private static void handedBack(GameTestHelper helper) {
        var player = woken(helper);
        var homunculus = HomunculusTests.homunculus(helper, BASE);
        helper.setBlock(WORKED, ClickedBlock.CLICKED.get());
        chest(helper, CHEST, new ItemStack(Items.PAPER, 2));
        assign(helper, player, homunculus, CHEST);
        helper.succeedWhen(() -> {
            var clicks = ClickedBlock.clicks(helper.absolutePos(WORKED));
            helper.assertTrue(!clicks.isEmpty() && clicks.getFirst().held().is(Items.PAPER), "no click with paper yet");
            helper.assertTrue(homunculus.getMainHandItem().is(Items.MAP), "it holds " + homunculus.getMainHandItem() + ", not the map");
            helper.assertTrue(clicks.stream().allMatch(click -> !click.held().is(Items.PAPER) || click == clicks.getFirst()), "it clicked paper twice");
            helper.assertValueEqual(count(helper, CHEST, Items.PAPER), 1, "the paper left in its Supply");
            helper.assertTrue(HomunculusTests.drops(helper, WORKED).stream().anyMatch(stack -> stack.is(Items.FEATHER)), "no feather by the block");
            player.leave(helper);
        });
    }

    /** Killed, it drops what it holds with its spent spark. */
    private static void dropsHeld(GameTestHelper helper) {
        var player = woken(helper);
        var homunculus = HomunculusTests.homunculus(helper, BASE);
        chest(helper, CHEST, new ItemStack(Items.STICK));
        assign(helper, player, homunculus, CHEST);
        helper.setBlock(WORKED, ClickedBlock.CLICKED.get());
        Vec3[] at = new Vec3[1];
        helper.startSequence()
                .thenWaitUntil(() -> helper.assertTrue(homunculus.getMainHandItem().is(Items.STICK), "it holds " + homunculus.getMainHandItem()))
                .thenExecute(() -> {
                    at[0] = homunculus.position();
                    homunculus.kill(helper.getLevel());
                })
                .thenExecute(() -> {
                    var drops = helper.getLevel().getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,
                            new net.minecraft.world.phys.AABB(at[0], at[0]).inflate(2)).stream().map(entity -> entity.getItem()).toList();
                    helper.assertTrue(drops.stream().anyMatch(stack -> stack.is(Items.STICK)), "a dead Homunculus left " + drops);
                    helper.assertTrue(drops.stream().anyMatch(Homunculus::spent), "a dead Homunculus left " + drops);
                    player.leave(helper);
                })
                .thenSucceed();
    }

    /**
     * A player sneak-clicks it with an empty hand to assign its Supply, then clicks a storage: one more than its range up is
     * refused, a block that holds nothing is refused, one in range is its Supply. A sneak-click again cancels the assigning,
     * and the next click opens the chest as ever. A player who may not build here can't assign one. A broken Supply is
     * forgotten.
     */
    private static void assigns(GameTestHelper helper) {
        var player = woken(helper);
        var homunculus = HomunculusTests.homunculus(helper, BASE);
        var high = BASE.above(5);
        chest(helper, high, ItemStack.EMPTY);
        chest(helper, CHEST, ItemStack.EMPTY);
        var other = new BlockPos(7, 1, 1);
        chest(helper, other, ItemStack.EMPTY);
        sneakClick(player, homunculus);
        player.click(helper, high, Direction.UP, ItemStack.EMPTY);
        helper.assertTrue(HomunculusTests.stand(helper, BASE).supply() == null, "a Supply out of range is kept");
        player.click(helper, new BlockPos(0, 0, 0), Direction.UP, ItemStack.EMPTY);
        player.click(helper, CHEST, Direction.UP, ItemStack.EMPTY);
        helper.assertValueEqual(HomunculusTests.stand(helper, BASE).supply(), helper.absolutePos(CHEST), "its Supply");
        sneakClick(player, homunculus);
        sneakClick(player, homunculus);
        player.click(helper, other, Direction.UP, ItemStack.EMPTY);
        helper.assertValueEqual(HomunculusTests.stand(helper, BASE).supply(), helper.absolutePos(CHEST), "its Supply after a cancelled assigning");
        helper.assertValueEqual(player.told(), List.of(TOLD + "assigning", TOLD + "too_far", TOLD + "not_storage", TOLD + "set", TOLD + "assigning",
                TOLD + "cancelled"), "what the action bar said");
        var visitor = Clicker.of(helper);
        visitor.setGameMode(GameType.ADVENTURE);
        sneakClick(visitor, homunculus);
        helper.assertValueEqual(visitor.told(), List.of(TOLD + "not_allowed"), "what the action bar told a player who can't build");
        helper.setBlock(CHEST, Blocks.AIR);
        helper.runAtTickTime(30, () -> {
            helper.assertTrue(HomunculusTests.stand(helper, BASE).supply() == null, "a broken Supply is kept");
            player.leave(helper);
            visitor.leave(helper);
            helper.succeed();
        });
    }

    /** A Homunculus kindled at {@link #BASE} by a player standing at {@link #PLAYER}, jumpstarted, with stews to last. */
    private static Clicker woken(GameTestHelper helper) {
        HomunculusTests.playLife(helper);
        var player = Clicker.of(helper);
        player.standAt(helper, PLAYER);
        HomunculusTests.rodOnClay(helper, BASE);
        player.click(helper, BASE.above(), Homunculus.vitalSpark(false));
        HomunculusTests.charge(helper, BASE, HomunculusTests.JUMPSTART);
        HomunculusTests.feed(helper, BASE, Items.MUSHROOM_STEW, 0, 9);
        return player;
    }

    private static void sneakClick(Clicker player, HomunculusEntity homunculus) {
        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        player.setShiftKeyDown(true);
        player.interactOn(homunculus, InteractionHand.MAIN_HAND, homunculus.position());
        player.setShiftKeyDown(false);
    }

    /** {@code player} assigns the chest at {@code chest} as the Homunculus's Supply. */
    private static void assign(GameTestHelper helper, Clicker player, HomunculusEntity homunculus, BlockPos chest) {
        sneakClick(player, homunculus);
        player.click(helper, chest, Direction.UP, ItemStack.EMPTY);
        helper.assertValueEqual(HomunculusTests.stand(helper, BASE).supply(), helper.absolutePos(chest), "its Supply");
    }

    private static void chest(GameTestHelper helper, BlockPos pos, ItemStack held) {
        helper.setBlock(pos, Blocks.CHEST);
        helper.getBlockEntity(pos, ChestBlockEntity.class).setItem(0, held);
    }

    private static int count(GameTestHelper helper, BlockPos pos, Item item) {
        var chest = helper.getBlockEntity(pos, ChestBlockEntity.class);
        int count = 0;
        for (int slot = 0; slot < chest.getContainerSize(); slot++) {
            count += chest.getItem(slot).is(item) ? chest.getItem(slot).getCount() : 0;
        }
        return count;
    }
}
