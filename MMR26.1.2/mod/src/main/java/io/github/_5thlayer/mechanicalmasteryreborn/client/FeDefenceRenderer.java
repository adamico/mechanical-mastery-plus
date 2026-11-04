// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github._5thlayer.mechanicalmasteryreborn.defence.FeDefenceBlockEntity;
import it.unimi.dsi.fastutil.HashCommon;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.jspecify.annotations.Nullable;

/**
 * The Electric Defence's sparks: a few jagged arcs over its top, like a plasma globe's without the glass, from the core
 * its shots start at, drawn anew every few ticks (#256). Each draw is some sixty quads, so redrawing them is cheap; the
 * few ticks between are for the look, a flicker rather than a blur.
 */
final class FeDefenceRenderer implements BlockEntityRenderer<FeDefenceBlockEntity, FeDefenceRenderer.State> {

    private static final int TICKS_PER_ARCS = 3;
    private static final int ARCS = 5;
    private static final int SEGMENTS = 4;
    private static final float WIDTH = 0.012F;
    private static final float CORE_WIDTH = 0.04F;

    FeDefenceRenderer(BlockEntityRendererProvider.Context context) {
    }

    static final class State extends BlockEntityRenderState {
        long seed;
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(FeDefenceBlockEntity defence, State state, float partialTicks, Vec3 cameraPosition,
            ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(defence, state, partialTicks, cameraPosition, breakProgress);
        long time = defence.getLevel() == null ? 0 : defence.getLevel().getGameTime();
        state.seed = HashCommon.mix(defence.getBlockPos().asLong() ^ (time / TICKS_PER_ARCS));
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        RandomSource random = RandomSource.create(state.seed);
        Vector3f core = new Vector3f((float) FeDefenceBlockEntity.SPARK_CORE.x, (float) FeDefenceBlockEntity.SPARK_CORE.y,
                (float) FeDefenceBlockEntity.SPARK_CORE.z);
        collector.submitCustomGeometry(poseStack, RenderTypes.lightning(), (pose, buffer) -> {
            Matrix4f matrix = pose.pose();
            for (int arc = 0; arc < ARCS; arc++) {
                // Out and a little up or down from the core, never back into the block.
                double angle = (arc + random.nextDouble() * 0.6) * Math.PI * 2 / ARCS;
                float reach = 0.5F + random.nextFloat() * 0.25F;
                Vector3f end = new Vector3f((float) Math.cos(angle) * reach, (random.nextFloat() - 0.35F) * 0.45F,
                        (float) Math.sin(angle) * reach).add(core);
                Vector3f from = new Vector3f(core);
                for (int segment = 1; segment <= SEGMENTS; segment++) {
                    Vector3f to = new Vector3f(core).lerp(end, (float) segment / SEGMENTS);
                    if (segment < SEGMENTS) {
                        to.add((random.nextFloat() - 0.5F) * 0.16F, (random.nextFloat() - 0.5F) * 0.16F, (random.nextFloat() - 0.5F) * 0.16F);
                    }
                    // Hot near the core, cooler toward the arc's end.
                    float heat = 1.0F - (float) segment / (SEGMENTS + 1);
                    bolt(matrix, buffer, from, to, WIDTH, 0.6F + 0.4F * heat, 0.2F + 0.5F * heat, 1.0F, 0.35F + 0.35F * heat);
                    from = to;
                }
            }
            float r = 0.12F;
            bolt(matrix, buffer, new Vector3f(core).sub(r, 0, 0), new Vector3f(core).add(r, 0, 0), CORE_WIDTH, 1, 1, 1, 0.9F);
            bolt(matrix, buffer, new Vector3f(core).sub(0, r, 0), new Vector3f(core).add(0, r, 0), CORE_WIDTH, 1, 1, 1, 0.9F);
            bolt(matrix, buffer, new Vector3f(core).sub(0, 0, r), new Vector3f(core).add(0, 0, r), CORE_WIDTH, 1, 1, 1, 0.9F);
            // A halo round the core, wider and fainter.
            float halo = 0.2F;
            bolt(matrix, buffer, new Vector3f(core).sub(halo, 0, 0), new Vector3f(core).add(halo, 0, 0), CORE_WIDTH, 1, 0.6F, 1, 0.4F);
            bolt(matrix, buffer, new Vector3f(core).sub(0, 0, halo), new Vector3f(core).add(0, 0, halo), CORE_WIDTH, 1, 0.6F, 1, 0.4F);
        });
    }

    /** A segment as two crossed thin quads, each drawn both ways round so it shows from every side. */
    private static void bolt(Matrix4f matrix, VertexConsumer buffer, Vector3f from, Vector3f to, float width, float red,
            float green, float blue, float alpha) {
        Vector3f along = new Vector3f(to).sub(from);
        Vector3f across = Math.abs(along.y) > 0.9F * along.length()
                ? new Vector3f(1, 0, 0) : new Vector3f(along).cross(0, 1, 0);
        across.normalize(width);
        Vector3f other = new Vector3f(along).cross(across).normalize(width);
        for (Vector3f side : new Vector3f[] {across, other}) {
            Vector3f a = new Vector3f(from).sub(side), b = new Vector3f(from).add(side);
            Vector3f c = new Vector3f(to).add(side), d = new Vector3f(to).sub(side);
            for (Vector3f[] quad : new Vector3f[][] {{a, b, c, d}, {d, c, b, a}}) {
                for (Vector3f corner : quad) {
                    buffer.addVertex(matrix, corner.x, corner.y, corner.z).setColor(red, green, blue, alpha);
                }
            }
        }
    }

    @Override
    public AABB getRenderBoundingBox(FeDefenceBlockEntity defence) {
        return new AABB(defence.getBlockPos()).expandTowards(0, 1, 0).inflate(1, 0, 1);
    }
}
