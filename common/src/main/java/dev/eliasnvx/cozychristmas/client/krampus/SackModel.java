package dev.eliasnvx.cozychristmas.client.krampus;

import dev.eliasnvx.cozychristmas.krampus.KrampusSack;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;

/**
 * Krampus's sack, big enough for a player: a burlap bag tied at the top, wobbling as whoever is inside struggles.
 * Texture 128x64, boxes in {@code tools/gen_krampus.py}.
 */
public final class SackModel extends HierarchicalModel<KrampusSack> {

    private static final float STRUGGLE = 0.08F;
    private static final float STRUGGLE_RATE = 0.7F;

    private final ModelPart root;
    private final ModelPart sack;

    public SackModel(ModelPart root) {
        super(RenderType::entityCutout);
        this.root = root;
        sack = root.getChild("sack");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition sack = root.addOrReplaceChild("sack", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-9.0F, -33.0F, -9.0F, 18, 33, 18), PartPose.offset(0.0F, 24.0F, 0.0F));
        sack.addOrReplaceChild("neck", CubeListBuilder.create()
                .texOffs(72, 0).addBox(-3.0F, -36.0F, -3.0F, 6, 3, 6)
                .texOffs(72, 10).addBox(-4.5F, -38.0F, -4.5F, 9, 2, 9), PartPose.ZERO);
        return LayerDefinition.create(mesh, 128, 64);
    }

    @Override
    public ModelPart root() {
        return root;
    }

    @Override
    public void setupAnim(KrampusSack bag, float walkPos, float walkSpeed, float ageInTicks, float headYaw, float headPitch) {
        sack.zRot = Mth.sin(ageInTicks * STRUGGLE_RATE) * STRUGGLE;
        sack.xRot = Mth.cos(ageInTicks * STRUGGLE_RATE * 1.3F) * STRUGGLE * 0.6F;
    }
}
