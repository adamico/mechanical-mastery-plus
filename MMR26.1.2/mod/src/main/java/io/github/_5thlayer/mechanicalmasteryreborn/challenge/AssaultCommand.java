// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.challenge;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import io.github._5thlayer.mechanicalmasteryreborn.record.Teams;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/**
 * {@code /mmr assault wave <n>}, for operators: puts the open Assault gate of the team whose island it is run on at
 * wave {@code n}, so a step up can be reached without playing every wave before it. A tool for checking and balancing,
 * not part of the challenge (#247).
 */
final class AssaultCommand {

    private AssaultCommand() {
    }

    static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("mmr")
                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(Commands.literal("assault").then(Commands.literal("wave")
                        .then(Commands.argument("wave", IntegerArgumentType.integer(1)).executes(AssaultCommand::wave)))));
    }

    private static int wave(CommandContext<CommandSourceStack> context) {
        var source = context.getSource();
        int wave = IntegerArgumentType.getInteger(context, "wave");
        var team = Teams.at(source.getLevel(), BlockPos.containing(source.getPosition()));
        if (!Assault.setWave(source.getServer(), team, wave)) {
            source.sendFailure(Component.translatable("mechanicalmasteryreborn.command.mmr.assault.wave.no_gate"));
            return 0;
        }
        source.sendSuccess(() -> Component.translatable("mechanicalmasteryreborn.command.mmr.assault.wave.set", wave), true);
        return 1;
    }
}
