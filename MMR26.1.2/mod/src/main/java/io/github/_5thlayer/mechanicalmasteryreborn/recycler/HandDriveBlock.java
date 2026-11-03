// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.recycler;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

import io.github._5thlayer.mechanicalmasteryreborn.ServerConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

/**
 * A hand drive: each right-click turns it once, and holding the click keeps it turning, each turn delivered
 * to its Recycler's core. It turns at most once every {@link #CAP} ticks, as fast as a player and never faster:
 * a click while it still turns is kept, one at most, and turns it when the cap allows. Drives add up, so
 * players on more drives run a Recycler faster.
 *
 * <p>Each turn is heard and seen ({@link Drives#show}), a lost one too.
 *
 * <p>Turning it is work (#222): each turn that counts adds {@link ServerConfig#handDriveExhaustion} to the food
 * exhaustion of the player whose click turned it, a kept click's player when it turns. A survival player too hungry
 * to sprint can't turn it: the click is refused as a lost turn, and the action bar says why. Creative players pay
 * nothing and are never refused.
 */
public final class HandDriveBlock extends Block implements RecyclerPart {

    /** Ticks between two turns of one drive. */
    public static final int CAP = 5;

    /** Turned less than {@link #CAP} ticks ago. */
    public static final BooleanProperty TURNING = BooleanProperty.create("turning");
    /** Clicked while turning, so it turns again when the cap allows. */
    public static final BooleanProperty QUEUED = BooleanProperty.create("queued");
    /** At or below this food a player can't turn a drive: vanilla's sprinting limit. */
    public static final int HUNGRY = 6;

    /** Who clicked each kept click, so they pay for it when it turns. */
    private static final Map<ServerLevel, Map<BlockPos, UUID>> KEPT = new WeakHashMap<>();

    public HandDriveBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(TURNING, false).setValue(QUEUED, false).setValue(PLACE, RecyclerPlace.NONE));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(TURNING, QUEUED, PLACE);
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
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level instanceof ServerLevel server) {
            if (!player.isCreative() && player.getFoodData().getFoodLevel() <= HUNGRY) {
                Drives.show(server, pos, null, RecyclerCoreBlockEntity.Turn.STOPPED);
                player.sendOverlayMessage(Component.translatable("mechanicalmasteryreborn.hand_drive.too_hungry"));
            } else if (!state.getValue(TURNING)) {
                turn(server, pos, state, player);
            } else if (!state.getValue(QUEUED)) {
                server.setBlock(pos, state.setValue(QUEUED, true), UPDATE_CLIENTS);
                KEPT.computeIfAbsent(server, l -> new HashMap<>()).put(pos.immutable(), player.getUUID());
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (state.getValue(QUEUED)) {
            var keptBy = forget(level, pos);
            turn(level, pos, state, keptBy == null ? null : level.getPlayerByUUID(keptBy));
        } else {
            level.setBlock(pos, state.setValue(TURNING, false), UPDATE_CLIENTS);
        }
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston) {
        super.affectNeighborsAfterRemoval(state, level, pos, movedByPiston);
        forget(level, pos);
    }

    /** Takes who kept the click at {@code pos}, if anyone did. A click kept over a restart is nobody's, and turns free. */
    private static @Nullable UUID forget(ServerLevel level, BlockPos pos) {
        var kept = KEPT.get(level);
        return kept == null ? null : kept.remove(pos);
    }

    private void turn(ServerLevel level, BlockPos pos, BlockState state, @Nullable Player player) {
        level.setBlock(pos, state.setValue(TURNING, true).setValue(QUEUED, false), UPDATE_CLIENTS);
        level.scheduleTick(pos, this, CAP);
        var core = RecyclerPart.coreOf(level, pos);
        var turn = core.map(RecyclerCoreBlockEntity::turn).orElse(RecyclerCoreBlockEntity.Turn.STOPPED);
        Drives.show(level, pos, core.orElse(null), turn);
        if (player != null && !player.isCreative() && turn != RecyclerCoreBlockEntity.Turn.STOPPED) {
            player.causeFoodExhaustion((float) ServerConfig.handDriveExhaustion());
        }
    }
}
