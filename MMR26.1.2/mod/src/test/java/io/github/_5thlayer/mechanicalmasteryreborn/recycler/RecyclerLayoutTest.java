// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.recycler;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * A Recycler's places, as offsets from its core on the 3x3's front edge: clockwise from above around the centre,
 * from the front-left one, skipping the core, then the centre, for each way its core faces.
 */
class RecyclerLayoutTest {

    @Test
    void facingSouthStartsSouthWestOfTheCentre() {
        assertArrayEquals(new int[][] {{-1, 0}, {-1, -1}, {-1, -2}, {0, -2}, {1, -2}, {1, -1}, {1, 0}, {0, -1}},
                RecyclerLayout.places(0, 1));
    }

    @Test
    void facingNorthStartsNorthEastOfTheCentre() {
        assertArrayEquals(new int[][] {{1, 0}, {1, 1}, {1, 2}, {0, 2}, {-1, 2}, {-1, 1}, {-1, 0}, {0, 1}},
                RecyclerLayout.places(0, -1));
    }

    @Test
    void facingEastStartsSouthEastOfTheCentre() {
        assertArrayEquals(new int[][] {{0, 1}, {-1, 1}, {-2, 1}, {-2, 0}, {-2, -1}, {-1, -1}, {0, -1}, {-1, 0}},
                RecyclerLayout.places(1, 0));
    }

    @Test
    void facingWestStartsNorthWestOfTheCentre() {
        assertArrayEquals(new int[][] {{0, -1}, {1, -1}, {2, -1}, {2, 0}, {2, 1}, {1, 1}, {0, 1}, {1, 0}},
                RecyclerLayout.places(-1, 0));
    }

    @Test
    void everyPlaceIsAPlaceAndNothingElseIs() {
        int[][] fronts = {{0, 1}, {0, -1}, {1, 0}, {-1, 0}};
        for (int[] front : fronts) {
            int[][] places = RecyclerLayout.places(front[0], front[1]);
            int found = 0;
            for (int x = -3; x <= 3; x++) {
                for (int z = -3; z <= 3; z++) {
                    boolean listed = false;
                    for (int[] place : places) {
                        listed |= place[0] == x && place[1] == z;
                    }
                    assertEquals(listed, RecyclerLayout.isPlace(front[0], front[1], x, z), "(" + x + ", " + z + ")");
                    found += listed ? 1 : 0;
                }
            }
            assertEquals(8, found);
            assertFalse(RecyclerLayout.isPlace(front[0], front[1], 0, 0), "the core's own place");
            assertTrue(RecyclerLayout.isPlace(front[0], front[1], -front[0], -front[1]), "the centre");
        }
    }
}
