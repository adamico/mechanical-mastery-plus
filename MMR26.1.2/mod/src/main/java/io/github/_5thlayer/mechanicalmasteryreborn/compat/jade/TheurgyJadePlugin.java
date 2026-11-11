// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.compat.jade;

import io.github._5thlayer.libworks.machine.jade.MachineReport;
import io.github._5thlayer.mechanicalmasteryreborn.MechanicalMasteryReborn;
import io.github._5thlayer.mechanicalmasteryreborn.compat.theurgy.FueledCatalyst;
import io.github._5thlayer.mechanicalmasteryreborn.compat.theurgy.IncubatorVessels;
import io.github._5thlayer.mechanicalmasteryreborn.compat.theurgy.TheurgyProgress;
import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.fml.ModList;
import snownee.jade.api.ITooltip;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;
import snownee.jade.api.ui.BoxStyle;
import snownee.jade.api.ui.JadeUI;
import snownee.jade.api.view.ProgressView;

/**
 * Shows on Theurgy's blocks what Theurgy's in-world HUD showed before the pack turned it off (#255) and Jade doesn't
 * show on its own (#327): an apparatus's processing as a bar with its percent, sent only while it processes
 * ({@link TheurgyProgress}), and on the Incubator its vessels' mercury, salt and sulfur ({@link IncubatorVessels}).
 * And the Mercury Catalyst's FE store (#328), against its capacity: Theurgy's own Jade plugin shows the Catalyst's
 * mercury flux in place of Jade's FE bar. Items and fluids Jade shows from the blocks' capabilities.
 * Registers nothing unless Theurgy is installed. Jade finds this by its annotation and loads it only when Jade is
 * installed.
 */
@WailaPlugin
public final class TheurgyJadePlugin implements IWailaPlugin {

    private static final Identifier UID = Identifier.fromNamespaceAndPath(MechanicalMasteryReborn.MOD_ID, "theurgy_progress");

    private static final MachineReport<BlockEntity, Integer> PROGRESS = new MachineReport<>(UID, BlockEntity.class,
            entity -> TheurgyProgress.percent(entity).orElse(null), ByteBufCodecs.VAR_INT.cast());


    private static final MachineReport<BlockEntity, List<ItemStack>> VESSELS = new MachineReport<>(
            Identifier.fromNamespaceAndPath(MechanicalMasteryReborn.MOD_ID, "theurgy_incubator_vessels"), BlockEntity.class,
            entity -> IncubatorVessels.of(entity).orElse(null),
            ItemStack.OPTIONAL_STREAM_CODEC.apply(ByteBufCodecs.list()));

    /** FE stored and the store's capacity. */
    record Fuel(long stored, long capacity) {

        static final StreamCodec<RegistryFriendlyByteBuf, Fuel> CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_LONG, Fuel::stored, ByteBufCodecs.VAR_LONG, Fuel::capacity, Fuel::new);
    }

    private static final MachineReport<BlockEntity, Fuel> FUEL = new MachineReport<>(
            Identifier.fromNamespaceAndPath(MechanicalMasteryReborn.MOD_ID, "theurgy_catalyst_fuel"), BlockEntity.class,
            entity -> entity instanceof FueledCatalyst catalyst
                    ? new Fuel(catalyst.mechanicalmasteryreborn$fuel().getAmountAsLong(), catalyst.mechanicalmasteryreborn$fuel().getCapacityAsLong())
                    : null,
            Fuel.CODEC);

    @Override
    public void register(IWailaCommonRegistration registration) {
        if (ModList.get().isLoaded("theurgy")) {
            registration.registerBlockDataProvider(PROGRESS, BlockEntity.class);
            registration.registerBlockDataProvider(VESSELS, BlockEntity.class);
            registration.registerBlockDataProvider(FUEL, BlockEntity.class);
        }
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        if (ModList.get().isLoaded("theurgy")) {
            registration.registerBlockComponent(new MachineReport.Client<>(PROGRESS, Bar::add), Block.class);
            registration.registerBlockComponent(new MachineReport.Client<>(VESSELS, Bar::vessels), Block.class);
            registration.registerBlockComponent(new MachineReport.Client<>(FUEL, Bar::fuel), Block.class);
        }
    }

    /**
     * The bar, in a class of its own: a dedicated server can't load Jade's {@code Element}, whose interfaces are the
     * client's, and verifying a method here that hands one to the tooltip would load it and fail the plugin (#308).
     */
    static final class Bar {

        private Bar() {
        }

        static void add(Integer percent, ITooltip tooltip) {
            tooltip.add(JadeUI.progress(new ProgressView(ProgressView.Part.of(percent / 100f),
                    Component.translatable("mechanicalmasteryreborn.jade.theurgy.progress", percent),
                    JadeUI.progressStyle(), BoxStyle.nestedBox())));
        }

        static void fuel(Fuel fuel, ITooltip tooltip) {
            tooltip.add(JadeUI.progress(new ProgressView(ProgressView.Part.of(fuel.capacity() == 0 ? 0 : Math.min(1f, (float) fuel.stored() / fuel.capacity())),
                    Component.translatable("mechanicalmasteryreborn.jade.theurgy.fuel", fuel.stored(), fuel.capacity()),
                    JadeUI.progressStyle(), BoxStyle.nestedBox())));
        }

        /** The three vessels in a row, mercury, salt then sulfur, an empty vessel as an empty slot. */
        static void vessels(List<ItemStack> held, ITooltip tooltip) {
            tooltip.add(Component.translatable("mechanicalmasteryreborn.jade.theurgy.vessels").withStyle(ChatFormatting.GRAY));
            tooltip.add(held.stream().map(stack -> stack.isEmpty() ? JadeUI.spacer(18, 18) : JadeUI.item(stack)).toList());
        }
    }
}
