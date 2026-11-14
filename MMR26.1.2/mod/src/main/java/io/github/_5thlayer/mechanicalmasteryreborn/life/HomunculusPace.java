// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.life;

/** How fast a Homunculus works on a meal (#367): the better the meal's quality, the shorter its interval. */
public final class HomunculusPace {

    private HomunculusPace() {
    }

    /**
     * The ticks between two of a Homunculus's works on a meal of Quality Food {@code level}: {@code baseInterval}
     * divided by one plus {@code speedUp} for each level, rounded, never under one tick.
     */
    public static int interval(int baseInterval, double speedUp, int level) {
        return Math.max(1, (int) Math.round(baseInterval / (1 + speedUp * Math.max(0, level))));
    }
}
