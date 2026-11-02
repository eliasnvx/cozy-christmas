package dev.eliasnvx.cozychristmas.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.eliasnvx.cozychristmas.client.cosmetic.FirstPersonCosmetics;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Draws the worn sleeve and mitten over the first-person hand (no loader has a common hook for this). */
@Mixin(AvatarRenderer.class)
public abstract class AvatarRendererHandMixin {

    @Inject(method = "renderHand", at = @At("TAIL"))
    private void cozychristmas$sleeveAndMitten(PoseStack poseStack, SubmitNodeCollector collector, int light, Identifier skin,
                                               ModelPart arm, boolean hasSleeve, CallbackInfo ci) {
        FirstPersonCosmetics.submit(((AvatarRenderer<?>) (Object) this).getModel(), arm, poseStack, collector, light);
    }
}
