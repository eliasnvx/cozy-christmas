package dev.eliasnvx.cozychristmas.present;

import net.minecraft.util.StringRepresentable;

import java.util.Locale;

/** Wrapping paper and ribbon of a present: a block state, so each design is its own model and texture. */
public enum PresentDesign implements StringRepresentable {
    /** Red paper, gold ribbon. */
    RED,
    /** Green paper, red ribbon. */
    GREEN,
    /** Blue paper, white ribbon. */
    BLUE,
    /** Gold paper, red ribbon. */
    GOLD;

    private final String name = name().toLowerCase(Locale.ROOT);

    @Override
    public String getSerializedName() {
        return name;
    }
}
