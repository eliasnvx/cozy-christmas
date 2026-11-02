package dev.eliasnvx.cozychristmas.client.cosmetic;

import dev.architectury.registry.client.level.entity.EntityModelLayerRegistry;
import dev.eliasnvx.cozychristmas.CozyChristmas;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.model.player.PlayerModel;

import java.util.Locale;

/**
 * The code models of the worn pieces. Cubes hang off the player model's own parts (head, body, arms), so they pose
 * like the wearer. Each shape is baked in two sizes: {@code fit} sits just above the skin's outer layer, {@code armor}
 * wraps around the armor piece of the slot while that shows. Shapes with arms are baked for slim arms too (fit only).
 *
 * <p>Texture layouts, for whoever repaints them (box UVs as vanilla: top/bottom above, then right, front, left, back):
 * <ul>
 *     <li>{@link #SWEATER} 64x64, the player skin layout: body at (16,16), right arm at (40,16), left arm at (32,48);
 *     collar at (0,0), hem at (0,6), cuffs at (32,0) right and (32,6) left</li>
 *     <li>{@link #POMPOM_HAT} 64x32: crown at (0,0), cuff at (0,12), pompom at (32,0)</li>
 *     <li>{@link #SCARF} 32x32: wrap at (0,0), front end at (0,8), back end at (8,8)</li>
 *     <li>{@link #MITTENS} 32x32: right mitten at (0,0), its cuff at (0,8); left at (16,0) and (16,8)</li>
 * </ul>
 */
public enum CosmeticShape {
    SWEATER(64, 64, true),
    POMPOM_HAT(64, 32, false),
    SCARF(32, 32, false),
    MITTENS(32, 32, true);

    /** Above the skin's outer layer (0.25) and, for the armor size, above the outer armor layer (1.0). */
    private static final float SWEATER_FIT = 0.3F;
    private static final float SWEATER_OVER_ARMOR = 1.1F;
    /** Ribbing (collar, hem, cuffs) stands out a little from the knit. */
    private static final float RIB = 0.15F;
    /** Over the skin's hat layer (0.5) / over the helmet (1.0). */
    private static final float HAT_FIT = 0.6F;
    private static final float HAT_OVER_ARMOR = 1.1F;
    private static final float HAT_CUFF = 0.3F;
    private static final float POMPOM_SIZE = 3.0F;
    private static final float SCARF_FIT = 0.6F;
    private static final float SCARF_OVER_ARMOR = 1.2F;
    /** Over sweater sleeves and cuffs, so mittens can be worn with any sweater. */
    private static final float MITTENS_FIT = 0.5F;
    private static final float MITTENS_OVER_ARMOR = 1.25F;
    private static final float MITTEN_CUFF = 0.15F;

    /** Name of the scarf's back end, swung by {@link CosmeticModel} while the wearer moves. */
    public static final String SCARF_TAIL = "scarf_tail";

    private final int textureWidth;
    private final int textureHeight;
    private final boolean hasArms;
    private final String name = name().toLowerCase(Locale.ROOT);

    CosmeticShape(int textureWidth, int textureHeight, boolean hasArms) {
        this.textureWidth = textureWidth;
        this.textureHeight = textureHeight;
        this.hasArms = hasArms;
    }

    /**
     * {@code cozychristmas:<shape>[_slim]#fit|armor}. Shapes without arms ignore {@code slim}, and so does the armor
     * size: armor has wide arms on every skin, so slim sleeves around it would let it poke through.
     */
    public ModelLayerLocation layer(boolean overArmor, boolean slim) {
        String path = hasSlimArms(overArmor, slim) ? name + "_slim" : name;
        return new ModelLayerLocation(CozyChristmas.id(path), overArmor ? "armor" : "fit");
    }

    public static void registerLayers() {
        for (CosmeticShape shape : values()) {
            for (boolean overArmor : new boolean[]{false, true}) {
                EntityModelLayerRegistry.register(shape.layer(overArmor, false), () -> shape.definition(overArmor, false));
                if (shape.hasSlimArms(overArmor, true)) {
                    EntityModelLayerRegistry.register(shape.layer(overArmor, true), () -> shape.definition(overArmor, true));
                }
            }
        }
    }

    private boolean hasSlimArms(boolean overArmor, boolean slim) {
        return hasArms && slim && !overArmor;
    }

    private LayerDefinition definition(boolean overArmor, boolean slim) {
        // the player's parts at their rest poses, without any cubes: ours are added as children
        MeshDefinition mesh = PlayerModel.createMesh(CubeDeformation.NONE, slim);
        PartDefinition root = mesh.getRoot();
        root.clearRecursively();
        switch (this) {
            case SWEATER -> sweater(root, overArmor ? SWEATER_OVER_ARMOR : SWEATER_FIT, slim);
            case POMPOM_HAT -> pompomHat(root, overArmor ? HAT_OVER_ARMOR : HAT_FIT);
            case SCARF -> scarf(root, overArmor ? SCARF_OVER_ARMOR : SCARF_FIT);
            case MITTENS -> mittens(root, overArmor ? MITTENS_OVER_ARMOR : MITTENS_FIT, slim);
        }
        return LayerDefinition.create(mesh, textureWidth, textureHeight);
    }

    private static void sweater(PartDefinition root, float inflate, boolean slim) {
        CubeDeformation knit = new CubeDeformation(inflate);
        CubeDeformation rib = new CubeDeformation(inflate + RIB);
        root.getChild("body").addOrReplaceChild("sweater", CubeListBuilder.create()
                .texOffs(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8, 12, 4, knit)
                .texOffs(0, 0).addBox(-4.0F, -0.5F, -2.0F, 8, 1, 4, rib)   // collar
                .texOffs(0, 6).addBox(-4.0F, 11.0F, -2.0F, 8, 1, 4, rib),  // hem
                PartPose.ZERO);
        int width = slim ? 3 : 4;
        root.getChild("right_arm").addOrReplaceChild("sweater", CubeListBuilder.create()
                .texOffs(40, 16).addBox(slim ? -2.0F : -3.0F, -2.0F, -2.0F, width, 12, 4, knit)
                .texOffs(32, 0).addBox(slim ? -2.0F : -3.0F, 9.0F, -2.0F, width, 1, 4, rib),
                PartPose.ZERO);
        root.getChild("left_arm").addOrReplaceChild("sweater", CubeListBuilder.create()
                .texOffs(32, 48).addBox(-1.0F, -2.0F, -2.0F, width, 12, 4, knit)
                .texOffs(32, 6).addBox(-1.0F, 9.0F, -2.0F, width, 1, 4, rib),
                PartPose.ZERO);
    }

    /** A beanie on the top of the head (it stops above the eyes) with a folded cuff and a pompom. */
    private static void pompomHat(PartDefinition root, float inflate) {
        float pompomBottom = -8.0F - inflate;
        root.getChild("head").addOrReplaceChild("pompom_hat", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8, 2, 8, new CubeDeformation(inflate))
                .texOffs(0, 12).addBox(-4.0F, -6.0F, -4.0F, 8, 1, 8, new CubeDeformation(inflate + HAT_CUFF))
                .texOffs(32, 0).addBox(-POMPOM_SIZE / 2, pompomBottom - POMPOM_SIZE + 0.4F, -POMPOM_SIZE / 2,
                        POMPOM_SIZE, POMPOM_SIZE, POMPOM_SIZE),
                PartPose.ZERO);
    }

    /** Wrapped around the neck; one end hangs in front, the other behind ({@link #SCARF_TAIL}, it flutters). */
    private static void scarf(PartDefinition root, float inflate) {
        PartDefinition body = root.getChild("body");
        body.addOrReplaceChild("scarf", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-4.0F, -1.0F, -2.0F, 8, 2, 4, new CubeDeformation(inflate))
                .texOffs(0, 8).addBox(1.0F, 1.0F, -3.0F - inflate, 2, 6, 1),
                PartPose.ZERO);
        body.addOrReplaceChild(SCARF_TAIL, CubeListBuilder.create()
                .texOffs(8, 8).addBox(-1.5F, 0.0F, 0.0F, 3, 8, 1),
                PartPose.offset(-1.5F, 0.5F, 2.0F + inflate));
    }

    /** From the wrist to the knuckles, with a cuff. */
    private static void mittens(PartDefinition root, float inflate, boolean slim) {
        int width = slim ? 3 : 4;
        CubeDeformation knit = new CubeDeformation(inflate);
        CubeDeformation cuff = new CubeDeformation(inflate + MITTEN_CUFF);
        root.getChild("right_arm").addOrReplaceChild("mitten", CubeListBuilder.create()
                .texOffs(0, 0).addBox(slim ? -2.0F : -3.0F, 6.0F, -2.0F, width, 4, 4, knit)
                .texOffs(0, 8).addBox(slim ? -2.0F : -3.0F, 5.0F, -2.0F, width, 1, 4, cuff),
                PartPose.ZERO);
        root.getChild("left_arm").addOrReplaceChild("mitten", CubeListBuilder.create()
                .texOffs(16, 0).addBox(-1.0F, 6.0F, -2.0F, width, 4, 4, knit)
                .texOffs(16, 8).addBox(-1.0F, 5.0F, -2.0F, width, 1, 4, cuff),
                PartPose.ZERO);
    }
}
