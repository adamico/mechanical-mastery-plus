// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.cod;

import io.github._5thlayer.mechanicalmasteryreborn.ServerConfig;
import io.github._5thlayer.mechanicalmasteryreborn.essence.Essence;
import io.github._5thlayer.mechanicalmasteryreborn.essence.Essences;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;

/**
 * A placed C.O.D. Placed, its reserve holds a stack of Advanced essence, so a new island has no wait. While
 * dormant it makes one more each {@link ServerConfig#codDormantInterval interval} its reserve holds less than
 * a stack, and nothing while it holds one: a steady pace, never an unlimited supply. What it makes while
 * dormant never counts toward Inflation. Waking, its faster pace and its team's race come in later slices
 * (#166-#168).
 */
public final class CodBlockEntity extends BlockEntity implements MenuProvider {

    private static final String RESERVE_KEY = "reserve";
    private static final String TICKS_TOWARD_NEXT_KEY = "ticks_toward_next";

    private final CodReserve reserve = new CodReserve(() -> Essences.item(Essence.ADVANCED), this::setChanged);
    /**
     * Ticks toward the next essence; none while the reserve holds a stack, so an emptied one starts afresh. Saved
     * whenever anything else marks C.O.D. changed, as making an essence does, rather than every tick.
     */
    private int ticksTowardNext;

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

    /** Whether C.O.D. is making essence: while its reserve holds less than a stack. */
    public boolean isMaking() {
        return !reserve.isFull();
    }

    /** The ticks done toward the next essence, out of {@link ServerConfig#codDormantInterval}; none while it isn't making. */
    public int ticksTowardNext() {
        return ticksTowardNext;
    }

    static void serverTick(Level level, BlockPos pos, BlockState state, CodBlockEntity cod) {
        cod.tick(ServerConfig.codDormantInterval());
    }

    private void tick(int interval) {
        if (reserve.isFull()) {
            ticksTowardNext = 0;
            return;
        }
        if (++ticksTowardNext >= interval) {
            ticksTowardNext = 0;
            reserve.makeOne();
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        reserve.serialize(output.child(RESERVE_KEY));
        output.putInt(TICKS_TOWARD_NEXT_KEY, ticksTowardNext);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        reserve.deserialize(input.childOrEmpty(RESERVE_KEY));
        ticksTowardNext = input.getIntOr(TICKS_TOWARD_NEXT_KEY, 0);
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
