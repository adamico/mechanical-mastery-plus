// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.cod;

/**
 * C.O.D.'s pace while a challenge is played: its waking pace, grown by a share of it for each minute since the
 * challenge began, without end, so {@code growth} 0.1 makes it twice the waking pace ten minutes in. A function of
 * that time alone, so a challenge that begins again starts again from the waking pace.
 */
final class CodPace {

    private static final double TICKS_PER_MINUTE = 1200.0;

    private CodPace() {
    }

    /**
     * The ticks C.O.D. takes to make one essence {@code elapsed} ticks into a challenge, from its {@code waking} interval,
     * to the nearest tick and never under one.
     */
    static int interval(int waking, double growth, long elapsed) {
        double speedup = 1 + growth * Math.max(0, elapsed) / TICKS_PER_MINUTE;
        return (int) Math.max(1, Math.round(waking / speedup));
    }
}
