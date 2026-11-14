// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.compat.jade;

import io.github._5thlayer.libworks.machine.jade.MachineReport;
import io.github._5thlayer.mechanicalmasteryreborn.life.PelicanBlock;
import io.github._5thlayer.mechanicalmasteryreborn.life.PelicanBlockEntity;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

/**
 * Shows, through libworks-runtime's machine report, a Pelican's batch against the three thresholds it must reach, and
 * its digesting as a bar (#371): it has no screen of its own. Jade finds this by its annotation and loads it only when
 * Jade is installed.
 */
@WailaPlugin
public final class PelicanJadePlugin implements IWailaPlugin {

    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(PelicanReport.REPORT, PelicanBlockEntity.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(new MachineReport.Client<>(PelicanReport.REPORT, PelicanReport.Lines::add), PelicanBlock.class);
    }
}
