// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.gametest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import io.github._5thlayer.mechanicalmasteryreborn.ServerConfig;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.ChainMachines;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.Challenges;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.Handicap;
import io.github._5thlayer.mechanicalmasteryreborn.life.Homunculus;
import io.github._5thlayer.mechanicalmasteryreborn.life.HomunculusEntity;
import io.github._5thlayer.mechanicalmasteryreborn.life.HomunculusStandBlock;
import io.github._5thlayer.mechanicalmasteryreborn.life.HomunculusStandBlockEntity;
import io.github._5thlayer.mechanicalmasteryreborn.life.HomunculusStandBlockEntity.Doing;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

/**
 * The Homunculus and its stand (#367). The tests play at a pace of their own, set through the server config as the server
 * starts ({@link #useTestPace}): a jumpstart of 1000 FE, ten ticks of work for each point of hunger, so a new Homunculus's full
 * belly (2) lasts 20 ticks, a bread (5) 50 and a mushroom stew (6) 60, and a work every 12 ticks on a food of no quality, every 3
 * on a diamond one. Most kindle their Homunculus in a cage of glass around the stand's base that leaves it one space beside
 * the base, so it stands there, where it eats, rather than wander off.
 */
final class HomunculusTests {

    static final int JUMPSTART = 1000;
    static final int TICKS_PER_HUNGER = 10;
    static final int FULL = 2;
    static final int INTERVAL = 12;
    static final double SPEED_UP = 1.0;

    private static final Identifier DIAMOND = Identifier.fromNamespaceAndPath("quality_food", "diamond");
    private static final String TOLD = "mechanicalmasteryreborn.homunculus.kindle.";

    private static final BlockPos BASE = new BlockPos(4, 1, 4);
    private static final BlockPos SPACE = BASE.south();
    private static final BlockPos OTHER = new BlockPos(1, 1, 4);
    /** Two stands far enough apart for a cage each. */
    private static final BlockPos FIRST = new BlockPos(2, 1, 2);
    private static final BlockPos SECOND = new BlockPos(6, 1, 6);

    private HomunculusTests() {
    }

    /** From the server's start, the Homunculus's jumpstart, food use and speed are the tests'. */
    static void useTestPace() {
        NeoForge.EVENT_BUS.addListener((ServerStartedEvent event) -> {
            ServerConfig.HOMUNCULUS_JUMPSTART.set(JUMPSTART);
            ServerConfig.HOMUNCULUS_TICKS_PER_HUNGER.set(TICKS_PER_HUNGER);
            ServerConfig.HOMUNCULUS_FULL.set(FULL);
            ServerConfig.HOMUNCULUS_INTERVAL.set(INTERVAL);
            ServerConfig.HOMUNCULUS_SPEED_UP.set(SPEED_UP);
        });
    }

    static void register(MechanicalMasteryRebornGameTests.Registrar tests) {
        tests.test("homunculus_stand_is_a_life_chain_machine", 1, HomunculusTests::lifeChainMachine);
        tests.test("homunculus_vital_spark_recipe_is_in_lifes_stage_file", 1, HomunculusTests::buildGate);
        tests.test("homunculus_kindled_from_a_spark_on_a_rod_on_clay", 10, HomunculusTests::kindled);
        tests.test("homunculus_rises_on_the_free_space_nearest_the_player", 1, HomunculusTests::nearestSpace);
        tests.test("homunculus_kindling_waits_for_life", 1, HomunculusTests::kindlingWaitsForLife);
        tests.test("homunculus_kindling_needs_the_right_to_build", 1, HomunculusTests::kindlingNeedsTheRightToBuild);
        tests.test("homunculus_kindling_needs_a_free_space", 1, HomunculusTests::kindlingNeedsAFreeSpace);
        tests.test("homunculus_stand_item_places_both_halves", 1, HomunculusTests::standItem);
        tests.test("homunculus_stand_breaks_whole_and_leaves_a_statue", 20, HomunculusTests::breaksWhole);
        tests.test("homunculus_stand_takes_food_and_fe_on_both_halves", 1, HomunculusTests::bothHalves);
        tests.test("homunculus_dead_leaves_a_spent_spark_that_raises_one_empty", 30, HomunculusTests::spentSpark);
        tests.test("homunculus_statue_breaks_at_a_players_blow", 5, HomunculusTests::statueBreaks);
        tests.test("homunculus_wakes_on_a_full_jumpstart_only", 20, HomunculusTests::jumpstart);
        tests.test("homunculus_stand_takes_only_food", 1, HomunculusTests::onlyFood);
        tests.test("homunculus_stand_is_fed_by_hand_on_either_half", 1, HomunculusTests::fedByHand);
        tests.test("homunculus_eats_one_food_at_a_time", 100, HomunculusTests::eats);
        tests.test("homunculus_kindled_full_works_before_it_eats", 40, HomunculusTests::kindledFull);
        tests.test("homunculus_works_faster_on_a_better_meal", 100, HomunculusTests::speedByMeal);
        tests.test("homunculus_starved_needs_a_new_jumpstart", 140, HomunculusTests::starves);
        tests.test("homunculus_eats_only_beside_its_stand", 200, HomunculusTests::eatsOnlyBesideItsStand);
        tests.test("homunculus_heals_at_its_stand", 200, HomunculusTests::heals);
        tests.test("homunculus_walks_within_its_range", 1, HomunculusTests::range);
        tests.test("homunculus_stands_still_after_a_stop_until_life_starts_again", 100, HomunculusTests::stop);
        tests.test("homunculus_works_on_after_a_reset", 60, HomunculusTests::reset);
    }

    private static void lifeChainMachine(GameTestHelper helper) {
        helper.assertValueEqual(ChainMachines.chainOf(Homunculus.STAND_ITEM.get()), Optional.of(Handicap.LIFE), "the stand's chain");
        helper.succeed();
    }

    /** The game-test pack's stand-in for the Vital Spark's recipe is in its life stage file, so life locks it. */
    private static void buildGate(GameTestHelper helper) {
        var server = helper.getLevel().getServer();
        var id = Identifier.fromNamespaceAndPath("mechanicalmasteryreborn", "gametest/vital_spark");
        helper.assertValueEqual(ChainMachines.recipes(server).get(id), Handicap.LIFE, "the Vital Spark's recipe's chain");
        UUID team = TestTeams.claim(helper);
        helper.assertTrue(ChainMachines.table(server, team).locked(Handicap.LIFE), "the recipe unmarked before life");
        Challenges.begin(server, team, Handicap.LIFE);
        helper.assertFalse(ChainMachines.table(server, team).locked(Handicap.LIFE), "the recipe marked once life started");
        helper.succeed();
    }

    /**
     * A spark used on a lightning rod on clay strikes, harming nothing: the clay and the rod become the stand's two halves,
     * the Homunculus rises beside the base, a statue facing it, bound to it and unhurt, and the spark is used up.
     */
    private static void kindled(GameTestHelper helper) {
        playLife(helper);
        var player = Clicker.of(helper);
        player.standAt(helper, new BlockPos(4, 1, 8));
        rodOnClay(helper, BASE);
        player.click(helper, BASE.above(), Homunculus.vitalSpark(false).copyWithCount(2));
        helper.assertFalse(helper.getEntities(EntityType.LIGHTNING_BOLT).isEmpty(), "no strike on the rod");
        expectHalf(helper, BASE, DoubleBlockHalf.LOWER);
        expectHalf(helper, BASE.above(), DoubleBlockHalf.UPPER);
        var homunculus = homunculus(helper, BASE);
        helper.assertValueEqual(homunculus.blockPosition(), helper.absolutePos(SPACE), "where the Homunculus rose");
        helper.assertTrue(stand(helper, BASE).seated(), "the stand has no Homunculus");
        helper.assertValueEqual(player.getMainHandItem().getCount(), 1, "the sparks left in hand");
        helper.runAtTickTime(5, () -> {
            helper.assertTrue(homunculus.isStatue(), "a new Homunculus isn't a statue");
            float off = Math.abs(Mth.wrapDegrees(homunculus.getYRot() - 180F));
            helper.assertTrue(off < 1F, "the statue faces " + homunculus.getYRot() + ", not its stand to the north");
            helper.assertValueEqual(homunculus.getHealth(), homunculus.getMaxHealth(), "the Homunculus's health after the strike");
            for (var pos : BlockPos.betweenClosed(BASE.offset(-2, 0, -2), BASE.offset(2, 2, 2))) {
                helper.assertBlockNotPresent(Blocks.FIRE, pos.immutable());
            }
            player.leave(helper);
            helper.succeed();
        });
    }

    /** The player to the west-north-west, the space west of the base filled: it rises on the free space nearest the player. */
    private static void nearestSpace(GameTestHelper helper) {
        playLife(helper);
        var player = Clicker.of(helper);
        player.standAt(helper, new BlockPos(0, 1, 3));
        rodOnClay(helper, BASE);
        helper.setBlock(BASE.west(), Blocks.GLASS);
        player.click(helper, BASE.above(), Homunculus.vitalSpark(false));
        helper.assertValueEqual(homunculus(helper, BASE).blockPosition(), helper.absolutePos(BASE.west().north()), "where the Homunculus rose");
        player.leave(helper);
        helper.succeed();
    }

    /** Before its team plays life, a spark kindles nothing and is kept; once life starts, it kindles. */
    private static void kindlingWaitsForLife(GameTestHelper helper) {
        UUID team = TestTeams.claim(helper);
        var player = Clicker.of(helper);
        rodOnClay(helper, BASE);
        player.click(helper, BASE.above(), Homunculus.vitalSpark(false));
        expectRefused(helper, player, "locked");
        Challenges.begin(helper.getLevel().getServer(), team, Handicap.LIFE);
        player.click(helper, BASE.above(), player.getMainHandItem());
        expectHalf(helper, BASE, DoubleBlockHalf.LOWER);
        player.leave(helper);
        helper.succeed();
    }

    private static void kindlingNeedsTheRightToBuild(GameTestHelper helper) {
        playLife(helper);
        var player = Clicker.of(helper);
        player.setGameMode(GameType.ADVENTURE);
        rodOnClay(helper, BASE);
        player.click(helper, BASE.above(), Homunculus.vitalSpark(false));
        expectRefused(helper, player, "not_allowed");
        player.leave(helper);
        helper.succeed();
    }

    private static void kindlingNeedsAFreeSpace(GameTestHelper helper) {
        playLife(helper);
        var player = Clicker.of(helper);
        rodOnClay(helper, BASE);
        for (var pos : BlockPos.betweenClosed(BASE.offset(-1, 0, -1), BASE.offset(1, 0, 1))) {
            if (!pos.equals(BASE)) {
                helper.setBlock(pos.immutable(), Blocks.GLASS);
            }
        }
        player.click(helper, BASE.above(), Homunculus.vitalSpark(false));
        expectRefused(helper, player, "no_room");
        player.leave(helper);
        helper.succeed();
    }

    /**
     * The stand's item, used on the floor, places both halves; under a block it places none. Breaking the base takes the rod
     * with it, and the stand drops once.
     */
    private static void standItem(GameTestHelper helper) {
        var player = Clicker.of(helper);
        player.click(helper, BASE.below(), new ItemStack(Homunculus.STAND_ITEM.get(), 2));
        expectHalf(helper, BASE, DoubleBlockHalf.LOWER);
        expectHalf(helper, BASE.above(), DoubleBlockHalf.UPPER);
        helper.setBlock(OTHER.above(), Blocks.GLASS);
        player.click(helper, OTHER.below(), player.getMainHandItem());
        helper.assertBlockNotPresent(Homunculus.STAND.get(), OTHER);
        helper.assertValueEqual(player.getMainHandItem().getCount(), 1, "the stands left in hand");
        helper.getLevel().destroyBlock(helper.absolutePos(BASE), true);
        helper.assertBlockNotPresent(Homunculus.STAND.get(), BASE.above());
        helper.assertValueEqual(dropped(helper, BASE, Homunculus.STAND_ITEM.get()), 1, "the stands a broken base dropped");
        player.leave(helper);
        helper.succeed();
    }

    /**
     * Breaking the rod breaks the whole stand: it drops once and spills its food. Its Homunculus, awake till then, stands a
     * statue for good.
     */
    private static void breaksWhole(GameTestHelper helper) {
        var player = playerIn(helper);
        kindle(helper, player, BASE, SPACE, false);
        var homunculus = homunculus(helper, BASE);
        feed(helper, BASE.above(), Items.BREAD, 0, 3);
        charge(helper, BASE, JUMPSTART);
        helper.runAtTickTime(5, () -> {
            helper.assertFalse(homunculus.isStatue(), "a jumpstarted Homunculus is a statue");
            helper.getLevel().destroyBlock(helper.absolutePos(BASE.above()), true);
            helper.assertBlockNotPresent(Homunculus.STAND.get(), BASE);
            helper.assertValueEqual(dropped(helper, BASE, Homunculus.STAND_ITEM.get()), 1, "the stands a broken stand dropped");
            helper.assertValueEqual(dropped(helper, BASE, Items.BREAD), 3, "the bread a broken stand spilled");
        });
        helper.runAtTickTime(15, () -> {
            helper.assertTrue(homunculus.isStatue(), "a Homunculus whose stand is gone isn't a statue");
            helper.assertValueEqual(homunculus.doing(), HomunculusEntity.STANDLESS, "what a Homunculus whose stand is gone shows");
            player.leave(helper);
            helper.succeed();
        });
    }

    /** Each half takes food and, while its Homunculus sleeps, FE. */
    private static void bothHalves(GameTestHelper helper) {
        var player = playerIn(helper);
        kindle(helper, player, BASE, SPACE, false);
        helper.assertValueEqual(feed(helper, BASE, Items.BREAD, 0, 1), 1, "the bread the base took");
        helper.assertValueEqual(feed(helper, BASE.above(), Items.BREAD, 0, 1), 1, "the bread the rod took");
        helper.assertValueEqual(held(helper, BASE.above()), 2, "the bread the rod's face shows");
        helper.assertValueEqual(charge(helper, BASE, 10), 10, "the FE the base took");
        helper.assertValueEqual(charge(helper, BASE.above(), 10), 10, "the FE the rod took");
        player.leave(helper);
        helper.succeed();
    }

    /**
     * Killed, it leaves only its spark, spent, and frees its stand. The spark on the stand's base raises nothing; on its rod it
     * raises a Homunculus with an empty belly, which, woken with nothing to eat, works nothing.
     */
    private static void spentSpark(GameTestHelper helper) {
        var player = playerIn(helper);
        kindle(helper, player, BASE, SPACE, false);
        var first = homunculus(helper, BASE);
        first.kill(helper.getLevel());
        helper.assertFalse(stand(helper, BASE).seated(), "the stand of a dead Homunculus isn't free");
        var drops = drops(helper, SPACE);
        helper.assertValueEqual(drops.size(), 1, "the stacks a dead Homunculus left");
        var spark = drops.getFirst();
        helper.assertTrue(spark.is(Homunculus.VITAL_SPARK.get()) && Homunculus.spent(spark), "a dead Homunculus left " + spark + ", not a spent spark");
        player.click(helper, BASE, Direction.SOUTH, spark.copy());
        helper.assertFalse(stand(helper, BASE).seated(), "a spark on the stand's base raised a Homunculus");
        player.click(helper, BASE.above(), player.getMainHandItem());
        helper.assertTrue(stand(helper, BASE).seated(), "a spent spark on a free stand's rod raised no Homunculus");
        helper.assertTrue(player.getMainHandItem().isEmpty(), "the spark wasn't used up");
        charge(helper, BASE, JUMPSTART);
        helper.runAtTickTime(20, () -> {
            helper.assertTrue(stand(helper, BASE).awake(), "a Homunculus from a spent spark with no food waits awake");
            helper.assertValueEqual(stand(helper, BASE).works(), 0, "the works of a Homunculus with an empty belly");
            player.leave(helper);
            helper.succeed();
        });
    }

    /** A sleeping Homunculus is a statue that one blow from a player breaks; it leaves its spark, spent. */
    private static void statueBreaks(GameTestHelper helper) {
        var player = playerIn(helper);
        kindle(helper, player, BASE, SPACE, false);
        var homunculus = homunculus(helper, BASE);
        helper.runAtTickTime(2, () -> {
            player.attack(homunculus);
            helper.assertFalse(homunculus.isAlive(), "a statue stood a player's blow");
            var drops = drops(helper, SPACE);
            helper.assertTrue(drops.size() == 1 && Homunculus.spent(drops.getFirst()), "a broken statue left " + drops);
            player.leave(helper);
            helper.succeed();
        });
    }

    /** Its stand takes a jumpstart's worth of FE while it sleeps, wakes it only once full, then takes none. */
    private static void jumpstart(GameTestHelper helper) {
        var player = playerIn(helper);
        kindle(helper, player, BASE, SPACE, false);
        var homunculus = homunculus(helper, BASE);
        helper.assertValueEqual(charge(helper, BASE, JUMPSTART - 1), JUMPSTART - 1, "the FE a sleeping Homunculus's stand took");
        helper.runAtTickTime(5, () -> {
            helper.assertFalse(stand(helper, BASE).awake(), "a Homunculus woke short of its jumpstart");
            helper.assertTrue(homunculus.isStatue(), "a sleeping Homunculus isn't a statue");
            helper.assertValueEqual(charge(helper, BASE, JUMPSTART), 1, "the FE that completes the jumpstart");
        });
        helper.runAtTickTime(10, () -> {
            helper.assertTrue(stand(helper, BASE).awake(), "a jumpstarted Homunculus sleeps");
            helper.assertFalse(homunculus.isStatue(), "an awake Homunculus is a statue");
            helper.assertValueEqual(charge(helper, BASE, JUMPSTART), 0, "the FE an awake Homunculus's stand took");
            player.leave(helper);
            helper.succeed();
        });
    }

    private static void onlyFood(GameTestHelper helper) {
        placeStand(helper, BASE);
        helper.assertValueEqual(feed(helper, BASE, Items.STONE, 0, 1), 0, "the stone the stand took");
        helper.assertValueEqual(feed(helper, BASE, Items.ROTTEN_FLESH, 0, 1), 0, "the rotten flesh the stand took");
        helper.assertValueEqual(feed(helper, BASE, Items.BREAD, 0, 3), 3, "the bread the stand took");
        helper.assertValueEqual(charge(helper, BASE, JUMPSTART), 0, "the FE a stand with no Homunculus took");
        try (var transaction = Transaction.openRoot()) {
            helper.assertValueEqual(food(helper, BASE).extract(ItemResource.of(Items.BREAD), 3, transaction), 0, "the bread taken from the stand");
        }
        helper.succeed();
    }

    /** A player's right-click with food puts the held stack in the stand, on either half; with anything else, nothing. */
    private static void fedByHand(GameTestHelper helper) {
        placeStand(helper, BASE);
        var player = Clicker.of(helper);
        player.click(helper, BASE, Direction.SOUTH, new ItemStack(Items.BREAD, 3));
        helper.assertTrue(player.getMainHandItem().isEmpty(), "bread left in hand after feeding the base");
        player.click(helper, BASE.above(), Direction.SOUTH, new ItemStack(Items.COOKED_BEEF, 2));
        helper.assertTrue(player.getMainHandItem().isEmpty(), "beef left in hand after feeding the rod");
        player.click(helper, BASE, Direction.SOUTH, new ItemStack(Items.ROTTEN_FLESH, 2));
        helper.assertValueEqual(player.getMainHandItem().getCount(), 2, "the rotten flesh left in hand");
        helper.assertValueEqual(held(helper, BASE), 5, "the food a player fed the stand");
        player.leave(helper);
        helper.succeed();
    }

    /** Awake, it eats one food at once and the next once the first is used up: a mushroom stew lasts 60 ticks. */
    private static void eats(GameTestHelper helper) {
        var player = awake(helper, BASE, SPACE);
        feed(helper, BASE, Items.MUSHROOM_STEW, 0, 3);
        helper.runAtTickTime(30, () -> helper.assertValueEqual(held(helper, BASE), 2, "the stews left while it eats the first"));
        helper.runAtTickTime(90, () -> {
            helper.assertValueEqual(held(helper, BASE), 1, "the stews left while it eats the second");
            player.leave(helper);
            helper.succeed();
        });
    }

    /** Kindled full, it works its belly off with nothing to eat, then falls asleep, having never eaten from the stand. */
    private static void kindledFull(GameTestHelper helper) {
        var player = awake(helper, BASE, SPACE);
        helper.runAtTickTime(15, () -> {
            helper.assertTrue(stand(helper, BASE).awake(), "a new Homunculus fell asleep on its full belly");
            helper.assertTrue(stand(helper, BASE).works() > 0, "no works on a new Homunculus's full belly");
        });
        helper.runAtTickTime(30, () -> {
            helper.assertFalse(stand(helper, BASE).awake(), "a Homunculus stays awake past its full belly with nothing to eat");
            helper.assertTrue(homunculus(helper, BASE).isStatue(), "a Homunculus asleep again isn't a statue");
            player.leave(helper);
            helper.succeed();
        });
    }

    /** Past their full bellies, over the same 48 ticks, one on a plain stew works every 12 ticks and one on a diamond stew every 3. */
    private static void speedByMeal(GameTestHelper helper) {
        var player = awake(helper, FIRST, FIRST.south());
        kindle(helper, player, SECOND, SECOND.south(), false);
        charge(helper, SECOND, JUMPSTART);
        feed(helper, FIRST, Items.MUSHROOM_STEW, 0, 1);
        feed(helper, SECOND, Items.MUSHROOM_STEW, 3, 1);
        int[] before = new int[2];
        helper.runAtTickTime(30, () -> {
            before[0] = stand(helper, FIRST).works();
            before[1] = stand(helper, SECOND).works();
        });
        helper.runAtTickTime(78, () -> {
            int plain = stand(helper, FIRST).works() - before[0];
            int diamond = stand(helper, SECOND).works() - before[1];
            helper.assertTrue(plain >= 3 && plain <= 5, "a Homunculus on a plain stew worked " + plain + " times, not 4");
            helper.assertTrue(diamond >= 15 && diamond <= 17, "a Homunculus on a diamond stew worked " + diamond + " times, not 16");
            player.leave(helper);
            helper.succeed();
        });
    }

    /** Its full belly and a bread last 70 ticks: then it falls asleep, eats no food given it, and wakes only on a new jumpstart. */
    private static void starves(GameTestHelper helper) {
        var player = awake(helper, BASE, SPACE);
        helper.runAtTickTime(2, () -> feed(helper, BASE, Items.BREAD, 0, 1));
        helper.runAtTickTime(85, () -> {
            helper.assertFalse(stand(helper, BASE).awake(), "a starved Homunculus stays awake");
            feed(helper, BASE, Items.BREAD, 0, 1);
        });
        helper.runAtTickTime(105, () -> {
            helper.assertValueEqual(held(helper, BASE), 1, "the bread a starved Homunculus ate");
            helper.assertFalse(stand(helper, BASE).awake(), "food alone woke a starved Homunculus");
            helper.assertValueEqual(charge(helper, BASE, JUMPSTART), JUMPSTART, "the FE a starved Homunculus's stand took");
        });
        helper.runAtTickTime(110, () -> {
            helper.assertTrue(stand(helper, BASE).awake(), "a new jumpstart didn't wake a starved Homunculus");
            helper.assertValueEqual(held(helper, BASE), 0, "the bread a newly woken Homunculus left");
            player.leave(helper);
            helper.succeed();
        });
    }

    /**
     * Penned in a cell away from its stand, it works off its full belly and then eats nothing, and doesn't fall asleep; let out, it walks
     * back to the space by the base and eats.
     */
    private static void eatsOnlyBesideItsStand(GameTestHelper helper) {
        var space = BASE.north();
        var player = playerIn(helper);
        kindle(helper, player, BASE, space, false);
        var homunculus = homunculus(helper, BASE);
        // A cell of glass north of the cage, shut off from the space by the cage's wall.
        var cell = new BlockPos(4, 1, 1);
        for (int up = 0; up < 4; up++) {
            for (var wall : List.of(cell.west(), cell.east(), cell.north())) {
                helper.setBlock(wall.above(up), Blocks.GLASS);
            }
        }
        var pen = helper.absolutePos(cell);
        homunculus.teleportTo(pen.getX() + 0.5, pen.getY(), pen.getZ() + 0.5);
        charge(helper, BASE, JUMPSTART);
        feed(helper, BASE, Items.MUSHROOM_STEW, 0, 1);
        helper.runAtTickTime(60, () -> {
            helper.assertValueEqual(held(helper, BASE), 1, "the stews a Homunculus away from its stand ate");
            helper.assertTrue(stand(helper, BASE).awake(), "a Homunculus away from its stand fell asleep");
            for (int up = 0; up < 4; up++) {
                helper.setBlock(space.north().above(up), Blocks.AIR);
            }
        });
        helper.succeedWhen(() -> {
            helper.assertTrue(helper.getTick() > 60, "not yet let out");
            helper.assertValueEqual(held(helper, BASE), 0, "the stews a Homunculus let out ate");
            helper.assertTrue(homunculus.atStand(), "a Homunculus ate away from its stand");
            player.leave(helper);
        });
    }

    /** Hurt while awake at its stand, it heals 1 health every 40 ticks until it is whole. */
    private static void heals(GameTestHelper helper) {
        var player = awake(helper, BASE, SPACE);
        feed(helper, BASE, Items.MUSHROOM_STEW, 0, 4);
        var homunculus = homunculus(helper, BASE);
        helper.runAtTickTime(5, () -> homunculus.hurtServer(helper.getLevel(), helper.getLevel().damageSources().generic(), 4F));
        helper.runAtTickTime(100, () -> {
            float health = homunculus.getHealth();
            helper.assertTrue(health == 10F, "after 95 ticks a Homunculus 4 short has " + health + " health, not 10");
        });
        helper.runAtTickTime(190, () -> {
            helper.assertValueEqual(homunculus.getHealth(), homunculus.getMaxHealth(), "a healed Homunculus's health");
            player.leave(helper);
            helper.succeed();
        });
    }

    /** It walks no more than 8 blocks across from its stand, nor 4 up or down. */
    private static void range(GameTestHelper helper) {
        var player = playerIn(helper);
        kindle(helper, player, BASE, SPACE, false);
        var homunculus = homunculus(helper, BASE);
        var base = helper.absolutePos(BASE);
        helper.assertTrue(homunculus.isWithinHome(base.east(7)), "7 across is out of range");
        helper.assertFalse(homunculus.isWithinHome(base.east(9)), "9 across is in range");
        helper.assertTrue(homunculus.isWithinHome(base.above(4)), "4 up is out of range");
        helper.assertFalse(homunculus.isWithinHome(base.below(5)), "5 down is in range");
        player.leave(helper);
        helper.succeed();
    }

    /** A stop leaves it a statue, awake and eating nothing; a new start sets it working again with no new jumpstart. */
    private static void stop(GameTestHelper helper) {
        UUID team = playLife(helper);
        var server = helper.getLevel().getServer();
        var player = Clicker.of(helper);
        kindle(helper, player, BASE, SPACE, false);
        charge(helper, BASE, JUMPSTART);
        feed(helper, BASE, Items.MUSHROOM_STEW, 0, 3);
        var homunculus = homunculus(helper, BASE);
        int[] works = new int[1];
        helper.runAtTickTime(30, () -> {
            Challenges.stop(server, team);
            works[0] = stand(helper, BASE).works();
        });
        helper.runAtTickTime(80, () -> {
            helper.assertValueEqual(stand(helper, BASE).works(), works[0], "the works done after a stop");
            helper.assertValueEqual(held(helper, BASE), 2, "the stews left after a stop");
            helper.assertValueEqual(stand(helper, BASE).doing(), Doing.LOCKED, "what a stopped Homunculus does");
            helper.assertTrue(homunculus.isStatue(), "a stopped Homunculus isn't a statue");
            Challenges.begin(server, team, Handicap.LIFE);
        });
        helper.runAtTickTime(95, () -> {
            helper.assertTrue(stand(helper, BASE).works() > works[0], "no works once life started again");
            helper.assertFalse(homunculus.isStatue(), "a Homunculus started again is a statue");
            player.leave(helper);
            helper.succeed();
        });
    }

    /** A reset keeps life's chain unlocked: it works on. */
    private static void reset(GameTestHelper helper) {
        UUID team = playLife(helper);
        var player = Clicker.of(helper);
        kindle(helper, player, BASE, SPACE, false);
        charge(helper, BASE, JUMPSTART);
        feed(helper, BASE, Items.MUSHROOM_STEW, 0, 1);
        int[] works = new int[1];
        helper.runAtTickTime(15, () -> {
            Challenges.reset(helper.getLevel().getServer(), team);
            works[0] = stand(helper, BASE).works();
        });
        helper.runAtTickTime(45, () -> {
            helper.assertTrue(stand(helper, BASE).awake(), "a reset put the Homunculus to sleep");
            helper.assertTrue(stand(helper, BASE).works() > works[0], "no works past a reset");
            player.leave(helper);
            helper.succeed();
        });
    }

    private static UUID playLife(GameTestHelper helper) {
        UUID team = TestTeams.claim(helper);
        Challenges.begin(helper.getLevel().getServer(), team, Handicap.LIFE);
        return team;
    }

    /** A survival player, on an island whose team plays life. */
    private static Clicker playerIn(GameTestHelper helper) {
        playLife(helper);
        return Clicker.of(helper);
    }

    private static void rodOnClay(GameTestHelper helper, BlockPos base) {
        helper.setBlock(base, Blocks.CLAY);
        helper.setBlock(base.above(), Blocks.LIGHTNING_ROD);
    }

    /**
     * A Homunculus kindled by the player from a rod on clay at {@code base}, glass four blocks high round every space within two
     * of the base but {@code space}, so it rises there and stays: too high to climb even from the top of the stand's base.
     */
    private static void kindle(GameTestHelper helper, Clicker player, BlockPos base, BlockPos space, boolean spent) {
        for (var pos : BlockPos.betweenClosed(base.offset(-2, 0, -2), base.offset(2, 3, 2))) {
            boolean column = pos.getX() == base.getX() && pos.getZ() == base.getZ();
            boolean free = pos.getX() == space.getX() && pos.getZ() == space.getZ();
            if (!column && !free && helper.getBlockState(pos).isAir()) {
                helper.setBlock(pos.immutable(), Blocks.GLASS);
            }
        }
        rodOnClay(helper, base);
        player.click(helper, base.above(), Homunculus.vitalSpark(spent));
        helper.assertValueEqual(homunculus(helper, base).blockPosition(), helper.absolutePos(space), "where the Homunculus rose");
    }

    /** A Homunculus kindled at {@code base}, rising on {@code space}, and jumpstarted, which wakes it on its stand's next tick. */
    private static Clicker awake(GameTestHelper helper, BlockPos base, BlockPos space) {
        var player = playerIn(helper);
        kindle(helper, player, base, space, false);
        helper.assertValueEqual(charge(helper, base, JUMPSTART), JUMPSTART, "the jumpstart's FE");
        return player;
    }

    private static void placeStand(GameTestHelper helper, BlockPos base) {
        var lower = Homunculus.STAND.get().defaultBlockState();
        helper.setBlock(base, lower);
        helper.setBlock(base.above(), lower.setValue(HomunculusStandBlock.HALF, DoubleBlockHalf.UPPER));
    }

    private static void expectHalf(GameTestHelper helper, BlockPos pos, DoubleBlockHalf half) {
        helper.assertBlockPresent(Homunculus.STAND.get(), pos);
        helper.assertValueEqual(helper.getBlockState(pos).getValue(HomunculusStandBlock.HALF), half, "the stand's half at " + pos);
    }

    /** The player was told why, on the action bar, and the rod, the clay and the spark are as they were, with no Homunculus. */
    private static void expectRefused(GameTestHelper helper, Clicker player, String why) {
        helper.assertValueEqual(player.told(), List.of(TOLD + why), "what the action bar said");
        helper.assertBlockPresent(Blocks.CLAY, BASE);
        helper.assertBlockPresent(Blocks.LIGHTNING_ROD, BASE.above());
        helper.assertValueEqual(player.getMainHandItem().getCount(), 1, "the sparks left in hand");
        helper.assertTrue(helper.getEntities(Homunculus.HOMUNCULUS.get()).isEmpty(), "a refused spark raised a Homunculus");
    }

    /** The Homunculus bound to the stand at {@code base}. */
    private static HomunculusEntity homunculus(GameTestHelper helper, BlockPos base) {
        var absolute = helper.absolutePos(base);
        var found = helper.getEntities(Homunculus.HOMUNCULUS.get()).stream().filter(entity -> absolute.equals(entity.stand())).toList();
        helper.assertValueEqual(found.size(), 1, "the Homunculi bound to the stand at " + base);
        return found.getFirst();
    }

    private static HomunculusStandBlockEntity stand(GameTestHelper helper, BlockPos pos) {
        return helper.getBlockEntity(pos, HomunculusStandBlockEntity.class);
    }

    /** The stacks lying within two blocks of {@code pos}. */
    private static List<ItemStack> drops(GameTestHelper helper, BlockPos pos) {
        return helper.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(helper.absolutePos(pos)).inflate(2)).stream()
                .map(ItemEntity::getItem).toList();
    }

    /** How many of {@code item} lie within two blocks of {@code pos}. */
    private static int dropped(GameTestHelper helper, BlockPos pos, Item item) {
        return drops(helper, pos).stream().filter(stack -> stack.is(item)).mapToInt(ItemStack::getCount).sum();
    }

    /** Offers the stand's half at {@code pos} {@code amount} FE from above and says how much it took: none while it has no FE face. */
    private static int charge(GameTestHelper helper, BlockPos pos, int amount) {
        EnergyHandler store = helper.getLevel().getCapability(Capabilities.Energy.BLOCK, helper.absolutePos(pos), Direction.UP);
        if (store == null) {
            return 0;
        }
        try (var transaction = Transaction.openRoot()) {
            int taken = store.insert(amount, transaction);
            transaction.commit();
            return taken;
        }
    }

    /** Offers the stand's half at {@code pos} {@code count} of {@code item} at quality {@code level}, and says how many it took. */
    private static int feed(GameTestHelper helper, BlockPos pos, Item item, int level, int count) {
        ItemStack stack = new ItemStack(item);
        if (level > 0) {
            stack.set(QualityFoodStandIn.QUALITY.get(), new QualityFoodStandIn.Quality(DIAMOND, level));
        }
        int taken = 0;
        for (int one = 0; one < count; one++) {
            try (var transaction = Transaction.openRoot()) {
                taken += food(helper, pos).insert(ItemResource.of(stack), 1, transaction);
                transaction.commit();
            }
        }
        return taken;
    }

    /** How many foods the stand holds. */
    private static int held(GameTestHelper helper, BlockPos pos) {
        var food = food(helper, pos);
        int held = 0;
        for (int slot = 0; slot < food.size(); slot++) {
            held += food.getAmountAsInt(slot);
        }
        return held;
    }

    private static ResourceHandler<ItemResource> food(GameTestHelper helper, BlockPos pos) {
        var food = helper.getLevel().getCapability(Capabilities.Item.BLOCK, helper.absolutePos(pos), Direction.UP);
        helper.assertTrue(food != null, "the stand has no item face");
        return food;
    }
}
