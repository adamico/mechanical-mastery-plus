// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.cod;

import java.util.Arrays;
import java.util.UUID;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.Challenges;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.Handicap;
import io.github._5thlayer.mechanicalmasteryreborn.record.TeamState;
import io.github._5thlayer.mechanicalmasteryreborn.record.Teams;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/**
 * {@code /cod}, for operators: {@code status} says which team the player is in, which team's island they stand on,
 * whether its C.O.D. is awake and which challenge that team plays; {@code dormant} puts that island's C.O.D. back to
 * dormant, as a new island's is, so waking can be seen again, and leaves its challenge as it is; {@code challenge start
 * <handicap>} starts that island's team's challenge as its lab would, and {@code challenge end} ends it, its handicap
 * with it, so choosing can be seen again.
 */
final class CodCommand {

    private CodCommand() {
    }

    static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("cod")
                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(Commands.literal("status").executes(CodCommand::status))
                .then(Commands.literal("dormant").executes(CodCommand::dormant))
                .then(Commands.literal("challenge")
                        .then(Commands.literal("start").then(Commands.argument("handicap", StringArgumentType.word())
                                .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                                        Arrays.stream(Handicap.values()).map(Handicap::id), builder))
                                .executes(CodCommand::startChallenge)))
                        .then(Commands.literal("end").executes(CodCommand::endChallenge))));
    }

    private static int status(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        var source = context.getSource();
        var player = source.getPlayerOrException();
        UUID own = Teams.of(player);
        UUID island = island(source);
        var state = TeamState.get(source.getServer());
        source.sendSuccess(() -> Component.translatable("mechanicalmasteryreborn.command.cod.status", own.toString(), island.toString(),
                Component.translatable(state.isAwake(island) ? "mechanicalmasteryreborn.command.cod.awake" : "mechanicalmasteryreborn.command.cod.dormant"),
                state.made(island)), false);
        source.sendSuccess(() -> state.handicap(island)
                .map(handicap -> Component.translatable("mechanicalmasteryreborn.command.cod.challenge", Challenges.name(handicap)))
                .orElseGet(() -> Component.translatable("mechanicalmasteryreborn.command.cod.no_challenge")), false);
        if (!own.equals(island)) {
            source.sendSuccess(() -> Component.translatable("mechanicalmasteryreborn.command.cod.elsewhere"), false);
        }
        return 1;
    }

    private static int startChallenge(CommandContext<CommandSourceStack> context) {
        var source = context.getSource();
        String id = StringArgumentType.getString(context, "handicap");
        var handicap = Handicap.byId(id);
        if (handicap.isEmpty()) {
            source.sendFailure(Component.translatable("mechanicalmasteryreborn.command.cod.challenge.unknown", id));
            return 0;
        }
        if (!handicap.get().choosable()) {
            source.sendFailure(Component.translatable("mechanicalmasteryreborn.command.cod.challenge.later", Challenges.name(handicap.get())));
            return 0;
        }
        UUID island = island(source);
        var playing = TeamState.get(source.getServer()).handicap(island);
        if (playing.isPresent()) {
            source.sendFailure(Component.translatable("mechanicalmasteryreborn.command.cod.challenge.playing", Challenges.name(playing.get())));
            return 0;
        }
        if (!Challenges.start(source.getServer(), island, handicap.get())) {
            return 0;
        }
        source.sendSuccess(() -> Component.translatable("mechanicalmasteryreborn.command.cod.challenge.started", Challenges.name(handicap.get())), true);
        return 1;
    }

    private static int endChallenge(CommandContext<CommandSourceStack> context) {
        var source = context.getSource();
        if (!TeamState.get(source.getServer()).endChallenge(island(source))) {
            source.sendFailure(Component.translatable("mechanicalmasteryreborn.command.cod.no_challenge"));
            return 0;
        }
        source.sendSuccess(() -> Component.translatable("mechanicalmasteryreborn.command.cod.challenge.ended"), true);
        return 1;
    }

    /** The team whose island the source stands on. */
    private static UUID island(CommandSourceStack source) {
        return Teams.at(source.getLevel(), BlockPos.containing(source.getPosition()));
    }

    private static int dormant(CommandContext<CommandSourceStack> context) {
        var source = context.getSource();
        UUID island = island(source);
        TeamState.get(source.getServer()).makeDormant(island);
        source.sendSuccess(() -> Component.translatable("mechanicalmasteryreborn.command.cod.made_dormant"), true);
        return 1;
    }
}
