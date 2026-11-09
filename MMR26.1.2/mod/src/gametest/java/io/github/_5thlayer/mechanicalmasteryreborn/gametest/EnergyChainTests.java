// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.gametest;

import java.util.UUID;

import io.github._5thlayer.mechanicalmasteryreborn.challenge.ChainMachines;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.Chains;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.Challenges;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.GalvanicCharges;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.Handicap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.GameType;

/**
 * The energy chain's Galvanic Charges (ADR 0018, #330). The three charges are the mod's items. Their recipes, and the
 * reformations that raise salt and sulfur to the forms the charges take, are the pack's, on Theurgy's apparatus and
 * Craftworks' Assembler; Theurgy can't run on the game tests' NeoForge, so {@code GalvanicChargeRecipesTest} reads them
 * and the energy stage file that names them. Here the stage itself: EMI marks the energy chain's recipes and Chapters
 * locks them until the team starts energy, so shards saved up from the Basic mix can't be packed before (ADR 0007).
 */
final class EnergyChainTests {

    private EnergyChainTests() {
    }

    static void register(MechanicalMasteryRebornGameTests.Registrar tests) {
        tests.test("energy_chain_has_three_galvanic_charges", 1, EnergyChainTests::threeCharges);
        tests.test("energy_chain_charges_locked_and_marked_until_the_energy_challenge_starts", 1, EnergyChainTests::lockedUntilStarted);
    }

    private static void threeCharges(GameTestHelper helper) {
        for (String name : new String[] {"mercury", "salt", "sulfur"}) {
            var id = Identifier.fromNamespaceAndPath("mechanicalmasteryreborn", name + "_charge");
            helper.assertTrue(BuiltInRegistries.ITEM.containsKey(id), "no " + id);
        }
        helper.assertValueEqual(GalvanicCharges.ALL.size(), 3, "the Galvanic Charges");
        helper.succeed();
    }

    private static void lockedUntilStarted(GameTestHelper helper) {
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
}
