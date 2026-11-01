// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.recycler;

import io.github._5thlayer.mechanicalmasteryreborn.ServerConfig;
import io.github._5thlayer.mechanicalmasteryreborn.recycler.RecyclerCoreBlockEntity.Status;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.energy.SimpleEnergyHandler;

/**
 * An FE drive's store and clock. The store takes FE from any side, holds {@code recycler.fe_drive.store_turns}
 * turns' worth, and gives none back. Every {@code recycler.fe_drive.interval} ticks, when the store holds a turn's
 * worth ({@code recycler.fe_drive.fe_per_turn}) and its Recycler can work, the drive turns the core and spends it;
 * a Recycler that can't work costs nothing, so an idle drive fills up and then takes no more.
 */
public final class FeDriveBlockEntity extends BlockEntity {

    private static final String STORE_KEY = "store";
    private static final String COOLDOWN_KEY = "cooldown";

    private final Store store = new Store();
    /** Ticks until the next turn. */
    private int cooldown;

    public FeDriveBlockEntity(BlockPos pos, BlockState state) {
        super(Recycler.FE_DRIVE_ENTITY.get(), pos, state);
    }

    /** The store as a cable reaches it: insert only. */
    public EnergyHandler face() {
        return store;
    }

    static void serverTick(Level level, BlockPos pos, BlockState state, FeDriveBlockEntity drive) {
        drive.store.fit();
        if (drive.cooldown > 0) {
            drive.cooldown--;
            return;
        }
        int cost = ServerConfig.feDriveFePerTurn();
        if (drive.store.getAmountAsInt() < cost) {
            return;
        }
        var core = RecyclerPart.coreOf(level, pos).filter(found -> found.status() == Status.READY).orElse(null);
        if (core == null) {
            return;
        }
        var turn = core.turn();
        drive.store.set(drive.store.getAmountAsInt() - cost);
        // This tick counts: the next turn is the interval away.
        drive.cooldown = ServerConfig.feDriveInterval() - 1;
        Drives.show((ServerLevel) level, pos, core, turn);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        store.serialize(output.child(STORE_KEY));
        output.putInt(COOLDOWN_KEY, cooldown);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        store.deserialize(input.childOrEmpty(STORE_KEY));
        cooldown = input.getIntOr(COOLDOWN_KEY, 0);
    }

    /** The store: as many turns' worth as the config says, which it follows, and nothing out. */
    private final class Store extends SimpleEnergyHandler {

        Store() {
            super(0, 0, 0);
            fit();
        }

        /** Sizes the store to the config, which may have changed since it was made, spilling what no longer fits. */
        void fit() {
            capacity = ServerConfig.feDriveFePerTurn() * ServerConfig.feDriveStoreTurns();
            maxInsert = capacity;
            if (energy > capacity) {
                set(capacity);
            }
        }

        @Override
        protected void onEnergyChanged(int previousAmount) {
            setChanged();
        }
    }
}
