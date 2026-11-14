// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.gametest;

import java.util.Optional;
import java.util.UUID;

import io.github._5thlayer.mechanicalmasteryreborn.ServerConfig;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.ChainMachines;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.Challenges;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.Handicap;
import io.github._5thlayer.mechanicalmasteryreborn.essence.Attempt;
import io.github._5thlayer.mechanicalmasteryreborn.essence.Attempts;
import io.github._5thlayer.mechanicalmasteryreborn.essence.Essence;
import io.github._5thlayer.mechanicalmasteryreborn.essence.Essences;
import io.github._5thlayer.mechanicalmasteryreborn.life.Batch;
import io.github._5thlayer.mechanicalmasteryreborn.life.Pelican;
import io.github._5thlayer.mechanicalmasteryreborn.life.PelicanBlock;
import io.github._5thlayer.mechanicalmasteryreborn.life.PelicanBlockEntity;
import io.github._5thlayer.mechanicalmasteryreborn.record.TeamState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

/**
 * The Pelican, which makes the Vital Essence by eating meals (#366). The game-test pack's meals are vanilla's stews and
 * soups, in its {@code spoil_classes/meal} tag: the mushroom stew, beetroot soup and suspicious stew feed 7.2 saturation,
 * the rabbit stew 12. The tests play at thresholds of their own, set through the server config as the server starts
 * ({@link #useTestThresholds}), not the defaults: so each is configurable, and a test digests in two seconds.
 */
final class PelicanTests {

    /** The thresholds and digesting time the tests play at. */
    static final double SATURATION = 30;
    static final int KINDS = 3;
    static final double QUALITY = 1.5;
    static final int DIGESTING = 40;
    private static final int MARGIN = 5;

    private static final Identifier IRON = Identifier.fromNamespaceAndPath("quality_food", "iron");
    private static final Identifier GOLD = Identifier.fromNamespaceAndPath("quality_food", "gold");
    private static final Identifier DIAMOND = Identifier.fromNamespaceAndPath("quality_food", "diamond");

    private static final BlockPos AT = new BlockPos(2, 2, 2);

    private PelicanTests() {
    }

    /** From the server's start, the Pelican's thresholds and digesting time are the tests'. */
    static void useTestThresholds() {
        NeoForge.EVENT_BUS.addListener((ServerStartedEvent event) -> {
            ServerConfig.PELICAN_SATURATION.set(SATURATION);
            ServerConfig.PELICAN_KINDS.set(KINDS);
            ServerConfig.PELICAN_QUALITY.set(QUALITY);
            ServerConfig.PELICAN_DIGESTING.set(DIGESTING);
        });
    }

    static void register(MechanicalMasteryRebornGameTests.Registrar tests) {
        tests.test("pelican_plays_at_the_configured_thresholds", 1, PelicanTests::configured);
        tests.test("pelican_is_a_life_chain_machine", 1, PelicanTests::lifeChainMachine);
        tests.test("pelican_refuses_anything_but_a_meal", 1, PelicanTests::onlyMeals);
        tests.test("pelican_waits_for_enough_saturation", 100, PelicanTests::shortOfSaturation);
        tests.test("pelican_waits_for_enough_kinds", 100, PelicanTests::shortOfKinds);
        tests.test("pelican_waits_for_a_high_enough_quality", 100, PelicanTests::shortOfQuality);
        tests.test("pelican_digests_a_ready_batch_into_a_vital_essence", 100, PelicanTests::digests);
        tests.test("pelican_refuses_meals_while_its_output_is_full", 100, PelicanTests::outputFull);
        tests.test("pelican_refuses_meals_until_life_starts", 1, PelicanTests::untilLifeStarts);
        tests.test("pelican_refuses_meals_after_a_stop_and_loses_its_batch", 1, PelicanTests::stopLosesBatch);
        tests.test("pelican_loses_its_batch_on_a_reset", 1, PelicanTests::resetLosesBatch);
        tests.test("pelican_keeps_its_batch_through_a_win", 1, PelicanTests::winKeepsBatch);
        tests.test("pelican_permit_given_on_a_start_and_taken_back_on_a_stop", 1, PelicanTests::permitStartAndStop);
        tests.test("pelican_permit_kept_through_a_win", 1, PelicanTests::permitKeptThroughAWin);
        tests.test("pelican_eats_a_meal_from_the_hand", 1, PelicanTests::handFed);
        tests.test("pelican_gives_its_essence_to_an_empty_hand", 100, PelicanTests::takenByHand);
        tests.test("pelican_is_lit_while_it_digests", 100, PelicanTests::litWhileDigesting);
        tests.test("pelican_goes_dim_when_a_reset_loses_its_batch", 100, PelicanTests::dimAfterReset);
    }

    private static void configured(GameTestHelper helper) {
        helper.assertValueEqual(Pelican.thresholds(), new Batch.Thresholds(SATURATION, KINDS, QUALITY), "the Pelican's thresholds");
        helper.assertValueEqual(Pelican.digestingTicks(), DIGESTING, "the Pelican's digesting time");
        helper.succeed();
    }

    private static void lifeChainMachine(GameTestHelper helper) {
        helper.assertValueEqual(ChainMachines.chainOf(Pelican.ITEM.get()), Optional.of(Handicap.LIFE), "the Pelican's chain");
        helper.succeed();
    }

    private static void onlyMeals(GameTestHelper helper) {
        playLife(helper);
        place(helper);
        helper.assertValueEqual(feed(helper, Items.BREAD, 0, 1), 0, "the bread, a food but no meal, the Pelican ate");
        helper.assertValueEqual(feed(helper, Items.ROTTEN_FLESH, 0, 1), 0, "the rotten flesh the Pelican ate");
        helper.assertValueEqual(feed(helper, Items.STONE, 0, 1), 0, "the stone the Pelican ate");
        helper.assertValueEqual(feed(helper, Items.MUSHROOM_STEW, 0, 1), 1, "the meal the Pelican ate");
        helper.assertValueEqual(pelican(helper).batch().meals(), 1, "the meals in the Pelican's batch");
        helper.succeed();
    }

    /** Three kinds at gold, but 21.6 saturation of 30: nothing, until a rabbit stew brings it to 33.6. */
    private static void shortOfSaturation(GameTestHelper helper) {
        playLife(helper);
        place(helper);
        feed(helper, Items.MUSHROOM_STEW, 2, 1);
        feed(helper, Items.BEETROOT_SOUP, 2, 1);
        feed(helper, Items.SUSPICIOUS_STEW, 2, 1);
        expectWaitsThenDigests(helper, Items.RABBIT_STEW, 2);
    }

    /** 43.2 saturation at gold, but two kinds of three: nothing, until a beetroot soup makes the third. */
    private static void shortOfKinds(GameTestHelper helper) {
        playLife(helper);
        place(helper);
        feed(helper, Items.RABBIT_STEW, 2, 3);
        feed(helper, Items.MUSHROOM_STEW, 2, 1);
        expectWaitsThenDigests(helper, Items.BEETROOT_SOUP, 2);
    }

    /**
     * 33.6 saturation of four kinds, at iron, iron, none and gold, a mean of 1: nothing, and a diamond rabbit stew brings
     * it only to 1.4; a second, to 1.67.
     */
    private static void shortOfQuality(GameTestHelper helper) {
        playLife(helper);
        place(helper);
        feed(helper, Items.RABBIT_STEW, 1, 1);
        feed(helper, Items.MUSHROOM_STEW, 1, 1);
        feed(helper, Items.BEETROOT_SOUP, 0, 1);
        feed(helper, Items.SUSPICIOUS_STEW, 2, 1);
        feed(helper, Items.RABBIT_STEW, 3, 1);
        expectWaitsThenDigests(helper, Items.RABBIT_STEW, 3);
    }

    /**
     * Whatever the batch holds now is short: past a digesting time it has made nothing and still holds it; {@code last}
     * at {@code level} makes it ready, and it starts digesting.
     */
    private static void expectWaitsThenDigests(GameTestHelper helper, Item last, int level) {
        PelicanBlockEntity pelican = pelican(helper);
        int meals = pelican.batch().meals();
        helper.runAtTickTime(DIGESTING + MARGIN, () -> {
            helper.assertTrue(output(helper).isEmpty(), "a short batch made " + output(helper));
            helper.assertFalse(pelican.digesting(), "a short batch is digested");
            helper.assertValueEqual(pelican.batch().meals(), meals, "the meals a short batch keeps");
            helper.assertValueEqual(feed(helper, last, level, 1), 1, "the meal that makes the batch ready");
            helper.assertTrue(pelican.digesting(), "a ready batch isn't digested");
            helper.succeed();
        });
    }

    /** A ready batch digests for the digesting time, eating nothing meanwhile, then gives a Vital Essence of the attempt. */
    private static void digests(GameTestHelper helper) {
        UUID team = playLife(helper);
        place(helper);
        feedReady(helper);
        PelicanBlockEntity pelican = pelican(helper);
        helper.assertTrue(pelican.digesting(), "a ready batch isn't digested");
        helper.assertValueEqual(feed(helper, Items.SUSPICIOUS_STEW, 2, 1), 0, "the meal a digesting Pelican ate");
        helper.runAtTickTime(DIGESTING - MARGIN, () -> {
            helper.assertTrue(output(helper).isEmpty(), "the Pelican gave " + output(helper) + " before it had digested");
            helper.assertValueEqual(feed(helper, Items.SUSPICIOUS_STEW, 2, 1), 0, "the meal a digesting Pelican ate");
        });
        helper.runAtTickTime(DIGESTING + MARGIN, () -> {
            ItemStack made = output(helper);
            helper.assertTrue(made.is(Essences.item(Essence.VITAL)) && made.getCount() == 1, "the Pelican gave " + made + ", not a Vital Essence");
            int attempt = TeamState.get(helper.getLevel().getServer()).attempt(team);
            helper.assertValueEqual(Attempts.of(made), Optional.of(new Attempt(team, attempt)), "the Vital Essence's attempt");
            helper.assertFalse(pelican.digesting(), "the Pelican still digests past its digesting time");
            helper.assertValueEqual(pelican.batch(), Batch.EMPTY, "the Pelican's batch once digested");
            helper.succeed();
        });
    }

    /** Its output holds one Vital Essence: while it does, the Pelican eats nothing; taken out, it eats again. */
    private static void outputFull(GameTestHelper helper) {
        playLife(helper);
        place(helper);
        feedReady(helper);
        helper.runAtTickTime(DIGESTING + MARGIN, () -> {
            helper.assertFalse(output(helper).isEmpty(), "the Pelican gave nothing");
            helper.assertValueEqual(feed(helper, Items.MUSHROOM_STEW, 2, 1), 0, "the meal a Pelican with a full output ate");
            ItemStack taken = take(helper);
            helper.assertTrue(taken.is(Essences.item(Essence.VITAL)), "automation took " + taken + " from the Pelican");
            helper.assertValueEqual(feed(helper, Items.MUSHROOM_STEW, 2, 1), 1, "the meal a Pelican with an emptied output ate");
            helper.succeed();
        });
    }

    /** It idles until its team starts life: before any challenge, and while another is played. */
    private static void untilLifeStarts(GameTestHelper helper) {
        UUID team = TestTeams.claim(helper);
        var server = helper.getLevel().getServer();
        place(helper);
        helper.assertValueEqual(feed(helper, Items.MUSHROOM_STEW, 0, 1), 0, "the meal the Pelican ate before any challenge");
        Challenges.begin(server, team, Handicap.ENERGY);
        helper.assertValueEqual(feed(helper, Items.MUSHROOM_STEW, 0, 1), 0, "the meal the Pelican ate while energy is played");
        Challenges.end(server, team);
        Challenges.begin(server, team, Handicap.LIFE);
        helper.assertValueEqual(feed(helper, Items.MUSHROOM_STEW, 0, 1), 1, "the meal the Pelican ate once life started");
        helper.succeed();
    }

    /** A stop locks life's chain again: the Pelican eats nothing and its batch is gone, and stays gone past a new start. */
    private static void stopLosesBatch(GameTestHelper helper) {
        UUID team = playLife(helper);
        var server = helper.getLevel().getServer();
        place(helper);
        feed(helper, Items.RABBIT_STEW, 2, 2);
        Challenges.stop(server, team);
        helper.assertValueEqual(feed(helper, Items.MUSHROOM_STEW, 2, 1), 0, "the meal the Pelican ate after a stop");
        helper.assertValueEqual(pelican(helper).batch(), Batch.EMPTY, "the Pelican's batch after a stop");
        Challenges.begin(server, team, Handicap.LIFE);
        helper.assertValueEqual(pelican(helper).batch(), Batch.EMPTY, "the Pelican's batch once life started again");
        helper.assertValueEqual(feed(helper, Items.MUSHROOM_STEW, 2, 1), 1, "the meal the Pelican ate once life started again");
        helper.assertValueEqual(pelican(helper).batch().meals(), 1, "the meals in the Pelican's batch begun again");
        helper.succeed();
    }

    /** A reset loses the batch, and a ready one's digesting with it; the Pelican eats again at once. */
    private static void resetLosesBatch(GameTestHelper helper) {
        UUID team = playLife(helper);
        place(helper);
        feedReady(helper);
        Challenges.reset(helper.getLevel().getServer(), team);
        PelicanBlockEntity pelican = pelican(helper);
        helper.assertValueEqual(pelican.batch(), Batch.EMPTY, "the Pelican's batch after a reset");
        helper.assertFalse(pelican.digesting(), "the Pelican digests past a reset");
        helper.assertValueEqual(feed(helper, Items.MUSHROOM_STEW, 2, 1), 1, "the meal the Pelican ate after a reset");
        helper.succeed();
    }

    /** A win keeps life's chain unlocked: the batch stays, and the Pelican eats on, past another challenge's start. */
    private static void winKeepsBatch(GameTestHelper helper) {
        UUID team = playLife(helper);
        var server = helper.getLevel().getServer();
        place(helper);
        feed(helper, Items.RABBIT_STEW, 2, 1);
        Challenges.win(server, team);
        Challenges.begin(server, team, Handicap.ENERGY);
        helper.assertValueEqual(pelican(helper).batch().meals(), 1, "the meals in the Pelican's batch past a win");
        helper.assertValueEqual(feed(helper, Items.MUSHROOM_STEW, 2, 1), 1, "the meal the Pelican ate past a win");
        helper.succeed();
    }

    /** Nothing crafts it: the Shop sells it behind {@link Pelican#PERMIT}, held from life's start until a stop. */
    private static void permitStartAndStop(GameTestHelper helper) {
        UUID team = TestTeams.claim(helper);
        var server = helper.getLevel().getServer();
        TestAccounts.open(team, 0);
        helper.assertFalse(TestAccounts.holds(team, Pelican.PERMIT), "the Pelican's Permit before life");
        Challenges.begin(server, team, Handicap.LIFE);
        helper.assertTrue(TestAccounts.holds(team, Pelican.PERMIT), "no Pelican's Permit once life started");
        Challenges.stop(server, team);
        helper.assertFalse(TestAccounts.holds(team, Pelican.PERMIT), "the Pelican's Permit past a stop");
        helper.succeed();
    }

    /** A win keeps life's chain, and the Permit with it; a reverted win takes both back. */
    private static void permitKeptThroughAWin(GameTestHelper helper) {
        UUID team = TestTeams.claim(helper);
        var server = helper.getLevel().getServer();
        TestAccounts.open(team, 0);
        Challenges.begin(server, team, Handicap.LIFE);
        Challenges.win(server, team);
        helper.assertTrue(TestAccounts.holds(team, Pelican.PERMIT), "no Pelican's Permit past a life win");
        Challenges.revert(server, team, Handicap.LIFE);
        helper.assertFalse(TestAccounts.holds(team, Pelican.PERMIT), "the Pelican's Permit past a reverted life win");
        helper.succeed();
    }

    /** A click with a meal in hand feeds it one; anything else in hand it leaves there. */
    private static void handFed(GameTestHelper helper) {
        playLife(helper);
        place(helper);
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BREAD, 3));
        helper.useBlock(AT, player);
        helper.assertValueEqual(player.getMainHandItem().getCount(), 3, "the bread left in hand");
        helper.assertValueEqual(pelican(helper).batch().meals(), 0, "the meals in the Pelican's batch after bread");
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.RABBIT_STEW, 3));
        helper.useBlock(AT, player);
        helper.assertValueEqual(player.getMainHandItem().getCount(), 2, "the rabbit stew left in hand");
        helper.assertValueEqual(pelican(helper).batch().meals(), 1, "the meals in the Pelican's batch after a click");
        helper.succeed();
    }

    /** A click with an empty hand takes out the Vital Essence it holds, and it eats again. */
    private static void takenByHand(GameTestHelper helper) {
        playLife(helper);
        place(helper);
        feedReady(helper);
        helper.runAtTickTime(DIGESTING + MARGIN, () -> {
            Player player = helper.makeMockPlayer(GameType.SURVIVAL);
            helper.useBlock(AT, player);
            ItemStack taken = player.getMainHandItem();
            helper.assertTrue(taken.is(Essences.item(Essence.VITAL)) && taken.getCount() == 1, "the hand took " + taken + ", not a Vital Essence");
            helper.assertTrue(output(helper).isEmpty(), "the Pelican still holds " + output(helper));
            helper.assertValueEqual(feed(helper, Items.MUSHROOM_STEW, 2, 1), 1, "the meal a Pelican emptied by hand ate");
            helper.succeed();
        });
    }

    /** Dim while it eats, lit while it digests a ready batch, counting its ticks down, and dim again once it gives the essence. */
    private static void litWhileDigesting(GameTestHelper helper) {
        playLife(helper);
        place(helper);
        feed(helper, Items.RABBIT_STEW, 2, 1);
        helper.runAtTickTime(MARGIN, () -> {
            helper.assertFalse(lit(helper), "a Pelican short of a ready batch is lit");
            feedReady(helper);
        });
        helper.runAtTickTime(2 * MARGIN, () -> {
            helper.assertTrue(lit(helper), "a digesting Pelican isn't lit");
            int left = pelican(helper).digestingLeft();
            helper.assertTrue(left > 0 && left < DIGESTING, "the digesting ticks left, " + left + ", aren't counting down");
        });
        helper.runAtTickTime(DIGESTING + 2 * MARGIN, () -> {
            helper.assertFalse(output(helper).isEmpty(), "the Pelican gave nothing");
            helper.assertFalse(lit(helper), "a Pelican that gave its essence is still lit");
            helper.assertValueEqual(pelican(helper).digestingLeft(), 0, "the digesting ticks left once digested");
            helper.succeed();
        });
    }

    /** A reset loses a digesting batch, and the Pelican goes dim. */
    private static void dimAfterReset(GameTestHelper helper) {
        UUID team = playLife(helper);
        place(helper);
        feedReady(helper);
        helper.runAtTickTime(MARGIN, () -> {
            helper.assertTrue(lit(helper), "a digesting Pelican isn't lit");
            Challenges.reset(helper.getLevel().getServer(), team);
        });
        helper.runAtTickTime(2 * MARGIN, () -> {
            helper.assertFalse(lit(helper), "a Pelican whose batch a reset lost is still lit");
            helper.succeed();
        });
    }

    private static boolean lit(GameTestHelper helper) {
        return helper.getBlockState(AT).getValue(PelicanBlock.LIT);
    }

    private static UUID playLife(GameTestHelper helper) {
        UUID team = TestTeams.claim(helper);
        Challenges.begin(helper.getLevel().getServer(), team, Handicap.LIFE);
        return team;
    }

    private static void place(GameTestHelper helper) {
        helper.setBlock(AT, Pelican.BLOCK.get());
    }

    private static PelicanBlockEntity pelican(GameTestHelper helper) {
        return helper.getBlockEntity(AT, PelicanBlockEntity.class);
    }

    /** 38.4 saturation of three kinds, all gold: ready. */
    private static void feedReady(GameTestHelper helper) {
        feed(helper, Items.RABBIT_STEW, 2, 2);
        feed(helper, Items.MUSHROOM_STEW, 2, 1);
        helper.assertValueEqual(feed(helper, Items.BEETROOT_SOUP, 2, 1), 1, "the meal that makes the batch ready");
    }

    /** Offers the Pelican {@code count} of {@code item} at quality {@code level} from above, one at a time, and says how many it ate. */
    private static int feed(GameTestHelper helper, Item item, int level, int count) {
        ItemStack stack = new ItemStack(item);
        if (level > 0) {
            Identifier type = level == 1 ? IRON : level == 2 ? GOLD : DIAMOND;
            stack.set(QualityFoodStandIn.QUALITY.get(), new QualityFoodStandIn.Quality(type, level));
        }
        int eaten = 0;
        for (int meal = 0; meal < count; meal++) {
            try (var transaction = Transaction.openRoot()) {
                eaten += face(helper).insert(ItemResource.of(stack), 1, transaction);
                transaction.commit();
            }
        }
        return eaten;
    }

    /** What its output holds, as automation sees it. */
    private static ItemStack output(GameTestHelper helper) {
        var face = face(helper);
        for (int index = 0; index < face.size(); index++) {
            if (!face.getResource(index).isEmpty()) {
                return face.getResource(index).toStack(face.getAmountAsInt(index));
            }
        }
        return ItemStack.EMPTY;
    }

    /** Takes what its output holds, as a hopper below would. */
    private static ItemStack take(GameTestHelper helper) {
        ItemStack held = output(helper);
        try (var transaction = Transaction.openRoot()) {
            int taken = face(helper).extract(ItemResource.of(held), held.getCount(), transaction);
            transaction.commit();
            return held.copyWithCount(taken);
        }
    }

    private static ResourceHandler<ItemResource> face(GameTestHelper helper) {
        var face = helper.getLevel().getCapability(Capabilities.Item.BLOCK, helper.absolutePos(AT), Direction.UP);
        helper.assertTrue(face != null, "the Pelican has no item face");
        return face;
    }
}
