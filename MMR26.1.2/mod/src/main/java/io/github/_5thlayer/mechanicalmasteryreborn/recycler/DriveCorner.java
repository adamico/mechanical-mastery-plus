// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.recycler;

import net.minecraft.core.BlockPos;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.Level;

/**
 * Which corner of its Recycler's 3x3 a drive stands at, which turns its model so the rail on its top and bottom
 * runs along the Recycler's outer edges. A drive off a corner, or in no Recycler, keeps {@link #NORTH_WEST}.
 */
public enum DriveCorner implements StringRepresentable {
    NORTH_WEST("north_west", -1, -1), NORTH_EAST("north_east", 1, -1), SOUTH_EAST("south_east", 1, 1), SOUTH_WEST("south_west", -1, 1);

    private final String name;
    /** The drive's offset from its 3x3's centre. */
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

    /** Where a drive at this corner of the 3x3 centred on {@code centre} stands. */
    public BlockPos of(BlockPos centre) {
        return centre.offset(x, 0, z);
    }

    /** The corner of its Recycler's 3x3 a drive at {@code pos} stands at; {@link #NORTH_WEST} if none. */
    static DriveCorner at(Level level, BlockPos pos) {
        var core = RecyclerPart.coreOf(level, pos);
        if (core.isPresent()) {
            BlockPos centre = RecyclerCoreBlock.centre(core.get().getBlockPos(), core.get().getBlockState());
            for (DriveCorner corner : values()) {
                if (corner.of(centre).equals(pos)) {
                    return corner;
                }
            }
        }
        return NORTH_WEST;
    }
}
