// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.compat.theurgy;

import java.util.List;
import java.util.Optional;

import com.klikli_dev.theurgy.content.apparatus.incubator.IncubatorBlockEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.transfer.item.ItemUtil;

/**
 * What an Incubator's three vessels hold, mercury, salt and sulfur, for Jade to show on the Incubator itself (#327):
 * Jade shows each vessel's on the vessel, and only the output on the Incubator. Loaded only when Theurgy is installed.
 */
public final class IncubatorVessels {

    private IncubatorVessels() {
    }

    /** The mercury, salt and sulfur, empty stacks for empty vessels, while the Incubator stands with all three. */
    public static Optional<List<ItemStack>> of(BlockEntity entity) {
        if (!(TheurgyApparatus.answering(entity, IncubatorBlockEntity.class) instanceof IncubatorBlockEntity incubator)
                || !incubator.isValidMultiblock()) {
            return Optional.empty();
        }
        return Optional.of(List.of(ItemUtil.getStack(incubator.mercuryVessel.inputInventory, 0),
                ItemUtil.getStack(incubator.saltVessel.inputInventory, 0),
                ItemUtil.getStack(incubator.sulfurVessel.inputInventory, 0)));
    }
}
