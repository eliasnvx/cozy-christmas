package dev.eliasnvx.cozychristmas.compat.catgirl;

import dev.architectury.platform.Platform;
import dev.eliasnvx.cozychristmas.CozyChristmas;
import dev.eliasnvx.cozychristmas.cosmetic.menu.CosmeticsLayout;

/**
 * Catgirl &amp; Catboy Mod puts its own cosmetics panel right of the survival inventory and the creative inventory tab,
 * in the same spot as ours (both come from the same code). With it loaded, ours moves right of its panel. Decided once
 * from which mods are loaded, so the server's menu and the client's screen agree on where our slots are.
 */
public final class CatgirlPanel {

    /** Catgirl's {@code CosmeticsLayout.WIDTH} when it can't be read (padding 7, two columns of 18). */
    private static final int FALLBACK_WIDTH = 50;
    private static final int OFFSET = Platform.isModLoaded(CatgirlEars.MOD_ID) ? width() + CosmeticsLayout.GAP : 0;

    private CatgirlPanel() {
    }

    /** How far right of where it would be our panel starts: past Catgirl's panel and a gap, or 0 without it. */
    public static int offset() {
        return OFFSET;
    }

    private static int width() {
        try {
            return Class.forName("dev.eliasnvx.catgirl.menu.CosmeticsLayout", false, CatgirlPanel.class.getClassLoader())
                    .getField("WIDTH").getInt(null);
        } catch (ReflectiveOperationException e) {
            CozyChristmas.LOGGER.warn("Catgirl & Catboy Mod's panel width couldn't be read, assuming {}", FALLBACK_WIDTH, e);
            return FALLBACK_WIDTH;
        }
    }
}
