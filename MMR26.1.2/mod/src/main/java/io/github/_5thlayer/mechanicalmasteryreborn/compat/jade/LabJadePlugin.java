// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.compat.jade;

import io.github._5thlayer.mechanicalmasteryreborn.MechanicalMasteryReborn;
import io.github._5thlayer.mechanicalmasteryreborn.lab.LabBlock;
import net.minecraft.resources.Identifier;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.JadeIds;
import snownee.jade.api.WailaPlugin;
import snownee.jade.api.config.IPluginConfig;

/**
 * Names an awakened lab the Hermetic Lab in Jade (#303). Jade names a block by the block, not by its pick, so the pick's
 * name alone left an awakened lab named Lab; this replaces the name Jade's own provider put, which runs first, by the
 * pick's, the Hermetic Lab's while the block state says so. The icon needs nothing here: Jade draws it from the pick,
 * which wears the Hermetic Lab's look ({@link LabBlock#HERMETIC_LOOK}). Client-only, from the block state, so no server
 * data. Jade finds this by its annotation and loads it only when Jade is installed.
 */
@WailaPlugin
public final class LabJadePlugin implements IWailaPlugin {

    private static final Identifier UID = Identifier.fromNamespaceAndPath(MechanicalMasteryReborn.MOD_ID, "lab");

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(new HermeticName(), LabBlock.class);
    }

    /** The awakened lab's name, in place of the block's. */
    static final class HermeticName implements IBlockComponentProvider {

        @Override
        public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
            if (accessor.getBlockState().getValue(LabBlock.HERMETIC)) {
                tooltip.replace(JadeIds.CORE_OBJECT_NAME, accessor.getPickedResult().getHoverName());
            }
        }

        @Override
        public Identifier getUid() {
            return UID;
        }
    }
}
