// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.challenge;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.BlockUtil;
import net.minecraft.world.level.block.Blocks;

/**
 * The obsidian floor around a Nether portal the game makes in mid-air, as it does over the Nether's lava sea, where it
 * finds no ground to stand a portal on (ADR 0012), and over the Overworld's void on the way home. Vanilla gives such a
 * portal a ledge one block wide; this floor reaches {@link #REACH} blocks past the frame on every side, so a player
 * arriving has room to step out and build from. It takes only air, so it never covers what is already there.
 */
public final class ArrivalPlatform {

    /** How far the floor reaches past the portal's frame on every side. */
    static final int REACH = 2;

    private ArrivalPlatform() {
    }

    /** Lays the floor around {@code portal}, the portal blocks the game made along {@code axis}, at its frame's foot. */
    public static void build(ServerLevel level, BlockUtil.FoundRectangle portal, Direction.Axis axis) {
        Direction along = Direction.get(Direction.AxisDirection.POSITIVE, axis);
        Direction across = along.getClockWise();
        BlockPos foot = portal.minCorner.below();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int a = -1 - REACH; a <= portal.axis1Size + REACH; a++) {
            for (int c = -REACH; c <= REACH; c++) {
                pos.setWithOffset(foot, along.getStepX() * a + across.getStepX() * c, 0, along.getStepZ() * a + across.getStepZ() * c);
                if (level.getBlockState(pos).isAir()) {
                    level.setBlockAndUpdate(pos, Blocks.OBSIDIAN.defaultBlockState());
                }
            }
        }
    }
}
