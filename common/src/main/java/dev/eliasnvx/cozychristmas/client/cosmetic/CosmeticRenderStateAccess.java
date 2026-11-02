package dev.eliasnvx.cozychristmas.client.cosmetic;

import org.jetbrains.annotations.Nullable;

/** Duck interface mixed into AvatarRenderState. */
public interface CosmeticRenderStateAccess {

    @Nullable CosmeticRenderData cozychristmas$getCosmetics();

    void cozychristmas$setCosmetics(@Nullable CosmeticRenderData data);
}
