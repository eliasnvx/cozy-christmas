package dev.eliasnvx.cozychristmas.fabric.gametest;

import dev.eliasnvx.cozychristmas.CozyChristmas;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

/** Test mod only: hands JEI's runtime to {@link CompatLookClientTest}, which opens its views for screenshots. */
public final class TestJeiPlugin implements IModPlugin {

    static volatile @Nullable IJeiRuntime runtime;

    @Override
    public Identifier getPluginUid() {
        return CozyChristmas.id("test_jei");
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
        runtime = jeiRuntime;
    }
}
