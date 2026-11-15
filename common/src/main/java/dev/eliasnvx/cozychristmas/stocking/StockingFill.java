package dev.eliasnvx.cozychristmas.stocking;

import net.minecraft.util.StringRepresentable;

import java.util.Locale;

/** What sticks out of a stocking's top (block state {@code fill}): nothing, a present or coal. */
public enum StockingFill implements StringRepresentable {
    EMPTY, PRESENT, COAL;

    private final String name = name().toLowerCase(Locale.ROOT);

    @Override
    public String getSerializedName() {
        return name;
    }
}
