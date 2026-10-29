// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.recycler;

import net.minecraft.core.BlockPos;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.Level;

/**
 * Which corner of its core a drive stands at, which turns its model so the rail on its top and bottom runs
 * along the Recycler's outer edges. A drive at no core's corner keeps {@link #NORTH_WEST}.
 */
public enum DriveCorner implements StringRepresentable {
    NORTH_WEST("north_west", -1, -1), NORTH_EAST("north_east", 1, -1), SOUTH_EAST("south_east", 1, 1), SOUTH_WEST("south_west", -1, 1);

    private final String name;
    /** The drive's offset from its core. */
    private final int x;
    private final int z;

    DriveCorner(String name, int x, int z) {
        this.name = name;
        this.x = x;
        this.z = z;
    }

    @Override
    public String getSerializedName() {
        return name;
    }

    /** Where a drive at this corner of the core at {@code core} stands. */
    public BlockPos of(BlockPos core) {
        return core.offset(x, 0, z);
    }

    /** The corner a drive at {@code pos} stands at, of the first core diagonal to it; {@link #NORTH_WEST} if none. */
    static DriveCorner at(Level level, BlockPos pos) {
        for (DriveCorner corner : values()) {
            if (level.getBlockState(pos.offset(-corner.x, 0, -corner.z)).getBlock() instanceof RecyclerCoreBlock) {
                return corner;
            }
        }
        return NORTH_WEST;
    }
}
