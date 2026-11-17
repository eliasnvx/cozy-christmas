package dev.eliasnvx.cozychristmas.client.wood;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.BoatModel;
import net.minecraft.client.model.ChestBoatModel;
import net.minecraft.client.model.ListModel;
import net.minecraft.client.model.WaterPatchModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.vehicle.Boat;
import org.joml.Quaternionf;

/** Renders our wood boats using vanilla's boat models and our texture. */
public final class CozyBoatRenderer extends EntityRenderer<Boat> {

    private final ResourceLocation texture;
    private final ListModel<Boat> model;

    public CozyBoatRenderer(EntityRendererProvider.Context context, ModelLayerLocation layer, ResourceLocation texture,
                            boolean chest) {
        super(context);
        this.shadowRadius = 0.8F;
        this.texture = texture;
        ModelPart part = context.bakeLayer(layer);
        this.model = chest ? new ChestBoatModel(part) : new BoatModel(part);
    }

    @Override
    public void render(Boat boat, float entityYaw, float partialTicks, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.translate(0.0F, 0.375F, 0.0F);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - entityYaw));
        float hurtTime = boat.getHurtTime() - partialTicks;
        float damage = boat.getDamage() - partialTicks;
        if (damage < 0.0F) {
            damage = 0.0F;
        }
        if (hurtTime > 0.0F) {
            poseStack.mulPose(Axis.XP.rotationDegrees(Mth.sin(hurtTime) * hurtTime * damage / 10.0F * boat.getHurtDir()));
        }
        float bubbleAngle = boat.getBubbleAngle(partialTicks);
        if (!Mth.equal(bubbleAngle, 0.0F)) {
            poseStack.mulPose(new Quaternionf().setAngleAxis(bubbleAngle * (float) (Math.PI / 180.0), 1.0F, 0.0F, 1.0F));
        }
        poseStack.scale(-1.0F, -1.0F, 1.0F);
        poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
        model.setupAnim(boat, partialTicks, 0.0F, -0.1F, 0.0F, 0.0F);
        VertexConsumer consumer = buffer.getBuffer(model.renderType(texture));
        model.renderToBuffer(poseStack, consumer, packedLight, OverlayTexture.NO_OVERLAY);
        if (!boat.isUnderWater() && model instanceof WaterPatchModel patch) {
            patch.waterPatch().render(poseStack, buffer.getBuffer(RenderType.waterMask()), packedLight, OverlayTexture.NO_OVERLAY);
        }
        poseStack.popPose();
        super.render(boat, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(Boat boat) {
        return texture;
    }
}
