// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.compat.jade;

import io.github._5thlayer.libworks.machine.jade.MachineReport;
import io.github._5thlayer.mechanicalmasteryreborn.MechanicalMasteryReborn;
import io.github._5thlayer.mechanicalmasteryreborn.cod.CodBlockEntity;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import snownee.jade.api.ITooltip;
import snownee.jade.api.ui.BoxStyle;
import snownee.jade.api.ui.JadeUI;
import snownee.jade.api.view.ProgressView;

/**
 * What the server tells Jade about a C.O.D.: whether it is making essence, and the ticks done toward the next out
 * of its interval, which the server's config sets and which is shorter once it is awake. {@link Lines} words it on
 * the client, apart so that a dedicated server never loads Jade's drawing.
 */
final class CodReport {

    private static final Identifier UID = Identifier.fromNamespaceAndPath(MechanicalMasteryReborn.MOD_ID, "cod");

    private static final StreamCodec<RegistryFriendlyByteBuf, Data> CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, Data::making,
            ByteBufCodecs.VAR_INT, Data::ticks,
            ByteBufCodecs.VAR_INT, Data::interval,
            Data::new);

    record Data(boolean making, int ticks, int interval) {
    }

    static final MachineReport<CodBlockEntity, Data> REPORT = new MachineReport<>(UID, CodBlockEntity.class, CodReport::read, CODEC);

    private static Data read(CodBlockEntity cod) {
        return new Data(cod.isMaking(), cod.ticksTowardNext(), cod.interval());
    }

    private CodReport() {
    }

    /** Words the report: the next essence as a bar with the seconds left while it is making, or that its slot is full. */
    static final class Lines {

        private Lines() {
        }

        static void add(Data report, ITooltip tooltip) {
            if (!report.making()) {
                tooltip.add(Component.translatable("mechanicalmasteryreborn.jade.cod.full"));
                return;
            }
            int seconds = Mth.positiveCeilDiv(report.interval() - report.ticks(), 20);
            Component next = Component.translatable("mechanicalmasteryreborn.jade.cod.next", seconds);
            tooltip.add(JadeUI.progress(new ProgressView(ProgressView.Part.of((float) report.ticks() / report.interval()), next,
                    JadeUI.progressStyle(), BoxStyle.nestedBox())));
        }
    }
}
