// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.record;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * The last stand's sums (ADR 0014): the Lead, what Quintessence cancelled less what C.O.D. made, never below minus the
 * margin; and C.O.D.'s rate, which follows the team's pace a lag behind, between its floor and its ceiling.
 */
class LastStandTest {

    private static final double FLOOR = 10;
    private static final double CEILING = 60;
    private static final int LAG = 100;
    private static final long MARGIN = 50;

    private static LastStand ticks(LastStand stand, int ticks) {
        for (int i = 0; i < ticks; i++) {
            stand = stand.afterTick(FLOOR, CEILING, LAG);
        }
        return stand;
    }

    @Test
    void itStartsLevelAtTheFloor() {
        LastStand stand = LastStand.starting(FLOOR);
        assertEquals(0, stand.lead());
        assertEquals(FLOOR, stand.rate());
        assertFalse(stand.won());
    }

    @Test
    void withNothingCancelledTheRateStaysAtTheFloor() {
        assertEquals(FLOOR, ticks(LastStand.starting(FLOOR), 10 * LAG).rate(), 1e-9);
    }

    @Test
    void aRateAboveTheFloorSettlesBackToItOnceTheTeamStalls() {
        LastStand stand = ticks(new LastStand(0, 40, 40, false), 20 * LAG);
        assertEquals(FLOOR, stand.rate(), 0.01);
    }

    @Test
    void cancellingMovesTheLeadByWhatItCancels() {
        assertEquals(64, LastStand.starting(FLOOR).plusCancelled(64, LAG).lead());
    }

    @Test
    void eachNullessenceMadeTakesOneFromTheLead() {
        assertEquals(-2, LastStand.starting(FLOOR).plusOneMade(MARGIN).plusOneMade(MARGIN).lead());
    }

    @Test
    void theLeadNeverFallsBelowMinusTheMargin() {
        LastStand stand = new LastStand(-MARGIN, 0, FLOOR, false).plusOneMade(MARGIN);
        assertEquals(-MARGIN, stand.lead());
        // So a team that stalled loses nothing more, and what it cancels next counts from there.
        assertEquals(-MARGIN + 5, stand.plusCancelled(5, LAG).lead());
    }

    @Test
    void theRateFollowsTheTeamsPaceALagBehind() {
        // A steady 40 a minute, a share each tick: the team's pace reaches it, and C.O.D.'s rate after it.
        LastStand stand = LastStand.starting(FLOOR);
        double perTick = 40.0 / 1200;
        double cancelled = 0;
        for (int i = 0; i < 2 * LAG; i++) {
            cancelled += perTick;
            long whole = (long) cancelled;
            cancelled -= whole;
            stand = stand.plusCancelled(whole, LAG).afterTick(FLOOR, CEILING, LAG);
        }
        double soon = stand.rate();
        assertTrue(soon > FLOOR, "the rate didn't rise: " + soon);
        assertTrue(soon < 40, "the rate rose with no lag: " + soon);
        for (int i = 0; i < 30 * LAG; i++) {
            cancelled += perTick;
            long whole = (long) cancelled;
            cancelled -= whole;
            stand = stand.plusCancelled(whole, LAG).afterTick(FLOOR, CEILING, LAG);
        }
        assertEquals(40, stand.rate(), 1.0);
    }

    @Test
    void aHandfulAtOnceRaisesTheRateOnlyAfterALag() {
        LastStand stand = LastStand.starting(FLOOR).plusCancelled(1000, LAG);
        assertEquals(FLOOR, stand.rate(), "the rate jumped as the handful went in");
        assertTrue(stand.afterTick(FLOOR, CEILING, LAG).rate() < FLOOR + 1, "the rate jumped a tick later");
    }

    @Test
    void theRateNeverPassesTheCeiling() {
        LastStand stand = LastStand.starting(FLOOR);
        for (int i = 0; i < 50 * LAG; i++) {
            stand = stand.plusCancelled(10, LAG).afterTick(FLOOR, CEILING, LAG);
            assertTrue(stand.rate() <= CEILING, "the rate passed the ceiling: " + stand.rate());
        }
        assertEquals(CEILING, stand.rate(), 0.01);
    }

    @Test
    void aCeilingBelowTheFloorHoldsTheRateAtTheFloor() {
        assertEquals(FLOOR, LastStand.starting(FLOOR).plusCancelled(1000, LAG).afterTick(FLOOR, FLOOR / 2, LAG).rate(), 1e-9);
    }

    @Test
    void theIntervalIsAMinuteOverTheRate() {
        assertEquals(120, new LastStand(0, 0, 10, false).interval());
        assertEquals(1, new LastStand(0, 0, 100_000, false).interval());
    }

    @Test
    void shutDownItIsWonAndMakesNothingMore() {
        LastStand stand = new LastStand(MARGIN, 30, 30, false).shutDown();
        assertTrue(stand.won());
        assertEquals(0, stand.rate());
        assertEquals(stand, stand.afterTick(FLOOR, CEILING, LAG));
        assertEquals(stand, stand.plusOneMade(MARGIN));
        assertEquals(stand, stand.plusCancelled(5, LAG));
    }
}
