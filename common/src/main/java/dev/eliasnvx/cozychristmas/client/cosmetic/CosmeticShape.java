package dev.eliasnvx.cozychristmas.client.cosmetic;

import dev.architectury.registry.client.level.entity.EntityModelLayerRegistry;
import dev.eliasnvx.cozychristmas.CozyChristmas;
import dev.eliasnvx.cozychristmas.compat.catgirl.CatgirlEars;
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
 *     <li>{@link #EAR_WARMERS} 64x32: band over the top at (0,0), down the sides at (24,0), side muffs at (0,8); over
 *     cat ears a short band between them at (0,2) and the muffs in three tiers at (32,0), (32,5) and (48,0). Both
 *     sides share their texture</li>
 *     <li>{@link #ELF_HAT} 64x32: crown at (0,0), cuff at (0,10), the cone's six segments from the bottom at (32,0),
 *     (32,9), (0,19), (20,19), (36,19), (48,19), the bell at (56,19)</li>
 *     <li>{@link #ANTLER_HEADBAND} 32x32: band over the top at (0,0), down the sides at (24,0); each antler's beam at
 *     (0,2), front tine at (4,2), outer tine at (8,2)</li>
 * </ul>
 */
public enum CosmeticShape {
    SWEATER(64, 64, true),
    POMPOM_HAT(64, 32, false),
    SCARF(32, 32, false),
    MITTENS(32, 32, true),
    EAR_WARMERS(64, 32, false),
    ELF_HAT(64, 32, false),
    ANTLER_HEADBAND(32, 32, false);

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

    /**
     * Headbands (ear warmers, antlers) are drawn just above the hat layer and scaled up around the neck over a helmet,
     * more across than up, so they clear its shell (1 px off the head) without floating above it.
     */
    private static final float BAND_OVER_ARMOR_XZ = 1.15F;
    private static final float BAND_OVER_ARMOR_Y = 1.08F;
    private static final int[] ELF_HAT_WIDTHS = {7, 6, 5, 4, 3, 2};
    /** Each segment of the elf hat's cone bends this much further back than the one below: it flops over. */
    private static final float[] ELF_HAT_BEND = {-0.15F, -0.3F, -0.45F, -0.55F, -0.6F, -0.6F};
    private static final int ELF_HAT_SEGMENT = 2;
    private static final int[][] ELF_HAT_UV = {{32, 0}, {32, 9}, {0, 19}, {20, 19}, {36, 19}, {48, 19}};

    /** Name of the scarf's back end, swung by {@link CosmeticModel} while the wearer moves. */
    public static final String SCARF_TAIL = "scarf_tail";
    /** The ear warmers' parts: band, side bands and muffs on a plain head; short band and muffs over cat ears. */
    public static final String EAR_WARMERS_BAND = "ear_warmers_band";
    public static final String EAR_WARMERS_CAT_BAND = "ear_warmers_cat_band";
    public static final String EAR_WARMERS_SIDES = "ear_warmers_sides";
    public static final String EAR_WARMERS_CAT_LEFT = "ear_warmers_cat_left";
    public static final String EAR_WARMERS_CAT_RIGHT = "ear_warmers_cat_right";
    /** The elf hat's cone segments are {@code elf_hat_0} (on the crown) to {@code elf_hat_5}, each a child of the last. */
    public static final String ELF_HAT_PREFIX = "elf_hat_";
    public static final int ELF_HAT_SEGMENTS = ELF_HAT_WIDTHS.length;

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
            case EAR_WARMERS -> earWarmers(root, overArmor);
            case ELF_HAT -> elfHat(root, overArmor ? HAT_OVER_ARMOR : HAT_FIT);
            case ANTLER_HEADBAND -> antlerHeadband(root, overArmor);
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

    /** The rest pose of an elf hat segment: how far it bends back from the one below. */
    public static float elfHatBend(int segment) {
        return ELF_HAT_BEND[segment];
    }

    private static PartPose band(boolean overArmor) {
        return overArmor ? PartPose.ZERO.scaled(BAND_OVER_ARMOR_XZ, BAND_OVER_ARMOR_Y, BAND_OVER_ARMOR_XZ) : PartPose.ZERO;
    }

    /** The band over the top of the head, just above the hat layer (top -8.5, sides ±4.5). */
    private static CubeListBuilder bandTop(CubeListBuilder builder, float z) {
        return builder.texOffs(0, 0).addBox(-5.5F, -9.6F, z, 11, 1, 1);
    }

    /** The band down both sides of the head, under {@link #bandTop}. */
    private static CubeListBuilder bandSides(CubeListBuilder builder, float z) {
        return builder.texOffs(24, 0).addBox(4.5F, -8.6F, z, 1, 4, 1)
                .texOffs(24, 0).addBox(-5.5F, -8.6F, z, 1, 4, 1);
    }

    /**
     * Fluffy muffs on a band. On a plain head they cover the ears on its sides; over cat ears (Catgirl &amp; Catboy
     * Mod) they sit on top, posed like each ear by {@link CosmeticModel}, so the ear stays inside its muff.
     */
    private static void earWarmers(PartDefinition root, boolean overArmor) {
        PartDefinition head = root.getChild("head");
        head.addOrReplaceChild(EAR_WARMERS_BAND, bandTop(CubeListBuilder.create(), -0.5F), band(overArmor));
        head.addOrReplaceChild(EAR_WARMERS_SIDES, bandSides(CubeListBuilder.create(), -0.5F)
                .texOffs(0, 8).addBox(4.5F, -5.0F, -2.0F, 2, 4, 4)
                .texOffs(0, 8).addBox(-6.5F, -5.0F, -2.0F, 2, 4, 4), band(overArmor));
        float lift = overArmor ? CatgirlEars.HELMET_LIFT : 0.0F;
        // the band over the top would stick out past the muffs: between cat ears it only joins them
        head.addOrReplaceChild(EAR_WARMERS_CAT_BAND, CubeListBuilder.create()
                .texOffs(0, 2).addBox(-2.5F, -9.0F - lift, -0.5F, 5, 1, 1), PartPose.ZERO);
        for (int side = -1; side <= 1; side += 2) {
            // around one cat ear (3.8 wide, 4.2 tall, -1.05..0.75 deep from its pivot), pivoted where it is
            head.addOrReplaceChild(side < 0 ? EAR_WARMERS_CAT_LEFT : EAR_WARMERS_CAT_RIGHT, CubeListBuilder.create()
                    .texOffs(32, 0).addBox(-2.5F, -1.8F, -1.5F, 5, 2, 3)
                    .texOffs(32, 5).addBox(-2.0F, -3.8F, -1.5F, 4, 2, 3)
                    .texOffs(48, 0).addBox(-1.0F, -5.0F, -1.0F, 2, 2, 2),
                    PartPose.offsetAndRotation(side * CatgirlEars.SPACING, CatgirlEars.BASE_Y - lift, CatgirlEars.BASE_Z,
                            0.0F, 0.0F, side * CatgirlEars.TILT));
        }
    }

    /** A felt cap with a cuff, its cone bending back and down to a bell; the upper segments swing as the wearer walks. */
    private static void elfHat(PartDefinition root, float inflate) {
        PartDefinition head = root.getChild("head");
        head.addOrReplaceChild("elf_hat", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8, 2, 8, new CubeDeformation(inflate))
                .texOffs(0, 10).addBox(-4.0F, -6.0F, -4.0F, 8, 1, 8, new CubeDeformation(inflate + HAT_CUFF)),
                PartPose.ZERO);
        PartDefinition parent = head;
        float y = -8.0F - inflate;
        for (int i = 0; i < ELF_HAT_WIDTHS.length; i++) {
            float w = ELF_HAT_WIDTHS[i];
            parent = parent.addOrReplaceChild(ELF_HAT_PREFIX + i, CubeListBuilder.create()
                    .texOffs(ELF_HAT_UV[i][0], ELF_HAT_UV[i][1])
                    .addBox(-w / 2, -ELF_HAT_SEGMENT, -w / 2, w, ELF_HAT_SEGMENT, w),
                    PartPose.offsetAndRotation(0.0F, y, 0.0F, ELF_HAT_BEND[i], 0.0F, 0.0F));
            y = -ELF_HAT_SEGMENT;
        }
        parent.addOrReplaceChild("elf_hat_bell", CubeListBuilder.create()
                .texOffs(56, 19).addBox(-1.0F, -1.5F, -1.0F, 2, 2, 2),
                PartPose.offsetAndRotation(0.0F, -ELF_HAT_SEGMENT, 0.0F, -0.3F, 0.0F, 0.0F));
    }

    /**
     * A band with two antlers, set a little behind the middle of the head: behind where cat ears stand, as a reindeer's
     * antlers are behind its ears.
     */
    private static void antlerHeadband(PartDefinition root, boolean overArmor) {
        PartDefinition piece = root.getChild("head").addOrReplaceChild("antler_headband",
                bandSides(bandTop(CubeListBuilder.create(), 1.0F), 1.0F), band(overArmor));
        for (int side = -1; side <= 1; side += 2) {
            PartDefinition antler = piece.addOrReplaceChild(side < 0 ? "left_antler" : "right_antler", CubeListBuilder.create()
                    .texOffs(0, 2).addBox(-0.5F, -6.0F, -0.5F, 1, 6, 1),
                    PartPose.offsetAndRotation(side * 3.4F, -9.3F, 1.5F, -0.2F, 0.0F, side * 0.35F));
            antler.addOrReplaceChild("front_tine", CubeListBuilder.create()
                    .texOffs(4, 2).addBox(-0.5F, -3.0F, -0.5F, 1, 3, 1),
                    PartPose.offsetAndRotation(0.0F, -2.0F, 0.0F, 0.9F, 0.0F, 0.0F));
            antler.addOrReplaceChild("outer_tine", CubeListBuilder.create()
                    .texOffs(8, 2).addBox(-0.5F, -2.0F, -0.5F, 1, 2, 1),
                    PartPose.offsetAndRotation(0.0F, -4.0F, 0.0F, 0.0F, 0.0F, side * 0.9F));
        }
    }
}
