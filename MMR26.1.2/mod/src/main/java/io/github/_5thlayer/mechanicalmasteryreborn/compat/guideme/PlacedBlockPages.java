// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.compat.guideme;

import java.util.Arrays;
import java.util.Optional;

import com.mojang.blaze3d.platform.InputConstants;
import guideme.GuidesCommon;
import guideme.PageAnchor;
import io.github._5thlayer.mechanicalmasteryreborn.MechanicalMasteryReborn;
import io.github._5thlayer.mechanicalmasteryreborn.cod.CodBlock;
import io.github._5thlayer.mechanicalmasteryreborn.lab.LabBlock;
import io.github._5thlayer.mechanicalmasteryreborn.port.PortBlock;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.common.NeoForge;
import org.jspecify.annotations.Nullable;

/**
 * GuideME's key, pressed in the world while looking at a block that only ever stands placed, so has no item to hold the
 * key over, opens that block's page of the pack's manual: the lab's, or the Hermetic Lab's once it has awakened, and
 * C.O.D.'s for C.O.D. and its Ports. GuideME opens a page by its key only from an item's tooltip, and its key is bound in
 * the GUI context, so the game never counts it pressed in the world: this matches the raw key press against the key
 * bound to it instead. Loaded only when GuideME is installed, on the client.
 */
public final class PlacedBlockPages {

    /** The pack's manual. */
    static final Identifier MANUAL = Identifier.fromNamespaceAndPath(MechanicalMasteryReborn.MOD_ID, "manual");
    /** GuideME's key, as its keybind names it. */
    private static final String KEY = "key.guideme.guide";

    private static @Nullable KeyMapping key;

    private PlacedBlockPages() {
    }

    public static void register() {
        NeoForge.EVENT_BUS.addListener(InputEvent.Key.class, PlacedBlockPages::onKey);
    }

    private static void onKey(InputEvent.Key event) {
        Minecraft minecraft = Minecraft.getInstance();
        KeyMapping hotkey = key(minecraft);
        if (hotkey == null || event.getAction() != InputConstants.PRESS || minecraft.screen != null
                || !hotkey.getKey().equals(InputConstants.getKey(event.getKeyEvent()))
                || !hotkey.getKeyModifier().isActive(KeyConflictContext.IN_GAME)) {
            return;
        }
        if (minecraft.player == null || minecraft.level == null
                || !(minecraft.hitResult instanceof BlockHitResult hit) || hit.getType() != HitResult.Type.BLOCK) {
            return;
        }
        page(minecraft.level.getBlockState(hit.getBlockPos()))
                .ifPresent(page -> GuidesCommon.openGuide(minecraft.player, MANUAL, PageAnchor.page(page)));
    }

    /** The manual's page of the block {@code state} is of, if it is one that only ever stands placed. */
    static Optional<Identifier> page(BlockState state) {
        String path;
        if (state.getBlock() instanceof LabBlock) {
            path = state.getValue(LabBlock.HERMETIC) ? "hermetic_lab.md" : "lab.md";
        } else if (state.getBlock() instanceof CodBlock || state.getBlock() instanceof PortBlock) {
            path = "cod.md";
        } else {
            return Optional.empty();
        }
        return Optional.of(Identifier.fromNamespaceAndPath(MechanicalMasteryReborn.MOD_ID, path));
    }

    /** GuideME's key, found once among the game's keybinds. */
    private static @Nullable KeyMapping key(Minecraft minecraft) {
        if (key == null) {
            key = Arrays.stream(minecraft.options.keyMappings).filter(mapping -> mapping.getName().equals(KEY)).findFirst().orElse(null);
        }
        return key;
    }
}
