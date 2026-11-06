package dev.eliasnvx.cozychristmas.reindeer;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

/** When a saddled reindeer can fly (config {@code reindeer.flight}). */
public enum FlightMode implements StringRepresentable {
    /** During the Christmas season only (the SPEC's default: flying is the seasonal magic). */
    SEASON("season"),
    /** All year. */
    ALWAYS("always"),
    /** Never: reindeer are ground mounts. */
    NEVER("never");

    public static final Codec<FlightMode> CODEC = StringRepresentable.fromEnum(FlightMode::values);

    private final String name;

    FlightMode(String name) {
        this.name = name;
    }

    /** Whether reindeer may fly now, given whether the Christmas season is on. */
    public boolean allows(boolean seasonActive) {
        return switch (this) {
            case SEASON -> seasonActive;
            case ALWAYS -> true;
            case NEVER -> false;
        };
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
