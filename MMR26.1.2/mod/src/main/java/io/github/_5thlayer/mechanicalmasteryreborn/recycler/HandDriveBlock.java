// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.recycler;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;

/**
 * A hand drive: each right-click turns it once, and holding the click keeps it turning, each turn delivered
 * to its Recycler's core. It turns at most once every {@link #CAP} ticks, as fast as a player and never faster:
 * a click while it still turns is kept, one at most, and turns it when the cap allows. Drives add up, so
 * players on more drives run a Recycler faster.
 */
public final class HandDriveBlock extends Block implements RecyclerPart {

    /** Ticks between two turns of one drive. */
    public static final int CAP = 5;

    /** Turned less than {@link #CAP} ticks ago. */
    public static final BooleanProperty TURNING = BooleanProperty.create("turning");
    /** Clicked while turning, so it turns again when the cap allows. */
    public static final BooleanProperty QUEUED = BooleanProperty.create("queued");

    public HandDriveBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(TURNING, false).setValue(QUEUED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(TURNING, QUEUED);
    }

    @Override
    public Role role() {
        return Role.DRIVE;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level instanceof ServerLevel server) {
            if (!state.getValue(TURNING)) {
                turn(server, pos, state);
            } else if (!state.getValue(QUEUED)) {
                server.setBlock(pos, state.setValue(QUEUED, true), UPDATE_CLIENTS);
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (state.getValue(QUEUED)) {
            turn(level, pos, state);
        } else {
            level.setBlock(pos, state.setValue(TURNING, false), UPDATE_CLIENTS);
        }
    }

    private void turn(ServerLevel level, BlockPos pos, BlockState state) {
        level.setBlock(pos, state.setValue(TURNING, true).setValue(QUEUED, false), UPDATE_CLIENTS);
        level.scheduleTick(pos, this, CAP);
        RecyclerPart.coreOf(level, pos).ifPresent(RecyclerCoreBlockEntity::turn);
    }
}
