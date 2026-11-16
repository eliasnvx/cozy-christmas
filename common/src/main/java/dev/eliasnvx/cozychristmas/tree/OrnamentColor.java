package dev.eliasnvx.cozychristmas.tree;

import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.material.MapColor;

import java.util.Locale;

/** The ornaments' glass colors and the dye each is made with. One block per color: {@code red_ornament}… */
public enum OrnamentColor {
    RED(DyeColor.RED, MapColor.COLOR_RED),
    GOLD(DyeColor.YELLOW, MapColor.GOLD),
    SILVER(DyeColor.LIGHT_GRAY, MapColor.METAL),
    BLUE(DyeColor.BLUE, MapColor.COLOR_BLUE),
    GREEN(DyeColor.GREEN, MapColor.COLOR_GREEN),
    PURPLE(DyeColor.PURPLE, MapColor.COLOR_PURPLE);

    private final DyeColor dye;
    private final MapColor mapColor;
    private final String blockName = name().toLowerCase(Locale.ROOT) + "_ornament";

    OrnamentColor(DyeColor dye, MapColor mapColor) {
        this.dye = dye;
        this.mapColor = mapColor;
    }

    public DyeColor dye() {
        return dye;
    }

    public MapColor mapColor() {
        return mapColor;
    }

    /** {@code red_ornament}: the block's and the item's id path. */
    public String blockName() {
        return blockName;
    }
}
