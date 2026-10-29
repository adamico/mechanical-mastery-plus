// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.gametest;

import io.github._5thlayer.mechanicalmasteryreborn.MechanicalMasteryReborn;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddPackFindersEvent;

/**
 * Switches on the Module's dev pack, {@code dev_pack/}, in the {@code client} run and its game-test
 * pack, {@code gametest_pack/}, in {@code gameTestServer}, each when the Module has one: datapacks
 * in {@code src/gametest/resources}, which only the Module's own runs load and the jar leaves out
 * (libworks ADR 0004). The convention plugin names the run in {@code libworks.run}.
 */
@EventBusSubscriber(modid = MechanicalMasteryReborn.MOD_ID)
final class DevPacks {

    private DevPacks() {
    }

    @SubscribeEvent
    static void addPackFinders(AddPackFindersEvent event) {
        if (event.getPackType() != PackType.SERVER_DATA) {
            return;
        }
        String folder = switch (System.getProperty("libworks.run", "")) {
            case "client" -> "dev_pack";
            case "gameTestServer" -> "gametest_pack";
            default -> null;
        };
        if (folder == null || !has(folder)) {
            return;
        }
        event.addPackFinders(Identifier.fromNamespaceAndPath(MechanicalMasteryReborn.MOD_ID, folder), PackType.SERVER_DATA,
                Component.literal(MechanicalMasteryReborn.MOD_ID + " " + folder), PackSource.BUILT_IN, true, Pack.Position.TOP);
    }

    private static boolean has(String folder) {
        return ModList.get().getModFileById(MechanicalMasteryReborn.MOD_ID).getFile().getContents().findFile(folder + "/pack.mcmeta").isPresent();
    }
}
