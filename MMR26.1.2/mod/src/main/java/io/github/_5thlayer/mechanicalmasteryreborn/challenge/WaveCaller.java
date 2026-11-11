// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.challenge;

import java.util.UUID;

import com.mojang.serialization.MapCodec;
import io.github._5thlayer.mechanicalmasteryreborn.MechanicalMasteryReborn;
import io.github._5thlayer.mechanicalmasteryreborn.record.TeamState;
import io.github._5thlayer.mechanicalmasteryreborn.record.Teams;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * The Wave Caller (the line's glossary, #341): a block a team places anywhere on its island, as many as it likes, whose
 * use brings on its gate's next wave at once, from wherever it stands. The Assault's comes at a platform drawn at random
 * ({@link Assault#call}); with no Assault running, its open home gates' come in the same gate. It calls nothing off its
 * team's island, nor while the Assault is paused, and stays past a win.
 */
public final class WaveCaller extends Block {

    /**
     * The Permit a team's Account is given when it starts the protection challenge, kept by a win and taken back by a
     * stop, which the Wave Caller's Offer needs.
     */
    public static final String PERMIT = "reborn_protection_started";

    public static final MapCodec<WaveCaller> CODEC = simpleCodec(WaveCaller::new);

    public WaveCaller(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level instanceof ServerLevel server && player instanceof ServerPlayer caller) {
            UUID team = Teams.at(server, pos);
            if (!Teams.onIsland(server, pos) || !team.equals(Teams.of(caller))) {
                caller.sendOverlayMessage(Component.translatable(MechanicalMasteryReborn.MOD_ID + ".wave_caller.not_home"));
            } else if (!call(server, team)) {
                caller.sendOverlayMessage(Component.translatable(MechanicalMasteryReborn.MOD_ID + ".wave_caller.no_gate"));
            }
        }
        return InteractionResult.SUCCESS;
    }

    /** Gives {@code team}'s Account {@link #PERMIT} if its protection chain is unlocked, by a start not stopped since or a win. */
    public static void grant(MinecraftServer server, UUID team) {
        if (TeamState.get(server).unlocked(team, Handicap.PROTECTION)) {
            Accounts.get().grant(server, team, PERMIT);
        }
    }

    /** Takes {@link #PERMIT} from {@code team}'s Account, as a stop locks its protection chain again (ADR 0013). */
    static void lock(MinecraftServer server, UUID team) {
        Accounts.get().revoke(server, team, PERMIT);
    }

    /** Calls {@code team}'s next wave: its running Assault's, or else its open home gates'. Says whether one was called. */
    public static boolean call(ServerLevel level, UUID team) {
        return Assault.call(level.getServer(), team) || Gates.get().callHome(level, team);
    }
}
