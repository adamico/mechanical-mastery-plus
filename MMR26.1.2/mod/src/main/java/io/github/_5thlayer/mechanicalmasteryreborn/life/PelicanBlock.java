// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.life;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jspecify.annotations.Nullable;

/**
 * A placed {@link Pelican}: fed and emptied by automation on any side, or by hand, a meal at a time in and its Vital
 * Essence out, its work in its {@link PelicanBlockEntity}. It stands as the alchemical vessel it is named after (#371): a
 * bronze base, a glass belly and a neck that rises and curves back down into the belly on both sides. Its contents are
 * dim while it eats and lit while it digests ({@link #LIT}).
 */
public final class PelicanBlock extends BaseEntityBlock {

    /** Whether it digests a ready batch: its contents lit and glowing. */
    public static final BooleanProperty LIT = BlockStateProperties.LIT;

    /** The light its contents give while it digests. */
    static final int LIT_LIGHT = 7;

    private static final MapCodec<PelicanBlock> CODEC = simpleCodec(PelicanBlock::new);

    /**
     * Its base, its belly and shoulder, its neck, and the arms that curve back into the belly, as its model draws them
     * ({@code publish/make-pelican.py}).
     */
    private static final VoxelShape SHAPE = Shapes.or(
            Block.box(2, 0, 2, 14, 3, 14),
            Block.box(3, 3, 3, 13, 11, 13),
            Block.box(4, 11, 4, 12, 12, 12),
            Block.box(7, 12, 7, 9, 14, 9),
            Block.box(1, 14, 7, 15, 16, 9),
            Block.box(1, 6, 7, 3, 14, 9),
            Block.box(13, 6, 7, 15, 14, 9));

    public PelicanBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(LIT, false));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LIT);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PelicanBlockEntity(pos, state);
    }

    /**
     * A click with a meal in hand feeds it one, as automation would, if it would eat it now; the click is taken either
     * way, so a meal is never eaten by the player while aiming at it. An empty hand goes on to {@link #useWithoutItem};
     * anything else falls through to the item's own use.
     */
    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit) {
        if (stack.isEmpty()) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        if (!Pelican.meal(ItemResource.of(stack))) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (!(level.getBlockEntity(pos) instanceof PelicanBlockEntity pelican)) {
            return InteractionResult.PASS;
        }
        int eaten;
        try (var transaction = Transaction.openRoot()) {
            eaten = pelican.face().insert(ItemResource.of(stack), 1, transaction);
            transaction.commit();
        }
        stack.consume(eaten, player);
        return eaten > 0 ? InteractionResult.SUCCESS_SERVER : InteractionResult.CONSUME;
    }

    /**
     * A click with an empty hand takes out the Vital Essence it holds, into the player's inventory, or at their feet if it
     * is full. The client can't see its output, so the click is taken either way, the same on both sides.
     */
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (!(level.getBlockEntity(pos) instanceof PelicanBlockEntity pelican)) {
            return InteractionResult.PASS;
        }
        ItemStack taken = pelican.takeOutput();
        if (taken.isEmpty()) {
            return InteractionResult.CONSUME;
        }
        player.getInventory().placeItemBackInInventory(taken);
        return InteractionResult.SUCCESS_SERVER;
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide() ? null : createTickerHelper(type, Pelican.BLOCK_ENTITY.get(), PelicanBlockEntity::serverTick);
    }
}
