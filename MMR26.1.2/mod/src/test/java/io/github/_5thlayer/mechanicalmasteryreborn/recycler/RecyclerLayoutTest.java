// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.recycler;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import org.junit.jupiter.api.Test;

/** A Recycler's places read clockwise from above, from the front-left one, for each way its core faces. */
class RecyclerLayoutTest {

    @Test
    void facingSouthStartsSouthWest() {
        assertArrayEquals(new int[][] {{-1, 1}, {-1, 0}, {-1, -1}, {0, -1}, {1, -1}, {1, 0}, {1, 1}, {0, 1}},
                RecyclerLayout.clockwiseFromFrontLeft(0, 1));
    }

    @Test
    void facingNorthStartsNorthEast() {
        assertArrayEquals(new int[][] {{1, -1}, {1, 0}, {1, 1}, {0, 1}, {-1, 1}, {-1, 0}, {-1, -1}, {0, -1}},
                RecyclerLayout.clockwiseFromFrontLeft(0, -1));
    }

    @Test
    void facingEastStartsSouthEast() {
        assertArrayEquals(new int[][] {{1, 1}, {0, 1}, {-1, 1}, {-1, 0}, {-1, -1}, {0, -1}, {1, -1}, {1, 0}},
                RecyclerLayout.clockwiseFromFrontLeft(1, 0));
    }

    @Test
    void facingWestStartsNorthWest() {
        assertArrayEquals(new int[][] {{-1, -1}, {0, -1}, {1, -1}, {1, 0}, {1, 1}, {0, 1}, {-1, 1}, {-1, 0}},
                RecyclerLayout.clockwiseFromFrontLeft(-1, 0));
    }
}
