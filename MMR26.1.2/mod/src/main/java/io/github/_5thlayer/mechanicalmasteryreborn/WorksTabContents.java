// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn;

import io.github._5thlayer.libworks.WorksTab;
import io.github._5thlayer.mechanicalmasteryreborn.cod.Cod;
import io.github._5thlayer.mechanicalmasteryreborn.essence.Essence;
import io.github._5thlayer.mechanicalmasteryreborn.essence.Essences;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

/**
 * Every obtainable item of the mod, in the suite's Works tab and no other (FactoryWorks ADR-0128): C.O.D.,
 * then the Mechanical Essences. One listener, in the order the tab shows them, which
 * {@code CreativeTabTests} lists too. The essences no player can have yet are in no tab.
 */
final class WorksTabContents {

    private WorksTabContents() {
    }

    static void add(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() != WorksTab.KEY) {
            return;
        }
        event.accept(Cod.ITEM.get());
        for (Essence essence : Essence.values()) {
            if (essence.obtainable()) {
                event.accept(Essences.item(essence));
            }
        }
    }
}
