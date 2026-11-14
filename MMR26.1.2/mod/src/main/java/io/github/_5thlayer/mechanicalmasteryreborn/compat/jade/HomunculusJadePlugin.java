// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.compat.jade;

import io.github._5thlayer.mechanicalmasteryreborn.MechanicalMasteryReborn;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.ChainMachines;
import io.github._5thlayer.mechanicalmasteryreborn.challenge.Handicap;
import io.github._5thlayer.mechanicalmasteryreborn.life.HomunculusEntity;
import io.github._5thlayer.mechanicalmasteryreborn.life.HomunculusStandBlockEntity.Doing;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;
import snownee.jade.api.config.IPluginConfig;

/**
 * Says what a Homunculus is doing (#367): asleep until a jumpstart, standing still while life's chain is locked, awake with
 * how long its belly lasts or hungry, or a statue for good once its stand is gone. Its stand's food and, while it sleeps,
 * the stand's FE Jade shows on its own, from the stand's faces. Read from what the Homunculus sends every client, so it needs
 * no server data. Jade finds this by its annotation and loads it only when Jade is installed.
 */
@WailaPlugin
public final class HomunculusJadePlugin implements IWailaPlugin {

    private static final Identifier UID = Identifier.fromNamespaceAndPath(MechanicalMasteryReborn.MOD_ID, "homunculus");
    private static final String KEY = MechanicalMasteryReborn.MOD_ID + ".jade.homunculus.";

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerEntityComponent(new IEntityComponentProvider() {
            @Override
            public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
                if (accessor.getEntity() instanceof HomunculusEntity homunculus) {
                    tooltip.add(line(homunculus));
                }
            }

            @Override
            public Identifier getUid() {
                return UID;
            }
        }, HomunculusEntity.class);
    }

    private static Component line(HomunculusEntity homunculus) {
        if (homunculus.doing() == HomunculusEntity.STANDLESS) {
            return Component.translatable(KEY + "standless").withStyle(ChatFormatting.GRAY);
        }
        return switch (Doing.values()[homunculus.doing()]) {
            case ASLEEP -> Component.translatable(KEY + "asleep").withStyle(ChatFormatting.YELLOW);
            case LOCKED -> Component.translatable(KEY + "locked").append(": ").append(ChainMachines.line(Handicap.LIFE))
                    .withStyle(ChatFormatting.YELLOW);
            case AWAKE -> homunculus.fedSeconds() > 0 ? Component.translatable(KEY + "fed", homunculus.fedSeconds())
                    : Component.translatable(KEY + "hungry").withStyle(ChatFormatting.YELLOW);
        };
    }
}
