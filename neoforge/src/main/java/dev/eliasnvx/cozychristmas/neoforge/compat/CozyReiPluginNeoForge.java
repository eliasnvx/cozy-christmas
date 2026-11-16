package dev.eliasnvx.cozychristmas.neoforge.compat;

import dev.eliasnvx.cozychristmas.compat.rei.CozyReiPlugin;
import me.shedaniel.rei.forge.REIPluginClient;

/** REI finds NeoForge plugins by this annotation, which only its NeoForge jar has. */
@REIPluginClient
public final class CozyReiPluginNeoForge extends CozyReiPlugin {
}
