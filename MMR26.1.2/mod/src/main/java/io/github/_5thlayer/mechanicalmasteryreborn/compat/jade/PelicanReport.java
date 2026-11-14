// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.compat.jade;

import java.util.Locale;

import io.github._5thlayer.libworks.machine.jade.MachineReport;
import io.github._5thlayer.mechanicalmasteryreborn.MechanicalMasteryReborn;
import io.github._5thlayer.mechanicalmasteryreborn.life.Batch;
import io.github._5thlayer.mechanicalmasteryreborn.life.Pelican;
import io.github._5thlayer.mechanicalmasteryreborn.life.PelicanBlockEntity;
import net.minecraft.ChatFormatting;
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
 * What the server tells Jade about a Pelican (#371): its batch's saturation, kinds of meal and average quality, the
 * thresholds each must reach, which are the server's config, and the ticks of digesting left out of the whole.
 * {@link Lines} words it on the client, apart so that a dedicated server never loads Jade's drawing.
 */
final class PelicanReport {

    private static final Identifier UID = Identifier.fromNamespaceAndPath(MechanicalMasteryReborn.MOD_ID, "pelican");

    private static final StreamCodec<RegistryFriendlyByteBuf, Batch.Thresholds> THRESHOLDS_CODEC = StreamCodec.composite(
            ByteBufCodecs.DOUBLE, Batch.Thresholds::saturation,
            ByteBufCodecs.VAR_INT, Batch.Thresholds::kinds,
            ByteBufCodecs.DOUBLE, Batch.Thresholds::quality,
            Batch.Thresholds::new);

    private static final StreamCodec<RegistryFriendlyByteBuf, Data> CODEC = StreamCodec.composite(
            ByteBufCodecs.DOUBLE, Data::saturation,
            ByteBufCodecs.VAR_INT, Data::kinds,
            ByteBufCodecs.DOUBLE, Data::quality,
            THRESHOLDS_CODEC, Data::needs,
            ByteBufCodecs.BOOL, Data::ready,
            ByteBufCodecs.VAR_INT, Data::left,
            ByteBufCodecs.VAR_INT, Data::total,
            Data::new);

    /**
     * Its batch's three measures, what they need, whether the batch is ready, and the ticks of digesting left of the
     * {@code total} a batch takes: none left on a ready batch means it is digested and waits for room in its output.
     */
    record Data(double saturation, int kinds, double quality, Batch.Thresholds needs, boolean ready, int left, int total) {
    }

    static final MachineReport<PelicanBlockEntity, Data> REPORT = new MachineReport<>(UID, PelicanBlockEntity.class, PelicanReport::read, CODEC);

    private static Data read(PelicanBlockEntity pelican) {
        Batch batch = pelican.batch();
        return new Data(batch.saturation(), batch.kinds().size(), batch.quality(), Pelican.thresholds(), pelican.digesting(),
                pelican.digestingLeft(), Pelican.digestingTicks());
    }

    private PelicanReport() {
    }

    /**
     * Words the report: each of the three measures against what it needs, green once reached; then, while it digests,
     * a bar of its digesting, or once digested, that it waits for its output to be emptied.
     */
    static final class Lines {

        private Lines() {
        }

        static void add(Data report, ITooltip tooltip) {
            Batch.Thresholds needs = report.needs();
            measure(tooltip, "saturation", decimal(report.saturation()), decimal(needs.saturation()), report.saturation() >= needs.saturation());
            measure(tooltip, "kinds", report.kinds(), needs.kinds(), report.kinds() >= needs.kinds());
            measure(tooltip, "quality", decimal(report.quality()), decimal(needs.quality()), report.quality() >= needs.quality());
            if (report.ready() && report.left() > 0) {
                Component digesting = Component.translatable("mechanicalmasteryreborn.jade.pelican.digesting");
                float part = report.total() == 0 ? 1 : Math.clamp(1 - (float) report.left() / report.total(), 0, 1);
                tooltip.add(JadeUI.progress(new ProgressView(ProgressView.Part.of(part), digesting, JadeUI.progressStyle(), BoxStyle.nestedBox())));
            } else if (report.ready()) {
                tooltip.add(Component.translatable("mechanicalmasteryreborn.jade.pelican.output_full").withStyle(ChatFormatting.RED));
            }
        }

        private static void measure(ITooltip tooltip, String name, Object has, Object needs, boolean reached) {
            tooltip.add(Component.translatable("mechanicalmasteryreborn.jade.pelican." + name, has, needs)
                    .withStyle(reached ? ChatFormatting.GREEN : ChatFormatting.GRAY));
        }

        private static String decimal(double value) {
            return String.format(Locale.ROOT, "%.1f", value);
        }
    }
}
