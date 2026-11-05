// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.challenge;

import java.util.UUID;

import com.mojang.logging.LogUtils;
import io.github._5thlayer.mechanicalmasteryreborn.MechanicalMasteryReborn;
import io.github._5thlayer.mechanicalmasteryreborn.ServerConfig;
import io.github._5thlayer.mechanicalmasteryreborn.record.Spell;
import io.github._5thlayer.mechanicalmasteryreborn.record.TeamState;
import io.github._5thlayer.mechanicalmasteryreborn.record.Teams;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSoundEntityPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.slf4j.Logger;

/**
 * The handicaps a team takes on at its Athanor in its last stand (#263, ADR 0014), each for a fixed
 * {@link ServerConfig#athanorSpellLength spell} that can't be ended early, at its challenge's peak from the start: its
 * generators stopped and its banked FE cut ({@link Generators}), the hungriest Diet ({@link LifeHandicap}), an Assault
 * opened at its hardest steps whose mobs drop Null Shards ({@link Assault}, {@link NullShards}), and its money set aside
 * with sales paying less and the Shop's prices up ({@link MoneyHandicap}). Spells overlap, and one can be taken again once
 * it ends. Each tick ends the spells that are over, and its handicap with it.
 */
public final class Spells {

    private static final Logger LOGGER = LogUtils.getLogger();

    private Spells() {
    }

    public static void register() {
        NeoForge.EVENT_BUS.addListener(ServerTickEvent.Post.class, event -> endOver(event.getServer()));
    }

    /**
     * Whether {@code team} can take on {@code handicap} at its Athanor now: its last stand runs, no spell of it runs yet,
     * and, for money, it has an Account to act on.
     */
    public static boolean canBegin(MinecraftServer server, UUID team, Handicap handicap) {
        TeamState state = TeamState.get(server);
        return state.inLastStand(team) && !state.underSpell(team, handicap)
                && (handicap != Handicap.MONEY || Accounts.get().opened(server, team));
    }

    /**
     * Has {@code team} take on {@code handicap} for a spell from now, and tells its players online, as the Athanor's
     * button does. Says whether it did: only as {@link #canBegin} allows.
     */
    public static boolean begin(MinecraftServer server, UUID team, Handicap handicap) {
        return begin(server, team, handicap, ServerConfig.athanorSpellLength());
    }

    /** Has {@code team} take on {@code handicap}, as {@link #begin(MinecraftServer, UUID, Handicap)} does, for a spell of {@code length} ticks, as the game tests do. */
    public static boolean begin(MinecraftServer server, UUID team, Handicap handicap, int length) {
        if (!canBegin(server, team, handicap)) {
            return false;
        }
        TeamState state = TeamState.get(server);
        Spell spell = Spell.starting(server.overworld().getGameTime(), length);
        if (!state.beginSpell(team, handicap, spell)) {
            return false;
        }
        switch (handicap) {
            case MONEY -> MoneyHandicap.setAside(server, team);
            case PROTECTION -> Assault.start(server, team, ServerConfig.athanorProtectionWave());
            case LIFE -> LifeHandicap.refresh(server, team);
            case ENERGY -> {
            }
        }
        Component name = title(handicap).withStyle(ChatFormatting.GOLD);
        Component cost = cost(handicap);
        int told = tell(server, team, Component.translatable(MechanicalMasteryReborn.MOD_ID + ".spell.started", name, cost).withStyle(ChatFormatting.GRAY),
                name, cost, SoundEvents.RAID_HORN, Challenges.RAID_HORN_VOLUME);
        LOGGER.info("Team {} took on the {} spell until {}; told {} of its players online", team, handicap.id(), spell.ends(), told);
        return true;
    }

    /** Ends every spell that is over, and its handicap with it. */
    static void endOver(MinecraftServer server) {
        TeamState state = TeamState.get(server);
        for (var over : state.spellsOver(server.overworld().getGameTime())) {
            end(server, over.getKey(), over.getValue());
        }
    }

    /** Ends every spell {@code team} bears, and its handicap with it, as winning the pack does: C.O.D. is shut down, and the Athanor with it. */
    static void endAll(MinecraftServer server, UUID team) {
        for (Handicap handicap : Handicap.values()) {
            end(server, team, handicap);
        }
    }

    /** Ends {@code team}'s spell of {@code handicap} as its time runs out, its handicap with it, and tells its players online. */
    private static void end(MinecraftServer server, UUID team, Handicap handicap) {
        if (TeamState.get(server).endSpell(team, handicap).isEmpty()) {
            return;
        }
        switch (handicap) {
            case MONEY -> MoneyHandicap.end(server, team);
            case PROTECTION -> Assault.end(server, team);
            case LIFE -> LifeHandicap.refresh(server, team);
            case ENERGY -> {
            }
        }
        Component title = Component.translatable(MechanicalMasteryReborn.MOD_ID + ".spell.ended.title", title(handicap)).withStyle(ChatFormatting.GOLD);
        int told = tell(server, team, Component.translatable(MechanicalMasteryReborn.MOD_ID + ".spell.ended", title(handicap).withStyle(ChatFormatting.GOLD))
                .withStyle(ChatFormatting.GRAY), title, Component.empty(), BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.BEACON_DEACTIVATE), 1.0F);
        LOGGER.info("Team {}'s {} spell ended; told {} of its players online", team, handicap.id(), told);
    }

    /** The spell's name, such as "Energy Spell", as its start's title and the Athanor show it. */
    public static MutableComponent title(Handicap handicap) {
        return Component.translatable(MechanicalMasteryReborn.MOD_ID + ".spell.title", Challenges.name(handicap));
    }

    /** What the handicap costs the team while it bears its spell, as the Athanor shows it. */
    public static MutableComponent cost(Handicap handicap) {
        return Component.translatable(MechanicalMasteryReborn.MOD_ID + ".spell." + handicap.id() + ".cost");
    }

    /** Tells {@code team}'s players online, with a title and a sound, and says how many it told. */
    private static int tell(MinecraftServer server, UUID team, Component message, Component title, Component subtitle, Holder<SoundEvent> holder,
            float volume) {
        int told = 0;
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (Teams.of(player).equals(team)) {
                told++;
                player.sendSystemMessage(message);
                player.connection.send(new ClientboundSetSubtitleTextPacket(subtitle));
                player.connection.send(new ClientboundSetTitleTextPacket(title));
                player.connection.send(new ClientboundSoundEntityPacket(holder, SoundSource.UI, player, volume, 1.0F, player.getRandom().nextLong()));
            }
        }
        return told;
    }
}
