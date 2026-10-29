// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.compat.jade;

import java.util.Locale;

import io.github._5thlayer.libworks.machine.jade.MachineReport;
import io.github._5thlayer.mechanicalmasteryreborn.MechanicalMasteryReborn;
import io.github._5thlayer.mechanicalmasteryreborn.recycler.RecyclerCoreBlockEntity;
import io.github._5thlayer.mechanicalmasteryreborn.recycler.RecyclerCoreBlockEntity.Status;
import io.netty.buffer.ByteBuf;
import net.minecraft.ChatFormatting;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import snownee.jade.api.ITooltip;
import snownee.jade.api.ui.BoxStyle;
import snownee.jade.api.ui.JadeUI;
import snownee.jade.api.view.ProgressView;

/**
 * What the server tells Jade about a Recycler core: its {@link Status}, the turns done toward the decompose
 * under way, and what it holds. {@link Lines} words it on the client, apart so that a dedicated server never
 * loads Jade's drawing.
 */
final class RecyclerReport {

    private static final Identifier UID = Identifier.fromNamespaceAndPath(MechanicalMasteryReborn.MOD_ID, "recycler");

    private static final StreamCodec<ByteBuf, Status> STATUS_CODEC = ByteBufCodecs.idMapper(ordinal -> Status.values()[ordinal], Status::ordinal);

    private static final StreamCodec<RegistryFriendlyByteBuf, Data> CODEC = StreamCodec.composite(
            STATUS_CODEC, Data::status,
            ByteBufCodecs.VAR_INT, Data::turns,
            ItemStack.OPTIONAL_STREAM_CODEC, Data::held,
            Data::new);

    record Data(Status status, int turns, ItemStack held) {
    }

    static final MachineReport<RecyclerCoreBlockEntity, Data> REPORT = new MachineReport<>(UID, RecyclerCoreBlockEntity.class,
            core -> new Data(core.status(), core.turns(), core.held()), CODEC);

    private RecyclerReport() {
    }

    /** Words the report: what it holds, its turns as a bar while it can decompose, and why it can't otherwise. */
    static final class Lines {

        private Lines() {
        }

        static void add(Data report, ITooltip tooltip) {
            if (!report.held().isEmpty()) {
                tooltip.add(Component.translatable("mechanicalmasteryreborn.jade.recycler.held", report.held().getCount(),
                        report.held().getHoverName()));
            }
            Component status = Component.translatable("mechanicalmasteryreborn.jade.recycler." + report.status().name().toLowerCase(Locale.ROOT));
            if (report.status() == Status.READY) {
                Component turns = Component.translatable("mechanicalmasteryreborn.jade.recycler.turns", report.turns(), RecyclerCoreBlockEntity.TURNS);
                tooltip.add(JadeUI.progress(new ProgressView(ProgressView.Part.of((float) report.turns() / RecyclerCoreBlockEntity.TURNS), turns,
                        JadeUI.progressStyle(), BoxStyle.nestedBox())));
            } else {
                tooltip.add(status.copy().withStyle(ChatFormatting.RED));
            }
        }
    }
}
