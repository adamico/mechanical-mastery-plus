// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.recycler;

import java.util.Arrays;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.Level;

/**
 * Where a part stands in its Recycler's 3x3: a corner or the middle of a side, named by its compass direction from
 * the centre, or the centre itself. The places name all nine positions of the 3x3, the core's among them, so the
 * frame is drawn the same whichever of them a part fills, as on a floor above the core's. Its models draw the 3x3's frame from it, the border on its top and bottom turned
 * to run along the Recycler's outer edges. A part in no Recycler, or where two cores' 3x3s overlap, stands at
 * {@link #NONE} and shows no frame.
 */
public enum RecyclerPlace implements StringRepresentable {
    NONE("none", 0, 0), CENTRE("centre", 0, 0),
    NORTH("north", 0, -1), NORTH_EAST("north_east", 1, -1), EAST("east", 1, 0), SOUTH_EAST("south_east", 1, 1),
    SOUTH("south", 0, 1), SOUTH_WEST("south_west", -1, 1), WEST("west", -1, 0), NORTH_WEST("north_west", -1, -1);

    /** The 3x3's nine places, all but {@link #NONE}. */
    public static final List<RecyclerPlace> IN_3X3 = Arrays.stream(values()).filter(place -> place != NONE).toList();

    private final String name;
    /** The place's offset from its 3x3's centre. */
    private final int x;
    private final int z;

    RecyclerPlace(String name, int x, int z) {
        this.name = name;
        this.x = x;
        this.z = z;
    }

    @Override
    public String getSerializedName() {
        return name;
    }

    /** Where this place of the 3x3 centred on {@code centre} is; {@link #NONE} is nowhere. */
    public BlockPos of(BlockPos centre) {
        if (this == NONE) {
            throw new IllegalStateException("no 3x3 holds the place none");
        }
        return centre.offset(x, 0, z);
    }

    /** The place {@code pos} stands at in its Recycler's 3x3; {@link #NONE} if it is in none, or in two. */
    static RecyclerPlace at(Level level, BlockPos pos) {
        var core = RecyclerPart.coreOf(level, pos);
        if (core.isPresent()) {
            BlockPos centre = RecyclerCoreBlock.centre(core.get().getBlockPos(), core.get().getBlockState());
            for (RecyclerPlace place : IN_3X3) {
                if (place.of(centre).equals(pos)) {
                    return place;
                }
            }
        }
        return NONE;
    }
}
