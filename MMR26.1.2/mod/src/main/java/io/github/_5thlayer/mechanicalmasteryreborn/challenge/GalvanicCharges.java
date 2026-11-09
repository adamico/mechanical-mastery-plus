// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.challenge;

import java.util.List;

import io.github._5thlayer.mechanicalmasteryreborn.MechanicalMasteryReborn;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * The energy chain's Galvanic Charges (the line's glossary, ADR 0018, #330): mercury, salt and sulfur, each packed in
 * an Assembler by the pack's recipes, salt and sulfur only once they have climbed their reformation stages. The
 * recipes are the energy chain's, locked until the team starts it.
 */
public final class GalvanicCharges {

    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MechanicalMasteryReborn.MOD_ID);

    public static final DeferredItem<Item> MERCURY = ITEMS.registerSimpleItem("mercury_charge");
    public static final DeferredItem<Item> SALT = ITEMS.registerSimpleItem("salt_charge");
    public static final DeferredItem<Item> SULFUR = ITEMS.registerSimpleItem("sulfur_charge");
    /** The three charges, in the order the Works tab lists them. */
    public static final List<DeferredItem<Item>> ALL = List.of(MERCURY, SALT, SULFUR);
    /** The Galvanic Core the Incubator makes from one charge of each branch, several of which make a Galvanic Essence (#331). */
    public static final DeferredItem<Item> CORE = ITEMS.registerSimpleItem("galvanic_core");

    private GalvanicCharges() {
    }

    public static void register(IEventBus modBus) {
        ITEMS.register(modBus);
    }
}
