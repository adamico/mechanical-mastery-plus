// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.recycler;

import java.util.Optional;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

/** A block that can stand in one of a Recycler's eight places, the rest of the 3x3 its core fronts, in one role. */
public interface RecyclerPart {

    /** What a part does in its Recycler (the line's glossary: Drive, Casing). */
    enum Role {
        DRIVE, INPUT, OUTPUT, CASING
    }

    Role role();

    /** The one core whose 3x3 holds {@code pos} as a place, the Recycler a part there belongs to; none if two do. */
    static Optional<RecyclerCoreBlockEntity> coreOf(Level level, BlockPos pos) {
        RecyclerCoreBlockEntity found = null;
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                if ((x != 0 || z != 0) && level.getBlockEntity(pos.offset(x, 0, z)) instanceof RecyclerCoreBlockEntity core
                        && core.holdsPlace(pos)) {
                    if (found != null) {
                        return Optional.empty();
                    }
                    found = core;
                }
            }
        }
        return Optional.ofNullable(found);
    }
}
