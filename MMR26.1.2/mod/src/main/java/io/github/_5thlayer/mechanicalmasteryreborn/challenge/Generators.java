// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.challenge;

import io.github._5thlayer.mechanicalmasteryreborn.MechanicalMasteryReborn;
import io.github._5thlayer.mechanicalmasteryreborn.record.TeamState;
import io.github._5thlayer.mechanicalmasteryreborn.record.Teams;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.energy.EmptyEnergyHandler;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jspecify.annotations.Nullable;

/**
 * The energy handicap's cost: a generator, a block in the pack's {@link #TAG}, gives no FE while the team whose island
 * it stands on plays the energy challenge (MMR26.1.2 ADR 0005). This answers for its FE ahead of every other mod, on
 * every block, with a face that shows the generator's own buffer but gives and takes nothing; anywhere else it answers
 * nothing, and the block's own mod answers as before. The suite's networks draw a generator's FE through that face
 * every tick, so the generator fills its own buffer and idles until the challenge ends. The pack fills the tag, so this names no suite mod.
 */
public final class Generators {

    /** The blocks the energy handicap stops: empty here, and filled by the pack. */
    public static final TagKey<Block> TAG = TagKey.create(Registries.BLOCK,
            Identifier.fromNamespaceAndPath(MechanicalMasteryReborn.MOD_ID, "generators"));

    /** Set while this asks a stopped generator's own mod for its face, so that it answers nothing to itself. */
    private static final ThreadLocal<Boolean> ASKING_OWN = ThreadLocal.withInitial(() -> false);

    private Generators() {
    }

    public static void register(IEventBus modBus) {
        // Ahead of the suite's FE faces, which register at the normal priority: NeoForge asks providers in turn.
        modBus.addListener(EventPriority.HIGH, RegisterCapabilitiesEvent.class, Generators::registerCapabilities);
    }

    private static void registerCapabilities(RegisterCapabilitiesEvent event) {
        // The tag isn't loaded yet, so the face is on every block, and asks the tag first on each lookup.
        event.registerBlock(Capabilities.Energy.BLOCK,
                (level, pos, state, entity, side) -> !ASKING_OWN.get() && isStopped(level, pos, state)
                        ? new Stopped(own(level, pos, side)) : null,
                BuiltInRegistries.BLOCK.stream().toArray(Block[]::new));
    }

    /** The face the generator's own mod answers with, which the next provider in turn gives, or an empty one. */
    private static EnergyHandler own(Level level, BlockPos pos, @Nullable Direction side) {
        boolean asking = ASKING_OWN.get();
        ASKING_OWN.set(true);
        try {
            EnergyHandler own = level.getCapability(Capabilities.Energy.BLOCK, pos, side);
            return own != null ? own : EmptyEnergyHandler.INSTANCE;
        } finally {
            ASKING_OWN.set(asking);
        }
    }

    /** Whether the block at {@code pos} is a generator its team's energy challenge stops. Server-side only. */
    public static boolean isStopped(Level level, BlockPos pos, BlockState state) {
        if (!(level instanceof ServerLevel server) || !state.is(TAG)) {
            return false;
        }
        TeamState teams = TeamState.get(server.getServer());
        return teams.anyPlays(Handicap.ENERGY) && teams.plays(Teams.at(server, pos), Handicap.ENERGY);
    }

    /** A stopped generator's face: its own buffer, for Jade and anything else that reads it, but no FE in or out. */
    private record Stopped(EnergyHandler own) implements EnergyHandler {

        @Override
        public long getAmountAsLong() {
            return own.getAmountAsLong();
        }

        @Override
        public long getCapacityAsLong() {
            return own.getCapacityAsLong();
        }

        @Override
        public int insert(int amount, TransactionContext transaction) {
            TransferPreconditions.checkNonNegative(amount);
            return 0;
        }

        @Override
        public int extract(int amount, TransactionContext transaction) {
            TransferPreconditions.checkNonNegative(amount);
            return 0;
        }
    }
}
