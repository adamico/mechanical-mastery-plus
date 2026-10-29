// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github._5thlayer.mechanicalmasteryreborn.recycler.RecyclerLayout;
import io.github._5thlayer.mechanicalmasteryreborn.recycler.RecyclerOutputBlock;
import io.github._5thlayer.mechanicalmasteryreborn.recycler.RecyclerOutputBlockEntity;
import it.unimi.dsi.fastutil.HashCommon;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.ShelfRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/** An output's three positions, drawn as vanilla's shelf draws its items, into vanilla's shelf render state. */
final class RecyclerOutputRenderer implements BlockEntityRenderer<RecyclerOutputBlockEntity, ShelfRenderState> {

    private final ItemModelResolver itemModelResolver;

    RecyclerOutputRenderer(BlockEntityRendererProvider.Context context) {
        this.itemModelResolver = context.itemModelResolver();
    }

    @Override
    public ShelfRenderState createRenderState() {
        return new ShelfRenderState();
    }

    @Override
    public void extractRenderState(RecyclerOutputBlockEntity output, ShelfRenderState state, float partialTicks, Vec3 cameraPosition,
            ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(output, state, partialTicks, cameraPosition, breakProgress);
        state.facing = output.getBlockState().getValue(RecyclerOutputBlock.FACING);
        int seed = HashCommon.long2int(output.getBlockPos().asLong());
        for (int position = 0; position < RecyclerLayout.POSITIONS_PER_OUTPUT; position++) {
            ItemStack stack = output.item(position);
            if (stack.isEmpty()) {
                state.items[position] = null;
            } else {
                var item = new ItemStackRenderState();
                itemModelResolver.updateForTopItem(item, stack, ItemDisplayContext.ON_SHELF, output.getLevel(), null, seed + position);
                state.items[position] = item;
            }
        }
    }

    @Override
    public void submit(ShelfRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        float yRot = -state.facing.toYRot();
        for (int position = 0; position < state.items.length; position++) {
            ItemStackRenderState item = state.items[position];
            if (item == null) {
                continue;
            }
            poseStack.pushPose();
            poseStack.translate(0.5F, 0.5F, 0.5F);
            poseStack.mulPose(Axis.YP.rotationDegrees(yRot));
            poseStack.translate(new Vec3((position - 1) * 0.3125F, 0.0, -0.25));
            poseStack.scale(0.25F, 0.25F, 0.25F);
            AABB box = item.getModelBoundingBox();
            poseStack.translate(0.0, -box.minY - (box.maxY - box.minY) / 2.0, 0.0);
            item.submit(poseStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
            poseStack.popPose();
        }
    }
}
