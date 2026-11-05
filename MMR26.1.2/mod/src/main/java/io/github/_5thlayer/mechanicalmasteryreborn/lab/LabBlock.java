// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.lab;

import com.mojang.serialization.MapCodec;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.NullShards;
import io.github._5thlayer.mechanicalmasteryreborn.essence.Essence;
import io.github._5thlayer.mechanicalmasteryreborn.essence.Essences;
import io.github._5thlayer.mechanicalmasteryreborn.record.TeamState;
import io.github._5thlayer.mechanicalmasteryreborn.record.Teams;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
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
 * The lab, where the team whose island it stands on chooses a handicap to start a challenge. A click opens it. Once the
 * team's last stand has started it awakens as the Hermetic Lab (#263), and stands changed ({@link #HERMETIC}): a click
 * opens the Hermetic Lab's menu instead, Jade names it so, and a hand of Nullessence or Null Shards puts them in, told on the action bar why
 * it takes none. On the server it ticks, to work as the Hermetic Lab ({@link LabBlockEntity}).
 */
public final class LabBlock extends BaseEntityBlock {

    /** Whether it has awakened as the Hermetic Lab, its team's last stand started. */
    public static final BooleanProperty HERMETIC = BooleanProperty.create("hermetic");

    private static final MapCodec<LabBlock> CODEC = simpleCodec(LabBlock::new);
    private static final Component TITLE = Component.translatable("container.mechanicalmasteryreborn.lab");
    private static final Component HERMETIC_NAME = Component.translatable("container.mechanicalmasteryreborn.hermetic_lab");

    public LabBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(HERMETIC, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(HERMETIC);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    /** The lab's item, named the Hermetic Lab while it stands awakened, as Jade and a pick show it. */
    @Override
    protected ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData) {
        ItemStack stack = super.getCloneItemStack(level, pos, state, includeData);
        if (state.getValue(HERMETIC)) {
            stack.set(DataComponents.ITEM_NAME, HERMETIC_NAME);
        }
        return stack;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new LabBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide() ? null : createTickerHelper(type, Lab.BLOCK_ENTITY.get(), LabBlockEntity::serverTick);
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
        if (!hermetic((ServerLevel) level, pos) || !(level.getBlockEntity(pos) instanceof LabBlockEntity lab)) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        if (lab.feed(stack) == 0) {
            player.sendOverlayMessage(Component.translatable(shard ? "mechanicalmasteryreborn.hermetic_lab.shard_refused"
                    : "mechanicalmasteryreborn.hermetic_lab.full"));
            return InteractionResult.CONSUME;
        }
        return InteractionResult.SUCCESS_SERVER;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!(level instanceof ServerLevel server)) {
            return InteractionResult.SUCCESS;
        }
        // Asked for here, a lab placed before it had a block entity gets one, and ticks from then on.
        if (server.getBlockEntity(pos) instanceof LabBlockEntity lab && hermetic(server, pos)) {
            player.openMenu(lab, pos);
        } else {
            LabMenu.open(player, server, pos);
        }
        return InteractionResult.SUCCESS;
    }

    /** What a click opens: the Hermetic Lab's menu once the team's last stand has started, and the lab's before. */
    @Override
    protected @Nullable MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos) {
        if (!(level instanceof ServerLevel server)) {
            return null;
        }
        if (hermetic(server, pos) && server.getBlockEntity(pos) instanceof LabBlockEntity lab) {
            return lab;
        }
        return new SimpleMenuProvider((containerId, inventory, player) -> new LabMenu(containerId, inventory, server, pos), TITLE);
    }

    /** Whether the lab at {@code pos} is the Hermetic Lab: its team's last stand has started. */
    static boolean hermetic(ServerLevel level, BlockPos pos) {
        return TeamState.get(level.getServer()).lastStand(Teams.at(level, pos)).isPresent();
    }
}
