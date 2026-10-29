// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.recipe;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/** A yield returns the whole part of an amount, and what is left over as the chance of one more. */
class YieldTest {

    @Test
    void aWholeShareComesBackWhatever() {
        assertEquals(1, Yield.roll(4, 0.25, () -> 0.99));
    }

    @Test
    void aLeftoverIsTheChanceOfOneMore() {
        // A piston's three planks at 25%: 0.75, so one plank whenever the roll comes under it.
        assertEquals(1, Yield.roll(3, 0.25, () -> 0.74));
        assertEquals(0, Yield.roll(3, 0.25, () -> 0.75));
    }

    @Test
    void wholeAndLeftoverAddUp() {
        assertEquals(3, Yield.roll(9, 0.25, () -> 0.2));
        assertEquals(2, Yield.roll(9, 0.25, () -> 0.3));
    }

    @Test
    void aShareOfARecipeThatMakesSeveral() {
        // One of four sticks, from two planks, at 25%: an eighth of a plank.
        assertEquals(1, Yield.roll(2.0 / 4, 0.25, () -> 0.12));
        assertEquals(0, Yield.roll(2.0 / 4, 0.25, () -> 0.13));
    }

    @Test
    void aFullYieldOfAThirdTimesThreeIsOne() {
        // Arithmetic that lands a hair under a whole still returns it.
        assertEquals(1, Yield.roll(3 * (1.0 / 3), 1.0, () -> 0.9999999));
    }

    @Test
    void theMostARollCanReturn() {
        assertEquals(1, Yield.most(3, 0.25));
        assertEquals(1, Yield.most(4, 0.25));
        assertEquals(3, Yield.most(9, 0.25));
        assertEquals(1, Yield.most(3 * (1.0 / 3), 1.0));
        assertEquals(0, Yield.most(0, 0.25));
    }
}
