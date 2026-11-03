// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.challenge;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import io.github._5thlayer.mechanicalmasteryreborn.MechanicalMasteryReborn;
import io.github._5thlayer.mechanicalmasteryreborn.record.Teams;
import net.minecraft.ChatFormatting;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import org.slf4j.Logger;

/**
 * While a team's {@link Assault} runs, explosions damage its claimed blocks (#241, ADR 0010), so its creepers are a
 * threat to the base: the team's {@link Explosions} are turned on as the Assault starts, and its own value, kept here
 * and saved with the world so a restart in between still knows it, put back as it ends, by a win or a reset. A player
 * who turns them off meanwhile sees them turned back on, and the team is told. Every other claim protection stays.
 */
public final class AssaultExplosions extends SavedData {

    /** Told to a team whose player turned explosions off while its Assault runs. */
    public static final String STAY_ON = MechanicalMasteryReborn.MOD_ID + ".assault.explosions_stay_on";

    private static final Logger LOGGER = LogUtils.getLogger();

    private static final Codec<AssaultExplosions> CODEC = Codec.unboundedMap(UUIDUtil.STRING_CODEC, Codec.BOOL)
            .xmap(AssaultExplosions::new, state -> state.own).fieldOf("own").codec();

    public static final SavedDataType<AssaultExplosions> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath(MechanicalMasteryReborn.MOD_ID, "assault_explosions"), AssaultExplosions::new, CODEC);

    /** Each team whose explosions are lifted, and its own value, put back as its Assault ends. */
    private final Map<UUID, Boolean> own;

    private AssaultExplosions() {
        this(Map.of());
    }

    private AssaultExplosions(Map<UUID, Boolean> own) {
        this.own = new HashMap<>(own);
    }

    public static AssaultExplosions get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(TYPE);
    }

    /** Whether {@code team}'s explosions are lifted for its Assault. */
    public boolean lifted(UUID team) {
        return own.containsKey(team);
    }

    /** Turns {@code team}'s explosions on, as its Assault starts, keeping its own value; once only. */
    static void lift(MinecraftServer server, UUID team) {
        AssaultExplosions state = get(server);
        if (state.lifted(team)) {
            return;
        }
        Explosions.get().allowed(server, team).ifPresent(allowed -> {
            state.own.put(team, allowed);
            state.setDirty();
            Explosions.get().set(server, team, true);
            LOGGER.info("Lifted team {}'s explosion protection for its Assault; its own value is {}", team, allowed);
        });
    }

    /** Puts back {@code team}'s own value, as its Assault ends. */
    static void restore(MinecraftServer server, UUID team) {
        AssaultExplosions state = get(server);
        Boolean allowed = state.own.remove(team);
        if (allowed != null) {
            state.setDirty();
            Explosions.get().set(server, team, allowed);
            LOGGER.info("Restored team {}'s explosion protection to its own value, {}", team, allowed);
        }
    }

    /**
     * Turns {@code team}'s explosions back on, on the next tick, if a player turned them off while its Assault runs,
     * and tells the team. The next tick, so FTB has finished its own change and sync first.
     */
    public static void changed(MinecraftServer server, UUID team) {
        server.execute(() -> {
            if (get(server).lifted(team) && !Explosions.get().allowed(server, team).orElse(true)) {
                Explosions.get().set(server, team, true);
                Component told = Component.translatable(STAY_ON).withStyle(ChatFormatting.RED);
                for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                    if (Teams.of(player).equals(team)) {
                        player.sendSystemMessage(told);
                    }
                }
                LOGGER.info("Team {} turned explosions off during its Assault; turned back on", team);
            }
        });
    }
}
