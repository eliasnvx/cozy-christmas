package dev.eliasnvx.cozychristmas.mixin;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FireBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/** Fire spreads to and burns blocks listed in FireBlock; the registering method is private. */
@Mixin(FireBlock.class)
public interface FireBlockInvoker {

    @Invoker("setFlammable")
    void cozychristmas$setFlammable(Block block, int igniteOdds, int burnOdds);
}
