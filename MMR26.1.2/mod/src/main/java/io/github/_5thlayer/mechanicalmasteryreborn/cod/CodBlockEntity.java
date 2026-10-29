// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.cod;

import io.github._5thlayer.mechanicalmasteryreborn.essence.Essence;
import io.github._5thlayer.mechanicalmasteryreborn.essence.Essences;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;

/**
 * A placed C.O.D. While dormant it only keeps its reserve, a stack of Advanced essence that refills the
 * moment it is emptied; it makes nothing else and raises no Inflation. Waking, its production and its
 * team's race come in later slices (#166-#168).
 */
public final class CodBlockEntity extends BlockEntity implements MenuProvider {

    private static final String RESERVE_KEY = "reserve";

    private final CodReserve reserve = new CodReserve(() -> Essences.item(Essence.ADVANCED), this::setChanged);

    public CodBlockEntity(BlockPos pos, BlockState state) {
        super(Cod.BLOCK_ENTITY.get(), pos, state);
        reserve.fill();
    }

    /** The reserve, as automation and the menu reach it: extract only. */
    public ResourceHandler<ItemResource> reserve() {
        return reserve;
    }

    CodReserve reserveSlot() {
        return reserve;
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        reserve.serialize(output.child(RESERVE_KEY));
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        reserve.deserialize(input.childOrEmpty(RESERVE_KEY));
        // A saved reserve is never empty, but one from a damaged save is filled rather than left so.
        if (reserve.getResource(0).isEmpty()) {
            reserve.fill();
        }
    }

    @Override
    public Component getDisplayName() {
        return getBlockState().getBlock().getName();
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new CodMenu(containerId, inventory, this);
    }
}
