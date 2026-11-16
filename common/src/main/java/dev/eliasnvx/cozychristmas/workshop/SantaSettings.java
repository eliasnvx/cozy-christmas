package dev.eliasnvx.cozychristmas.workshop;

import com.mojang.serialization.Codec;
import dev.eliasnvx.cozychristmas.config.ConfigCodecs;
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
            ConfigCodecs.field(Codec.doubleRange(1.0, 1024.0), "health", DEFAULTS.health)
                    .forGetter(SantaSettings::health),
            ConfigCodecs.field(Codec.doubleRange(0.0, 2048.0), "attack_damage", DEFAULTS.attackDamage)
                    .forGetter(SantaSettings::attackDamage),
            ConfigCodecs.field(Codec.doubleRange(0.0, 1.0), "speed", DEFAULTS.speed).forGetter(SantaSettings::speed),
            ConfigCodecs.field(Codec.intRange(0, 72000), "anger_ticks", DEFAULTS.angerTicks)
                    .forGetter(SantaSettings::angerTicks),
            ConfigCodecs.field(Codec.intRange(0, 128), "alarm_radius", DEFAULTS.alarmRadius)
                    .forGetter(SantaSettings::alarmRadius),
            ConfigCodecs.field(Codec.doubleRange(0.0, 1.0), "nice_discount", DEFAULTS.niceDiscount)
                    .forGetter(SantaSettings::niceDiscount),
            ConfigCodecs.field(Codec.BOOL, "daily_gifts", DEFAULTS.dailyGifts).forGetter(SantaSettings::dailyGifts)
    ).apply(i, SantaSettings::new));

    /** How many fewer items a nice player pays for a price of {@code price} (the merchant keeps every price at 1+). */
    public int discount(int price) {
        return Mth.floor(price * niceDiscount);
    }
}
