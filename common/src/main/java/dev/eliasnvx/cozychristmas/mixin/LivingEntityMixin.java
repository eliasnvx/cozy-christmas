package dev.eliasnvx.cozychristmas.mixin;

import dev.eliasnvx.cozychristmas.cold.Cold;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Warm entities don't freeze, like with leather armor ({@code #minecraft:freeze_immune_wearables}): vanilla stops
 * adding freeze ticks in powder snow and thaws what's there. Players don't override {@code canFreeze}.
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    @Inject(method = "canFreeze", at = @At("HEAD"), cancellable = true)
    private void cozychristmas$keptWarm(CallbackInfoReturnable<Boolean> cir) {
        if (Cold.isKeptWarm((LivingEntity) (Object) this)) {
            cir.setReturnValue(false);
        }
    }
}
