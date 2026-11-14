// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.gametest;

import static io.github._5thlayer.mechanicalmasteryreborn.gametest.HomunculusClickTests.BASE;
import static io.github._5thlayer.mechanicalmasteryreborn.gametest.HomunculusClickTests.CHEST;
import static io.github._5thlayer.mechanicalmasteryreborn.gametest.HomunculusClickTests.TOLD;
import static io.github._5thlayer.mechanicalmasteryreborn.gametest.HomunculusClickTests.WORKED;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CarvedPumpkinBlock;

/**
 * The Homunculus's Table (#368): the spot a player assigns it above a block ringed by eight hoppers, where it sets down the
 * meals from its Supply before it clicks anything else, as a sneaking player places one, facing it. The game-test pack adds
 * a carved pumpkin to the meals, so a test can see which way one faces. Each Table is the space above {@link #RINGED}.
 */
final class HomunculusTableTests {

    private static final String TABLE_TOLD = "mechanicalmasteryreborn.homunculus.table.";
    /** The block ringed by hoppers. */
    private static final BlockPos RINGED = WORKED;
    private static final BlockPos TABLE = RINGED.above();

    private HomunculusTableTests() {
    }

    static void register(MechanicalMasteryRebornGameTests.Registrar tests) {
        tests.test("homunculus_table_sets_down_a_meal_from_its_supply", 400, HomunculusTableTests::setsDown);
        tests.test("homunculus_table_sets_down_before_it_clicks", 400, HomunculusTableTests::beforeClicking);
        tests.test("homunculus_table_waits_while_it_is_taken", 600, HomunculusTableTests::waitsWhileTaken);
        tests.test("homunculus_table_a_player_assigns", 40, HomunculusTableTests::assigns);
    }

    /**
     * A carved pumpkin in its Supply: it carries it in its off hand and places it on its Table, facing it, as its kindler,
     * and holds nothing after.
     */
    private static void setsDown(GameTestHelper helper) {
        var player = HomunculusClickTests.woken(helper);
        var homunculus = HomunculusTests.homunculus(helper, BASE);
        ring(helper);
        HomunculusClickTests.chest(helper, CHEST, new ItemStack(Items.CARVED_PUMPKIN));
        HomunculusClickTests.assign(helper, player, homunculus, CHEST);
        assignTable(helper, player, homunculus, RINGED);
        helper.succeedWhen(() -> {
            helper.assertBlockPresent(Blocks.CARVED_PUMPKIN, TABLE);
            var facing = helper.getBlockState(TABLE).getValue(CarvedPumpkinBlock.FACING);
            var table = helper.absolutePos(TABLE);
            var toward = Direction.getApproximateNearest(homunculus.getX() - (table.getX() + 0.5), 0, homunculus.getZ() - (table.getZ() + 0.5));
            helper.assertValueEqual(facing, toward, "the way the meal faces");
            helper.assertValueEqual(HomunculusClickTests.count(helper, CHEST, Items.CARVED_PUMPKIN), 0, "the meals left in its Supply");
            helper.assertTrue(homunculus.getOffhandItem().isEmpty(), "it holds " + homunculus.getOffhandItem());
            player.leave(helper);
        });
    }

    /** A block it works nearer its stand than its Table, with a stick and a meal in its Supply: it sets the meal down first. */
    private static void beforeClicking(GameTestHelper helper) {
        var player = HomunculusClickTests.woken(helper);
        var homunculus = HomunculusTests.homunculus(helper, BASE);
        ring(helper);
        var near = new BlockPos(4, 1, 1);
        helper.setBlock(near, ClickedBlock.CLICKED.get());
        HomunculusClickTests.chest(helper, CHEST, new ItemStack(Items.STICK));
        helper.getBlockEntity(CHEST, net.minecraft.world.level.block.entity.ChestBlockEntity.class).setItem(1, new ItemStack(Items.CARVED_PUMPKIN));
        HomunculusClickTests.assign(helper, player, homunculus, CHEST);
        assignTable(helper, player, homunculus, RINGED);
        var clicked = helper.absolutePos(near);
        helper.onEachTick(() -> {
            if (!ClickedBlock.clicks(clicked).isEmpty() && !helper.getBlockState(TABLE).is(Blocks.CARVED_PUMPKIN)) {
                helper.fail("it clicked before it set its meal down");
            }
        });
        helper.succeedWhen(() -> {
            helper.assertBlockPresent(Blocks.CARVED_PUMPKIN, TABLE);
            helper.assertFalse(ClickedBlock.clicks(clicked).isEmpty(), "no click after it set its meal down");
            player.leave(helper);
        });
    }

    /** Its Table taken by a block, it keeps the meal until the Table is clear, then sets it down. */
    private static void waitsWhileTaken(GameTestHelper helper) {
        var player = HomunculusClickTests.woken(helper);
        var homunculus = HomunculusTests.homunculus(helper, BASE);
        ring(helper);
        HomunculusClickTests.chest(helper, CHEST, new ItemStack(Items.CARVED_PUMPKIN));
        HomunculusClickTests.assign(helper, player, homunculus, CHEST);
        assignTable(helper, player, homunculus, RINGED);
        helper.setBlock(TABLE, Blocks.GLASS);
        helper.runAtTickTime(200, () -> {
            helper.assertBlockPresent(Blocks.GLASS, TABLE);
            helper.setBlock(TABLE, Blocks.AIR);
        });
        helper.succeedWhen(() -> {
            helper.assertTrue(helper.getTick() > 200, "set down before its Table was clear");
            helper.assertBlockPresent(Blocks.CARVED_PUMPKIN, TABLE);
            player.leave(helper);
        });
    }

    /**
     * A player assigns its Table as its Supply: a block with only seven hoppers round it is refused, one out of range is
     * refused, and a block ringed by eight puts the Table above it; clicking a meal on a ringed block puts the Table where the
     * meal is. Its Table is forgotten once a hopper of its ring is gone.
     */
    private static void assigns(GameTestHelper helper) {
        var player = HomunculusClickTests.woken(helper);
        var homunculus = HomunculusTests.homunculus(helper, BASE);
        ring(helper);
        var far = BASE.above(5);
        helper.setBlock(far, Blocks.STONE);
        ring(helper, far);
        var partial = new BlockPos(1, 1, 6);
        helper.setBlock(partial, Blocks.STONE);
        ring(helper, partial);
        helper.setBlock(partial.north().east(), Blocks.AIR);
        HomunculusClickTests.sneakClick(player, homunculus);
        player.click(helper, partial, Direction.UP, ItemStack.EMPTY);
        player.click(helper, far, Direction.UP, ItemStack.EMPTY);
        helper.assertTrue(HomunculusTests.stand(helper, BASE).table() == null, "a Table refused is kept");
        player.click(helper, RINGED, Direction.UP, ItemStack.EMPTY);
        helper.assertValueEqual(HomunculusTests.stand(helper, BASE).table(), helper.absolutePos(TABLE), "its Table");
        helper.setBlock(TABLE, Blocks.CAKE);
        HomunculusClickTests.sneakClick(player, homunculus);
        player.click(helper, TABLE, Direction.UP, ItemStack.EMPTY);
        helper.assertValueEqual(HomunculusTests.stand(helper, BASE).table(), helper.absolutePos(TABLE), "its Table, clicked on its meal");
        helper.assertBlockPresent(Blocks.CAKE, TABLE);
        helper.assertValueEqual(player.told(), List.of(TOLD + "assigning", TOLD + "not_storage", TOLD + "too_far", TABLE_TOLD + "set",
                TOLD + "assigning", TABLE_TOLD + "set"), "what the action bar said");
        helper.setBlock(RINGED.south().west(), Blocks.AIR);
        helper.runAtTickTime(30, () -> {
            helper.assertTrue(HomunculusTests.stand(helper, BASE).table() == null, "a Table with its ring broken is kept");
            player.leave(helper);
            helper.succeed();
        });
    }

    /** {@code player} assigns the space above {@code ringed} as the Homunculus's Table. */
    private static void assignTable(GameTestHelper helper, Clicker player, io.github._5thlayer.mechanicalmasteryreborn.life.HomunculusEntity homunculus,
            BlockPos ringed) {
        HomunculusClickTests.sneakClick(player, homunculus);
        player.click(helper, ringed, Direction.UP, ItemStack.EMPTY);
        helper.assertValueEqual(HomunculusTests.stand(helper, BASE).table(), helper.absolutePos(ringed.above()), "its Table");
    }

    /** Stone at {@link #RINGED}, ringed by eight hoppers. */
    private static void ring(GameTestHelper helper) {
        helper.setBlock(RINGED, Blocks.STONE);
        ring(helper, RINGED);
    }

    /** Eight hoppers round {@code pos}, the four sides and the four corners. */
    private static void ring(GameTestHelper helper, BlockPos pos) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (dx != 0 || dz != 0) {
                    helper.setBlock(pos.offset(dx, 0, dz), Blocks.HOPPER);
                }
            }
        }
    }
}
