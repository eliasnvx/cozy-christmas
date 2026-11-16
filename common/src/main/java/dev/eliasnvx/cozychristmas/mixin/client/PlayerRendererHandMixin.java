package dev.eliasnvx.cozychristmas.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.eliasnvx.cozychristmas.client.cosmetic.FirstPersonCosmetics;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Draws the worn sleeve and mitten over the first-person hand (no loader has a common hook for this). */
@Mixin(PlayerRenderer.class)
public abstract class PlayerRendererHandMixin {

    @Inject(method = "renderHand", at = @At("TAIL"))
    private void cozychristmas$sleeveAndMitten(PoseStack poseStack, MultiBufferSource buffers, int light, AbstractClientPlayer player,
                                               ModelPart arm, ModelPart sleeve, CallbackInfo ci) {
        FirstPersonCosmetics.render(((PlayerRenderer) (Object) this).getModel(), arm, poseStack, buffers, light, player);
    }
}
