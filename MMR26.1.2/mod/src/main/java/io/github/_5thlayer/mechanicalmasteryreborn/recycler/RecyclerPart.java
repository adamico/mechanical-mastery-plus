// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.recycler;

import java.util.Optional;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.EnumProperty;

/**
 * A block that can stand in one of a Recycler's eight places, the rest of the 3x3 its core fronts, in one role. Its
 * {@link #PLACE} is where in the 3x3 it stands and its {@link #FLOOR} on which floor, taken when it is placed and set again when a core is placed or
 * removed nearby, so its top and bottom draw the Recycler's frame.
 */
public interface RecyclerPart {

    EnumProperty<RecyclerPlace> PLACE = EnumProperty.create("place", RecyclerPlace.class);
    EnumProperty<RecyclerFloor> FLOOR = EnumProperty.create("floor", RecyclerFloor.class);

    /** What a part does in its Recycler (the line's glossary: Drive, Casing). */
    enum Role {
        DRIVE, INPUT, OUTPUT, CASING
    }

    Role role();

    /** {@code state} at the place a part placed by {@code context} stands at. */
    static BlockState placed(BlockState state, BlockPlaceContext context) {
        return framed(state, context.getLevel(), context.getClickedPos());
    }

    /** {@code state} with the place and floor of {@code pos}, so it draws the frame where it stands. */
    static BlockState framed(BlockState state, Level level, BlockPos pos) {
        return state.setValue(PLACE, RecyclerPlace.at(level, pos)).setValue(FLOOR, RecyclerFloor.at(level, pos));
    }

    /**
     * Sets every part and core near {@code pos} to how it now stands, after a part or core there came or went: a
     * floor filled or broken changes which floors count, and so the frame, of every Recycler that reaches it or
     * shares a place with one that does.
     */
    static void reframeAround(Level level, BlockPos pos) {
        // A core it changes stands up to 2 away and 3 below; a core sharing a place with that one's 3x3 up to 3 further
        // and 3 lower, its parts one beyond, and as far up as down.
        int reach = 6;
        for (int y = -reach; y <= reach; y++) {
            for (int x = -reach; x <= reach; x++) {
                for (int z = -reach; z <= reach; z++) {
                    BlockPos at = pos.offset(x, y, z);
                    BlockState state = level.getBlockState(at);
                    BlockState now = state.getBlock() instanceof RecyclerPart ? framed(state, level, at)
                            : state.getBlock() instanceof RecyclerCoreBlock && level.getBlockEntity(at) instanceof RecyclerCoreBlockEntity core
                                    ? state.setValue(RecyclerCoreBlock.TALL, core.floors() > 1) : state;
                    if (now != state) {
                        level.setBlock(at, now, Block.UPDATE_CLIENTS);
                    }
                }
            }
        }
    }

    /** The one core whose Recycler holds {@code pos} as a place, the Recycler a part there belongs to; none if two would. */
    static Optional<RecyclerCoreBlockEntity> coreOf(Level level, BlockPos pos) {
        return reacherOf(level, pos).filter(core -> core.holdsPlace(pos));
    }

    /**
     * The one core that would claim {@code pos} on its own floor or one above, counting only which floors are full
     * of parts; none if two would, and then the place belongs to neither.
     */
    static Optional<RecyclerCoreBlockEntity> reacherOf(Level level, BlockPos pos) {
        RecyclerCoreBlockEntity found = null;
        for (int below = 0; below < Recycler.TIERS; below++) {
            for (int x = -2; x <= 2; x++) {
                for (int z = -2; z <= 2; z++) {
                    if (level.getBlockEntity(pos.offset(x, -below, z)) instanceof RecyclerCoreBlockEntity core && core.reaches(pos)) {
                        if (found != null) {
                            return Optional.empty();
                        }
                        found = core;
                    }
                }
            }
        }
        return Optional.ofNullable(found);
    }
}
