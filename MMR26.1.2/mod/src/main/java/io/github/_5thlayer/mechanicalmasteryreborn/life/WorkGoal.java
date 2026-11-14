// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.life;

import java.util.EnumSet;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;

/**
 * An awake, fed and whole Homunculus's walk to its work (#368): with an empty hand, to its Supply, beside which it takes an
 * item; holding one, to the space it clicks the block it works from, where it stands facing the block. Its stand calls for
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
        BlockPos supply = homunculus.supplyToFetch();
        BlockPos space = homunculus.targetSpace();
        BlockPos target = homunculus.target();
        if (supply == null && space != null && target != null && homunculus.blockPosition().equals(space)) {
            homunculus.getNavigation().stop();
            homunculus.face(target);
            return;
        }
        if (!homunculus.getNavigation().isDone() || homunculus.tickCount < nextLook) {
            return;
        }
        nextLook = homunculus.tickCount + LOOK_INTERVAL;
        // To its Supply's side, or to the space itself: a path that ends a block short of it ends off the space.
        var path = supply != null ? homunculus.getNavigation().createPath(supply, 1)
                : space != null ? homunculus.getNavigation().createPath(space, 0) : null;
        if (path != null) {
            homunculus.getNavigation().moveTo(path, 1.0);
        }
    }
}
