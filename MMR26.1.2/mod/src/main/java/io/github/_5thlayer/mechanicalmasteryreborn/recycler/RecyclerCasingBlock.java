// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.recycler;

import net.minecraft.world.level.block.Block;

/** A casing: fills a Recycler's place that needs no more drives, inputs or outputs, and does nothing else. */
public final class RecyclerCasingBlock extends Block implements RecyclerPart {

    public RecyclerCasingBlock(Properties properties) {
        super(properties);
    }

    @Override
    public Role role() {
        return Role.CASING;
    }
}
