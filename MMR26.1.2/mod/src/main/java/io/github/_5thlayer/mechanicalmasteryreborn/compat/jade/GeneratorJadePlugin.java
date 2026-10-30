// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.compat.jade;

import io.github._5thlayer.libworks.machine.jade.MachineReport;
import io.github._5thlayer.mechanicalmasteryreborn.MechanicalMasteryReborn;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.Generators;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

/**
 * Says on a generator that the energy handicap stops it, while its team plays the energy challenge (#202). Only the
 * server knows which challenge a team plays, so it sends the line's one fact, and nothing for any other block. Jade
 * finds this by its annotation and loads it only when Jade is installed.
 */
@WailaPlugin
public final class GeneratorJadePlugin implements IWailaPlugin {

    private static final Identifier UID = Identifier.fromNamespaceAndPath(MechanicalMasteryReborn.MOD_ID, "generator");

    private static final MachineReport<BlockEntity, Boolean> REPORT = new MachineReport<>(UID, BlockEntity.class,
            GeneratorJadePlugin::read, ByteBufCodecs.BOOL.cast());

    /** Sent only while stopped, so a tooltip of any other block carries nothing of this. */
    private static Boolean read(BlockEntity entity) {
        return entity.getLevel() != null && Generators.isStopped(entity.getLevel(), entity.getBlockPos(), entity.getBlockState())
                ? Boolean.TRUE : null;
    }

    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(REPORT, BlockEntity.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(new MachineReport.Client<>(REPORT, (stopped, tooltip) ->
                tooltip.add(Component.translatable("mechanicalmasteryreborn.jade.generator.stopped"))), Block.class);
    }
}
