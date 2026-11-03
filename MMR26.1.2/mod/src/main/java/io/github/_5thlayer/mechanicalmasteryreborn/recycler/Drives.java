// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.recycler;

import io.github._5thlayer.mechanicalmasteryreborn.recycler.RecyclerCoreBlockEntity.Turn;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import org.jspecify.annotations.Nullable;

/**
 * What every drive's turn is heard and seen as: a click whose pitch rises with the decompose's progress, and sparks,
 * when it counts; the crafter's sound and a puff over the core when it decomposes; a dull click and smoke when the
 * Recycler can't decompose, so the turn is lost.
 */
final class Drives {

    private Drives() {
    }

    /** Plays what {@code turn} of the drive at {@code pos} did, on it and on {@code core}, null when it has none. */
    static void show(ServerLevel level, BlockPos pos, @Nullable RecyclerCoreBlockEntity core, Turn turn) {
        double x = pos.getX() + 0.5;
        double y = pos.getY() + 1.0;
        double z = pos.getZ() + 0.5;
        switch (turn) {
            case COUNTED -> {
                float progress = (float) core.turns() / core.turnsPerWork();
                level.playSound(null, pos, SoundEvents.LEVER_CLICK, SoundSource.BLOCKS, 0.4F, 0.7F + 0.6F * progress);
                level.sendParticles(ParticleTypes.ELECTRIC_SPARK, x, y, z, 3, 0.25, 0.05, 0.25, 0.05);
            }
            case WORKED -> {
                BlockPos at = core.getBlockPos();
                level.playSound(null, at, SoundEvents.CRAFTER_CRAFT, SoundSource.BLOCKS, 1.0F, 1.0F);
                level.sendParticles(ParticleTypes.POOF, at.getX() + 0.5, at.getY() + 1.0, at.getZ() + 0.5, 6, 0.3, 0.05, 0.3, 0.02);
            }
            case STOPPED -> {
                level.playSound(null, pos, SoundEvents.CRAFTER_FAIL, SoundSource.BLOCKS, 0.5F, 1.0F);
                level.sendParticles(ParticleTypes.SMOKE, x, y, z, 2, 0.2, 0.05, 0.2, 0.01);
            }
        }
    }
}
