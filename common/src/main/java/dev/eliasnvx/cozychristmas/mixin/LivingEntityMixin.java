package dev.eliasnvx.cozychristmas.mixin;

import dev.eliasnvx.cozychristmas.cold.Cold;
import dev.eliasnvx.cozychristmas.krampus.SackedEffect;
import dev.eliasnvx.cozychristmas.reindeer.RiderJumpInput;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Warm entities don't freeze, like with leather armor ({@code #minecraft:freeze_immune_wearables}): vanilla stops
 * adding freeze ticks in powder snow and thaws what's there. Players don't override {@code canFreeze}.
 * Also reads the jump input, which 1.20.1 keeps without a getter: a flying reindeer climbs while its rider holds jump.
 * And whoever is in Krampus's sack can't jump: 1.20.1 has no jump strength attribute for {@link SackedEffect} to lower.
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin implements RiderJumpInput {

    @Shadow
    protected boolean jumping;

    @Override
    public boolean cozychristmas$isJumping() {
        return jumping;
    }

    @Inject(method = "canFreeze", at = @At("HEAD"), cancellable = true)
    private void cozychristmas$keptWarm(CallbackInfoReturnable<Boolean> cir) {
        if (Cold.isKeptWarm((LivingEntity) (Object) this)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "getJumpPower", at = @At("HEAD"), cancellable = true)
    private void cozychristmas$sackedJump(CallbackInfoReturnable<Float> cir) {
        if (SackedEffect.blocksJump((LivingEntity) (Object) this)) {
            cir.setReturnValue(0.0F);
        }
    }
}
