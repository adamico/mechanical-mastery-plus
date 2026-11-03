// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.compat.jade;

import io.github._5thlayer.libworks.machine.jade.MachineReport;
import io.github._5thlayer.mechanicalmasteryreborn.MechanicalMasteryReborn;
import io.github._5thlayer.mechanicalmasteryreborn.port.PortBlockEntity;
import io.github._5thlayer.mechanicalmasteryreborn.port.PortPhase;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import snownee.jade.api.ITooltip;
import snownee.jade.api.ui.BoxStyle;
import snownee.jade.api.ui.JadeUI;
import snownee.jade.api.view.ProgressView;

/**
 * What the server tells Jade about a Port: whether it is closed, open or won, and while open the essence its team has
 * delivered out of the quota. {@link Lines} words it on the client, apart so that a dedicated server never loads Jade's drawing.
 */
final class PortReport {

    private static final Identifier UID = Identifier.fromNamespaceAndPath(MechanicalMasteryReborn.MOD_ID, "port");

    private static final StreamCodec<RegistryFriendlyByteBuf, Data> CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, Data::phase,
            ByteBufCodecs.VAR_LONG, Data::delivered,
            ByteBufCodecs.VAR_INT, Data::quota,
            Data::new);

    /** The phase by its ordinal. */
    record Data(int phase, long delivered, int quota) {
    }

    static final MachineReport<PortBlockEntity, Data> REPORT = new MachineReport<>(UID, PortBlockEntity.class, PortReport::read, CODEC);

    private static Data read(PortBlockEntity port) {
        return new Data(port.phase().ordinal(), port.delivered(), port.quota());
    }

    /** Words the report: "Closed" before the challenge, "Won" after, and in between the count out of the quota as a bar. */
    static final class Lines {

        private Lines() {
        }

        static void add(Data report, ITooltip tooltip) {
            PortPhase phase = PortPhase.values()[Math.floorMod(report.phase(), PortPhase.values().length)];
            switch (phase) {
                case CLOSED -> tooltip.add(Component.translatable("mechanicalmasteryreborn.jade.port.closed"));
                case WON -> tooltip.add(Component.translatable("mechanicalmasteryreborn.jade.port.won"));
                case OPEN -> {
                    Component count = Component.translatable("mechanicalmasteryreborn.jade.port.count", report.delivered(), report.quota());
                    float part = report.quota() == 0 ? 1 : Math.min(1, (float) report.delivered() / report.quota());
                    tooltip.add(JadeUI.progress(new ProgressView(ProgressView.Part.of(part), count, JadeUI.progressStyle(), BoxStyle.nestedBox())));
                }
            }
        }
    }
}
