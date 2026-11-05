// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.record;

/**
 * A team's last stand (ADR 0014), from the moment it starts it at the lab: its {@code lead}, what its Quintessence has
 * cancelled less every Nullessence C.O.D. has made since, never below minus the margin; the team's {@code pace}, what its
 * Quintessence cancels a minute, smoothed over the lag; C.O.D.'s {@code rate}, the Nullessence it makes a minute, which
 * follows that pace a lag behind, between its floor and its ceiling; and whether it is {@code won}, once the Lead reached the
 * margin, when C.O.D. shut down for good and its rate is none. {@link TeamState} keeps one per team that started it, and
 * nothing ever takes it back. Plain data, so a JUnit test reads it without Minecraft; {@link TeamState} saves it.
 */
public record LastStand(long lead, double pace, double rate, boolean won) {

    private static final double TICKS_PER_MINUTE = 1200.0;

    /** The last stand as it starts: level, with nothing cancelled yet and C.O.D. at its {@code floor}. */
    public static LastStand starting(double floor) {
        return new LastStand(0, 0, floor, false);
    }

    /**
     * This, a tick on: the team's pace falls off by a {@code lag}'s share, and C.O.D.'s rate moves by that share toward
     * the pace, held between {@code floor} and {@code ceiling}, so it follows the team a lag behind and settles at the
     * floor once the team stalls. A ceiling below the floor holds it at the floor. Once won, nothing changes.
     */
    LastStand afterTick(double floor, double ceiling, int lag) {
        if (won) {
            return this;
        }
        double share = 1.0 / Math.max(1, lag);
        double falling = pace * (1 - share);
        double target = Math.min(Math.max(falling, floor), Math.max(floor, ceiling));
        return new LastStand(lead, falling, rate + (target - rate) * share, false);
    }

    /**
     * This, with {@code amount} more cancelled by Quintessence: the Lead up by it, and the team's pace by its share of a
     * minute over the {@code lag}, so what is cancelled at a steady rate brings the pace to that rate. C.O.D.'s rate moves
     * only on the next {@link #afterTick tick}.
     */
    LastStand plusCancelled(long amount, int lag) {
        if (won || amount == 0) {
            return this;
        }
        return new LastStand(lead + amount, pace + amount * TICKS_PER_MINUTE / Math.max(1, lag), rate, false);
    }

    /** This, with one more Nullessence made: the Lead down by one, never below minus the {@code margin}. */
    LastStand plusOneMade(long margin) {
        return won ? this : new LastStand(Math.max(-margin, lead - 1), pace, rate, false);
    }

    /** This, won: C.O.D. shut down for good, its rate none. */
    LastStand shutDown() {
        return new LastStand(lead, 0, 0, true);
    }

    /** The ticks C.O.D. takes to make one Nullessence at its rate, to the nearest tick and never under one. */
    public int interval() {
        return (int) Math.max(1, Math.min(Integer.MAX_VALUE, Math.round(TICKS_PER_MINUTE / Math.max(rate, 1e-9))));
    }
}
