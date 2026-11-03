// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.compat.jade;

import io.github._5thlayer.libworks.machine.jade.MachineReport;
import io.github._5thlayer.mechanicalmasteryreborn.port.PortBlock;
import io.github._5thlayer.mechanicalmasteryreborn.port.PortBlockEntity;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

/**
 * Shows, through libworks-runtime's machine report, how far a Port's team has got toward its quota, or that it is
 * closed or won. Jade finds this by its annotation and loads it only when Jade is installed.
 */
@WailaPlugin
public final class PortJadePlugin implements IWailaPlugin {

    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(PortReport.REPORT, PortBlockEntity.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(new MachineReport.Client<>(PortReport.REPORT, PortReport.Lines::add), PortBlock.class);
    }
}
