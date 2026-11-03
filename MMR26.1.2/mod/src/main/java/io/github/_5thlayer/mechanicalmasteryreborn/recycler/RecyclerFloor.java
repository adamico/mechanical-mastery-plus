// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.recycler;

import net.minecraft.core.BlockPos;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.Level;

/**
 * Which of its Recycler's floors a part stands on, for the frame: one tall frame wraps every floor that counts, its
 * bottom border on the core's floor and its top one on the highest, with none where two floors meet.
 */
public enum RecyclerFloor implements StringRepresentable {
    /** The Recycler's only floor: both borders. */
    SINGLE("single"),
    /** The core's floor under more: the bottom border. */
    BOTTOM("bottom"),
    /** Between two floors: no border. */
    MIDDLE("middle"),
    /** The highest of several: the top border. */
    TOP("top");

    private final String name;

    RecyclerFloor(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }

    /** Floor {@code floor}, the core's own 0, of a Recycler of {@code floors}. */
    static RecyclerFloor of(int floor, int floors) {
        if (floors == 1) {
            return SINGLE;
        }
        return floor == 0 ? BOTTOM : floor == floors - 1 ? TOP : MIDDLE;
    }

    /** The floor {@code pos} stands on in its Recycler; {@link #SINGLE} when it is in none, where no frame shows anyway. */
    static RecyclerFloor at(Level level, BlockPos pos) {
        return RecyclerPart.coreOf(level, pos)
                .map(core -> of(pos.getY() - core.getBlockPos().getY(), core.floors()))
                .orElse(SINGLE);
    }
}
