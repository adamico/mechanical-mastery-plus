// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.compat.theurgy;

import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * What {@code CraftingBehaviourMixin} gives each of Theurgy's crafting behaviours, so that one which ticks its own way,
 * as the Mercury Catalyst's does, stops for the same reason. Outside the mixin package, since a mixin package holds only
 * mixins.
 */
public interface ChainMachine {

    /** Stops the machine's processing and answers true while its chain is locked for its island's team (#214). */
    boolean mechanicalmasteryreborn$stopIfLocked();

    /** The block entity the behaviour works for, so the Mercury Catalyst's reaches its FE store (#328). */
    BlockEntity mechanicalmasteryreborn$entity();
}
