// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.compat.jade;

import io.github._5thlayer.libworks.machine.jade.MachineReport;
import io.github._5thlayer.mechanicalmasteryreborn.MechanicalMasteryReborn;
import io.github._5thlayer.mechanicalmasteryreborn.life.Homunculus;
import io.github._5thlayer.mechanicalmasteryreborn.life.HomunculusStandBlock;
import io.github._5thlayer.mechanicalmasteryreborn.life.HomunculusStandBlockEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.BlockHitResult;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

/**
 * The food a Homunculus Stand holds (#367), on either half: a look at the rod is a look at the base, whose block entity
 * holds the food and the FE, so Jade's FE bar shows there too while the stand's Homunculus sleeps. Jade finds this by its
 * annotation and loads it only when Jade is installed.
 */
@WailaPlugin
public final class HomunculusStandJadePlugin implements IWailaPlugin {

    private static final Identifier UID = Identifier.fromNamespaceAndPath(MechanicalMasteryReborn.MOD_ID, "homunculus_stand");

    private static final MachineReport<HomunculusStandBlockEntity, Integer> REPORT =
            new MachineReport<>(UID, HomunculusStandBlockEntity.class, HomunculusStandBlockEntity::foods, ByteBufCodecs.VAR_INT.cast());

    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(REPORT, HomunculusStandBlockEntity.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(new MachineReport.Client<>(REPORT, (foods, tooltip) ->
                tooltip.add(Component.translatable(MechanicalMasteryReborn.MOD_ID + ".jade.homunculus.food", foods))), HomunculusStandBlock.class);
        registration.addRayTraceCallback((hit, accessor, original) -> {
            if (!(accessor instanceof BlockAccessor block) || !block.getBlockState().is(Homunculus.STAND.get())
                    || block.getBlockState().getValue(HomunculusStandBlock.HALF) != DoubleBlockHalf.UPPER) {
                return accessor;
            }
            var base = block.getPosition().below();
            var baseState = block.getLevel().getBlockState(base);
            if (!baseState.is(Homunculus.STAND.get())) {
                return accessor;
            }
            var baseHit = new BlockHitResult(block.getHitResult().getLocation(), block.getSide(), base, false);
            return registration.blockAccessor().from(block).blockState(baseState).blockEntity(block.getLevel().getBlockEntity(base))
                    .hit(baseHit).build();
        });
    }
}
