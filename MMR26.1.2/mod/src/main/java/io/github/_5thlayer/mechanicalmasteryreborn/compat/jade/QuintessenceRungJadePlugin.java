// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.compat.jade;

import io.github._5thlayer.libworks.machine.jade.MachineReport;
import io.github._5thlayer.mechanicalmasteryreborn.MechanicalMasteryReborn;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.QuintessenceRung;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

/**
 * Says on a Mutation Chamber that holds a Quartessence it won't climb why: its island's team hasn't started its last
 * stand (#262), in the words of EMI's mark. Only the server knows what a team has started, so it sends the line's flag,
 * and nothing for any other block or a Chamber that doesn't wait; a world without Voidworks has no Chamber to ask
 * ({@link QuintessenceRung.Chamber}). Jade finds this by its annotation and loads it only when Jade is installed.
 */
@WailaPlugin
public final class QuintessenceRungJadePlugin implements IWailaPlugin {

    private static final Identifier UID = Identifier.fromNamespaceAndPath(MechanicalMasteryReborn.MOD_ID, "quintessence_rung");

    private static final Component LINE = Component.translatable(MechanicalMasteryReborn.MOD_ID + ".last_stand.unlocked_by");

    private static final MachineReport<BlockEntity, Boolean> REPORT = new MachineReport<>(UID, BlockEntity.class,
            QuintessenceRungJadePlugin::read, ByteBufCodecs.BOOL.cast());

    /** Sent only while the Chamber waits, so a tooltip of any other block carries nothing of this. */
    private static Boolean read(BlockEntity entity) {
        return entity instanceof QuintessenceRung.Chamber chamber && chamber.mechanicalmasteryreborn$waitsForTheLastStand()
                ? Boolean.TRUE : null;
    }

    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(REPORT, BlockEntity.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(new MachineReport.Client<>(REPORT, (waits, tooltip) -> tooltip.add(LINE)), Block.class);
    }
}
