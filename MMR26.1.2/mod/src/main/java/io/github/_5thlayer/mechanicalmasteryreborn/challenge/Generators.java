// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.challenge;

import io.github._5thlayer.mechanicalmasteryreborn.MechanicalMasteryReborn;
import io.github._5thlayer.mechanicalmasteryreborn.record.TeamState;
import io.github._5thlayer.mechanicalmasteryreborn.record.Teams;
import net.minecraft.core.BlockPos;
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
import net.neoforged.neoforge.transfer.energy.EmptyEnergyHandler;

/**
 * The energy handicap's cost: a generator, a block in the pack's {@link #TAG}, gives no FE while the team whose island
 * it stands on plays the energy challenge (MMR26.1.2 ADR 0005). This answers for its FE ahead of every other mod, on
 * every block, with a face that gives nothing; anywhere else it answers nothing, and the block's own mod answers as
 * before. The suite's networks draw a generator's FE through that face every tick, so the generator fills its own
 * buffer and idles until the challenge ends. The pack fills the tag, so this names no suite mod.
 */
public final class Generators {

    /** The blocks the energy handicap stops: empty here, and filled by the pack. */
    public static final TagKey<Block> TAG = TagKey.create(Registries.BLOCK,
            Identifier.fromNamespaceAndPath(MechanicalMasteryReborn.MOD_ID, "generators"));

    private Generators() {
    }

    public static void register(IEventBus modBus) {
        // Ahead of the suite's FE faces, which register at the normal priority: NeoForge asks providers in turn.
        modBus.addListener(EventPriority.HIGH, RegisterCapabilitiesEvent.class, Generators::registerCapabilities);
    }

    private static void registerCapabilities(RegisterCapabilitiesEvent event) {
        // The tag isn't loaded yet, so the face is on every block, and asks the tag first on each lookup.
        event.registerBlock(Capabilities.Energy.BLOCK,
                (level, pos, state, entity, side) -> isStopped(level, pos, state) ? EmptyEnergyHandler.INSTANCE : null,
                BuiltInRegistries.BLOCK.stream().toArray(Block[]::new));
    }

    /** Whether the block at {@code pos} is a generator its team's energy challenge stops. Server-side only. */
    public static boolean isStopped(Level level, BlockPos pos, BlockState state) {
        if (!(level instanceof ServerLevel server) || !state.is(TAG)) {
            return false;
        }
        TeamState teams = TeamState.get(server.getServer());
        return teams.anyPlays(Handicap.ENERGY) && teams.plays(Teams.at(server, pos), Handicap.ENERGY);
    }
}
