package dev.eliasnvx.cozychristmas.forge.compat;

import dev.eliasnvx.cozychristmas.compat.rei.CozyReiPlugin;
import me.shedaniel.rei.forge.REIPluginClient;

/** REI finds Forge plugins by this annotation, which only its Forge jar has. */
@REIPluginClient
public final class CozyReiPluginForge extends CozyReiPlugin {
}
