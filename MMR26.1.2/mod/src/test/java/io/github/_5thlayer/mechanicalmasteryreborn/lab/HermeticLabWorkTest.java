// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.lab;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * The Hermetic Lab's pace and cost by how many lines count (#263, ADR 0014), at their defaults: each more line is faster and
 * cheaper, and only all four make a Quintessence for fewer Nullessence than it cancels.
 */
class HermeticLabWorkTest {

    @Test
    void noLineMakesNothing() {
        assertEquals(0.0, HermeticLabWork.progressPerTick(0, HermeticLabWork.DEFAULT_TICKS));
    }

    @Test
    void eachMoreLineIsFasterAndCheaper() {
        for (int lines = 2; lines <= HermeticLabWork.LINES; lines++) {
            assertTrue(HermeticLabWork.progressPerTick(lines, HermeticLabWork.DEFAULT_TICKS) > HermeticLabWork.progressPerTick(lines - 1, HermeticLabWork.DEFAULT_TICKS),
                    lines + " lines aren't faster than " + (lines - 1));
            assertTrue(HermeticLabWork.cost(lines, HermeticLabWork.DEFAULT_COSTS) < HermeticLabWork.cost(lines - 1, HermeticLabWork.DEFAULT_COSTS),
                    lines + " lines aren't cheaper than " + (lines - 1));
        }
    }

    @Test
    void onlyAllFourBeatBreakEven() {
        int cancels = HermeticLabWork.DEFAULT_QUINTESSENCE_CANCELS;
        assertTrue(HermeticLabWork.cost(HermeticLabWork.LINES, HermeticLabWork.DEFAULT_COSTS) < cancels, "all four lines don't beat break-even");
        for (int lines = 1; lines < HermeticLabWork.LINES; lines++) {
            assertTrue(HermeticLabWork.cost(lines, HermeticLabWork.DEFAULT_COSTS) > cancels, lines + " lines beat break-even");
        }
    }
}
