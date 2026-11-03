// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.compat.jade;

import io.github._5thlayer.libworks.machine.jade.MachineReport;
import io.github._5thlayer.mechanicalmasteryreborn.MechanicalMasteryReborn;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.ChainMachines;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.Handicap;
import io.github._5thlayer.mechanicalmasteryreborn.compat.theurgy.TheurgyHud;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.HitResult;
import net.neoforged.fml.ModList;
import snownee.jade.api.Accessor;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

/**
 * Says on a chain machine that does no work why: its island's team hasn't started the challenge that unlocks it
 * (#214), in the words of its item's tooltip ({@link ChainMachines#line}). Only the server knows what a team has
 * unlocked, so it sends the handicap, and nothing for any other block. With Theurgy installed Jade stays hidden on its
 * machines, whose own HUD tells more, and says the same ({@link TheurgyHud}). Jade finds this by its annotation and
 * loads it only when Jade is installed.
 */
@WailaPlugin
public final class ChainJadePlugin implements IWailaPlugin {

    private static final Identifier UID = Identifier.fromNamespaceAndPath(MechanicalMasteryReborn.MOD_ID, "chain");

    private static final MachineReport<BlockEntity, Integer> REPORT = new MachineReport<>(UID, BlockEntity.class,
            ChainJadePlugin::read, ByteBufCodecs.VAR_INT.cast());

    /** Sent only while locked, so a tooltip of any other block carries nothing of this. */
    private static Integer read(BlockEntity entity) {
        return entity.getLevel() == null ? null : ChainMachines.lockedAt(entity.getLevel(), entity.getBlockPos(), entity.getBlockState())
                .map(Handicap::ordinal).orElse(null);
    }

    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(REPORT, BlockEntity.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(new MachineReport.Client<>(REPORT, (ordinal, tooltip) ->
                Handicap.byOrdinal(ordinal).ifPresent(handicap -> tooltip.add(ChainMachines.line(handicap)))), Block.class);
        if (ModList.get().isLoaded("theurgy")) {
            registration.addRayTraceCallback(ChainJadePlugin::hideOnTheurgy);
        }
    }

    /** No target where Theurgy's HUD shows instead, so the two never overlap. */
    private static Accessor<?> hideOnTheurgy(HitResult hit, Accessor<?> accessor, Accessor<?> original) {
        return accessor instanceof BlockAccessor block && TheurgyHud.shows(block.getLevel(), block.getPosition()) ? null : accessor;
    }
}
