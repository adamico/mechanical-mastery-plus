// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.port;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.Challenges;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.Handicap;
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
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

/**
 * A challenge's Port, beside C.O.D. It takes its challenge's Transmuted Essence while its team plays that challenge,
 * by a right-click with it or from automation on any side, until the quota; and refuses anything else, a stale one made
 * before the attempt's last reset among them (ADR 0016). A hand keeps what it offered and is told why on the action bar;
 * automation is refused silently.
 */
public final class PortBlock extends BaseEntityBlock {

    public static final EnumProperty<PortPhase> PHASE = EnumProperty.create("phase", PortPhase.class);

    private static final Codec<Handicap> HANDICAP_CODEC = Codec.STRING.comapFlatMap(
            id -> Handicap.byId(id).map(DataResult::success).orElseGet(() -> DataResult.error(() -> "No handicap " + id)), Handicap::id);
    private static final MapCodec<PortBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            HANDICAP_CODEC.fieldOf("handicap").forGetter(PortBlock::handicap), propertiesCodec())
            .apply(instance, PortBlock::new));

    private final Handicap handicap;

    public PortBlock(Handicap handicap, Properties properties) {
        super(properties);
        this.handicap = handicap;
        registerDefaultState(stateDefinition.any().setValue(PHASE, PortPhase.CLOSED));
    }

    /** The challenge whose Port it is. */
    public Handicap handicap() {
        return handicap;
    }

    /** The Transmuted Essence it takes. */
    public Essence essence() {
        return Port.essence(handicap);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(PHASE);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PortBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide() ? null : createTickerHelper(type, Port.BLOCK_ENTITY.get(), PortBlockEntity::serverTick);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit) {
        if (stack.isEmpty()) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (!(level.getBlockEntity(pos) instanceof PortBlockEntity port)) {
            return InteractionResult.PASS;
        }
        if (!stack.is(Essences.item(essence()))) {
            return refuse(player, Component.translatable("mechanicalmasteryreborn.port.only", new ItemStack(Essences.item(essence())).getHoverName()));
        }
        PortPhase phase = port.phase();
        if (phase == PortPhase.CLOSED) {
            return refuse(player, Component.translatable("mechanicalmasteryreborn.port.closed",
                    Challenges.title(handicap)));
        }
        if (phase == PortPhase.WON) {
            return refuse(player, Component.translatable("mechanicalmasteryreborn.port.won"));
        }
        if (port.stale(stack)) {
            return refuse(player, Component.translatable("mechanicalmasteryreborn.port.stale"));
        }
        int taken = port.take(stack.getCount());
        stack.shrink(taken);
        return InteractionResult.SUCCESS_SERVER;
    }

    /** Takes the click, so nothing is placed, and tells {@code player} why on the action bar. */
    private static InteractionResult refuse(Player player, Component why) {
        player.sendOverlayMessage(why);
        return InteractionResult.CONSUME;
    }
}
