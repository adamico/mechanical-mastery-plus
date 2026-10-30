// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.compat.jade;

import java.util.Locale;

import io.github._5thlayer.libworks.machine.jade.MachineReport;
import io.github._5thlayer.mechanicalmasteryreborn.MechanicalMasteryReborn;
import io.github._5thlayer.mechanicalmasteryreborn.recycler.RecyclerCoreBlockEntity;
import io.github._5thlayer.mechanicalmasteryreborn.recycler.RecyclerCoreBlockEntity.Status;
import io.github._5thlayer.mechanicalmasteryreborn.recycler.RecyclerCoreBlockEntity.Work;
import io.github._5thlayer.mechanicalmasteryreborn.recycler.RecyclerPart;
import io.netty.buffer.ByteBuf;
import net.minecraft.ChatFormatting;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.StreamServerDataProvider;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.BoxStyle;
import snownee.jade.api.ui.JadeUI;
import snownee.jade.api.view.ProgressView;

/**
 * What the server tells Jade about a Recycler core: its {@link Status}, its {@link Work}, the turns done toward
 * the decompose or recycle under way, and what it holds. {@link Lines} words it on the client, apart so that a dedicated server never
 * loads Jade's drawing. A drive shows its core's report too ({@link #DRIVE}): it has no block entity, so the
 * server finds its core.
 */
final class RecyclerReport {

    private static final Identifier UID = Identifier.fromNamespaceAndPath(MechanicalMasteryReborn.MOD_ID, "recycler");

    private static final StreamCodec<ByteBuf, Status> STATUS_CODEC = ByteBufCodecs.idMapper(ordinal -> Status.values()[ordinal], Status::ordinal);
    private static final StreamCodec<ByteBuf, Work> WORK_CODEC = ByteBufCodecs.idMapper(ordinal -> Work.values()[ordinal], Work::ordinal);

    private static final StreamCodec<RegistryFriendlyByteBuf, Data> CODEC = StreamCodec.composite(
            STATUS_CODEC, Data::status,
            WORK_CODEC, Data::work,
            ByteBufCodecs.VAR_INT, Data::turns,
            ItemStack.OPTIONAL_STREAM_CODEC, Data::held,
            Data::new);

    record Data(Status status, Work work, int turns, ItemStack held) {
    }

    static final MachineReport<RecyclerCoreBlockEntity, Data> REPORT = new MachineReport<>(UID, RecyclerCoreBlockEntity.class,
            RecyclerReport::read, CODEC);

    /** A drive's report: its core's, or nothing when it is part of no Recycler. */
    static final DriveReport DRIVE = new DriveReport();

    private static Data read(RecyclerCoreBlockEntity core) {
        return new Data(core.status(), core.work(), core.turns(), core.held());
    }

    static final class DriveReport implements StreamServerDataProvider<BlockAccessor, Data> {

        private static final Identifier DRIVE_UID = Identifier.fromNamespaceAndPath(MechanicalMasteryReborn.MOD_ID, "drive");

        private DriveReport() {
        }

        @Override
        public @Nullable Data streamData(BlockAccessor accessor) {
            return RecyclerPart.coreOf(accessor.getLevel(), accessor.getPosition()).map(RecyclerReport::read).orElse(null);
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, Data> streamCodec() {
            return CODEC;
        }

        @Override
        public Identifier getUid() {
            return DRIVE_UID;
        }

        /** Its lines on the client, the core's. */
        static final class Client implements IBlockComponentProvider {

            @Override
            public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
                DRIVE.decodeFromData(accessor).ifPresent(data -> Lines.add(data, tooltip));
            }

            @Override
            public Identifier getUid() {
                return DRIVE_UID;
            }
        }
    }

    private RecyclerReport() {
    }

    /** Words the report: what it holds and what it does with it, its turns as a bar while it can, and why it can't otherwise. */
    static final class Lines {

        private Lines() {
        }

        static void add(Data report, ITooltip tooltip) {
            if (!report.held().isEmpty()) {
                tooltip.add(Component.translatable("mechanicalmasteryreborn.jade.recycler.held", report.held().getCount(),
                        report.held().getHoverName()));
                tooltip.add(Component.translatable("mechanicalmasteryreborn.jade.recycler.work." + report.work().name().toLowerCase(Locale.ROOT)));
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
