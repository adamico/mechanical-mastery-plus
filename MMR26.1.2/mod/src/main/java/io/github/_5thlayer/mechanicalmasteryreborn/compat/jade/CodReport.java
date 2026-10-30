// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.compat.jade;

import java.util.Optional;

import io.github._5thlayer.libworks.machine.jade.MachineReport;
import io.github._5thlayer.mechanicalmasteryreborn.MechanicalMasteryReborn;
import io.github._5thlayer.mechanicalmasteryreborn.cod.CodBlockEntity;
import io.github._5thlayer.mechanicalmasteryreborn.record.Inflation;
import net.minecraft.ChatFormatting;
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
 * What the server tells Jade about a C.O.D.: whether it is making essence, whether it is blocked, the ticks done
 * toward the next out of its interval, which the server's config sets and which is shorter once it is awake, and its
 * team's Inflation while the team plays a challenge.
 * {@link Lines} words it on the client, apart so that a dedicated server never loads Jade's drawing.
 */
final class CodReport {

    private static final Identifier UID = Identifier.fromNamespaceAndPath(MechanicalMasteryReborn.MOD_ID, "cod");

    private static final StreamCodec<RegistryFriendlyByteBuf, Data> CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, Data::making,
            ByteBufCodecs.BOOL, Data::blocked,
            ByteBufCodecs.VAR_INT, Data::ticks,
            ByteBufCodecs.VAR_INT, Data::interval,
            ByteBufCodecs.optional(StreamCodec.composite(
                    ByteBufCodecs.VAR_LONG, Inflation::began,
                    ByteBufCodecs.VAR_LONG, Inflation::made,
                    ByteBufCodecs.VAR_LONG, Inflation::decomposed,
                    Inflation::new)), Data::inflation,
            Data::new);

    record Data(boolean making, boolean blocked, int ticks, int interval, Optional<Inflation> inflation) {
    }

    static final MachineReport<CodBlockEntity, Data> REPORT = new MachineReport<>(UID, CodBlockEntity.class, CodReport::read, CODEC);

    private static Data read(CodBlockEntity cod) {
        return new Data(cod.isMaking(), cod.isBlocked(), cod.ticksTowardNext(), cod.interval(), cod.inflation());
    }

    private CodReport() {
    }

    /**
     * Words the report: the next essence as a bar with the seconds left while it is making, that it is blocked
     * while what it makes has nowhere to go, or that its slot is full; then, while its team plays a challenge, its
     * Inflation's excess, with what C.O.D. has made and the team has decomposed since the challenge began.
     */
    static final class Lines {

        private Lines() {
        }

        static void add(Data report, ITooltip tooltip) {
            if (!report.making()) {
                tooltip.add(Component.translatable("mechanicalmasteryreborn.jade.cod.full"));
            } else if (report.blocked()) {
                tooltip.add(Component.translatable("mechanicalmasteryreborn.jade.cod.blocked"));
            } else {
                int seconds = Mth.positiveCeilDiv(report.interval() - report.ticks(), 20);
                Component next = Component.translatable("mechanicalmasteryreborn.jade.cod.next", seconds);
                tooltip.add(JadeUI.progress(new ProgressView(ProgressView.Part.of((float) report.ticks() / report.interval()), next,
                        JadeUI.progressStyle(), BoxStyle.nestedBox())));
            }
            report.inflation().ifPresent(inflation -> {
                tooltip.add(Component.translatable("mechanicalmasteryreborn.jade.cod.inflation", inflation.excess()));
                tooltip.add(Component.translatable("mechanicalmasteryreborn.jade.cod.made_decomposed", inflation.made(),
                        inflation.decomposed()).withStyle(ChatFormatting.GRAY));
            });
        }
    }
}
