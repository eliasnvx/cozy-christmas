package dev.eliasnvx.cozychristmas.mixin;

import net.minecraft.world.level.block.state.properties.WoodType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/** Wood types are only registered through a private method; both loaders take the same route. */
@Mixin(WoodType.class)
public interface WoodTypeInvoker {

    @Invoker("register")
    static WoodType cozychristmas$register(WoodType type) {
        throw new AssertionError();
    }
}
