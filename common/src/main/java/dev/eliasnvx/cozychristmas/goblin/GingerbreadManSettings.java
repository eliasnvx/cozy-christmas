package dev.eliasnvx.cozychristmas.goblin;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * The {@code goblins.gingerbread_man} config section: {@code health}, {@code attackDamage}, {@code speed}. With
 * {@code soggy} on, water hurts them like it hurts endermen (they're biscuit). Where they spawn (packs around goblin
 * camps) is the camp structure's spawn overrides, in data.
 */
public record GingerbreadManSettings(double health, double attackDamage, double speed, boolean soggy) {

    public static final GingerbreadManSettings DEFAULTS = new GingerbreadManSettings(6.0, 2.0, 0.36, true);

    public static final Codec<GingerbreadManSettings> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.doubleRange(1.0, 1024.0).fieldOf("health").orElse(DEFAULTS.health).forGetter(GingerbreadManSettings::health),
            Codec.doubleRange(0.0, 100.0).fieldOf("attack_damage").orElse(DEFAULTS.attackDamage).forGetter(GingerbreadManSettings::attackDamage),
            Codec.doubleRange(0.0, 1.0).fieldOf("speed").orElse(DEFAULTS.speed).forGetter(GingerbreadManSettings::speed),
            Codec.BOOL.fieldOf("soggy").orElse(DEFAULTS.soggy).forGetter(GingerbreadManSettings::soggy)
    ).apply(i, GingerbreadManSettings::new));
}
