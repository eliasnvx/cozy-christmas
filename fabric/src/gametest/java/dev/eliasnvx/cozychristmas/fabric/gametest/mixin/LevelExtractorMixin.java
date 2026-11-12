package dev.eliasnvx.cozychristmas.fabric.gametest.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.eliasnvx.cozychristmas.fabric.gametest.TestCamera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.extract.LevelExtractor;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Test only: vanilla never draws the local player while the camera sits in another entity (spectating). With
 * {@link TestCamera#showPlayer} on, the "is the camera this player" check says yes for the local player, so screenshot
 * tests can look at its head from any side through an armor stand's eyes.
 */
@Mixin(LevelExtractor.class)
public abstract class LevelExtractorMixin {

    @ModifyExpressionValue(method = "extractVisibleEntities", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/Camera;entity()Lnet/minecraft/world/entity/Entity;", ordinal = 3))
    private Entity cozychristmasTest$showLocalPlayer(Entity cameraEntity) {
        return TestCamera.showPlayer ? Minecraft.getInstance().player : cameraEntity;
    }
}
