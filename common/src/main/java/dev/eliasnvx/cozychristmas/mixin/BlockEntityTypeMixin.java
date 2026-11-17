package dev.eliasnvx.cozychristmas.mixin;

import dev.eliasnvx.cozychristmas.wood.CozyWoods;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.BlockEntityTypes;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Signs of our woods are ordinary sign block entities: vanilla's sign types list their valid blocks once and for all,
 * so ours are let in here.
 */
@Mixin(BlockEntityType.class)
public abstract class BlockEntityTypeMixin {

    @Inject(method = "isValid", at = @At("HEAD"), cancellable = true)
    private void cozychristmas$ourSigns(BlockState state, CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this == BlockEntityTypes.SIGN ? CozyWoods.isSign(state.getBlock())
                : (Object) this == BlockEntityTypes.HANGING_SIGN && CozyWoods.isHangingSign(state.getBlock())) {
            cir.setReturnValue(true);
        }
    }
}
