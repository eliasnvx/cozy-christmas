package dev.eliasnvx.cozychristmas.mixin;

import net.minecraft.world.level.block.state.properties.BlockSetType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/** Block set types (doors, buttons, plates) are only registered through a private method. */
@Mixin(BlockSetType.class)
public interface BlockSetTypeInvoker {

    @Invoker("register")
    static BlockSetType cozychristmas$register(BlockSetType type) {
        throw new AssertionError();
    }
}
