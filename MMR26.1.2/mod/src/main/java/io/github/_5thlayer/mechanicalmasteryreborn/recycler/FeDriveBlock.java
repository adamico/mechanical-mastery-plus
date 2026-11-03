// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.recycler;

import net.minecraft.server.level.ServerLevel;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import org.jspecify.annotations.Nullable;

/**
 * An FE drive: a drive that turns its Recycler on FE from its own store ({@link FeDriveBlockEntity}), as fast as the
 * server config lets it, and stops when the store is empty. The energy handicap cuts only generators, so it keeps
 * turning on what it stores while its team plays the energy challenge (MMR26.1.2 ADR 0009).
 */
public final class FeDriveBlock extends BaseEntityBlock implements RecyclerPart {

    private static final MapCodec<FeDriveBlock> CODEC = simpleCodec(FeDriveBlock::new);

    public FeDriveBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(PLACE, RecyclerPlace.NONE).setValue(FLOOR, RecyclerFloor.SINGLE));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(PLACE, FLOOR);
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (!oldState.is(this)) {
            RecyclerPart.reframeAround(level, pos);
        }
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston) {
        super.affectNeighborsAfterRemoval(state, level, pos, movedByPiston);
        RecyclerPart.reframeAround(level, pos);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return RecyclerPart.placed(defaultBlockState(), context);
    }

    @Override
    public Role role() {
        return Role.DRIVE;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FeDriveBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide() ? null : createTickerHelper(type, Recycler.FE_DRIVE_ENTITY.get(), FeDriveBlockEntity::serverTick);
    }
}
