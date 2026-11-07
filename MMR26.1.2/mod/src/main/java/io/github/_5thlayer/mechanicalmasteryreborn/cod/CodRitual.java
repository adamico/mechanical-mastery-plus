// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.cod;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.mojang.logging.LogUtils;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.AssaultState;
import io.github._5thlayer.mechanicalmasteryreborn.record.Teams;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import org.slf4j.Logger;

/**
 * The ritual a challenge's start plays on its team's C.O.D.: an ominous trial spawner's call and a column of its flames
 * and the void's motes about C.O.D., for every player on the team's island to hear and see wherever they stand on it,
 * of any team. It is cosmetic and holds nothing; the look C.O.D. keeps while the challenge is played is its block's
 * ({@link CodBlock#CHALLENGED}). Only a start plays it: a reset, a stop or a win plays nothing.
 */
public final class CodRitual {

    private static final Logger LOGGER = LogUtils.getLogger();
    /** Loud enough to carry across an island: a sound is heard up to sixteen blocks for each unit of its volume. */
    private static final float VOLUME = 16.0F;

    private CodRitual() {
    }

    /**
     * Plays the ritual on {@code team}'s C.O.D., and says where: nowhere while its C.O.D. has not ticked yet, so none is
     * found.
     */
    public static Optional<GlobalPos> play(MinecraftServer server, UUID team) {
        Optional<GlobalPos> cod = AssaultState.get(server).cod(team);
        ServerLevel level = cod.map(pos -> server.getLevel(pos.dimension())).orElse(null);
        if (level == null) {
            LOGGER.warn("Team {} has no C.O.D. found, so no ritual is played on it", team);
            return Optional.empty();
        }
        BlockPos pos = cod.get().pos();
        double x = pos.getX() + 0.5;
        double y = pos.getY() + 1.0;
        double z = pos.getZ() + 0.5;
        var call = BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.TRIAL_SPAWNER_OMINOUS_ACTIVATE);
        var rise = BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.END_PORTAL_SPAWN);
        List<ServerPlayer> audience = audience(level, team);
        for (ServerPlayer player : audience) {
            long seed = player.getRandom().nextLong();
            player.connection.send(new ClientboundSoundPacket(call, SoundSource.BLOCKS, x, y, z, VOLUME, 0.8F, seed));
            player.connection.send(new ClientboundSoundPacket(rise, SoundSource.BLOCKS, x, y, z, VOLUME, 1.4F, seed));
            level.sendParticles(player, ParticleTypes.TRIAL_SPAWNER_DETECTED_PLAYER_OMINOUS, true, true, x, y, z, 40, 0.6, 1.2, 0.6, 0.02);
            level.sendParticles(player, ParticleTypes.SOUL_FIRE_FLAME, true, true, x, y, z, 30, 0.4, 0.8, 0.4, 0.05);
            level.sendParticles(player, ParticleTypes.REVERSE_PORTAL, true, true, x, y + 0.5, z, 80, 0.8, 1.5, 0.8, 0.1);
        }
        LOGGER.info("Played the challenge's ritual on team {}'s C.O.D. at {}, for {} players on its island", team, pos, audience.size());
        return cod;
    }

    /**
     * The players on {@code team}'s island, in {@code level}: those standing on it, of any team, and not on the spawn
     * island or in the void nearest it; in a world of one team, every player in {@code level}.
     */
    public static List<ServerPlayer> audience(ServerLevel level, UUID team) {
        return level.players().stream().filter(player -> on(level, player.blockPosition(), team)).toList();
    }

    private static boolean on(ServerLevel level, BlockPos pos, UUID team) {
        return Teams.at(level, pos).equals(team) && (team.equals(Teams.WORLD) || Teams.onIsland(level, pos));
    }
}
