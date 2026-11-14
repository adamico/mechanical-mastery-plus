// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.gametest;

import java.util.List;

import io.github._5thlayer.mechanicalmasteryreborn.MechanicalMasteryReborn;
import io.github._5thlayer.libworks.testkit.WorksTabCheck;

/**
 * Every obtainable item of the mod is in the suite's Works tab, in {@code WorksTabContents}' order, and
 * no item of the mod is in another tab.
 */
final class CreativeTabTests {

    /** The paths of the Module's items, as the Works tab shows them. */
    private static final List<String> WORKS_TAB_ORDER = List.of("cod", "lab", "warding_port", "vital_port", "galvanic_port", "aureate_port", "basic_mechanical_essence", "regular_mechanical_essence",
            "improved_mechanical_essence", "advanced_mechanical_essence", "galvanic_essence", "warding_essence", "vital_essence", "aureate_essence", "quartessence", "quintessence", "nullessence", "rift_shard", "ward_tallow_bucket", "obsidian_flakes", "charged_rift", "molten_copper_bucket", "molten_iron_bucket", "molten_gold_bucket", "mold", "copper_cast", "iron_cast", "gold_cast", "burnt_sand", "copper_coin", "iron_coin", "gold_coin", "reformed_salt", "reformed_sulfur", "mercury_charge", "salt_charge", "sulfur_charge", "galvanic_core", "null_shard", "recycler_core_1", "recycler_core_2", "recycler_core_3", "recycler_core_4", "hand_drive", "fe_drive", "recycler_input", "recycler_output",
            "recycler_casing", "fe_defence", "wave_caller", "pelican", "homunculus_stand", "homunculus");

    private CreativeTabTests() {
    }

    static void register(MechanicalMasteryRebornGameTests.Registrar tests) {
        tests.test("works_tab_holds_every_item_in_order", 1,
                helper -> WorksTabCheck.inOrder(helper, MechanicalMasteryReborn.MOD_ID, WORKS_TAB_ORDER));
        tests.test("no_other_tab_holds_a_module_item", 1, helper -> WorksTabCheck.noOtherTab(helper, MechanicalMasteryReborn.MOD_ID));
    }
}
