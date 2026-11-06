// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.compat.jade;

import io.github._5thlayer.libworks.machine.jade.MachineReport;
import io.github._5thlayer.mechanicalmasteryreborn.MechanicalMasteryReborn;
import io.github._5thlayer.mechanicalmasteryreborn.compat.theurgy.TheurgyProgress;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.Identifier;
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
 * Shows a Theurgy apparatus's processing as a bar with its percent, which Theurgy's in-world HUD showed before the pack
 * turned it off (#255). The server sends the percent only while the apparatus is processing ({@link TheurgyProgress}).
 * Registers nothing unless Theurgy is installed. Jade finds this by its annotation and loads it only when Jade is
 * installed.
 */
@WailaPlugin
public final class TheurgyJadePlugin implements IWailaPlugin {

    private static final Identifier UID = Identifier.fromNamespaceAndPath(MechanicalMasteryReborn.MOD_ID, "theurgy_progress");

    private static final MachineReport<BlockEntity, Integer> REPORT = new MachineReport<>(UID, BlockEntity.class,
            entity -> TheurgyProgress.percent(entity).orElse(null), ByteBufCodecs.VAR_INT.cast());

    @Override
    public void register(IWailaCommonRegistration registration) {
        if (ModList.get().isLoaded("theurgy")) {
            registration.registerBlockDataProvider(REPORT, BlockEntity.class);
        }
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        if (ModList.get().isLoaded("theurgy")) {
            registration.registerBlockComponent(new MachineReport.Client<>(REPORT, Bar::add), Block.class);
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
    }
}
