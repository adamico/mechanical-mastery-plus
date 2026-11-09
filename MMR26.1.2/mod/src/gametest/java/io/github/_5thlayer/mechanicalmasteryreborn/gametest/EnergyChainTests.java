// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.gametest;

import io.github._5thlayer.mechanicalmasteryreborn.ServerConfig;
import io.github._5thlayer.mechanicalmasteryreborn.compat.theurgy.CatalystFuel;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;

/**
 * The energy chain runs on banked FE (ADR 0018, #326). The Mercury Catalyst makes mercury flux only while it draws
 * {@code energy.fe_per_flux} FE for each unit, and none without it (#328). No Theurgy is on the game tests' runs, so
 * these test the Catalyst's {@link CatalystFuel}, the store a cable fills and the mixin asks before each unit of flux,
 * with a flux sink standing for the Catalyst's own.
 */
final class EnergyChainTests {

    private EnergyChainTests() {
    }

    static void register(MechanicalMasteryRebornGameTests.Registrar tests) {
        tests.test("chain_energy_catalyst_makes_no_flux_without_fe", 1, EnergyChainTests::noFlux);
        tests.test("chain_energy_catalyst_makes_flux_while_it_draws_fe", 1, EnergyChainTests::drawsFe);
        tests.test("chain_energy_catalyst_makes_only_the_flux_its_fe_pays_for", 1, EnergyChainTests::onlyWhatItPaysFor);
        tests.test("chain_energy_catalyst_pays_only_for_the_flux_it_stores", 1, EnergyChainTests::paysForStored);
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
