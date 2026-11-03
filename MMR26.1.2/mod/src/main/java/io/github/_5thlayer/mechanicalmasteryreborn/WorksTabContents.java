// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn;

import io.github._5thlayer.libworks.WorksTab;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.Handicap;
import io.github._5thlayer.mechanicalmasteryreborn.cod.Cod;
import io.github._5thlayer.mechanicalmasteryreborn.essence.Essence;
import io.github._5thlayer.mechanicalmasteryreborn.essence.Essences;
import io.github._5thlayer.mechanicalmasteryreborn.lab.Lab;
import io.github._5thlayer.mechanicalmasteryreborn.port.Port;
import io.github._5thlayer.mechanicalmasteryreborn.recycler.Recycler;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

/**
 * Every obtainable item of the mod, in the suite's Works tab and no other (FactoryWorks ADR-0128): C.O.D.,
 * the lab and the four Ports, then the Mechanical and Transmuted Essences, then the Recycler's parts. One listener, in the order the tab shows them, which
 * {@code CreativeTabTests} lists too. Quintessence, which no player can have yet, is in no tab.
 */
final class WorksTabContents {

    private WorksTabContents() {
    }

    static void add(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() != WorksTab.KEY) {
            return;
        }
        event.accept(Cod.ITEM.get());
        event.accept(Lab.ITEM.get());
        for (Handicap handicap : Handicap.values()) {
            event.accept(Port.ITEMS_BY_HANDICAP.get(handicap).get());
        }
        for (Essence essence : Essence.values()) {
            if (essence.obtainable()) {
                event.accept(Essences.item(essence));
            }
        }
        for (int tier = 1; tier <= Recycler.TIERS; tier++) {
            event.accept(Recycler.coreItem(tier));
        }
        event.accept(Recycler.HAND_DRIVE_ITEM.get());
        event.accept(Recycler.FE_DRIVE_ITEM.get());
        event.accept(Recycler.INPUT_ITEM.get());
        event.accept(Recycler.OUTPUT_ITEM.get());
        event.accept(Recycler.CASING_ITEM.get());
    }
}
