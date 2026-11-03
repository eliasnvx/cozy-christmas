package dev.eliasnvx.cozychristmas.present;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

/** What a surprise present holds, rolled by karma when someone first comes close or opens it. */
public enum PresentOutcome implements StringRepresentable {
    /** A real gift from the present loot table. */
    LOOT("loot"),
    /** A lump of coal: Santa noticed. */
    COAL("coal"),
    /** Confetti, snow and a bang in your face. */
    BOOM("boom");

    public static final Codec<PresentOutcome> CODEC = StringRepresentable.fromEnum(PresentOutcome::values);

    private final String name;

    PresentOutcome(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
