// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.client;

import io.github._5thlayer.mechanicalmasteryreborn.MechanicalMasteryReborn;
import io.github._5thlayer.mechanicalmasteryreborn.life.HomunculusEntity;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.ArmedEntityRenderState;
import net.minecraft.resources.Identifier;

/** The Homunculus, and as a grey statue while it sleeps, is locked or has lost its stand. */
public final class HomunculusRenderer extends MobRenderer<HomunculusEntity, HomunculusRenderState, HomunculusModel> {

    public static final ModelLayerLocation LAYER = new ModelLayerLocation(id("homunculus"), "main");

    private static final Identifier TEXTURE = id("textures/entity/homunculus/homunculus.png");
    private static final Identifier STATUE = id("textures/entity/homunculus/homunculus_statue.png");

    public HomunculusRenderer(EntityRendererProvider.Context context) {
        super(context, new HomunculusModel(context.bakeLayer(LAYER)), 0.3F);
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MechanicalMasteryReborn.MOD_ID, path);
    }

    @Override
    public Identifier getTextureLocation(HomunculusRenderState state) {
        return state.statue ? STATUE : TEXTURE;
    }

    @Override
    public HomunculusRenderState createRenderState() {
        return new HomunculusRenderState();
    }

    @Override
    public void extractRenderState(HomunculusEntity entity, HomunculusRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        ArmedEntityRenderState.extractArmedEntityRenderState(entity, state, itemModelResolver, partialTicks);
        state.statue = entity.isStatue();
    }
}
