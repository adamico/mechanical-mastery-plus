// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.gametest;

import java.util.Optional;
import java.util.UUID;

import io.github._5thlayer.mechanicalmasteryreborn.ServerConfig;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.ChainMachines;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.Challenges;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.Handicap;
import io.github._5thlayer.mechanicalmasteryreborn.life.Homunculus;
import io.github._5thlayer.mechanicalmasteryreborn.life.HomunculusStandBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

/**
 * The Homunculus at its stand (#367). The tests play at a pace of their own, set through the server config as the server
 * starts ({@link #useTestPace}): a jumpstart of 1000 FE, ten ticks of work for each point of hunger, so a new Homunculus's full
 * belly (2) lasts 20 ticks, a bread (5) 50 and a mushroom stew (6) 60, and a work every 12 ticks on a food of no quality, every 3 on a diamond one.
 */
final class HomunculusTests {

    static final int JUMPSTART = 1000;
    static final int TICKS_PER_HUNGER = 10;
    static final int FULL = 2;
    static final int INTERVAL = 12;
    static final double SPEED_UP = 1.0;

    private static final Identifier DIAMOND = Identifier.fromNamespaceAndPath("quality_food", "diamond");

    private static final BlockPos AT = new BlockPos(2, 2, 2);
    private static final BlockPos OTHER = new BlockPos(4, 2, 2);

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
        tests.test("homunculus_build_recipe_is_in_lifes_stage_file", 1, HomunculusTests::buildGate);
        tests.test("homunculus_is_built_by_using_one_on_its_stand", 1, HomunculusTests::built);
        tests.test("homunculus_taken_off_its_stand_once_woken_is_built_again_empty", 20, HomunculusTests::rebuiltSpent);
        tests.test("homunculus_wakes_on_a_full_jumpstart_only", 20, HomunculusTests::jumpstart);
        tests.test("homunculus_stand_takes_only_food", 1, HomunculusTests::onlyFood);
        tests.test("homunculus_eats_one_food_at_a_time", 100, HomunculusTests::eats);
        tests.test("homunculus_built_full_works_before_it_eats", 40, HomunculusTests::builtFull);
        tests.test("homunculus_works_faster_on_a_better_meal", 100, HomunculusTests::speedByMeal);
        tests.test("homunculus_starved_needs_a_new_jumpstart", 140, HomunculusTests::starves);
        tests.test("homunculus_idle_until_life_starts", 60, HomunculusTests::untilLifeStarts);
        tests.test("homunculus_stands_still_after_a_stop_until_life_starts_again", 100, HomunculusTests::stop);
        tests.test("homunculus_works_on_after_a_reset", 60, HomunculusTests::reset);
    }

    private static void lifeChainMachine(GameTestHelper helper) {
        helper.assertValueEqual(ChainMachines.chainOf(Homunculus.STAND_ITEM.get()), Optional.of(Handicap.LIFE), "the stand's chain");
        helper.succeed();
    }

    /** The game-test pack's stand-in for the pack's build recipe is in its life stage file, so life locks it. */
    private static void buildGate(GameTestHelper helper) {
        var server = helper.getLevel().getServer();
        var id = Identifier.fromNamespaceAndPath("mechanicalmasteryreborn", "gametest/homunculus");
        helper.assertValueEqual(ChainMachines.recipes(server).get(id), Handicap.LIFE, "the Homunculus's build recipe's chain");
        UUID team = TestTeams.claim(helper);
        helper.assertTrue(ChainMachines.table(server, team).locked(Handicap.LIFE), "the build recipe unmarked before life");
        Challenges.begin(server, team, Handicap.LIFE);
        helper.assertFalse(ChainMachines.table(server, team).locked(Handicap.LIFE), "the build recipe marked once life started");
        helper.succeed();
    }

    /** Used on an empty stand, a Homunculus takes its place there, asleep; a seated stand takes no other. */
    private static void built(GameTestHelper helper) {
        helper.setBlock(AT, Homunculus.STAND.get());
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Homunculus.ITEM.get(), 2));
        helper.useBlock(AT, player);
        helper.assertTrue(stand(helper, AT).seated(), "no Homunculus at the stand it was used on");
        helper.assertFalse(stand(helper, AT).awake(), "a new Homunculus is awake before its jumpstart");
        helper.assertValueEqual(player.getMainHandItem().getCount(), 1, "the Homunculi left in hand once one was built");
        helper.useBlock(AT, player);
        helper.assertValueEqual(player.getMainHandItem().getCount(), 1, "the Homunculi left in hand past a seated stand");
        helper.succeed();
    }

    /** Broken off its stand once woken, it drops spent, and is built again with an empty belly: woken again, it waits for food, working nothing. */
    private static void rebuiltSpent(GameTestHelper helper) {
        playLife(helper);
        awake(helper, AT);
        helper.runAtTickTime(3, () -> {
            helper.destroyBlock(AT);
            var dropped = helper.getLevel().getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,
                    new net.minecraft.world.phys.AABB(helper.absolutePos(AT)).inflate(2)).stream()
                    .map(entity -> entity.getItem()).filter(stack -> stack.is(Homunculus.ITEM.get())).findFirst();
            helper.assertTrue(dropped.isPresent(), "no Homunculus dropped from its broken stand");
            helper.assertTrue(Homunculus.spent(dropped.get()), "a woken Homunculus dropped unspent");
            helper.setBlock(AT, Homunculus.STAND.get());
            stand(helper, AT).seat(Homunculus.spent(dropped.get()));
            charge(helper, AT, JUMPSTART);
        });
        helper.runAtTickTime(10, () -> {
            helper.assertTrue(stand(helper, AT).awake(), "a spent Homunculus with no food waits awake");
            helper.assertValueEqual(stand(helper, AT).works(), 0, "the works of a spent Homunculus with an empty belly");
            helper.succeed();
        });
    }

    /** An empty stand takes no FE; a seated one takes a jumpstart's worth, wakes only once it is full, then takes none. */
    private static void jumpstart(GameTestHelper helper) {
        helper.setBlock(AT, Homunculus.STAND.get());
        helper.assertValueEqual(charge(helper, AT, JUMPSTART), 0, "the FE an empty stand took");
        seat(helper, AT);
        helper.assertValueEqual(charge(helper, AT, JUMPSTART - 1), JUMPSTART - 1, "the FE a sleeping Homunculus's stand took");
        helper.runAtTickTime(5, () -> {
            helper.assertFalse(stand(helper, AT).awake(), "a Homunculus woke short of its jumpstart");
            helper.assertValueEqual(charge(helper, AT, JUMPSTART), 1, "the FE that completes the jumpstart");
        });
        helper.runAtTickTime(10, () -> {
            helper.assertTrue(stand(helper, AT).awake(), "a jumpstarted Homunculus sleeps");
            helper.assertValueEqual(charge(helper, AT, JUMPSTART), 0, "the FE an awake Homunculus's stand took");
            helper.succeed();
        });
    }

    private static void onlyFood(GameTestHelper helper) {
        helper.setBlock(AT, Homunculus.STAND.get());
        helper.assertValueEqual(feed(helper, AT, Items.STONE, 0, 1), 0, "the stone the stand took");
        helper.assertValueEqual(feed(helper, AT, Items.ROTTEN_FLESH, 0, 1), 0, "the rotten flesh the stand took");
        helper.assertValueEqual(feed(helper, AT, Items.BREAD, 0, 3), 3, "the bread the stand took");
        try (var transaction = Transaction.openRoot()) {
            helper.assertValueEqual(food(helper, AT).extract(ItemResource.of(Items.BREAD), 3, transaction), 0, "the bread taken from the stand");
        }
        helper.succeed();
    }

    /** Awake, it eats one food at once and the next once the first is used up: a mushroom stew lasts 60 ticks. */
    private static void eats(GameTestHelper helper) {
        playLife(helper);
        awake(helper, AT);
        feed(helper, AT, Items.MUSHROOM_STEW, 0, 3);
        helper.runAtTickTime(30, () -> helper.assertValueEqual(held(helper, AT), 2, "the stews left while it eats the first"));
        helper.runAtTickTime(90, () -> {
            helper.assertValueEqual(held(helper, AT), 1, "the stews left while it eats the second");
            helper.succeed();
        });
    }

    /** Built full, it works its belly off with nothing to eat, then starves, having never eaten from the stand. */
    private static void builtFull(GameTestHelper helper) {
        playLife(helper);
        awake(helper, AT);
        helper.runAtTickTime(15, () -> {
            helper.assertTrue(stand(helper, AT).awake(), "a new Homunculus fell asleep on its full belly");
            helper.assertTrue(stand(helper, AT).works() > 0, "no works on a new Homunculus's full belly");
        });
        helper.runAtTickTime(30, () -> {
            helper.assertFalse(stand(helper, AT).awake(), "a Homunculus stays awake past its full belly with nothing to eat");
            helper.succeed();
        });
    }

    /** Past their full bellies, over the same 48 ticks, one on a plain stew works every 12 ticks and one on a diamond stew every 3. */
    private static void speedByMeal(GameTestHelper helper) {
        playLife(helper);
        awake(helper, AT);
        awake(helper, OTHER);
        feed(helper, AT, Items.MUSHROOM_STEW, 0, 1);
        feed(helper, OTHER, Items.MUSHROOM_STEW, 3, 1);
        int[] before = new int[2];
        helper.runAtTickTime(30, () -> {
            before[0] = stand(helper, AT).works();
            before[1] = stand(helper, OTHER).works();
        });
        helper.runAtTickTime(78, () -> {
            int plain = stand(helper, AT).works() - before[0];
            int diamond = stand(helper, OTHER).works() - before[1];
            helper.assertTrue(plain >= 3 && plain <= 5, "a Homunculus on a plain stew worked " + plain + " times, not 4");
            helper.assertTrue(diamond >= 15 && diamond <= 17, "a Homunculus on a diamond stew worked " + diamond + " times, not 16");
            helper.succeed();
        });
    }

    /** Its full belly and a bread last 70 ticks: then it falls asleep, eats no food given it, and wakes only on a new jumpstart. */
    private static void starves(GameTestHelper helper) {
        playLife(helper);
        awake(helper, AT);
        helper.runAtTickTime(2, () -> feed(helper, AT, Items.BREAD, 0, 1));
        helper.runAtTickTime(85, () -> {
            helper.assertFalse(stand(helper, AT).awake(), "a starved Homunculus stays awake");
            feed(helper, AT, Items.BREAD, 0, 1);
        });
        helper.runAtTickTime(105, () -> {
            helper.assertValueEqual(held(helper, AT), 1, "the bread a starved Homunculus ate");
            helper.assertFalse(stand(helper, AT).awake(), "food alone woke a starved Homunculus");
            helper.assertValueEqual(charge(helper, AT, JUMPSTART), JUMPSTART, "the FE a starved Homunculus's stand took");
        });
        helper.runAtTickTime(110, () -> {
            helper.assertTrue(stand(helper, AT).awake(), "a new jumpstart didn't wake a starved Homunculus");
            helper.assertValueEqual(held(helper, AT), 0, "the bread a newly woken Homunculus left");
            helper.succeed();
        });
    }

    /** Jumpstarted and fed before its team starts life, it neither eats nor works; once life starts it does. */
    private static void untilLifeStarts(GameTestHelper helper) {
        UUID team = TestTeams.claim(helper);
        awake(helper, AT);
        feed(helper, AT, Items.MUSHROOM_STEW, 0, 1);
        helper.runAtTickTime(15, () -> {
            helper.assertValueEqual(stand(helper, AT).works(), 0, "the works done before life");
            helper.assertValueEqual(held(helper, AT), 1, "the stews left before life");
            Challenges.begin(helper.getLevel().getServer(), team, Handicap.LIFE);
        });
        helper.runAtTickTime(45, () -> {
            helper.assertValueEqual(held(helper, AT), 0, "the stews left once life started");
            helper.assertTrue(stand(helper, AT).works() > 0, "no works once life started");
            helper.succeed();
        });
    }

    /** A stop leaves it still and awake, eating nothing; a new start sets it working again with no new jumpstart. */
    private static void stop(GameTestHelper helper) {
        UUID team = playLife(helper);
        var server = helper.getLevel().getServer();
        awake(helper, AT);
        feed(helper, AT, Items.MUSHROOM_STEW, 0, 3);
        int[] works = new int[1];
        helper.runAtTickTime(30, () -> {
            Challenges.stop(server, team);
            works[0] = stand(helper, AT).works();
        });
        helper.runAtTickTime(80, () -> {
            helper.assertValueEqual(stand(helper, AT).works(), works[0], "the works done after a stop");
            helper.assertValueEqual(held(helper, AT), 2, "the stews left after a stop");
            helper.assertTrue(stand(helper, AT).awake(), "a stop put the Homunculus to sleep");
            Challenges.begin(server, team, Handicap.LIFE);
        });
        helper.runAtTickTime(95, () -> {
            helper.assertTrue(stand(helper, AT).works() > works[0], "no works once life started again");
            helper.succeed();
        });
    }

    /** A reset keeps life's chain unlocked: it works on. */
    private static void reset(GameTestHelper helper) {
        UUID team = playLife(helper);
        awake(helper, AT);
        feed(helper, AT, Items.MUSHROOM_STEW, 0, 1);
        int[] works = new int[1];
        helper.runAtTickTime(15, () -> {
            Challenges.reset(helper.getLevel().getServer(), team);
            works[0] = stand(helper, AT).works();
        });
        helper.runAtTickTime(45, () -> {
            helper.assertTrue(stand(helper, AT).awake(), "a reset put the Homunculus to sleep");
            helper.assertTrue(stand(helper, AT).works() > works[0], "no works past a reset");
            helper.succeed();
        });
    }

    private static UUID playLife(GameTestHelper helper) {
        UUID team = TestTeams.claim(helper);
        Challenges.begin(helper.getLevel().getServer(), team, Handicap.LIFE);
        return team;
    }

    private static void seat(GameTestHelper helper, BlockPos pos) {
        stand(helper, pos).seat();
    }

    /** Places a stand at {@code pos}, builds a Homunculus at it and jumpstarts it, which wakes it on its next tick. */
    private static void awake(GameTestHelper helper, BlockPos pos) {
        helper.setBlock(pos, Homunculus.STAND.get());
        seat(helper, pos);
        helper.assertValueEqual(charge(helper, pos, JUMPSTART), JUMPSTART, "the jumpstart's FE");
    }

    private static HomunculusStandBlockEntity stand(GameTestHelper helper, BlockPos pos) {
        return helper.getBlockEntity(pos, HomunculusStandBlockEntity.class);
    }

    /** Offers the stand {@code amount} FE from above and says how much it took. */
    private static int charge(GameTestHelper helper, BlockPos pos, int amount) {
        EnergyHandler store = helper.getLevel().getCapability(Capabilities.Energy.BLOCK, helper.absolutePos(pos), Direction.UP);
        helper.assertTrue(store != null, "the stand has no energy face");
        try (var transaction = Transaction.openRoot()) {
            int taken = store.insert(amount, transaction);
            transaction.commit();
            return taken;
        }
    }

    /** Offers the stand {@code count} of {@code item} at quality {@code level} from above, and says how many it took. */
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
