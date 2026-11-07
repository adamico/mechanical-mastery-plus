// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.cod;

import com.mojang.serialization.MapCodec;
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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

/**
 * C.O.D., the Conscious Overproduction Device. A click opens its one slot; a click with Quintessence puts it in, in its
 * last stand, and a hand is told on the action bar why it takes none otherwise. On the server it ticks, to make essence.
 */
public final class CodBlock extends BaseEntityBlock {

    /** Whether it has shut down for good, its team's last stand won, and so stands dark. */
    public static final BooleanProperty SHUT_DOWN = BooleanProperty.create("shut_down");

    /** Whether its team is awake, so the client lights the fish on its lid; dormant, the fish stands dark. */
    public static final BooleanProperty AWAKE = BooleanProperty.create("awake");

    /**
     * Whether its team plays a challenge, so the client lights the fish on its lid red in place of green; the team's
     * state holds it, as it does {@link #AWAKE}.
     */
    public static final BooleanProperty CHALLENGED = BooleanProperty.create("challenged");

    private static final MapCodec<CodBlock> CODEC = simpleCodec(CodBlock::new);

    public CodBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(SHUT_DOWN, false).setValue(AWAKE, false).setValue(CHALLENGED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(SHUT_DOWN, AWAKE, CHALLENGED);
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
        return new CodBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide() ? null : createTickerHelper(type, Cod.BLOCK_ENTITY.get(), CodBlockEntity::serverTick);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit) {
        if (!stack.is(Essences.item(Essence.QUINTESSENCE))) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (!(level.getBlockEntity(pos) instanceof CodBlockEntity cod)) {
            return InteractionResult.PASS;
        }
        if (state.getValue(SHUT_DOWN)) {
            return refuse(player, Component.translatable("mechanicalmasteryreborn.cod.shut_down"));
        }
        if (cod.feed(stack) == 0) {
            return refuse(player, Component.translatable("mechanicalmasteryreborn.cod.no_last_stand"));
        }
        return InteractionResult.SUCCESS_SERVER;
    }

    /** Takes the click, so nothing is placed, and tells {@code player} why on the action bar. */
    private static InteractionResult refuse(Player player, Component why) {
        player.sendOverlayMessage(why);
        return InteractionResult.CONSUME;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof CodBlockEntity cod) {
            player.openMenu(cod, pos);
        }
        return InteractionResult.SUCCESS;
    }
}
