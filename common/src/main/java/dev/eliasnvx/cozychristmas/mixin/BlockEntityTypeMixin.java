package dev.eliasnvx.cozychristmas.mixin;

import dev.eliasnvx.cozychristmas.wood.CozyWoods;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.BlockEntityTypes;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Signs and shelves of our woods are ordinary block entities: vanilla's types list their valid blocks once and for all,
 * so ours are let in here.
 */
@Mixin(BlockEntityType.class)
public abstract class BlockEntityTypeMixin {

    @Inject(method = "isValid", at = @At("HEAD"), cancellable = true)
    private void cozychristmas$ourSigns(BlockState state, CallbackInfoReturnable<Boolean> cir) {
        Block block = state.getBlock();
        if ((Object) this == BlockEntityTypes.SIGN ? CozyWoods.isSign(block)
                : (Object) this == BlockEntityTypes.HANGING_SIGN ? CozyWoods.isHangingSign(block)
                : (Object) this == BlockEntityTypes.SHELF && CozyWoods.isShelf(block)) {
            cir.setReturnValue(true);
        }
    }
}
