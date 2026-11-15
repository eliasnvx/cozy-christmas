package dev.eliasnvx.cozychristmas.stocking;

import net.minecraft.util.StringRepresentable;

import java.util.Locale;

/**
 * How a hung stocking looks (block state {@code style}): our Christmas stocking, or a striped sock for anything hung
 * from {@code #cozychristmas:hangs_as_stocking} (Femboy Mod's socks).
 */
public enum StockingStyle implements StringRepresentable {
    STOCKING, SOCK;

    private final String name = name().toLowerCase(Locale.ROOT);

    @Override
    public String getSerializedName() {
        return name;
    }
}
