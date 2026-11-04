// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.compat.jade;

import io.github._5thlayer.libworks.machine.jade.MachineReport;
import io.github._5thlayer.mechanicalmasteryreborn.MechanicalMasteryReborn;
import io.github._5thlayer.mechanicalmasteryreborn.defence.FeDefenceBlock;
import io.github._5thlayer.mechanicalmasteryreborn.defence.FeDefenceBlockEntity;
import io.github._5thlayer.mechanicalmasteryreborn.defence.FeDefenceBlockEntity.Status;
import io.netty.buffer.ByteBuf;
import net.minecraft.ChatFormatting;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

/**
 * Shows, through libworks-runtime's machine report, that an FE defence does nothing for lack of FE while it holds less
 * than a shot's worth (#236). Its store Jade shows on its own, from the block's energy capability. Jade finds this by
 * its annotation and loads it only when Jade is installed.
 */
@WailaPlugin
public final class DefenceJadePlugin implements IWailaPlugin {

    private static final Identifier UID = Identifier.fromNamespaceAndPath(MechanicalMasteryReborn.MOD_ID, "defence");

    private static final StreamCodec<ByteBuf, Status> STATUS_CODEC = ByteBufCodecs.idMapper(ordinal -> Status.values()[ordinal], Status::ordinal);

    private static final StreamCodec<RegistryFriendlyByteBuf, Data> CODEC = StreamCodec.composite(STATUS_CODEC, Data::status, Data::new);

    record Data(Status status) {
    }

    private static final MachineReport<FeDefenceBlockEntity, Data> REPORT = new MachineReport<>(UID, FeDefenceBlockEntity.class,
            defence -> new Data(defence.status()), CODEC);

    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(REPORT, FeDefenceBlockEntity.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(new MachineReport.Client<>(REPORT, (data, tooltip) -> {
            if (data.status() == Status.NO_FE) {
                tooltip.add(Component.translatable("mechanicalmasteryreborn.jade.defence.no_fe").withStyle(ChatFormatting.RED));
            }
        }), FeDefenceBlock.class);
    }
}
