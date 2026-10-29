// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.recycler;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;

/**
 * A hand drive: each right-click turns it once, and holding the click keeps it turning, each turn delivered
 * to its Recycler's core. It turns at most once every {@link #CAP} ticks, as fast as a player and never faster:
 * a click while it still turns is kept, one at most, and turns it when the cap allows. Drives add up, so
 * players on more drives run a Recycler faster.
 *
 * <p>Each turn is heard and seen: a click whose pitch rises with the decompose's progress, and sparks, when it
 * counts; the crafter's sound and a puff over the core when it decomposes; a dull click and smoke when the Recycler
 * can't decompose, so the turn is lost.
 *
 * <p>Its {@link #CORNER} is the corner of its core it stands at, taken when it is placed and set again when a core
 * is placed beside it, so its rails run along the Recycler's outer edges.
 */
public final class HandDriveBlock extends Block implements RecyclerPart {

    /** Ticks between two turns of one drive. */
    public static final int CAP = 5;

    /** Turned less than {@link #CAP} ticks ago. */
    public static final BooleanProperty TURNING = BooleanProperty.create("turning");
    /** Clicked while turning, so it turns again when the cap allows. */
    public static final BooleanProperty QUEUED = BooleanProperty.create("queued");
    public static final EnumProperty<DriveCorner> CORNER = EnumProperty.create("corner", DriveCorner.class);

    public HandDriveBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(TURNING, false).setValue(QUEUED, false).setValue(CORNER, DriveCorner.NORTH_WEST));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(TURNING, QUEUED, CORNER);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(CORNER, DriveCorner.at(context.getLevel(), context.getClickedPos()));
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
        var core = RecyclerPart.coreOf(level, pos);
        var turn = core.map(RecyclerCoreBlockEntity::turn).orElse(RecyclerCoreBlockEntity.Turn.STOPPED);
        double x = pos.getX() + 0.5;
        double y = pos.getY() + 1.0;
        double z = pos.getZ() + 0.5;
        switch (turn) {
            case COUNTED -> {
                float progress = (float) core.get().turns() / RecyclerCoreBlockEntity.TURNS;
                level.playSound(null, pos, SoundEvents.LEVER_CLICK, SoundSource.BLOCKS, 0.4F, 0.7F + 0.6F * progress);
                level.sendParticles(ParticleTypes.ELECTRIC_SPARK, x, y, z, 3, 0.25, 0.05, 0.25, 0.05);
            }
            case WORKED -> {
                BlockPos at = core.get().getBlockPos();
                level.playSound(null, at, SoundEvents.CRAFTER_CRAFT, SoundSource.BLOCKS, 1.0F, 1.0F);
                level.sendParticles(ParticleTypes.POOF, at.getX() + 0.5, at.getY() + 1.0, at.getZ() + 0.5, 6, 0.3, 0.05, 0.3, 0.02);
            }
            case STOPPED -> {
                level.playSound(null, pos, SoundEvents.CRAFTER_FAIL, SoundSource.BLOCKS, 0.5F, 1.0F);
                level.sendParticles(ParticleTypes.SMOKE, x, y, z, 2, 0.2, 0.05, 0.2, 0.01);
            }
        }
    }
}
