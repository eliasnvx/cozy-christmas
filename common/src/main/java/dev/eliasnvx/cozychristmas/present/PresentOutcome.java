package dev.eliasnvx.cozychristmas.present;

import net.minecraft.util.StringRepresentable;

/** What a present holds, rolled when it's opened. */
public enum PresentOutcome implements StringRepresentable {
    /** A real gift from the present loot table. */
    LOOT("loot"),
    /** A lump of coal: Santa noticed. */
    COAL("coal"),
    /** Confetti, snow and a bang in your face. */
    BOOM("boom");

    private final String name;

    PresentOutcome(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
