// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.gametest;

import java.util.UUID;

import io.github._5thlayer.mechanicalmasteryreborn.ServerConfig;
import io.github._5thlayer.mechanicalmasteryreborn.lab.EmergencySaplings;
import io.github._5thlayer.mechanicalmasteryreborn.lab.Lab;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * The lab's emergency sapling (#302): a member of the lab's team holding dirt, and carrying no sapling, swaps one for an
 * oak sapling, once each cooldown for the whole team; anyone else, or a member who carries a sapling, is refused. Each
 * test is an island of its own, with another team's in a corner.
 */
final class LabSaplingTests {

    private static final BlockPos LAB = new BlockPos(4, 2, 4);
    /** Another team's island, in a corner of the test's. */
    private static final BlockPos OTHER_ISLAND = new BlockPos(7, 2, 7);

    private LabSaplingTests() {
    }

    static void register(MechanicalMasteryRebornGameTests.Registrar tests) {
        tests.test("lab_sapling_swaps_a_dirt_for_an_oak_sapling", 1, LabSaplingTests::swapsADirt);
        tests.test("lab_sapling_refused_to_a_player_carrying_a_sapling", 1, LabSaplingTests::refusedCarryingOne);
        tests.test("lab_sapling_once_each_cooldown_for_the_whole_team", 1, LabSaplingTests::oncePerCooldown);
        tests.test("lab_sapling_refused_to_another_team", 1, LabSaplingTests::refusedToAnotherTeam);
    }

    private static void swapsADirt(GameTestHelper helper) {
        UUID team = TestTeams.claim(helper);
        Player player = member(helper, team);
        ask(helper, player, 3);
        helper.assertValueEqual(player.getMainHandItem().getCount(), 2, "the dirt left in hand");
        helper.assertValueEqual(count(player, Items.OAK_SAPLING), 1, "the oak saplings given");
        helper.succeed();
    }

    private static void refusedCarryingOne(GameTestHelper helper) {
        UUID team = TestTeams.claim(helper);
        Player player = member(helper, team);
        // Off the hotbar's selected slot, which the dirt goes in.
        player.getInventory().setItem(20, new ItemStack(Items.BIRCH_SAPLING));
        ask(helper, player, 3);
        helper.assertValueEqual(player.getMainHandItem().getCount(), 3, "the dirt left in hand");
        helper.assertValueEqual(count(player, Items.OAK_SAPLING), 0, "the oak saplings given");
        helper.assertValueEqual(EmergencySaplings.get(helper.getLevel().getServer()).ticksLeft(team, now(helper)), 0L,
                "the cooldown after a refusal");
        helper.succeed();
    }

    /** A second ask, even by another member who carries no sapling, waits out the team's cooldown; another team's doesn't. */
    private static void oncePerCooldown(GameTestHelper helper) {
        UUID team = TestTeams.claim(helper);
        UUID other = TestTeams.claim(helper, OTHER_ISLAND, OTHER_ISLAND);
        ask(helper, member(helper, team), 1);
        Player second = member(helper, team);
        ask(helper, second, 3);
        helper.assertValueEqual(second.getMainHandItem().getCount(), 3, "the second member's dirt left in hand");
        helper.assertValueEqual(count(second, Items.OAK_SAPLING), 0, "the oak saplings given to the second member");
        var saplings = EmergencySaplings.get(helper.getLevel().getServer());
        long cooldown = ServerConfig.labSaplingCooldown();
        helper.assertValueEqual(saplings.ticksLeft(team, now(helper)), cooldown, "the team's cooldown");
        helper.assertValueEqual(saplings.ticksLeft(team, now(helper) + cooldown), 0L, "the team's cooldown a cooldown later");
        helper.assertValueEqual(saplings.ticksLeft(other, now(helper)), 0L, "the other team's cooldown");
        helper.succeed();
    }

    private static void refusedToAnotherTeam(GameTestHelper helper) {
        TestTeams.claim(helper);
        UUID other = TestTeams.claim(helper, OTHER_ISLAND, OTHER_ISLAND);
        Player visitor = member(helper, other);
        ask(helper, visitor, 3);
        helper.assertValueEqual(visitor.getMainHandItem().getCount(), 3, "the visitor's dirt left in hand");
        helper.assertValueEqual(count(visitor, Items.OAK_SAPLING), 0, "the oak saplings given to the visitor");
        helper.succeed();
    }

    private static Player member(GameTestHelper helper, UUID team) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        TestTeams.join(player, team);
        return player;
    }

    /** Right-clicks the lab, placed first if it isn't yet, holding {@code dirt} dirt. */
    private static void ask(GameTestHelper helper, Player player, int dirt) {
        if (!helper.getBlockState(LAB).is(Lab.BLOCK.get())) {
            helper.setBlock(LAB, Lab.BLOCK.get());
        }
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.DIRT, dirt));
        BlockPos pos = helper.absolutePos(LAB);
        helper.getBlockState(LAB).useItemOn(player.getMainHandItem(), helper.getLevel(), player, InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false));
    }

    private static int count(Player player, Item item) {
        return player.getInventory().countItem(item);
    }

    private static long now(GameTestHelper helper) {
        return helper.getLevel().getServer().overworld().getGameTime();
    }
}
