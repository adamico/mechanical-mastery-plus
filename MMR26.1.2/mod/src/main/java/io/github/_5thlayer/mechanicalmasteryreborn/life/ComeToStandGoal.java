// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.life;

import java.util.EnumSet;

import net.minecraft.world.entity.ai.goal.Goal;

/**
 * A hungry or hurt Homunculus's walk back to the free space by its stand's base nearest it, where it stays until it has
 * eaten and is whole: there it eats, falls asleep, or heals. With no way there it waits where it stands, looking again.
 */
final class ComeToStandGoal extends Goal {

    /** Ticks between its looks for a way to its stand. */
    private static final int LOOK_INTERVAL = 20;

    private final HomunculusEntity homunculus;
    private int nextLook;

    ComeToStandGoal(HomunculusEntity homunculus) {
        this.homunculus = homunculus;
        setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        return homunculus.wantsStand();
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
        if (homunculus.atStand()) {
            homunculus.getNavigation().stop();
            return;
        }
        var stand = homunculus.stand();
        if (stand == null || !homunculus.getNavigation().isDone() || homunculus.tickCount < nextLook) {
            return;
        }
        nextLook = homunculus.tickCount + LOOK_INTERVAL;
        // To the space itself: a path that ends a block short of it ends off the stand's spaces.
        HomunculusEntity.spaceBeside(homunculus.level(), stand, homunculus.position())
                .map(space -> homunculus.getNavigation().createPath(space, 0))
                .ifPresent(path -> homunculus.getNavigation().moveTo(path, 1.0));
    }
}
