// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.recipe;

import java.util.List;
import java.util.stream.Collectors;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/**
 * {@code /mmr mixes}, for operators: says whether the pack's mixes are closed, every item of them recycling only into
 * what some mix holds, or else names each item that recycles outside them and what into (#292).
 */
final class MixesCommand {

    private MixesCommand() {
    }

    static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("mmr")
                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(Commands.literal("mixes").executes(MixesCommand::mixes)));
    }

    private static int mixes(CommandContext<CommandSourceStack> context) {
        var source = context.getSource();
        var unclosed = Decomposing.unclosed(source.getServer());
        if (unclosed.isEmpty()) {
            source.sendSuccess(() -> Component.translatable("mechanicalmasteryreborn.command.mmr.mixes.closed"), false);
            return 1;
        }
        unclosed.forEach((item, outside) -> source.sendFailure(
                Component.translatable("mechanicalmasteryreborn.command.mmr.mixes.unclosed", id(item), ids(outside))));
        return 0;
    }

    private static String id(Item item) {
        return BuiltInRegistries.ITEM.getKey(item).toString();
    }

    private static String ids(List<Item> items) {
        return items.stream().map(MixesCommand::id).collect(Collectors.joining(", "));
    }
}
