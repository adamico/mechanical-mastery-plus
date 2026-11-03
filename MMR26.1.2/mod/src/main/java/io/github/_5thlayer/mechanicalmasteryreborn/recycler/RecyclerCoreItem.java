// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.recycler;

import java.util.function.Consumer;

import io.github._5thlayer.mechanicalmasteryreborn.MechanicalMasteryReborn;
import io.github._5thlayer.mechanicalmasteryreborn.ServerConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

/**
 * A core's item, whose tooltip, in the inventory and in EMI alike, says how many turns a decompose or a recycle takes
 * on it, as the server plays it.
 */
public final class RecyclerCoreItem extends BlockItem {

    private final RecyclerCoreBlock core;

    public RecyclerCoreItem(RecyclerCoreBlock core, Properties properties) {
        super(core, properties);
        this.core = core;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, builder, flag);
        builder.accept(Component.translatable(MechanicalMasteryReborn.MOD_ID + ".recycler_core.turns",
                ServerConfig.turnsOf(core.tier())).withStyle(ChatFormatting.GRAY));
    }
}
