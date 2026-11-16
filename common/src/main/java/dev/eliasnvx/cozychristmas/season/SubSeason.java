package dev.eliasnvx.cozychristmas.season;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

import java.util.Locale;

/** Serene Seasons' sub-seasons, by the same names, so the config and the rules don't need the mod. */
public enum SubSeason implements StringRepresentable {
    EARLY_SPRING, MID_SPRING, LATE_SPRING,
    EARLY_SUMMER, MID_SUMMER, LATE_SUMMER,
    EARLY_AUTUMN, MID_AUTUMN, LATE_AUTUMN,
    EARLY_WINTER, MID_WINTER, LATE_WINTER;

    public static final Codec<SubSeason> CODEC = StringRepresentable.fromEnum(SubSeason::values);

    private final String name = name().toLowerCase(Locale.ROOT);

    @Override
    public String getSerializedName() {
        return name;
    }
}
