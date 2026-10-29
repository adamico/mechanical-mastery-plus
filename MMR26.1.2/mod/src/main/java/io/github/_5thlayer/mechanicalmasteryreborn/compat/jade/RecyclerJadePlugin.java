// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.compat.jade;

import io.github._5thlayer.libworks.machine.jade.MachineReport;
import io.github._5thlayer.mechanicalmasteryreborn.recycler.RecyclerCoreBlock;
import io.github._5thlayer.mechanicalmasteryreborn.recycler.RecyclerCoreBlockEntity;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

/**
 * Shows, through libworks-runtime's machine report, a Recycler core's turns, what it holds and why it is
 * stopped. Jade finds this by its annotation and loads it only when Jade is installed; nothing else in the mod
 * names a Jade type, so the mod loads without it.
 */
@WailaPlugin
public final class RecyclerJadePlugin implements IWailaPlugin {

    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(RecyclerReport.REPORT, RecyclerCoreBlockEntity.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(new MachineReport.Client<>(RecyclerReport.REPORT, RecyclerReport.Lines::add), RecyclerCoreBlock.class);
    }
}
