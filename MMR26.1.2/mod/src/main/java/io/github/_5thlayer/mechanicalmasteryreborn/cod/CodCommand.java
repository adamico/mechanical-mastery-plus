// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.cod;

import java.util.UUID;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github._5thlayer.mechanicalmasteryreborn.record.TeamState;
import io.github._5thlayer.mechanicalmasteryreborn.record.Teams;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/**
 * {@code /cod}, for operators: {@code status} says which team the player is in, which team's island they stand on and
 * whether its C.O.D. is awake; {@code dormant} puts that island's C.O.D. back to dormant, as a new island's is, so
 * waking can be seen again.
 */
final class CodCommand {

    private CodCommand() {
    }

    static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("cod")
                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(Commands.literal("status").executes(CodCommand::status))
                .then(Commands.literal("dormant").executes(CodCommand::dormant)));
    }

    private static int status(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        var source = context.getSource();
        var player = source.getPlayerOrException();
        UUID own = Teams.of(player);
        UUID island = Teams.at(source.getLevel(), BlockPos.containing(source.getPosition()));
        var state = TeamState.get(source.getServer());
        source.sendSuccess(() -> Component.translatable("mechanicalmasteryreborn.command.cod.status", own.toString(), island.toString(),
                Component.translatable(state.isAwake(island) ? "mechanicalmasteryreborn.command.cod.awake" : "mechanicalmasteryreborn.command.cod.dormant"),
                state.made(island)), false);
        if (!own.equals(island)) {
            source.sendSuccess(() -> Component.translatable("mechanicalmasteryreborn.command.cod.elsewhere"), false);
        }
        return 1;
    }

    private static int dormant(CommandContext<CommandSourceStack> context) {
        var source = context.getSource();
        UUID island = Teams.at(source.getLevel(), BlockPos.containing(source.getPosition()));
        TeamState.get(source.getServer()).makeDormant(island);
        source.sendSuccess(() -> Component.translatable("mechanicalmasteryreborn.command.cod.made_dormant"), true);
        return 1;
    }
}
