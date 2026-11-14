// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.life;

import java.util.EnumSet;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;

/**
 * An awake, fed and whole Homunculus's walk to its work (#368): to its Supply, beside which it takes an item or a meal;
 * holding a meal for its clear Table, to a space beside the Table, facing the block under it; holding an item, to the space
 * it clicks the block it works from, where it stands facing the block. Its stand calls for
 * each click. With no way on it waits where it stands, looking again.
 */
final class WorkGoal extends Goal {

    /** Ticks between its looks for a way on. */
    private static final int LOOK_INTERVAL = 20;

    private final HomunculusEntity homunculus;
    private int nextLook;

    WorkGoal(HomunculusEntity homunculus) {
        this.homunculus = homunculus;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        return !homunculus.isStatue() && !homunculus.wantsStand() && homunculus.hasWork();
    }

    @Override
    public void start() {
        nextLook = 0;
    }

    @Override
    public void stop() {
        homunculus.getNavigation().stop();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        BlockPos to = homunculus.walkTo();
        BlockPos facing = homunculus.facing();
        if (to != null && facing != null && homunculus.blockPosition().equals(to)) {
            homunculus.getNavigation().stop();
            homunculus.face(facing);
            return;
        }
        if (!homunculus.getNavigation().isDone() || homunculus.tickCount < nextLook) {
            return;
        }
        nextLook = homunculus.tickCount + LOOK_INTERVAL;
        // To the space itself, beside its Supply, its Table or its block: a path that ends a block short of it ends off the space.
        var path = to != null ? homunculus.getNavigation().createPath(to, 0) : null;
        if (path != null) {
            homunculus.getNavigation().moveTo(path, 1.0);
        }
    }
}
