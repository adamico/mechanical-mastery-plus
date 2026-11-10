// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.compat.theurgy;

import java.util.Optional;

import com.klikli_dev.theurgy.content.capability.MercuryFluxHandler;
import com.klikli_dev.theurgy.registry.CapabilityRegistry;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * The mercury flux a Theurgy block holds, as Theurgy's in-world HUD shows it, for Jade (#327): Jade knows no flux of its
 * own. Loaded only when Theurgy is installed.
 */
public final class TheurgyFlux {

    private TheurgyFlux() {
    }

    /** Flux held against the store's capacity. */
    public record Stored(int amount, int capacity) {
    }

    /** What a block with a mercury flux store holds, and nothing for any other block. */
    public static Optional<Stored> of(BlockEntity entity) {
        if (entity.getLevel() == null) {
            return Optional.empty();
        }
        MercuryFluxHandler flux = entity.getLevel().getCapability(CapabilityRegistry.MERCURY_FLUX_HANDLER, entity.getBlockPos(),
                entity.getBlockState(), entity, null);
        return flux == null ? Optional.empty() : Optional.of(new Stored(flux.getAmountAsInt(), flux.getCapacityAsInt()));
    }
}
