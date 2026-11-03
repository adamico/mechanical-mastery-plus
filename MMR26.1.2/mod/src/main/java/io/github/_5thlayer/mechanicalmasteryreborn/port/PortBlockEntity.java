// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.port;

import java.util.UUID;

import io.github._5thlayer.mechanicalmasteryreborn.ServerConfig;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.Challenges;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.Handicap;
import io.github._5thlayer.mechanicalmasteryreborn.essence.Essence;
import io.github._5thlayer.mechanicalmasteryreborn.record.TeamState;
import io.github._5thlayer.mechanicalmasteryreborn.record.Teams;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jspecify.annotations.Nullable;

/**
 * A placed Port. What it has taken is counted in its team's state ({@link TeamState#delivered}), never here, so it
 * survives a reload and a reset clears it. It belongs to the team whose island it stands on, found as C.O.D.'s is,
 * and shows each tick whether that team plays its challenge, or has won it. Taking the last essence of the quota wins
 * the challenge ({@link Challenges#win}).
 */
public final class PortBlockEntity extends BlockEntity {

    private final PortInput input = new PortInput(this);
    /** The team whose island it stands on, found on first need: a Port never moves. */
    private @Nullable UUID team;

    public PortBlockEntity(BlockPos pos, BlockState state) {
        super(Port.BLOCK_ENTITY.get(), pos, state);
    }

    /** What automation inserts into, on every side: insert only, and only what {@link #needed} allows. */
    public ResourceHandler<ItemResource> input() {
        return input;
    }

    /** The challenge whose Port it is. */
    public Handicap handicap() {
        return ((PortBlock) getBlockState().getBlock()).handicap();
    }

    /** The Transmuted Essence it takes. */
    public Essence essence() {
        return Port.essence(handicap());
    }

    /** Whether its team plays its challenge, has won it, or neither; closed on a client, which doesn't know. */
    public PortPhase phase() {
        if (!(level instanceof ServerLevel server)) {
            return PortPhase.CLOSED;
        }
        TeamState state = TeamState.get(server.getServer());
        UUID owner = team(server);
        return state.won(owner, handicap()) ? PortPhase.WON : state.plays(owner, handicap()) ? PortPhase.OPEN : PortPhase.CLOSED;
    }

    /** The essence its team has delivered toward the quota; on the server only. */
    public long delivered() {
        return level instanceof ServerLevel server ? TeamState.get(server.getServer()).delivered(team(server)) : 0;
    }

    /** The essence that wins the challenge, by the tier its team's C.O.D. makes; none once every tier is blocked, or on a client. */
    public int quota() {
        if (!(level instanceof ServerLevel server)) {
            return 0;
        }
        return TeamState.get(server.getServer()).tier(team(server)).map(ServerConfig::quota).orElse(0);
    }

    /** How many more it takes: what's left of the quota while open, and none otherwise. */
    int needed() {
        return phase() == PortPhase.OPEN ? (int) Math.max(0, quota() - delivered()) : 0;
    }

    /** Takes up to {@code count} essence toward the quota, winning the challenge on the last, and says how many it took. */
    int take(int count) {
        int taken = Math.min(count, needed());
        if (taken == 0 || !(level instanceof ServerLevel server)) {
            return 0;
        }
        UUID owner = team(server);
        TeamState state = TeamState.get(server.getServer());
        state.deliver(owner, taken);
        if (state.delivered(owner) >= quota()) {
            Challenges.win(server.getServer(), owner);
        }
        showPhase();
        return taken;
    }

    static void serverTick(Level level, BlockPos pos, BlockState state, PortBlockEntity port) {
        port.showPhase();
    }

    /** Sets the block's look to its {@link #phase}, if it changed. */
    private void showPhase() {
        if (level == null) {
            return;
        }
        BlockState state = getBlockState();
        PortPhase phase = phase();
        if (state.getValue(PortBlock.PHASE) != phase) {
            level.setBlock(worldPosition, state.setValue(PortBlock.PHASE, phase), Block.UPDATE_ALL);
        }
    }

    private UUID team(ServerLevel server) {
        if (team == null) {
            team = Teams.at(server, worldPosition);
        }
        return team;
    }
}
