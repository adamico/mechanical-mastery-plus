// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.lab;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import io.github._5thlayer.mechanicalmasteryreborn.challenge.Refunds;
import io.github._5thlayer.mechanicalmasteryreborn.record.Teams;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jspecify.annotations.Nullable;

/**
 * The lab's ticking part (#259): it pushes the refund waiting for the team whose island it stands on, a stack at a
 * time, into any block beside it that takes items, and starts again when one has room. It holds nothing itself, since
 * the refund is the team's ({@link Refunds}), and offers nothing to take from it.
 */
public final class LabBlockEntity extends BlockEntity {

    private @Nullable Map<Direction, BlockCapabilityCache<ResourceHandler<ItemResource>, @Nullable Direction>> neighbours;

    public LabBlockEntity(BlockPos pos, BlockState state) {
        super(Lab.BLOCK_ENTITY.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, LabBlockEntity lab) {
        if (level instanceof ServerLevel server) {
            lab.push(server);
        }
    }

    /** Pushes the first stack of the refund that one of the blocks beside it takes, as much of it as that block takes. */
    private void push(ServerLevel server) {
        Refunds refunds = Refunds.get(server.getServer());
        UUID team = Teams.at(server, worldPosition);
        if (refunds.count(team) == 0) {
            return;
        }
        if (neighbours == null) {
            neighbours = new EnumMap<>(Direction.class);
            for (Direction side : Direction.values()) {
                neighbours.put(side, BlockCapabilityCache.create(Capabilities.Item.BLOCK, server, worldPosition.relative(side), side.getOpposite()));
            }
        }
        List<ItemStack> waiting = refunds.waiting(team);
        for (ItemStack stack : waiting) {
            if (stack.isEmpty()) {
                continue;
            }
            for (var neighbour : neighbours.values()) {
                ResourceHandler<ItemResource> handler = neighbour.getCapability();
                if (handler == null) {
                    continue;
                }
                try (Transaction transaction = Transaction.openRoot()) {
                    int moved = handler.insert(ItemResource.of(stack), stack.getCount(), transaction);
                    if (moved > 0) {
                        transaction.commit();
                        stack.shrink(moved);
                        refunds.compact(team);
                        refunds.setDirty();
                        return;
                    }
                }
            }
        }
    }
}
