// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.compat.jade;

import java.util.Optional;

import io.github._5thlayer.libworks.machine.jade.MachineReport;
import io.github._5thlayer.mechanicalmasteryreborn.MechanicalMasteryReborn;
import io.github._5thlayer.mechanicalmasteryreborn.ServerConfig;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.LastStands;
import io.github._5thlayer.mechanicalmasteryreborn.cod.CodBlockEntity;
import io.github._5thlayer.mechanicalmasteryreborn.record.Overproduction;
import io.github._5thlayer.mechanicalmasteryreborn.record.LastStand;
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
 * What the server tells Jade about a C.O.D.: whether its team has blocked all four tiers,
 * whether it is making essence, whether it is blocked, the ticks done
 * toward the next out of its interval, which the server's config sets and which is shorter once it is awake, and its
 * team's Overproduction and its cap while the team plays a challenge, and its last stand and the margin once started.
 * {@link Lines} words it on the client, apart so that a dedicated server never loads Jade's drawing.
 */
final class CodReport {

    private static final Identifier UID = Identifier.fromNamespaceAndPath(MechanicalMasteryReborn.MOD_ID, "cod");

    private static final StreamCodec<RegistryFriendlyByteBuf, Data> CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, Data::allBlocked,
            ByteBufCodecs.BOOL, Data::making,
            ByteBufCodecs.BOOL, Data::blocked,
            ByteBufCodecs.VAR_INT, Data::ticks,
            ByteBufCodecs.VAR_INT, Data::interval,
            ByteBufCodecs.optional(Overproduction.STREAM_CODEC), Data::overproduction,
            ByteBufCodecs.VAR_INT, Data::cap,
            ByteBufCodecs.optional(StreamCodec.composite(
                    ByteBufCodecs.VAR_LONG, LastStand::lead,
                    ByteBufCodecs.DOUBLE, LastStand::pace,
                    ByteBufCodecs.DOUBLE, LastStand::rate,
                    ByteBufCodecs.BOOL, LastStand::won,
                    LastStand::new)), Data::lastStand,
            ByteBufCodecs.VAR_INT, Data::margin,
            Data::new);

    record Data(boolean allBlocked, boolean making, boolean blocked, int ticks, int interval, Optional<Overproduction> overproduction, int cap,
            Optional<LastStand> lastStand, int margin) {
    }

    static final MachineReport<CodBlockEntity, Data> REPORT = new MachineReport<>(UID, CodBlockEntity.class, CodReport::read, CODEC);

    /** The ticks done are held to the interval, which may have shrunk by a tick since C.O.D. last counted one. */
    private static Data read(CodBlockEntity cod) {
        int interval = cod.interval();
        return new Data(cod.making().isEmpty(), cod.isMaking(), cod.isBlocked(), Math.min(cod.ticksTowardNext(), interval), interval, cod.overproduction(),
                ServerConfig.overproductionCap(), cod.lastStand(), ServerConfig.lastStandMargin());
    }

    private CodReport() {
    }

    /**
     * Words the report: that all four tiers are blocked, if they are; otherwise the next essence as a bar with the seconds left while it is making, that it is blocked
     * while what it makes has nowhere to go, or that its slot is full; then, while its team plays a challenge, its
     * Overproduction's excess against its cap, as its bar words it, with what C.O.D. has made and the team has decomposed since the challenge began.
     * Once its last stand is won, only that it has shut down for good; while it runs, its Lead and C.O.D.'s rate, as its bar
     * words them.
     */
    static final class Lines {

        private Lines() {
        }

        static void add(Data report, ITooltip tooltip) {
            if (report.lastStand().filter(LastStand::won).isPresent()) {
                tooltip.add(Component.translatable("mechanicalmasteryreborn.jade.cod.shut_down"));
                return;
            }
            if (report.allBlocked()) {
                tooltip.add(Component.translatable("mechanicalmasteryreborn.jade.cod.all_blocked"));
            } else if (!report.making()) {
                tooltip.add(Component.translatable("mechanicalmasteryreborn.jade.cod.full"));
            } else if (report.blocked()) {
                tooltip.add(Component.translatable("mechanicalmasteryreborn.jade.cod.blocked"));
            } else {
                int seconds = Mth.positiveCeilDiv(report.interval() - report.ticks(), 20);
                Component next = Component.translatable("mechanicalmasteryreborn.jade.cod.next", seconds);
                tooltip.add(JadeUI.progress(new ProgressView(ProgressView.Part.of((float) report.ticks() / report.interval()), next,
                        JadeUI.progressStyle(), BoxStyle.nestedBox())));
            }
            report.overproduction().ifPresent(overproduction -> {
                tooltip.add(Component.translatable("mechanicalmasteryreborn.bar.overproduction", overproduction.excess(), report.cap()));
                tooltip.add(Component.translatable("mechanicalmasteryreborn.jade.cod.made_decomposed", overproduction.made(),
                        overproduction.decomposed()).withStyle(ChatFormatting.GRAY));
            });
            report.lastStand().ifPresent(stand -> tooltip.add(LastStands.lead(stand, report.margin())));
        }
    }
}
