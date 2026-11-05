// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.gametest;

import io.github._5thlayer.craftworks.machine.AssemblerBlockEntity;
import io.github._5thlayer.craftworks.machine.AssemblerSlots;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.Items;

/**
 * The End Portal Frame, crafted on the Assembler from four calcite, four ender pearls and four nether wart, and placed
 * empty to take its eye as in vanilla (ADR 0012, #265): the pack's own recipe in {@code suite_pack/}, run on a {@link SuiteAssembler}. Its lock until the fourth win
 * is the pack's Chapters stage file, which no game test run loads. Only the {@code suiteGameTestServer} run, which
 * loads Craftworks, registers it.
 */
final class LastChapterFrameTests {

    private LastChapterFrameTests() {
    }

    static void register(MechanicalMasteryRebornGameTests.Registrar tests) {
        tests.test("suite_last_chapter_frame_assembles_from_calcite_pearls_and_nether_wart", 40, LastChapterFrameTests::assembles);
    }

    private static void assembles(GameTestHelper helper) {
        AssemblerBlockEntity assembler = SuiteAssembler.holding(helper, "end_portal_frame");
        SuiteAssembler.put(assembler, 0, Items.CALCITE, 4);
        SuiteAssembler.put(assembler, 1, Items.ENDER_PEARL, 4);
        SuiteAssembler.put(assembler, 2, Items.NETHER_WART, 4);
        SuiteAssembler.tickUntil(helper, assembler, () -> !assembler.inventory().getResource(AssemblerSlots.OUTPUT).isEmpty());
        SuiteAssembler.expectOutput(helper, assembler, Items.END_PORTAL_FRAME, "the frame's recipe");
        helper.succeed();
    }
}
