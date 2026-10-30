// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.recycler;

/**
 * Where a Recycler's eight places are, and the order its outputs are read in. Its core stands in the middle of
 * the 3x3's front edge, its front facing out, so the 3x3's centre is the place right behind it. The places read
 * clockwise seen from above, around the centre, from the front-left one, which is on the left of a player facing
 * the core's front, skipping the core; the centre comes last. A mix's result <i>i</i> lands on the <i>i</i>th
 * output position, three to an output. Plain offsets, so a JUnit test reads it without Minecraft.
 */
public final class RecyclerLayout {

    /** Positions on one output's shelf. */
    public static final int POSITIONS_PER_OUTPUT = 3;

    /** The ring around a centre as {x, z} offsets, clockwise from above (x east, z south), from north. */
    private static final int[][] RING = {{0, -1}, {1, -1}, {1, 0}, {1, 1}, {0, 1}, {-1, 1}, {-1, 0}, {-1, -1}};

    private RecyclerLayout() {
    }

    /**
     * The eight places as {x, z} offsets from the core, in the order outputs are read, for a core whose front
     * faces {@code (frontX, frontZ)}, a horizontal unit step.
     */
    public static int[][] places(int frontX, int frontZ) {
        // The centre is one step behind the core. A player at the front looks along (-frontX, -frontZ); the left
        // of a look (x, z) is (z, -x), so the front-left place is (frontX - frontZ, frontZ + frontX) from the centre.
        int startX = frontX - frontZ;
        int startZ = frontZ + frontX;
        int start = 0;
        while (RING[start][0] != startX || RING[start][1] != startZ) {
            start++;
        }
        int[][] places = new int[RING.length][];
        int n = 0;
        for (int i = 0; i < RING.length; i++) {
            int[] around = RING[(start + i) % RING.length];
            if (around[0] != frontX || around[1] != frontZ) {
                places[n++] = new int[] {around[0] - frontX, around[1] - frontZ};
            }
        }
        places[n] = new int[] {-frontX, -frontZ};
        return places;
    }

    /** Whether {@code (x, z)} from a core whose front faces {@code (frontX, frontZ)} is one of its eight places. */
    public static boolean isPlace(int frontX, int frontZ, int x, int z) {
        int fromCentreX = x + frontX;
        int fromCentreZ = z + frontZ;
        return Math.abs(fromCentreX) <= 1 && Math.abs(fromCentreZ) <= 1 && (fromCentreX != frontX || fromCentreZ != frontZ);
    }
}
