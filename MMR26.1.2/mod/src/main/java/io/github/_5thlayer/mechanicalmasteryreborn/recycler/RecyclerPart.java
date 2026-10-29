// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.recycler;

import java.util.Optional;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

/** A block that can stand in one of a Recycler's eight places around its core, in one role. */
public interface RecyclerPart {

    /** What a part does in its Recycler (the line's glossary: Drive, Casing). */
    enum Role {
        DRIVE, INPUT, OUTPUT, CASING
    }

    Role role();

    /** The one core in the eight places around {@code pos}, the Recycler a part there belongs to; none if two are. */
    static Optional<RecyclerCoreBlockEntity> coreOf(Level level, BlockPos pos) {
        RecyclerCoreBlockEntity found = null;
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                if ((x != 0 || z != 0) && level.getBlockEntity(pos.offset(x, 0, z)) instanceof RecyclerCoreBlockEntity core) {
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
