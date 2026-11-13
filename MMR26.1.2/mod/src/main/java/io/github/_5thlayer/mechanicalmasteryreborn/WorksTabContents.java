// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn;

import io.github._5thlayer.libworks.WorksTab;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.Assault;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.GalvanicParts;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.Handicap;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.Mint;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.NullShards;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.ObsidianFlakes;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.RiftShards;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.WardTallow;
import io.github._5thlayer.mechanicalmasteryreborn.cod.Cod;
import io.github._5thlayer.mechanicalmasteryreborn.defence.Defences;
import io.github._5thlayer.mechanicalmasteryreborn.essence.Essence;
import io.github._5thlayer.mechanicalmasteryreborn.essence.Essences;
import io.github._5thlayer.mechanicalmasteryreborn.lab.Lab;
import io.github._5thlayer.mechanicalmasteryreborn.life.Pelican;
import io.github._5thlayer.mechanicalmasteryreborn.port.Port;
import io.github._5thlayer.mechanicalmasteryreborn.recycler.Recycler;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

/**
 * Every obtainable item of the mod, in the suite's Works tab and no other (FactoryWorks ADR-0128): C.O.D.,
 * the lab and the four Ports, then every essence, then the protection Chain's Rift Shard, Ward Tallow bucket,
 * Obsidian Flakes and Charged Rift Shard, the money Chain's mint, from the molten metals' buckets to the Coins,
 * the energy chain's reformed salt and sulfur, Galvanic Charges and Core, and the Hermetic Lab's Null Shard, then the Recycler's parts, then the
 * Defences, the Wave Caller and the Pelican. One listener, in the order the tab shows them, which
 * {@code CreativeTabTests} lists too.
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
            event.accept(Essences.item(essence));
        }
        event.accept(RiftShards.ITEM.get());
        event.accept(WardTallow.BUCKET.get());
        event.accept(ObsidianFlakes.ITEM.get());
        event.accept(RiftShards.CHARGED_RIFT.get());
        for (Mint.Metal metal : Mint.METALS) {
            event.accept(metal.bucket.get());
        }
        event.accept(Mint.MOLD.get());
        for (Mint.Metal metal : Mint.METALS) {
            event.accept(metal.cast.get());
        }
        event.accept(Mint.BURNT_SAND.get());
        for (Mint.Metal metal : Mint.METALS) {
            event.accept(metal.coin.get());
        }
        event.accept(GalvanicParts.REFORMED_SALT.get());
        event.accept(GalvanicParts.REFORMED_SULFUR.get());
        for (var charge : GalvanicParts.CHARGES) {
            event.accept(charge.get());
        }
        event.accept(GalvanicParts.CORE.get());
        event.accept(NullShards.ITEM.get());
        for (int tier = 1; tier <= Recycler.TIERS; tier++) {
            event.accept(Recycler.coreItem(tier));
        }
        event.accept(Recycler.HAND_DRIVE_ITEM.get());
        event.accept(Recycler.FE_DRIVE_ITEM.get());
        event.accept(Recycler.INPUT_ITEM.get());
        event.accept(Recycler.OUTPUT_ITEM.get());
        event.accept(Recycler.CASING_ITEM.get());
        event.accept(Defences.FE_DEFENCE_ITEM.get());
        event.accept(Assault.WAVE_CALLER_ITEM.get());
        event.accept(Pelican.ITEM.get());
    }
}
