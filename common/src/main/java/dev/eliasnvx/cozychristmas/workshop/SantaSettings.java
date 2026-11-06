package dev.eliasnvx.cozychristmas.workshop;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Mth;

/**
 * The {@code workshop.santa} config section. Santa has {@code health}, hits for {@code attackDamage} and walks at
 * {@code speed}. When someone hurts him or an elf, or steals from the warehouse, every Santa within
 * {@code alarmRadius} blocks goes after them for {@code angerTicks}. Nice players pay {@code niceDiscount} less (0.25 =
 * a quarter off, rounded down); in season they also get one present a day if {@code dailyGifts} is on.
 */
public record SantaSettings(double health, double attackDamage, double speed, int angerTicks, int alarmRadius,
                            double niceDiscount, boolean dailyGifts) {

    public static final SantaSettings DEFAULTS = new SantaSettings(80.0, 8.0, 0.3, 1200, 32, 0.25, true);

    public static final Codec<SantaSettings> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.doubleRange(1.0, 1024.0).fieldOf("health").orElse(DEFAULTS.health).forGetter(SantaSettings::health),
            Codec.doubleRange(0.0, 2048.0).fieldOf("attack_damage").orElse(DEFAULTS.attackDamage).forGetter(SantaSettings::attackDamage),
            Codec.doubleRange(0.0, 1.0).fieldOf("speed").orElse(DEFAULTS.speed).forGetter(SantaSettings::speed),
            Codec.intRange(0, 72000).fieldOf("anger_ticks").orElse(DEFAULTS.angerTicks).forGetter(SantaSettings::angerTicks),
            Codec.intRange(0, 128).fieldOf("alarm_radius").orElse(DEFAULTS.alarmRadius).forGetter(SantaSettings::alarmRadius),
            Codec.doubleRange(0.0, 1.0).fieldOf("nice_discount").orElse(DEFAULTS.niceDiscount).forGetter(SantaSettings::niceDiscount),
            Codec.BOOL.fieldOf("daily_gifts").orElse(DEFAULTS.dailyGifts).forGetter(SantaSettings::dailyGifts)
    ).apply(i, SantaSettings::new));

    /** How many fewer items a nice player pays for a price of {@code price} (the merchant keeps every price at 1+). */
    public int discount(int price) {
        return Mth.floor(price * niceDiscount);
    }
}
