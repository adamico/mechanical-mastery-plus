// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.recycler;

/**
 * Where a Recycler's eight places are, around its core, and the order its outputs are read in: clockwise
 * seen from above, from the front-left place, which is on the left of a player facing the core's front.
 * A mix's result <i>i</i> lands on the <i>i</i>th output position, three to an output. Plain offsets, so a
 * JUnit test reads it without Minecraft.
 */
public final class RecyclerLayout {

    /** Positions on one output's shelf. */
    public static final int POSITIONS_PER_OUTPUT = 3;

    /** The eight places as {x, z} offsets, clockwise from above (x east, z south), from north. */
    private static final int[][] RING = {{0, -1}, {1, -1}, {1, 0}, {1, 1}, {0, 1}, {-1, 1}, {-1, 0}, {-1, -1}};

    private RecyclerLayout() {
    }

    /**
     * The eight places as {x, z} offsets from the core, clockwise from the front-left one, for a core whose
     * front faces {@code (frontX, frontZ)}, a horizontal unit step.
     */
    public static int[][] clockwiseFromFrontLeft(int frontX, int frontZ) {
        // A player at the front looks along (-frontX, -frontZ); the left of a look (x, z) is (z, -x).
        int startX = frontX - frontZ;
        int startZ = frontZ + frontX;
        int start = 0;
        while (RING[start][0] != startX || RING[start][1] != startZ) {
            start++;
        }
        int[][] places = new int[RING.length][];
        for (int i = 0; i < RING.length; i++) {
            places[i] = RING[(start + i) % RING.length].clone();
        }
        return places;
    }
}
