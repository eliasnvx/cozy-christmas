package dev.eliasnvx.cozychristmas.goblin;

import com.mojang.serialization.Codec;
import dev.eliasnvx.cozychristmas.config.ConfigCodecs;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * The {@code goblins.gingerbread_man} config section: {@code health}, {@code attackDamage}, {@code speed}. With
 * {@code soggy} on, water hurts them like it hurts endermen (they're biscuit). Where they spawn (packs around goblin
 * camps) is the camp structure's spawn overrides, in data.
 */
public record GingerbreadManSettings(double health, double attackDamage, double speed, boolean soggy) {

    public static final GingerbreadManSettings DEFAULTS = new GingerbreadManSettings(6.0, 2.0, 0.36, true);

    public static final Codec<GingerbreadManSettings> CODEC = RecordCodecBuilder.create(i -> i.group(
            ConfigCodecs.field(Codec.doubleRange(1.0, 1024.0), "health", DEFAULTS.health)
                    .forGetter(GingerbreadManSettings::health),
            ConfigCodecs.field(Codec.doubleRange(0.0, 100.0), "attack_damage", DEFAULTS.attackDamage)
                    .forGetter(GingerbreadManSettings::attackDamage),
            ConfigCodecs.field(Codec.doubleRange(0.0, 1.0), "speed", DEFAULTS.speed)
                    .forGetter(GingerbreadManSettings::speed),
            ConfigCodecs.field(Codec.BOOL, "soggy", DEFAULTS.soggy).forGetter(GingerbreadManSettings::soggy)
    ).apply(i, GingerbreadManSettings::new));
}
