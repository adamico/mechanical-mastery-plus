// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.gametest;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import io.github._5thlayer.mechanicalmasteryreborn.ServerConfig;
import io.github._5thlayer.mechanicalmasteryreborn.athanor.Athanor;
import io.github._5thlayer.mechanicalmasteryreborn.athanor.AthanorBlockEntity;
import io.github._5thlayer.mechanicalmasteryreborn.athanor.AthanorMenu;
import io.github._5thlayer.mechanicalmasteryreborn.athanor.AthanorWork;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.Accounts;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.Assault;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.AssaultState;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.Handicap;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.LastStands;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.LifeHandicap;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.MoneyHandicap;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.NullShards;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.RiftShards;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.Spells;
import io.github._5thlayer.mechanicalmasteryreborn.cod.Cod;
import io.github._5thlayer.mechanicalmasteryreborn.essence.Essence;
import io.github._5thlayer.mechanicalmasteryreborn.essence.Essences;
import io.github._5thlayer.mechanicalmasteryreborn.record.Spell;
import io.github._5thlayer.mechanicalmasteryreborn.record.TeamState;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.transfer.energy.SimpleEnergyHandler;

/**
 * The Athanor (#263, ADR 0014): it does no work before the last stand, and none in it without a spell; each handicap a
 * team takes on there feeds its line only with what it takes from the team during its spell, which can't be ended early
 * and ends its handicap with it; only all four lines make a Quintessence for less than it cancels; and one works to a
 * team. The tests play at the default config but for the spells' length, short enough to watch one end. No suite mod is
 * on the run, so a generator with a block entity is a chiseled bookshelf, which the game-test pack puts in the generator
 * tag and this class gives ample FE; a target block stands for a battery ({@link GeneratorTests}).
 */
final class AthanorTests {

    private static final BlockPos COD = new BlockPos(1, 2, 1);
    private static final BlockPos ATHANOR = new BlockPos(5, 2, 5);
    private static final BlockPos SECOND = new BlockPos(7, 2, 5);
    private static final BlockPos GENERATOR = new BlockPos(5, 2, 7);
    private static final BlockPos BATTERY = new BlockPos(7, 2, 7);
    private static final BlockPos LODESTONE = new BlockPos(1, 2, 7);
    /** A generator with a block entity, as every real one has: the Athanor finds them by theirs. */
    private static final Block GENERATOR_BLOCK = Blocks.CHISELED_BOOKSHELF;
    private static final int OUTPUT = 1_000_000;
    /** Ticks by which an Athanor ticking since the test's start has paid for its lines once more. */
    private static final int PAID = AthanorBlockEntity.INTERVAL + 3;
    /** A spell short enough to watch end. */
    private static final int SHORT = 40;

    private AthanorTests() {
    }

    static void registerCapabilities(IEventBus modBus) {
        modBus.addListener((RegisterCapabilitiesEvent event) -> event.registerBlock(Capabilities.Energy.BLOCK,
                (level, pos, state, entity, side) -> new SimpleEnergyHandler(OUTPUT, OUTPUT, OUTPUT, OUTPUT), GENERATOR_BLOCK));
    }

    static void register(MechanicalMasteryRebornGameTests.Registrar tests) {
        tests.test("athanor_does_no_work_before_the_last_stand", PAID + 1, AthanorTests::lockedBeforeTheLastStand);
        tests.test("athanor_makes_nothing_with_no_spell", 2 * PAID, AthanorTests::nothingWithNoSpell);
        tests.test("athanor_energy_spell_feeds_on_generators_and_cuts_banked_fe", SHORT + 4, AthanorTests::energyLine);
        tests.test("athanor_energy_line_unfed_without_generators", SHORT + 4, AthanorTests::energyLineUnfed);
        tests.test("athanor_hearts_feed_the_life_line_and_no_fluid_goes_in", SHORT + PAID + 4, AthanorTests::lifeLine);
        tests.test("athanor_null_shards_of_an_earlier_spell_feed_nothing", 120, AthanorTests::earlierShards);
        tests.test("athanor_paused_assault_feeds_no_line", 2 * AthanorBlockEntity.INTERVAL + SHORT + 8, AthanorTests::pausedAssault);
        tests.test("athanor_spell_assault_mobs_drop_null_shards", SHORT + 6, AthanorTests::nullShardDrops);
        tests.test("athanor_money_line_drains_what_is_earned_until_unpaid", 100, AthanorTests::moneyLine);
        tests.test("athanor_all_four_lines_make_a_quintessence_for_less_than_it_cancels", 380, AthanorTests::allFourLines);
        tests.test("athanor_second_of_a_team_does_nothing", 2 * PAID + AthanorBlockEntity.INTERVAL, AthanorTests::secondDoesNothing);
        tests.test("athanor_spell_cant_be_ended_early_and_ends_its_handicap", SHORT + 6, AthanorTests::spellRunsItsLength);
    }

    /** All four won but the last stand not started: no spell can be taken on, and one noted all the same does nothing. */
    private static void lockedBeforeTheLastStand(GameTestHelper helper) {
        UUID team = TestTeams.claim(helper);
        win(helper, team);
        helper.setBlock(GENERATOR, GENERATOR_BLOCK);
        helper.setBlock(ATHANOR, Athanor.BLOCK.get());
        athanor(helper).feed(nullessence(64));
        helper.assertFalse(Spells.begin(server(helper), team, Handicap.ENERGY), "a spell was taken on before the last stand");
        state(helper).beginSpell(team, Handicap.ENERGY, Spell.starting(server(helper).overworld().getGameTime(), PAID + 10));
        helper.runAtTickTime(PAID, () -> {
            AthanorBlockEntity athanor = athanor(helper);
            helper.assertValueEqual(athanor.phase(), AthanorBlockEntity.Phase.LOCKED, "the Athanor before the last stand");
            helper.assertValueEqual(athanor.linesCounting(), 0, "lines counting before the last stand");
            helper.assertValueEqual(athanor.progress(), 0.0, "progress before the last stand");
            state(helper).endSpell(team, Handicap.ENERGY);
            helper.succeed();
        });
    }

    private static void nothingWithNoSpell(GameTestHelper helper) {
        started(helper);
        helper.setBlock(ATHANOR, Athanor.BLOCK.get());
        athanor(helper).feed(nullessence(64));
        helper.runAtTickTime(2 * PAID - 1, () -> {
            AthanorBlockEntity athanor = athanor(helper);
            helper.assertValueEqual(athanor.phase(), AthanorBlockEntity.Phase.IDLE, "the Athanor with no spell");
            helper.assertValueEqual(athanor.progress(), 0.0, "progress with no spell");
            helper.assertValueEqual(athanor.nullessence(), 64, "the Nullessence left with no spell");
            helper.assertValueEqual(athanor.quintessence(), 0, "the Quintessence made with no spell");
            helper.succeed();
        });
    }

    /**
     * Under the energy spell the team's generator feeds the line and gives nothing to anything else, a battery on its
     * island gives no FE either, and both give again once the spell ends.
     */
    private static void energyLine(GameTestHelper helper) {
        UUID team = started(helper);
        helper.setBlock(GENERATOR, GENERATOR_BLOCK);
        helper.setBlock(BATTERY, Blocks.TARGET);
        helper.setBlock(ATHANOR, Athanor.BLOCK.get());
        // One line's Quintessence takes the most.
        for (int fed = 0; fed < AthanorWork.cost(1, ServerConfig.athanorCosts()); fed += 64) {
            athanor(helper).feed(nullessence(64));
        }
        helper.assertValueEqual(GeneratorTests.draw(helper, BATTERY), GeneratorTests.DRAW, "FE drawn from a battery before the spell");
        helper.assertTrue(Spells.begin(server(helper), team, Handicap.ENERGY, SHORT), "the energy spell wasn't taken on");
        helper.assertValueEqual(GeneratorTests.draw(helper, BATTERY), 0, "banked FE drawn under the energy spell");
        helper.assertValueEqual(GeneratorTests.draw(helper, GENERATOR), 0, "a generator's FE drawn by anything but the Athanor");
        helper.runAtTickTime(PAID, () -> {
            AthanorBlockEntity athanor = athanor(helper);
            helper.assertTrue(athanor.counts(Handicap.ENERGY), "the energy line isn't fed by a generator");
            helper.assertValueEqual(athanor.phase(), AthanorBlockEntity.Phase.WORKING, "the Athanor with its energy line fed");
            helper.assertTrue(athanor.progress() > 0, "no progress with the energy line fed");
        });
        helper.runAtTickTime(SHORT + 2, () -> {
            helper.assertFalse(state(helper).underSpell(team, Handicap.ENERGY), "the energy spell outran its length");
            helper.assertValueEqual(GeneratorTests.draw(helper, BATTERY), GeneratorTests.DRAW, "FE drawn from a battery once the spell ended");
            helper.succeed();
        });
    }

    private static void energyLineUnfed(GameTestHelper helper) {
        UUID team = started(helper);
        helper.setBlock(ATHANOR, Athanor.BLOCK.get());
        athanor(helper).feed(nullessence(64));
        helper.assertTrue(Spells.begin(server(helper), team, Handicap.ENERGY, SHORT), "the energy spell wasn't taken on");
        helper.runAtTickTime(PAID, () -> {
            helper.assertFalse(athanor(helper).counts(Handicap.ENERGY), "the energy line fed with no generator");
            helper.assertValueEqual(athanor(helper).phase(), AthanorBlockEntity.Phase.IDLE, "the Athanor with no line fed");
        });
        helper.runAtTickTime(SHORT + 2, helper::succeed);
    }

    /**
     * A heart given at the Athanor under the life spell feeds the life line, as none can before it; the Athanor answers
     * for no fluid, so no Essentia Vitae comes piped in; the spell's hunger is the hungriest; and what is left of the
     * Essentia once the spell ends is lost.
     */
    private static void lifeLine(GameTestHelper helper) {
        UUID team = started(helper);
        helper.setBlock(ATHANOR, Athanor.BLOCK.get());
        athanor(helper).feed(nullessence(64));
        Player player = member(helper, team);
        helper.runAtTickTime(2, () -> {
            AthanorBlockEntity athanor = athanor(helper);
            helper.assertTrue(athanor.giveHeart(player).isPresent(), "a heart taken with no life spell");
            helper.assertTrue(Spells.begin(server(helper), team, Handicap.LIFE, SHORT), "the life spell wasn't taken on");
            helper.assertTrue(helper.getLevel().getCapability(Capabilities.Fluid.BLOCK, helper.absolutePos(ATHANOR), null) == null,
                    "the Athanor answers for fluid");
            float health = player.getHealth();
            helper.assertTrue(athanor.giveHeart(player).isEmpty(), "a heart refused under the life spell");
            helper.assertValueEqual(player.getHealth(), health - AthanorBlockEntity.HEART, "the health left after a heart");
            helper.assertValueEqual(athanor.essentia(), ServerConfig.athanorLifePerHeart(), "the Essentia Vitae a heart became");
            helper.assertValueEqual(LifeHandicap.exhaustion(player, 1.0F), (float) ServerConfig.athanorLifeHungerRate(),
                    "the exhaustion under the life spell");
        });
        helper.runAtTickTime(PAID + 2, () -> {
            AthanorBlockEntity athanor = athanor(helper);
            helper.assertTrue(athanor.counts(Handicap.LIFE), "the life line isn't fed by a heart");
            helper.assertTrue(athanor.essentia() < ServerConfig.athanorLifePerHeart(), "the life line took no Essentia Vitae");
        });
        helper.runAtTickTime(SHORT + PAID + 2, () -> {
            helper.assertValueEqual(LifeHandicap.exhaustion(player, 1.0F), 1.0F, "the exhaustion once the life spell ended");
            helper.assertValueEqual(athanor(helper).essentia(), 0, "the Essentia Vitae kept past the life spell");
            helper.assertFalse(athanor(helper).counts(Handicap.LIFE), "the life line fed past its spell");
            helper.succeed();
        });
    }

    /** A protection spell's Null Shards go into the Athanor only during it: the next spell's line takes only its own. */
    private static void earlierShards(GameTestHelper helper) {
        UUID team = started(helper);
        helper.setBlock(COD, Cod.BLOCK.get());
        helper.setBlock(ATHANOR, Athanor.BLOCK.get());
        ServerPlayer home = AssaultAbsenceTests.player(helper, team);
        List<ItemStack> earlier = new ArrayList<>();
        helper.runAtTickTime(2, () -> {
            helper.assertTrue(Spells.begin(server(helper), team, Handicap.PROTECTION, 10), "the first protection spell wasn't taken on");
            earlier.add(NullShards.of(state(helper).spell(team, Handicap.PROTECTION).orElseThrow(), 4));
        });
        int second = 16;
        helper.runAtTickTime(second, () -> {
            helper.assertFalse(state(helper).underSpell(team, Handicap.PROTECTION), "the first protection spell outran its length");
            helper.assertTrue(AssaultState.get(server(helper)).assault(team).isEmpty(), "the Assault outlived its spell");
            helper.assertTrue(Spells.begin(server(helper), team, Handicap.PROTECTION, 60), "the protection spell wasn't taken on again");
            helper.assertTrue(AssaultState.get(server(helper)).assault(team).isPresent(), "the second spell sent no Assault");
            helper.assertValueEqual(athanor(helper).feed(earlier.getFirst().copy()), 0, "an earlier spell's Null Shards went in");
            helper.assertValueEqual(athanor(helper).feed(NullShards.of(state(helper).spell(team, Handicap.PROTECTION).orElseThrow(), 2)), 2,
                    "the spell's own Null Shards went in");
            athanor(helper).feed(nullessence(64));
        });
        helper.runAtTickTime(second + PAID, () -> helper.assertTrue(athanor(helper).counts(Handicap.PROTECTION),
                "the protection line isn't fed by its spell's Null Shards"));
        helper.runAtTickTime(second + 62, () -> {
            helper.assertTrue(AssaultState.get(server(helper)).assault(team).isEmpty(), "the Assault outlived its spell");
            AssaultAbsenceTests.leave(home);
            helper.succeed();
        });
    }

    /** With no one home, the protection spell's Assault pauses, and its line counts for nothing while the spell runs on. */
    private static void pausedAssault(GameTestHelper helper) {
        UUID team = started(helper);
        helper.setBlock(COD, Cod.BLOCK.get());
        helper.setBlock(ATHANOR, Athanor.BLOCK.get());
        int length = 2 * AthanorBlockEntity.INTERVAL + SHORT;
        helper.runAtTickTime(2, () -> {
            helper.assertTrue(Spells.begin(server(helper), team, Handicap.PROTECTION, length), "the protection spell wasn't taken on");
            athanor(helper).feed(NullShards.of(state(helper).spell(team, Handicap.PROTECTION).orElseThrow(), 8));
        });
        helper.runAtTickTime(2 + 2 * AthanorBlockEntity.INTERVAL + 2, () -> {
            helper.assertTrue(AssaultState.get(server(helper)).paused(team).isPresent(), "the Assault didn't pause with no one home");
            helper.assertFalse(athanor(helper).counts(Handicap.PROTECTION), "a paused Assault's line counts");
            helper.assertTrue(state(helper).underSpell(team, Handicap.PROTECTION), "the spell stopped with its Assault paused");
        });
        helper.runAtTickTime(length + 6, helper::succeed);
    }

    /** A mob of the protection spell's Assault drops Null Shards of that spell where it would drop Rift Shards, and the rest as ever. */
    private static void nullShardDrops(GameTestHelper helper) {
        UUID team = started(helper);
        helper.setBlock(COD, Cod.BLOCK.get());
        ServerPlayer home = AssaultAbsenceTests.player(helper, team);
        helper.runAtTickTime(2, () -> {
            helper.assertTrue(Spells.begin(server(helper), team, Handicap.PROTECTION, SHORT), "the protection spell wasn't taken on");
            UUID gate = AssaultState.get(server(helper)).assault(team).orElseThrow().gates().getFirst();
            var zombie = EntityType.ZOMBIE.create(helper.getLevel(), EntitySpawnReason.COMMAND);
            Assault.markOwner(zombie, gate);
            var pos = helper.absolutePos(COD).getCenter();
            List<ItemEntity> drops = new ArrayList<>(List.of(
                    new ItemEntity(helper.getLevel(), pos.x, pos.y, pos.z, new ItemStack(RiftShards.ITEM.get(), 2)),
                    new ItemEntity(helper.getLevel(), pos.x, pos.y, pos.z, new ItemStack(Items.ROTTEN_FLESH))));
            NeoForge.EVENT_BUS.post(new LivingDropsEvent(zombie, helper.getLevel().damageSources().generic(), drops, true));
            ItemStack shards = drops.getFirst().getItem();
            helper.assertTrue(shards.is(NullShards.ITEM.get()), "the Rift Shards dropped as " + shards);
            helper.assertValueEqual(shards.getCount(), 2, "the Null Shards dropped");
            helper.assertValueEqual(NullShards.spellOf(shards), state(helper).spell(team, Handicap.PROTECTION).map(Spell::id),
                    "the spell the Null Shards are stamped with");
            helper.assertTrue(drops.get(1).getItem().is(Items.ROTTEN_FLESH), "the rest of the drops changed");
            zombie.discard();
        });
        helper.runAtTickTime(SHORT + 4, () -> {
            AssaultAbsenceTests.leave(home);
            helper.succeed();
        });
    }

    /**
     * Under the money spell the Balance is set aside, sales pay a quarter and prices rise by half; the line drains what is
     * earned, and stops counting once the Account can't pay; the end gives back what was set aside.
     */
    private static void moneyLine(GameTestHelper helper) {
        UUID team = started(helper);
        TestAccounts.open(team, 1000);
        helper.setBlock(ATHANOR, Athanor.BLOCK.get());
        athanor(helper).feed(nullessence(64));
        int drain = ServerConfig.athanorMoneyDemand() * AthanorBlockEntity.INTERVAL / 20;
        int begin = 5;
        int length = 80;
        helper.runAtTickTime(begin, () -> {
            helper.assertTrue(Spells.begin(server(helper), team, Handicap.MONEY, length), "the money spell wasn't taken on");
            helper.assertValueEqual(TestAccounts.balanceOf(team), 0L, "the Balance under the money spell");
            helper.assertValueEqual(MoneyHandicap.sale(server(helper), team, 1000), 250L, "a sale of 1000 under the money spell");
            helper.assertValueEqual(MoneyHandicap.purchase(server(helper), team, 1000), 1500L, "a purchase of 1000 under the money spell");
            Accounts.get().give(server(helper), team, 2L * drain + drain / 2);
        });
        helper.runAtTickTime(begin + 2 * AthanorBlockEntity.INTERVAL - 1, () -> {
            helper.assertTrue(athanor(helper).counts(Handicap.MONEY), "the money line isn't fed by what was earned");
            helper.assertValueEqual(TestAccounts.balanceOf(team), (long) drain / 2, "the Balance after two drains");
        });
        helper.runAtTickTime(begin + 3 * AthanorBlockEntity.INTERVAL - 1, () -> {
            helper.assertFalse(athanor(helper).counts(Handicap.MONEY), "the money line counts with the Account unable to pay");
            helper.assertValueEqual(TestAccounts.balanceOf(team), (long) drain / 2, "the Balance after a drain it couldn't pay");
        });
        helper.runAtTickTime(begin + length + 2, () -> {
            helper.assertValueEqual(TestAccounts.balanceOf(team), 1000L + drain / 2, "the Balance once the money spell ended");
            helper.assertValueEqual(MoneyHandicap.purchase(server(helper), team, 1000), 1000L, "a purchase of 1000 after the money spell");
            helper.succeed();
        });
    }

    /** With all four lines fed, the Athanor makes a Quintessence at its fastest, for fewer Nullessence than one cancels. */
    private static void allFourLines(GameTestHelper helper) {
        UUID team = started(helper);
        TestAccounts.open(team, 0);
        helper.setBlock(COD, Cod.BLOCK.get());
        helper.setBlock(GENERATOR, GENERATOR_BLOCK);
        helper.setBlock(ATHANOR, Athanor.BLOCK.get());
        athanor(helper).feed(nullessence(64));
        ServerPlayer home = AssaultAbsenceTests.player(helper, team);
        Player giver = member(helper, team);
        int length = 360;
        helper.runAtTickTime(2, () -> {
            for (Handicap handicap : Handicap.values()) {
                helper.assertTrue(Spells.begin(server(helper), team, handicap, length), "the " + handicap.id() + " spell wasn't taken on");
            }
            Accounts.get().give(server(helper), team, 100_000);
            athanor(helper).feed(NullShards.of(state(helper).spell(team, Handicap.PROTECTION).orElseThrow(), 4));
            athanor(helper).giveHeart(giver);
            athanor(helper).giveHeart(giver);
        });
        helper.runAtTickTime(2 + PAID, () -> helper.assertValueEqual(athanor(helper).linesCounting(), AthanorWork.LINES, "the lines counting"));
        int cost = AthanorWork.cost(AthanorWork.LINES, ServerConfig.athanorCosts());
        helper.runAtTickTime(2 + PAID + ServerConfig.athanorTicks().get(AthanorWork.LINES - 1), () -> {
            AthanorBlockEntity athanor = athanor(helper);
            helper.assertValueEqual(athanor.quintessence(), 1, "the Quintessence all four lines made");
            helper.assertValueEqual(athanor.nullessence(), 64 - cost, "the Nullessence left");
            helper.assertTrue(cost < ServerConfig.lastStandQuintessenceCancels(), "a Quintessence of all four lines costs " + cost);
        });
        helper.runAtTickTime(2 + length + 3, () -> {
            for (Handicap handicap : Handicap.values()) {
                helper.assertFalse(state(helper).underSpell(team, handicap), "the " + handicap.id() + " spell outran its length");
            }
            helper.assertTrue(AssaultState.get(server(helper)).assault(team).isEmpty(), "the Assault outlived its spell");
            AssaultAbsenceTests.leave(home);
            helper.succeed();
        });
    }

    /** Of two Athanors of a team, one works and the other does nothing, until the one that works is broken. */
    private static void secondDoesNothing(GameTestHelper helper) {
        UUID team = started(helper);
        helper.setBlock(GENERATOR, GENERATOR_BLOCK);
        helper.setBlock(ATHANOR, Athanor.BLOCK.get());
        helper.setBlock(SECOND, Athanor.BLOCK.get());
        athanor(helper).feed(nullessence(64));
        athanor(helper, SECOND).feed(nullessence(64));
        helper.assertTrue(Spells.begin(server(helper), team, Handicap.ENERGY, 2 * PAID + AthanorBlockEntity.INTERVAL), "the energy spell wasn't taken on");
        helper.runAtTickTime(PAID, () -> {
            var phases = List.of(athanor(helper).phase(), athanor(helper, SECOND).phase());
            helper.assertTrue(phases.contains(AthanorBlockEntity.Phase.WORKING) && phases.contains(AthanorBlockEntity.Phase.SECOND),
                    "two Athanors of a team are " + phases);
            BlockPos working = athanor(helper).phase() == AthanorBlockEntity.Phase.WORKING ? ATHANOR : SECOND;
            helper.setBlock(working, Blocks.AIR);
        });
        helper.runAtTickTime(2 * PAID, () -> {
            BlockPos left = helper.getBlockState(ATHANOR).is(Athanor.BLOCK.get()) ? ATHANOR : SECOND;
            helper.assertValueEqual(athanor(helper, left).phase(), AthanorBlockEntity.Phase.WORKING, "the Athanor left once the other broke");
            helper.succeed();
        });
    }

    /** A spell runs its length: it can't be taken on again, nor stopped at the Athanor, and its handicap ends with it. */
    private static void spellRunsItsLength(GameTestHelper helper) {
        UUID team = started(helper);
        helper.setBlock(LODESTONE, GeneratorTests.GENERATOR);
        helper.setBlock(ATHANOR, Athanor.BLOCK.get());
        Player player = member(helper, team);
        helper.assertTrue(Spells.begin(server(helper), team, Handicap.ENERGY, SHORT), "the energy spell wasn't taken on");
        helper.assertFalse(Spells.begin(server(helper), team, Handicap.ENERGY), "the energy spell was taken on twice");
        AthanorMenu menu = (AthanorMenu) athanor(helper).createMenu(0, player.getInventory(), player);
        helper.assertFalse(menu.clickMenuButton(player, Handicap.ENERGY.ordinal()), "the Athanor's button took the running spell on");
        helper.assertValueEqual(GeneratorTests.draw(helper, LODESTONE), 0, "FE drawn from a generator under the energy spell");
        helper.runAtTickTime(SHORT + 2, () -> {
            helper.assertFalse(state(helper).underSpell(team, Handicap.ENERGY), "the energy spell outran its length");
            helper.assertValueEqual(GeneratorTests.draw(helper, LODESTONE), GeneratorTests.DRAW, "FE drawn from a generator once the spell ended");
            helper.assertTrue(Spells.begin(server(helper), team, Handicap.ENERGY, 1), "the energy spell wasn't taken on again once it ended");
        });
        helper.runAtTickTime(SHORT + 5, helper::succeed);
    }

    /** A team of the test's island that has won all four challenges and started its last stand. */
    private static UUID started(GameTestHelper helper) {
        UUID team = TestTeams.claim(helper);
        win(helper, team);
        helper.assertTrue(LastStands.begin(server(helper), team), "the last stand didn't start");
        return team;
    }

    private static void win(GameTestHelper helper, UUID team) {
        for (Handicap handicap : Handicap.values()) {
            helper.assertTrue(state(helper).blockTier(team, handicap), "the " + handicap.id() + " challenge wasn't noted won");
        }
    }

    private static ItemStack nullessence(int count) {
        return new ItemStack(Essences.item(Essence.NULLESSENCE), count);
    }

    private static Player member(GameTestHelper helper, UUID team) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        TestTeams.join(player, team);
        return player;
    }

    private static AthanorBlockEntity athanor(GameTestHelper helper) {
        return athanor(helper, ATHANOR);
    }

    private static AthanorBlockEntity athanor(GameTestHelper helper, BlockPos pos) {
        return helper.getBlockEntity(pos, AthanorBlockEntity.class);
    }

    private static TeamState state(GameTestHelper helper) {
        return TeamState.get(server(helper));
    }

    private static MinecraftServer server(GameTestHelper helper) {
        return helper.getLevel().getServer();
    }
}
