package dev.eliasnvx.cozychristmas.fabric;

import dev.eliasnvx.cozychristmas.client.CozyChristmasClient;
import dev.eliasnvx.cozychristmas.client.cosmetic.CosmeticLayer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityRenderLayerRegistrationCallback;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;

public final class CozyChristmasFabricClient implements ClientModInitializer {

    @Override
    @SuppressWarnings("unchecked")
    public void onInitializeClient() {
        CozyChristmasClient.init();
        LivingEntityRenderLayerRegistrationCallback.EVENT.register((type, renderer, helper, context) -> {
            if (renderer instanceof AvatarRenderer<?> avatar) {
                helper.register(new CosmeticLayer((RenderLayerParent<AvatarRenderState, PlayerModel>) avatar, context.getModelSet()));
            }
        });
    }
}
