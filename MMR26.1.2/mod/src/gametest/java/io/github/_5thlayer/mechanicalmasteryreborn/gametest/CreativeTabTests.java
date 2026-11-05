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
            "improved_mechanical_essence", "advanced_mechanical_essence", "galvanic_essence", "warding_essence", "vital_essence", "aureate_essence", "nullessence", "rift_shard", "ward_tallow_bucket", "obsidian_flakes", "charged_rift", "recycler_core_1", "recycler_core_2", "recycler_core_3", "recycler_core_4", "hand_drive", "fe_drive", "recycler_input", "recycler_output",
            "recycler_casing", "fe_defence");

    private CreativeTabTests() {
    }

    static void register(MechanicalMasteryRebornGameTests.Registrar tests) {
        tests.test("works_tab_holds_every_item_in_order", 1,
                helper -> WorksTabCheck.inOrder(helper, MechanicalMasteryReborn.MOD_ID, WORKS_TAB_ORDER));
        tests.test("no_other_tab_holds_a_module_item", 1, helper -> WorksTabCheck.noOtherTab(helper, MechanicalMasteryReborn.MOD_ID));
    }
}
