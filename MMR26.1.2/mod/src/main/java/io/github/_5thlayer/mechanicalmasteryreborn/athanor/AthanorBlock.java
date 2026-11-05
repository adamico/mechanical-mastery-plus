// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.athanor;

import com.mojang.serialization.MapCodec;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.NullShards;
import io.github._5thlayer.mechanicalmasteryreborn.essence.Essence;
import io.github._5thlayer.mechanicalmasteryreborn.essence.Essences;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

/**
 * The Athanor's block (#263). A hand of Nullessence or Null Shards puts them in, and is told on the action bar why it
 * takes none; an empty hand opens its menu. On the server it ticks, to work.
 */
public final class AthanorBlock extends BaseEntityBlock {

    private static final MapCodec<AthanorBlock> CODEC = simpleCodec(AthanorBlock::new);

    public AthanorBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AthanorBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide() ? null : createTickerHelper(type, Athanor.BLOCK_ENTITY.get(), AthanorBlockEntity::serverTick);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit) {
        boolean shard = stack.is(NullShards.ITEM.get());
        if (!stack.is(Essences.item(Essence.NULLESSENCE)) && !shard) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (!(level.getBlockEntity(pos) instanceof AthanorBlockEntity athanor)) {
            return InteractionResult.PASS;
        }
        if (athanor.feed(stack) == 0) {
            player.sendOverlayMessage(Component.translatable(shard ? "mechanicalmasteryreborn.athanor.shard_refused"
                    : "mechanicalmasteryreborn.athanor.full"));
            return InteractionResult.CONSUME;
        }
        return InteractionResult.SUCCESS_SERVER;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof AthanorBlockEntity athanor) {
            player.openMenu(athanor, pos);
        }
        return InteractionResult.SUCCESS;
    }
}
