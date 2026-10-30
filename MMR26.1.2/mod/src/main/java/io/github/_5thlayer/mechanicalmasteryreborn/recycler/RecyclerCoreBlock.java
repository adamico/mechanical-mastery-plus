// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.recycler;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;

/**
 * A Recycler's core, in the middle of its 3x3's front edge (the line's glossary: Core). Its front faces the player
 * who placed it, out of the Recycler, whose 3x3 lies behind it, and the order its outputs are read in starts from
 * there ({@link RecyclerLayout}). Tier 1, the first core; its tier sets how many turns a decompose takes. Placed,
 * it turns the drives at its 3x3's corners to them.
 * An empty-hand right-click takes back the whole stack it holds, into the player's inventory, and gives up the
 * work under way; a click on an empty core, or with an item in hand, passes.
 */
public final class RecyclerCoreBlock extends BaseEntityBlock {

    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;

    private static final MapCodec<RecyclerCoreBlock> CODEC = simpleCodec(RecyclerCoreBlock::new);

    public RecyclerCoreBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    /** The centre of the 3x3 a core at {@code pos} fronts: the place right behind it. */
    public static BlockPos centre(BlockPos pos, BlockState state) {
        return pos.relative(state.getValue(FACING).getOpposite());
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (oldState.is(this)) {
            return;
        }
        BlockPos centre = centre(pos, state);
        for (DriveCorner corner : DriveCorner.values()) {
            BlockPos drive = corner.of(centre);
            BlockState at = level.getBlockState(drive);
            if (at.getBlock() instanceof HandDriveBlock && at.getValue(HandDriveBlock.CORNER) != corner) {
                level.setBlock(drive, at.setValue(HandDriveBlock.CORNER, corner), UPDATE_CLIENTS);
            }
        }
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit) {
        return stack.isEmpty() ? InteractionResult.TRY_WITH_EMPTY_HAND : InteractionResult.PASS;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof RecyclerCoreBlockEntity core) || core.held().isEmpty()) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide()) {
            player.getInventory().placeItemBackInInventory(core.takeBack());
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RecyclerCoreBlockEntity(pos, state);
    }
}
