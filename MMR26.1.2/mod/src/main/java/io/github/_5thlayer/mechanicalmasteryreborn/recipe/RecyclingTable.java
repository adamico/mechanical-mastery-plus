// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.recipe;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.github._5thlayer.mechanicalmasteryreborn.MechanicalMasteryReborn;
import io.github._5thlayer.mechanicalmasteryreborn.recipe.RecyclingRules.Share;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;

/**
 * What every item a recipe makes recycles into at full cost ({@link Recycling#byRecipe}), sent to each client
 * with the recipes: a client is sent no crafting or smelting recipe to derive it from, and EMI shows it. The
 * client keeps the last one it was sent in {@link #received}.
 */
public record RecyclingTable(Map<Item, List<Share<Item>>> table) implements CustomPacketPayload {

    public static final Type<RecyclingTable> TYPE = new Type<>(Identifier.fromNamespaceAndPath(MechanicalMasteryReborn.MOD_ID, "recycling_table"));

    private static final StreamCodec<RegistryFriendlyByteBuf, Item> ITEM = ByteBufCodecs.registry(Registries.ITEM);
    private static final StreamCodec<RegistryFriendlyByteBuf, Share<Item>> SHARE = StreamCodec.composite(
            ITEM, Share::item, ByteBufCodecs.DOUBLE, Share::amount, Share::new);

    public static final StreamCodec<RegistryFriendlyByteBuf, RecyclingTable> STREAM_CODEC = ByteBufCodecs
            .<RegistryFriendlyByteBuf, Item, List<Share<Item>>, Map<Item, List<Share<Item>>>>map(HashMap::new, ITEM,
                    SHARE.apply(ByteBufCodecs.list()))
            .map(RecyclingTable::new, RecyclingTable::table);

    /** The table the client was last sent: empty until a server sends one. */
    private static volatile RecyclingTable received = new RecyclingTable(Map.of());

    public static RecyclingTable received() {
        return received;
    }

    /** Keeps {@code table} as the one the client was last sent. */
    public static void receive(RecyclingTable table) {
        received = table;
    }

    @Override
    public Type<RecyclingTable> type() {
        return TYPE;
    }
}
