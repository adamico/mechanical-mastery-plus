// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.cod;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/** While a challenge is played, C.O.D.'s pace grows from its waking pace by a share of it each minute, without end. */
class CodPaceTest {

    private static final int MINUTE = 1200;

    @Test
    void asTheChallengeBeginsItIsTheWakingPace() {
        assertEquals(40, CodPace.interval(40, 0.1, 0));
    }

    @Test
    void itGrowsByAShareOfTheWakingPaceEachMinute() {
        // A tenth more each minute: twice the waking pace ten minutes in, three times twenty minutes in.
        assertEquals(20, CodPace.interval(40, 0.1, 10 * MINUTE));
        assertEquals(13, CodPace.interval(40, 0.1, 20 * MINUTE));
    }

    @Test
    void itGrowsWithinAMinuteToo() {
        // 400 ticks is a third of a minute: 40 / (1 + 0.1 / 3) is 38.7.
        assertEquals(39, CodPace.interval(40, 0.1, 400));
    }

    @Test
    void itNeverGoesFasterThanOneATick() {
        assertEquals(1, CodPace.interval(40, 0.1, 1000L * MINUTE));
    }

    @Test
    void noGrowthKeepsTheWakingPace() {
        assertEquals(40, CodPace.interval(40, 0.0, 60 * MINUTE));
    }
}
