// SPDX-FileCopyrightText: 2026 5thlayer
// SPDX-License-Identifier: MIT

package io.github._5thlayer.mechanicalmasteryreborn.client;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.util.Mth;

/**
 * The Homunculus: a small made figure, 18 pixels tall, its head as tall as the rest of it, with a long nose and stub ears,
 * reduced from a CC0 sculpt (NOTICE); its face is in the texture. Walking, its legs and arms swing; working, its right arm
 * swings down toward its stand. A statue holds still, arms at its sides.
 */
public final class HomunculusModel extends EntityModel<HomunculusRenderState> {

    private final ModelPart head;
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;

    public HomunculusModel(ModelPart root) {
        super(root);
        head = root.getChild("head");
        rightArm = root.getChild("right_arm");
        leftArm = root.getChild("left_arm");
        rightLeg = root.getChild("right_leg");
        leftLeg = root.getChild("left_leg");
    }

    /** On a 64 by 32 texture; its x across, its y down from the top of its head to its feet, and its front toward negative z. */
    public static LayerDefinition createBodyLayer() {
        var mesh = new MeshDefinition().transformed(pose -> pose.translated(0F, 24F, 0F));
        var root = mesh.getRoot();
        root.addOrReplaceChild("body", CubeListBuilder.create()
                        .texOffs(28, 0).addBox(-2F, 0F, -1F, 4F, 4F, 2.5F)
                        .texOffs(42, 7).addBox(-1F, -1F, -0.5F, 2F, 1F, 1.5F),
                PartPose.offset(0F, -8F, 0F));
        root.addOrReplaceChild("head", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-3.5F, -9F, -4F, 7F, 9F, 7F)
                        .texOffs(42, 0).addBox(-1F, -7F, -6F, 2F, 3F, 2F)
                        .texOffs(50, 0).addBox(-4.5F, -6F, -0.5F, 1F, 3F, 2F)
                        .texOffs(56, 0).addBox(3.5F, -6F, -0.5F, 1F, 3F, 2F),
                PartPose.offset(0F, -9F, 0F));
        root.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(0, 16).addBox(-0.5F, 0F, -0.5F, 1F, 5.5F, 1F),
                PartPose.offset(-2.5F, -8F, 0F));
        root.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(5, 16).addBox(-0.5F, 0F, -0.5F, 1F, 5.5F, 1F),
                PartPose.offset(2.5F, -8F, 0F));
        root.addOrReplaceChild("right_leg", CubeListBuilder.create()
                        .texOffs(10, 16).addBox(-0.75F, 0F, -0.5F, 1.5F, 3F, 1.5F)
                        .texOffs(24, 16).addBox(-0.75F, 3F, -1.5F, 1.5F, 1F, 2.5F),
                PartPose.offset(-1.25F, -4F, 0F));
        root.addOrReplaceChild("left_leg", CubeListBuilder.create()
                        .texOffs(17, 16).addBox(-0.75F, 0F, -0.5F, 1.5F, 3F, 1.5F)
                        .texOffs(33, 16).addBox(-0.75F, 3F, -1.5F, 1.5F, 1F, 2.5F),
                PartPose.offset(1.25F, -4F, 0F));
        return LayerDefinition.create(mesh, 64, 32);
    }

    @Override
    public void setupAnim(HomunculusRenderState state) {
        super.setupAnim(state);
        if (state.statue) {
            return;
        }
        head.xRot = state.xRot * Mth.DEG_TO_RAD;
        head.yRot = state.yRot * Mth.DEG_TO_RAD;
        float swing = Mth.cos(state.walkAnimationPos * 0.6662F) * state.walkAnimationSpeed;
        rightLeg.xRot = swing * 1.4F;
        leftLeg.xRot = -swing * 1.4F;
        rightArm.xRot = -swing;
        leftArm.xRot = swing;
        if (state.attackTime > 0F) {
            rightArm.xRot = -1.6F + Mth.sin(state.attackTime * Mth.PI) * 1.2F;
        }
    }
}
