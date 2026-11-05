// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.cod;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github._5thlayer.mechanicalmasteryreborn.ServerConfig;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.Challenges;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.Handicap;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.InflationBar;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.LastStands;
import io.github._5thlayer.mechanicalmasteryreborn.essence.Essence;
import io.github._5thlayer.mechanicalmasteryreborn.essence.Essences;
import io.github._5thlayer.mechanicalmasteryreborn.record.TeamState;
import io.github._5thlayer.mechanicalmasteryreborn.record.Teams;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/**
 * {@code /cod}, for operators: {@code status} says which team the player is in, which team's island they stand on,
 * whether its C.O.D. is awake, which challenge that team plays and its Inflation; {@code dormant} puts that island's
 * C.O.D. back to dormant, as a new island's is, so waking can be seen again, and leaves its challenge as it is;
 * {@code challenge start <handicap>} starts that island's team's challenge as its lab would (#235), and {@code challenge end}
 * ends it, its handicap with it, so choosing can be seen again, and {@code challenge revert <handicap>} forgets
 * that it won that challenge, unblocking the tier its win blocked, so winning can be seen again; {@code tier <team> <tier>} sets the tier a team's C.O.D.
 * makes, or none, for tests and in-game checks; {@code last_stand start} starts that island's team's last stand as its lab
 * would, and {@code last_stand lead <lead>} sets its Lead, so the floor, the cap and the win can be seen.
 */
final class CodCommand {

    /** What {@code /cod tier} takes: each tier by name, highest first, and none. */
    private static final String NONE = "none";
    private static final List<String> TIERS = Stream.concat(Essence.TIERS.stream().map(CodCommand::tierName), Stream.of(NONE)).toList();

    private CodCommand() {
    }

    static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("cod")
                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(Commands.literal("status").executes(CodCommand::status))
                .then(Commands.literal("dormant").executes(CodCommand::dormant))
                .then(Commands.literal("tier").then(Commands.argument("team", StringArgumentType.word())
                        .suggests((context, builder) -> SharedSuggestionProvider.suggest(teams(context.getSource()), builder))
                        .then(Commands.argument("tier", StringArgumentType.word())
                                .suggests((context, builder) -> SharedSuggestionProvider.suggest(TIERS, builder))
                                .executes(CodCommand::tier))))
                .then(Commands.literal("challenge")
                        .then(Commands.literal("start").then(Commands.argument("handicap", StringArgumentType.word())
                                .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                                        Arrays.stream(Handicap.values()).map(Handicap::id), builder))
                                .executes(CodCommand::startChallenge)))
                        .then(Commands.literal("end").executes(CodCommand::endChallenge))
                        .then(Commands.literal("revert").then(Commands.argument("handicap", StringArgumentType.word())
                                .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                                        Arrays.stream(Handicap.values()).map(Handicap::id), builder))
                                .executes(CodCommand::revertWin))))
                .then(Commands.literal("last_stand")
                        .then(Commands.literal("start").executes(CodCommand::startLastStand))
                        .then(Commands.literal("lead").then(Commands.argument("lead", LongArgumentType.longArg())
                                .executes(CodCommand::setLead)))));
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
        source.sendSuccess(() -> state.tier(island)
                .map(tier -> Component.translatable("mechanicalmasteryreborn.command.cod.tier", new ItemStack(Essences.item(tier)).getHoverName()))
                .orElseGet(() -> Component.translatable("mechanicalmasteryreborn.command.cod.all_blocked")), false);
        state.inflation(island).ifPresent(inflation -> source.sendSuccess(() -> Component.translatable(
                "mechanicalmasteryreborn.command.cod.inflation", inflation.made(), inflation.decomposed(), inflation.excess(),
                ServerConfig.inflationCap()), false));
        state.lastStand(island).ifPresent(stand -> source.sendSuccess(() -> stand.won()
                ? Component.translatable("mechanicalmasteryreborn.command.cod.last_stand.won")
                : InflationBar.lead(stand, ServerConfig.lastStandMargin()), false));
        if (!own.equals(island)) {
            source.sendSuccess(() -> Component.translatable("mechanicalmasteryreborn.command.cod.elsewhere"), false);
        }
        return 1;
    }

    private static int startLastStand(CommandContext<CommandSourceStack> context) {
        var source = context.getSource();
        if (!LastStands.begin(source.getServer(), island(source))) {
            source.sendFailure(Component.translatable("mechanicalmasteryreborn.command.cod.last_stand.not_offered"));
            return 0;
        }
        source.sendSuccess(() -> Component.translatable("mechanicalmasteryreborn.command.cod.last_stand.started"), true);
        return 1;
    }

    private static int setLead(CommandContext<CommandSourceStack> context) {
        var source = context.getSource();
        long lead = LongArgumentType.getLong(context, "lead");
        UUID island = island(source);
        var state = TeamState.get(source.getServer());
        if (!state.setLead(island, lead, ServerConfig.lastStandMargin())) {
            source.sendFailure(Component.translatable("mechanicalmasteryreborn.command.cod.last_stand.none"));
            return 0;
        }
        source.sendSuccess(() -> Component.translatable("mechanicalmasteryreborn.command.cod.last_stand.lead",
                state.lastStand(island).orElseThrow().lead()), true);
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
        UUID island = island(source);
        var playing = TeamState.get(source.getServer()).handicap(island);
        if (playing.isPresent()) {
            source.sendFailure(Component.translatable("mechanicalmasteryreborn.command.cod.challenge.playing", Challenges.name(playing.get())));
            return 0;
        }
        if (!Challenges.begin(source.getServer(), island, handicap.get())) {
            return 0;
        }
        source.sendSuccess(() -> Component.translatable("mechanicalmasteryreborn.command.cod.challenge.started", Challenges.name(handicap.get())), true);
        return 1;
    }

    private static int endChallenge(CommandContext<CommandSourceStack> context) {
        var source = context.getSource();
        if (!Challenges.end(source.getServer(), island(source))) {
            source.sendFailure(Component.translatable("mechanicalmasteryreborn.command.cod.no_challenge"));
            return 0;
        }
        source.sendSuccess(() -> Component.translatable("mechanicalmasteryreborn.command.cod.challenge.ended"), true);
        return 1;
    }

    private static int revertWin(CommandContext<CommandSourceStack> context) {
        var source = context.getSource();
        String id = StringArgumentType.getString(context, "handicap");
        var handicap = Handicap.byId(id);
        if (handicap.isEmpty()) {
            source.sendFailure(Component.translatable("mechanicalmasteryreborn.command.cod.challenge.unknown", id));
            return 0;
        }
        if (!TeamState.get(source.getServer()).revertWin(island(source), handicap.get())) {
            source.sendFailure(Component.translatable("mechanicalmasteryreborn.command.cod.challenge.not_won", Challenges.name(handicap.get())));
            return 0;
        }
        source.sendSuccess(() -> Component.translatable("mechanicalmasteryreborn.command.cod.challenge.reverted", Challenges.name(handicap.get())), true);
        return 1;
    }

    /**
     * Sets the tier {@code team}'s C.O.D. makes, by blocking every tier above it, or all four for {@code none}. The
     * team is its id, as {@code /cod status} shows it.
     */
    private static int tier(CommandContext<CommandSourceStack> context) {
        var source = context.getSource();
        String teamId = StringArgumentType.getString(context, "team");
        UUID team;
        try {
            team = UUID.fromString(teamId);
        } catch (IllegalArgumentException e) {
            source.sendFailure(Component.translatable("mechanicalmasteryreborn.command.cod.tier.unknown_team", teamId));
            return 0;
        }
        String name = StringArgumentType.getString(context, "tier");
        Optional<Essence> tier = Essence.TIERS.stream().filter(candidate -> tierName(candidate).equals(name)).findFirst();
        if (tier.isEmpty() && !name.equals(NONE)) {
            source.sendFailure(Component.translatable("mechanicalmasteryreborn.command.cod.tier.unknown", name));
            return 0;
        }
        TeamState.get(source.getServer()).setTier(team, tier);
        source.sendSuccess(() -> tier
                .map(made -> Component.translatable("mechanicalmasteryreborn.command.cod.tier.set", teamId, new ItemStack(Essences.item(made)).getHoverName()))
                .orElseGet(() -> Component.translatable("mechanicalmasteryreborn.command.cod.tier.none", teamId)), true);
        return 1;
    }

    /**
     * The team ids {@code /cod tier} suggests: the team whose island the source stands on first, then the player's
     * own, then every other team with a state.
     */
    private static Set<String> teams(CommandSourceStack source) {
        Set<String> teams = new LinkedHashSet<>();
        teams.add(island(source).toString());
        if (source.getPlayer() != null) {
            teams.add(Teams.of(source.getPlayer()).toString());
        }
        TeamState.get(source.getServer()).known().forEach(team -> teams.add(team.toString()));
        return teams;
    }

    /** A tier's name in {@code /cod tier}: {@code advanced} for Advanced Mechanical Essence. */
    private static String tierName(Essence tier) {
        return tier.name().toLowerCase(Locale.ROOT);
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
