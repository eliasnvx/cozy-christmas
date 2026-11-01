package dev.eliasnvx.cozychristmas.season;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

/** Where the Christmas season comes from. Serene Seasons joins here once its compat lands. */
public enum SeasonSource implements StringRepresentable {
    /** The server's real calendar date inside {@link SeasonSettings#season()}. */
    REAL_DATE("real_date"),
    /** Christmas all year (servers and showcases). */
    ALWAYS("always"),
    /** Never: the seasonal magic is off, everything else still works. */
    NEVER("never");

    public static final Codec<SeasonSource> CODEC = StringRepresentable.fromEnum(SeasonSource::values);

    private final String name;

    SeasonSource(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
