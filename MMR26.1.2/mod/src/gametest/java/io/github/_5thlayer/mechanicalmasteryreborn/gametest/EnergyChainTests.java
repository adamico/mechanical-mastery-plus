// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.gametest;

import java.util.UUID;

import io.github._5thlayer.mechanicalmasteryreborn.ServerConfig;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.ChainMachines;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.Chains;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.Challenges;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.GalvanicCharges;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.Handicap;
import io.github._5thlayer.mechanicalmasteryreborn.compat.theurgy.CatalystFuel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
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
        tests.test("chain_energy_has_three_galvanic_charges", 1, EnergyChainTests::threeCharges);
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

    private static void threeCharges(GameTestHelper helper) {
        for (String name : new String[] {"mercury", "salt", "sulfur"}) {
            var id = Identifier.fromNamespaceAndPath("mechanicalmasteryreborn", name + "_charge");
            helper.assertTrue(BuiltInRegistries.ITEM.containsKey(id), "no " + id);
        }
        helper.assertValueEqual(GalvanicCharges.ALL.size(), 3, "the Galvanic Charges");
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
