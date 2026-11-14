// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.life;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/** A Homunculus works faster on a better meal: its interval shrinks with the meal's quality, never under a tick. */
class HomunculusPaceTest {

    @Test
    void noQualityKeepsTheInterval() {
        assertEquals(40, HomunculusPace.interval(40, 0.5, 0));
    }

    @Test
    void eachLevelShortensIt() {
        assertEquals(27, HomunculusPace.interval(40, 0.5, 1));
        assertEquals(20, HomunculusPace.interval(40, 0.5, 2));
        assertEquals(16, HomunculusPace.interval(40, 0.5, 3));
    }

    @Test
    void neverUnderOneTick() {
        assertEquals(1, HomunculusPace.interval(1, 100, 3));
    }

    @Test
    void noSpeedUpMakesQualityNoFaster() {
        assertEquals(40, HomunculusPace.interval(40, 0, 3));
    }
}
