package dev.eliasnvx.cozychristmas.client.cosmetic;

import dev.eliasnvx.cozychristmas.compat.catgirl.CatgirlEars;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

/**
 * A baked {@link CosmeticShape}: poses like the wearer, swings the scarf's back end and the elf hat's tip, and puts the
 * ear warmers on the sides of the head or over cat ears. The cosmetic layer copies the wearer's pose onto it and draws
 * it right away, so one instance serves every player.
 */
public final class CosmeticModel extends PlayerModel<AbstractClientPlayer> {

    /** The back end hangs a little away from the back even when standing still. */
    private static final float TAIL_REST = 0.12F;
    /** How far it lifts behind a running player. */
    private static final float TAIL_LIFT = 1.0F;
    private static final float FLUTTER = 0.08F;
    private static final float FLUTTER_SPEED = 0.6F;

    /** The elf hat's upper segments flop back as the wearer walks, the lower ones hold their shape. */
    private static final int ELF_HAT_SWINGING_FROM = 2;
    private static final float ELF_HAT_WALK_BEND = -0.12F;
    private static final float ELF_HAT_SWAY = 0.05F;

    private final @Nullable ModelPart scarfTail;
    private final ModelPart @Nullable [] elfHat;
    private final @Nullable ModelPart earWarmersBand;
    private final @Nullable ModelPart earWarmersSides;
    private final @Nullable ModelPart earWarmersCatBand;
    private final @Nullable ModelPart earWarmersCatLeft;
    private final @Nullable ModelPart earWarmersCatRight;

    public CosmeticModel(ModelPart root, boolean slim) {
        super(root, slim);
        this.scarfTail = child(body, CosmeticShape.SCARF_TAIL);
        ModelPart segment = child(head, CosmeticShape.ELF_HAT_PREFIX + 0);
        if (segment != null) {
            elfHat = new ModelPart[CosmeticShape.ELF_HAT_SEGMENTS];
            for (int i = 0; i < elfHat.length; i++) {
                elfHat[i] = segment;
                segment = child(segment, CosmeticShape.ELF_HAT_PREFIX + (i + 1));
            }
        } else {
            elfHat = null;
        }
        this.earWarmersBand = child(head, CosmeticShape.EAR_WARMERS_BAND);
        this.earWarmersSides = child(head, CosmeticShape.EAR_WARMERS_SIDES);
        this.earWarmersCatBand = child(head, CosmeticShape.EAR_WARMERS_CAT_BAND);
        this.earWarmersCatLeft = child(head, CosmeticShape.EAR_WARMERS_CAT_LEFT);
        this.earWarmersCatRight = child(head, CosmeticShape.EAR_WARMERS_CAT_RIGHT);
    }

    private static @Nullable ModelPart child(@Nullable ModelPart parent, String name) {
        return parent != null && parent.hasChild(name) ? parent.getChild(name) : null;
    }

    /**
     * Poses the main parts like the wearer's model and shows only those it shows (a spectator's body is hidden), the
     * way 26.x's setupAnim did from the render state.
     */
    public void poseLike(PlayerModel<AbstractClientPlayer> wearer) {
        wearer.copyPropertiesTo(this);
        head.visible = wearer.head.visible;
        body.visible = wearer.body.visible;
        rightArm.visible = wearer.rightArm.visible;
        leftArm.visible = wearer.leftArm.visible;
        rightLeg.visible = wearer.rightLeg.visible;
        leftLeg.visible = wearer.leftLeg.visible;
    }

    /**
     * Also hides the skin's outer layer parts: cosmetics never draw there, and mods that put the skin's 3D layers into
     * every player model (3D Skin Layers) would otherwise draw the wearer's outer skin with the knit texture. The main
     * parts are posed by {@link #poseLike} before this is called.
     */
    public void setupAnim(PlayerPose state) {
        hat.visible = false;
        jacket.visible = false;
        leftSleeve.visible = false;
        rightSleeve.visible = false;
        leftPants.visible = false;
        rightPants.visible = false;
        CosmeticRenderData data = CosmeticRenderData.of(state);
        float walk = data.walkAmount();
        if (scarfTail != null) {
            float flutter = Mth.sin(state.ageInTicks * FLUTTER_SPEED + data.phase()) * FLUTTER * walk;
            scarfTail.xRot = TAIL_REST + walk * TAIL_LIFT + flutter;
        }
        if (elfHat != null) {
            for (int i = ELF_HAT_SWINGING_FROM; i < elfHat.length; i++) {
                float sway = Mth.sin(state.walkAnimationPos * 0.6662F + i * 0.6F) * ELF_HAT_SWAY * walk;
                elfHat[i].xRot = CosmeticShape.elfHatBend(i) + walk * ELF_HAT_WALK_BEND + sway;
            }
        }
        if (earWarmersBand != null && earWarmersSides != null && earWarmersCatBand != null && earWarmersCatLeft != null
                && earWarmersCatRight != null) {
            boolean cat = data.catEars();
            earWarmersBand.visible = !cat;
            earWarmersSides.visible = !cat;
            earWarmersCatBand.visible = cat;
            earWarmersCatLeft.visible = cat;
            earWarmersCatRight.visible = cat;
            if (cat) {
                poseLikeCatEar(earWarmersCatLeft, -1, state, data);
                poseLikeCatEar(earWarmersCatRight, 1, state, data);
            }
        }
    }

    private static void poseLikeCatEar(ModelPart muff, int side, PlayerPose state, CosmeticRenderData data) {
        muff.zRot = CatgirlEars.zRot(side, state.ageInTicks, data.phase(), state.walkAnimationPos, data.walkAmount(),
                data.flatten());
        muff.xRot = CatgirlEars.xRot(side, state.ageInTicks, data.phase(), data.flatten());
    }
}
