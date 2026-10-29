// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.client;

import io.github._5thlayer.mechanicalmasteryreborn.MechanicalMasteryReborn;
import io.github._5thlayer.mechanicalmasteryreborn.cod.Cod;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

/** The client's side: C.O.D.'s screen. */
@Mod(value = MechanicalMasteryReborn.MOD_ID, dist = Dist.CLIENT)
public final class MechanicalMasteryRebornClient {

    public MechanicalMasteryRebornClient(IEventBus modBus) {
        modBus.addListener(MechanicalMasteryRebornClient::registerScreens);
    }

    private static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(Cod.MENU.get(), CodScreen::new);
    }
}
