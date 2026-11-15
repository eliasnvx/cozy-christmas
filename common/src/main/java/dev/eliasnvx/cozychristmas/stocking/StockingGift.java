package dev.eliasnvx.cozychristmas.stocking;

import dev.eliasnvx.cozychristmas.karma.KarmaTier;

/**
 * What a stocking holds in the morning, by its owner's karma: the nice get a present that is a gift, the neutral a
 * surprise present (their karma decides when it's opened), the naughty a lump of coal.
 */
public enum StockingGift {
    GIFT, SURPRISE, COAL;

    public static StockingGift forTier(KarmaTier tier) {
        return switch (tier) {
            case NICE -> GIFT;
            case NEUTRAL -> SURPRISE;
            case NAUGHTY -> COAL;
        };
    }

    public StockingFill fill() {
        return this == COAL ? StockingFill.COAL : StockingFill.PRESENT;
    }
}
