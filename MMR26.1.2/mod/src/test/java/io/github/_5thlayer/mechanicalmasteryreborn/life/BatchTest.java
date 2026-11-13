// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.life;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * A Pelican's batch, the meals it has eaten since its last Vital Essence (#366): it is ready once its saturation, its
 * kinds and its average quality all reach the thresholds together.
 */
class BatchTest {

    private static final Batch.Thresholds THRESHOLDS = new Batch.Thresholds(60, 4, 1.5);

    @Test
    void anEmptyBatchIsNeverReady() {
        assertFalse(Batch.EMPTY.ready(new Batch.Thresholds(0, 0, 0)));
    }

    @Test
    void itAddsUpSaturationAndCountsKindsByItem() {
        Batch batch = Batch.EMPTY.eat("a:stew", 12, 2).eat("a:stew", 12, 1).eat("a:soup", 7.2, 0);
        assertEquals(31.2, batch.saturation(), 1e-9);
        assertEquals(2, batch.kinds().size());
        assertEquals(3, batch.meals());
    }

    @Test
    void itsQualityIsThePlainMeanOfTheLevelsNoneCountingAsZero() {
        Batch batch = Batch.EMPTY.eat("a:stew", 12, 3).eat("a:soup", 7, 0);
        assertEquals(1.5, batch.quality(), 1e-9);
    }

    @Test
    void allThreeTogetherMakeItReady() {
        Batch batch = Batch.EMPTY.eat("a:one", 15, 2).eat("a:two", 15, 2).eat("a:three", 15, 1).eat("a:four", 15, 1);
        assertTrue(batch.ready(THRESHOLDS));
    }

    @Test
    void tooLittleSaturationKeepsItWaiting() {
        Batch batch = Batch.EMPTY.eat("a:one", 14, 2).eat("a:two", 15, 2).eat("a:three", 15, 2).eat("a:four", 15, 2);
        assertFalse(batch.ready(THRESHOLDS));
    }

    @Test
    void tooFewKindsKeepItWaiting() {
        Batch batch = Batch.EMPTY.eat("a:one", 30, 2).eat("a:one", 30, 2).eat("a:two", 30, 2).eat("a:three", 30, 2);
        assertFalse(batch.ready(THRESHOLDS));
    }

    @Test
    void tooLowAQualityKeepsItWaiting() {
        Batch batch = Batch.EMPTY.eat("a:one", 15, 2).eat("a:two", 15, 2).eat("a:three", 15, 1).eat("a:four", 15, 0);
        assertFalse(batch.ready(THRESHOLDS));
    }
}
