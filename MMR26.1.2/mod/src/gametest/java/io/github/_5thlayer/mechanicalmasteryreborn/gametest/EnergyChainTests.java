// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.gametest;

import java.util.UUID;

import io.github._5thlayer.mechanicalmasteryreborn.ServerConfig;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.ChainMachines;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.Chains;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.Challenges;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.GalvanicParts;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.Handicap;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.LastStands;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.Spells;
import io.github._5thlayer.mechanicalmasteryreborn.record.TeamState;
import io.github._5thlayer.mechanicalmasteryreborn.compat.theurgy.BrazierHeat;
import io.github._5thlayer.mechanicalmasteryreborn.compat.theurgy.CatalystFuel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;

/**
 * The energy chain runs on banked FE (ADR 0018, #326). The Mercury Catalyst makes mercury flux only while it draws
 * {@code energy.fe_per_flux} FE for each unit, and none without it (#328). No Theurgy is on the game tests' runs, so
 * these test the Catalyst's {@link CatalystFuel}, the store a cable fills and the mixin asks before each unit of flux,
 * with a flux sink standing for the Catalyst's own.
 *
 * <p>The three Galvanic Charges are the mod's items (#330). Their recipes, and the reformations that raise salt and
 * sulfur to the forms the charges take, are the pack's, on Theurgy's apparatus and Craftworks' Assembler, so
 * {@code GalvanicChargeRecipesTest} reads them and the energy stage file that names them. Here the stage itself: EMI
 * marks the energy chain's recipes and Chapters locks them until the team starts energy, so shards saved up from the
 * Basic mix can't be packed before (ADR 0007).
 */
final class EnergyChainTests {

    private EnergyChainTests() {
    }

    static void register(MechanicalMasteryRebornGameTests.Registrar tests) {
        tests.test("chain_energy_catalyst_makes_no_flux_without_fe", 1, EnergyChainTests::noFlux);
        tests.test("chain_energy_catalyst_makes_flux_while_it_draws_fe", 1, EnergyChainTests::drawsFe);
        tests.test("chain_energy_catalyst_makes_only_the_flux_its_fe_pays_for", 1, EnergyChainTests::onlyWhatItPaysFor);
        tests.test("chain_energy_catalyst_pays_only_for_the_flux_it_stores", 1, EnergyChainTests::paysForStored);
        tests.test("chain_energy_catalyst_keeps_its_fe_through_a_config_reload", 1, EnergyChainTests::keepsFeOnReload);
        tests.test("chain_energy_catalyst_loads_no_more_fe_than_it_holds", 1, EnergyChainTests::loadsTrimmed);
        tests.test("chain_energy_brazier_gives_no_heat_while_the_team_plays_energy", 1, EnergyChainTests::brazierCold);
        tests.test("chain_energy_brazier_heats_again_after_a_win", 1, EnergyChainTests::brazierHeatsAfterWin);
        tests.test("chain_energy_brazier_heats_again_after_a_stop", 1, EnergyChainTests::brazierHeatsAfterStop);
        tests.test("chain_energy_brazier_heats_under_the_energy_spell", 1, EnergyChainTests::brazierHeatsUnderSpell);
        tests.test("chain_energy_has_three_galvanic_charges", 1, EnergyChainTests::threeCharges);
        tests.test("chain_energy_has_a_galvanic_core", 1, EnergyChainTests::galvanicCore);
        tests.test("chain_energy_charges_locked_and_marked_until_the_energy_challenge_starts", 1, EnergyChainTests::chargesLockedUntilStarted);
    }

    private static void noFlux(GameTestHelper helper) {
        var fuel = new CatalystFuel(() -> { });
        helper.assertValueEqual(fuel.convert(10, flux -> flux), 0, "the flux made with no FE");
        helper.succeed();
    }

    private static void drawsFe(GameTestHelper helper) {
        int rate = ServerConfig.energyFePerFlux();
        var fuel = new CatalystFuel(() -> { });
        fill(fuel, rate * 10);
        helper.assertValueEqual(fuel.convert(4, flux -> flux), 4, "the flux made with FE for ten");
        helper.assertValueEqual(fuel.getAmountAsInt(), rate * 6, "the FE left after four flux");
        helper.succeed();
    }

    private static void onlyWhatItPaysFor(GameTestHelper helper) {
        int rate = ServerConfig.energyFePerFlux();
        var fuel = new CatalystFuel(() -> { });
        fill(fuel, rate * 3 + rate / 2);
        helper.assertValueEqual(fuel.convert(5, flux -> flux), 3, "the flux made with FE for three and a half");
        helper.assertValueEqual(fuel.getAmountAsInt(), rate / 2, "the FE left, short of a unit");
        helper.assertValueEqual(fuel.convert(5, flux -> flux), 0, "the flux made once the FE is short of a unit");
        helper.succeed();
    }

    /** A full flux store takes less than the Catalyst would make, and the FE for the rest stays banked. */
    private static void paysForStored(GameTestHelper helper) {
        int rate = ServerConfig.energyFePerFlux();
        var fuel = new CatalystFuel(() -> { });
        fill(fuel, rate * 10);
        helper.assertValueEqual(fuel.convert(5, flux -> Math.min(flux, 2)), 2, "the flux the store took");
        helper.assertValueEqual(fuel.getAmountAsInt(), rate * 8, "the FE left after two flux stored");
        helper.succeed();
    }

    /** A config reload sizes the store again, and keeps the FE that still fits. */
    private static void keepsFeOnReload(GameTestHelper helper) {
        int rate = ServerConfig.energyFePerFlux();
        var fuel = new CatalystFuel(() -> { });
        fill(fuel, rate * 10);
        CatalystFuel.configReloaded();
        helper.assertValueEqual(fuel.convert(0, flux -> flux), 0, "the flux made when none was asked");
        helper.assertValueEqual(fuel.getAmountAsInt(), rate * 10, "the FE kept through the reload");
        helper.succeed();
    }

    /**
     * A store saved under a smaller {@code energy.fe_per_flux} than it loads under holds more FE than fits: it loads
     * only what fits (#327, where Jade's bar failed on it).
     */
    private static void loadsTrimmed(GameTestHelper helper) {
        var fuel = new CatalystFuel(() -> { });
        var saved = TagValueOutput.createWithoutContext(ProblemReporter.DISCARDING);
        saved.putInt("energy", fuel.getCapacityAsInt() * 4);
        fuel.deserialize(TagValueInput.create(ProblemReporter.DISCARDING, helper.getLevel().registryAccess(), saved.buildResult()));
        helper.assertValueEqual(fuel.getAmountAsInt(), fuel.getCapacityAsInt(), "the FE loaded");
        helper.succeed();
    }

    /** Where a Brazier would stand under a chain machine, on the test's island. */
    private static final BlockPos BRAZIER = new BlockPos(2, 2, 2);

    /**
     * The Brazier's heat while the team plays energy (#329). A Caloric Flux Emitter heats a machine through its heat
     * receiver, which the Brazier's check never touches, so it heats on: with no Theurgy here, that is checked in game.
     */
    private static void brazierCold(GameTestHelper helper) {
        UUID team = TestTeams.claim(helper);
        var server = helper.getLevel().getServer();
        var at = helper.absolutePos(BRAZIER);
        helper.assertFalse(BrazierHeat.cold(helper.getLevel(), at), "the Brazier was cold before the energy challenge");
        helper.assertTrue(Challenges.begin(server, team, Handicap.ENERGY), "the energy challenge didn't start");
        helper.assertTrue(BrazierHeat.cold(helper.getLevel(), at), "the Brazier heated while the team plays energy");
        helper.succeed();
    }

    private static void brazierHeatsAfterWin(GameTestHelper helper) {
        UUID team = TestTeams.claim(helper);
        var server = helper.getLevel().getServer();
        helper.assertTrue(Challenges.begin(server, team, Handicap.ENERGY), "the energy challenge didn't start");
        helper.assertTrue(Challenges.win(server, team), "the energy challenge wasn't won");
        helper.assertFalse(BrazierHeat.cold(helper.getLevel(), helper.absolutePos(BRAZIER)), "the Brazier stayed cold after the win");
        helper.succeed();
    }

    private static void brazierHeatsAfterStop(GameTestHelper helper) {
        UUID team = TestTeams.claim(helper);
        var server = helper.getLevel().getServer();
        helper.assertTrue(Challenges.begin(server, team, Handicap.ENERGY), "the energy challenge didn't start");
        helper.assertTrue(Challenges.stop(server, team), "the energy challenge wasn't stopped");
        helper.assertFalse(BrazierHeat.cold(helper.getLevel(), helper.absolutePos(BRAZIER)), "the Brazier stayed cold after the stop");
        helper.succeed();
    }

    /**
     * Under the last stand's energy spell a Brazier heats on: generators feed only the Hermetic Lab and banked FE stands
     * still, so a cold Brazier would leave the chain no heat at all.
     */
    private static void brazierHeatsUnderSpell(GameTestHelper helper) {
        UUID team = TestTeams.claim(helper);
        var server = helper.getLevel().getServer();
        for (Handicap handicap : Handicap.values()) {
            helper.assertTrue(TeamState.get(server).blockTier(team, handicap), "the " + handicap.id() + " challenge wasn't noted won");
        }
        helper.assertTrue(LastStands.begin(server, team), "the last stand didn't start");
        helper.assertTrue(Spells.begin(server, team, Handicap.ENERGY), "the energy spell wasn't taken on");
        helper.assertFalse(BrazierHeat.cold(helper.getLevel(), helper.absolutePos(BRAZIER)), "the Brazier went cold under the energy spell");
        helper.succeed();
    }

    private static void threeCharges(GameTestHelper helper) {
        for (String name : new String[] {"mercury", "salt", "sulfur"}) {
            var id = Identifier.fromNamespaceAndPath("mechanicalmasteryreborn", name + "_charge");
            helper.assertTrue(BuiltInRegistries.ITEM.containsKey(id), "no " + id);
        }
        helper.assertValueEqual(GalvanicParts.CHARGES.size(), 3, "the Galvanic Charges");
        helper.succeed();
    }

    private static void galvanicCore(GameTestHelper helper) {
        var id = Identifier.fromNamespaceAndPath("mechanicalmasteryreborn", "galvanic_core");
        helper.assertTrue(BuiltInRegistries.ITEM.containsKey(id), "no " + id);
        helper.succeed();
    }

    private static void chargesLockedUntilStarted(GameTestHelper helper) {
        UUID team = TestTeams.claim(helper);
        var server = helper.getLevel().getServer();
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        TestTeams.join(player, team);
        Chains.unlock(server, player);
        helper.assertTrue(ChainMachines.table(server, team).locked(Handicap.ENERGY), "the charges were unmarked before the energy challenge");
        helper.assertFalse(TestStages.holds(player, Handicap.ENERGY.id()), "the charges were unlocked before the energy challenge");
        helper.assertTrue(Challenges.begin(server, team, Handicap.ENERGY), "the energy challenge didn't start");
        Chains.unlock(server, player);
        helper.assertFalse(ChainMachines.table(server, team).locked(Handicap.ENERGY), "the charges stayed marked once the energy challenge started");
        helper.assertTrue(TestStages.holds(player, Handicap.ENERGY.id()), "the charges stayed locked once the energy challenge started");
        helper.succeed();
    }

    private static void fill(EnergyHandler fuel, int amount) {
        try (var transaction = Transaction.openRoot()) {
            int inserted = fuel.insert(amount, transaction);
            transaction.commit();
            if (inserted != amount) {
                throw new IllegalStateException("the store took " + inserted + " of " + amount);
            }
        }
    }
}
