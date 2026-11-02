package dev.eliasnvx.cozychristmas.mixin.client;

import dev.eliasnvx.cozychristmas.client.cosmetic.CosmeticRenderData;
import dev.eliasnvx.cozychristmas.client.cosmetic.CosmeticRenderStateAccess;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(AvatarRenderState.class)
public abstract class AvatarRenderStateMixin implements CosmeticRenderStateAccess {

    @Unique
    private @Nullable CosmeticRenderData cozychristmas$cosmetics;

    @Override
    public @Nullable CosmeticRenderData cozychristmas$getCosmetics() {
        return cozychristmas$cosmetics;
    }

    @Override
    public void cozychristmas$setCosmetics(@Nullable CosmeticRenderData data) {
        cozychristmas$cosmetics = data;
    }
}
