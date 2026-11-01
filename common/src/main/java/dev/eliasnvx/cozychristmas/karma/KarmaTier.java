package dev.eliasnvx.cozychristmas.karma;

import net.minecraft.util.StringRepresentable;

/** What Santa thinks of a player. Thresholds come from {@link KarmaSettings#tier(int)}. */
public enum KarmaTier implements StringRepresentable {
    NAUGHTY("naughty"),
    NEUTRAL("neutral"),
    NICE("nice");

    private final String name;

    KarmaTier(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }

    public String translationKey() {
        return "karma.cozychristmas.tier." + name;
    }
}
