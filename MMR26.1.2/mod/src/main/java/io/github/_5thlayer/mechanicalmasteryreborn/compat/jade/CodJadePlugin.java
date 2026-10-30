// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.compat.jade;

import io.github._5thlayer.libworks.machine.jade.MachineReport;
import io.github._5thlayer.mechanicalmasteryreborn.cod.CodBlock;
import io.github._5thlayer.mechanicalmasteryreborn.cod.CodBlockEntity;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

/**
 * Shows, through libworks-runtime's machine report, how far a C.O.D. is toward its next essence, or that its slot
 * is full. Jade finds this by its annotation and loads it only when Jade is installed.
 */
@WailaPlugin
public final class CodJadePlugin implements IWailaPlugin {

    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(CodReport.REPORT, CodBlockEntity.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(new MachineReport.Client<>(CodReport.REPORT, CodReport.Lines::add), CodBlock.class);
    }
}
