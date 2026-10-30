// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.gametest;

import io.github._5thlayer.mechanicalmasteryreborn.ServerConfig;
import io.github._5thlayer.mechanicalmasteryreborn.cod.Cod;
import io.github._5thlayer.mechanicalmasteryreborn.cod.CodBlockEntity;
import io.github._5thlayer.mechanicalmasteryreborn.essence.Essence;
import io.github._5thlayer.mechanicalmasteryreborn.essence.Essences;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.item.ItemResource;

/**
 * A dormant C.O.D. (#164, #196): a newly placed one holds a full stack of Advanced essence; emptied, by a
 * player or by automation, it makes one more each configured interval until the slot holds a stack again,
 * and puts nothing out of itself. Each test places its C.O.D. at {@link #COD} with a chest east of it.
 */
final class CodTests {

    private static final BlockPos COD = new BlockPos(4, 2, 4);
    private static final BlockPos CHEST = COD.east();
    private static final BlockPos HOPPER = COD.below();
    private static final int STACK = 64;
    /** Ticks either side of an interval's end, so a check never lands on the tick the essence is made. */
    private static final int MARGIN = 2;

    private CodTests() {
    }

    static void register(MechanicalMasteryRebornGameTests.Registrar tests) {
        tests.test("cod_placed_holds_a_full_stack_of_advanced_essence", 1, CodTests::placedHoldsAFullStack);
        tests.test("cod_taken_by_hand_is_left_empty", 1, CodTests::takenByHandIsLeftEmpty);
        tests.test("cod_shift_clicked_gives_one_stack_and_is_left_empty", 1, CodTests::shiftClickedGivesOneStack);
        tests.test("cod_emptied_by_automation_makes_one_essence_an_interval", 2 * interval() + 10, CodTests::makesOneAnInterval);
        tests.test("cod_stops_making_at_a_full_stack", 3 * interval() + 10, CodTests::stopsAtAFullStack);
        tests.test("cod_feeds_a_hopper_below_at_its_pace", 3 * interval() + 20, CodTests::feedsAHopperAtItsPace);
        tests.test("cod_partly_extracted_keeps_the_rest", 1, CodTests::partlyExtractedKeepsTheRest);
        tests.test("cod_takes_nothing_in", 1, CodTests::takesNothingIn);
        tests.test("cod_dormant_puts_nothing_out", 40, CodTests::dormantPutsNothingOut);
    }

    private static void placedHoldsAFullStack(GameTestHelper helper) {
        place(helper);
        expectFull(helper, "a placed C.O.D.");
        helper.succeed();
    }

    private static void takenByHandIsLeftEmpty(GameTestHelper helper) {
        var cod = place(helper);
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        AbstractContainerMenu menu = cod.createMenu(0, player.getInventory(), player);
        menu.clicked(0, 0, ContainerInput.PICKUP, player);
        expectAdvanced(helper, menu.getCarried(), STACK, "the stack a player picked up");
        expectHolds(helper, 0, "C.O.D. after a player took its stack");
        helper.succeed();
    }

    private static void shiftClickedGivesOneStack(GameTestHelper helper) {
        var cod = place(helper);
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        AbstractContainerMenu menu = cod.createMenu(0, player.getInventory(), player);
        menu.clicked(0, 0, ContainerInput.QUICK_MOVE, player);
        int moved = player.getInventory().countItem(Essences.item(Essence.ADVANCED));
        helper.assertValueEqual(moved, STACK, "the Advanced essence a shift-click put in the player's inventory");
        expectHolds(helper, 0, "C.O.D. after a shift-click");
        helper.succeed();
    }

    private static void makesOneAnInterval(GameTestHelper helper) {
        place(helper);
        int moved = ResourceHandlerUtil.move(codFace(helper), chestFace(helper), resource -> true, STACK, null);
        helper.assertValueEqual(moved, STACK, "the essence moved into the chest");
        expectHolds(helper, 0, "C.O.D. after automation emptied it");
        int interval = interval();
        helper.runAtTickTime(interval - MARGIN, () -> expectHolds(helper, 0, "C.O.D. just before an interval"));
        helper.runAtTickTime(interval + MARGIN, () -> expectHolds(helper, 1, "C.O.D. just after an interval"));
        helper.runAtTickTime(2 * interval - MARGIN, () -> expectHolds(helper, 1, "C.O.D. just before two intervals"));
        helper.runAtTickTime(2 * interval + MARGIN, () -> {
            expectHolds(helper, 2, "C.O.D. just after two intervals");
            helper.succeed();
        });
    }

    private static void stopsAtAFullStack(GameTestHelper helper) {
        place(helper);
        ResourceHandlerUtil.move(codFace(helper), chestFace(helper), resource -> true, 1, null);
        int interval = interval();
        helper.runAtTickTime(interval + MARGIN, () -> expectFull(helper, "C.O.D. an interval after one was taken"));
        helper.runAtTickTime(3 * interval + MARGIN, () -> {
            expectFull(helper, "C.O.D. three intervals after one was taken");
            helper.succeed();
        });
    }

    private static void feedsAHopperAtItsPace(GameTestHelper helper) {
        place(helper);
        ResourceHandlerUtil.move(codFace(helper), chestFace(helper), resource -> true, STACK, null);
        helper.setBlock(HOPPER, Blocks.HOPPER);
        int interval = interval();
        for (int made = 0; made < 3; made++) {
            int before = made;
            helper.runAtTickTime((made + 1) * interval - MARGIN,
                    () -> helper.assertValueEqual(inHopper(helper), before, "the essence in the hopper just before interval " + (before + 1)));
        }
        helper.runAtTickTime(3 * interval + 10 + MARGIN, () -> {
            helper.assertValueEqual(inHopper(helper), 3, "the essence in the hopper after three intervals");
            expectHolds(helper, 0, "C.O.D. over a hopper");
            helper.succeed();
        });
    }

    private static void partlyExtractedKeepsTheRest(GameTestHelper helper) {
        place(helper);
        ResourceHandlerUtil.move(codFace(helper), chestFace(helper), resource -> true, 10, null);
        var reserve = codFace(helper);
        helper.assertValueEqual(reserve.getAmountAsInt(0), STACK - 10, "C.O.D.'s essence after 10 were taken");
        helper.succeed();
    }

    private static void takesNothingIn(GameTestHelper helper) {
        place(helper);
        int moved = ResourceHandlerUtil.move(codFace(helper), codFace(helper), resource -> true, 1, null);
        helper.assertValueEqual(moved, 0, "the essence C.O.D. took back in");
        expectFull(helper, "C.O.D. offered its own essence back");
        helper.succeed();
    }

    private static void dormantPutsNothingOut(GameTestHelper helper) {
        place(helper);
        helper.runAtTickTime(39, () -> {
            helper.assertTrue(ResourceHandlerUtil.isEmpty(chestFace(helper)), "a dormant C.O.D. put essence into the chest beside it");
            expectFull(helper, "a dormant C.O.D. after 39 ticks");
            helper.succeed();
        });
    }

    private static CodBlockEntity place(GameTestHelper helper) {
        helper.setBlock(COD, Cod.BLOCK.get());
        helper.setBlock(CHEST, Blocks.CHEST);
        return helper.getBlockEntity(COD, CodBlockEntity.class);
    }

    private static void expectFull(GameTestHelper helper, String what) {
        expectHolds(helper, STACK, what);
    }

    private static void expectHolds(GameTestHelper helper, int count, String what) {
        if (count == 0) {
            helper.assertTrue(ResourceHandlerUtil.isEmpty(codFace(helper)), what + " isn't empty");
        } else {
            var reserve = codFace(helper);
            expectAdvanced(helper, reserve.getResource(0).toStack(reserve.getAmountAsInt(0)), count, what);
        }
    }

    private static int inHopper(GameTestHelper helper) {
        var hopper = helper.getLevel().getCapability(Capabilities.Item.BLOCK, helper.absolutePos(HOPPER), Direction.UP);
        helper.assertTrue(hopper != null, "the hopper has no item face");
        int count = 0;
        for (int slot = 0; slot < hopper.size(); slot++) {
            count += hopper.getAmountAsInt(slot);
        }
        return count;
    }

    private static int interval() {
        return ServerConfig.codDormantInterval();
    }

    private static void expectAdvanced(GameTestHelper helper, ItemStack stack, int count, String what) {
        helper.assertTrue(stack.is(Essences.item(Essence.ADVANCED)), what + " holds " + stack + ", not Advanced essence");
        helper.assertValueEqual(stack.getCount(), count, what + "'s count");
    }

    /** C.O.D.'s item face, from the chest's side, as automation finds it. */
    private static ResourceHandler<ItemResource> codFace(GameTestHelper helper) {
        var face = helper.getLevel().getCapability(Capabilities.Item.BLOCK, helper.absolutePos(COD), Direction.EAST);
        helper.assertTrue(face != null, "C.O.D. has no item face");
        return face;
    }

    private static ResourceHandler<ItemResource> chestFace(GameTestHelper helper) {
        var face = helper.getLevel().getCapability(Capabilities.Item.BLOCK, helper.absolutePos(CHEST), Direction.WEST);
        helper.assertTrue(face != null, "the chest has no item face");
        return face;
    }
}
