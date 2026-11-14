// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.life;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/** The Vital Spark (the line's glossary), named spent once a Homunculus has died off it. */
public final class VitalSparkItem extends Item {

    public VitalSparkItem(Properties properties) {
        super(properties);
    }

    @Override
    public Component getName(ItemStack stack) {
        return Homunculus.spent(stack) ? Component.translatable(getDescriptionId() + ".spent") : super.getName(stack);
    }
}
