package dev.eliasnvx.cozychristmas.goblin;

import com.mojang.serialization.Codec;
import dev.eliasnvx.cozychristmas.config.ConfigCodecs;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * The {@code goblins.present_goblin} config section. A present goblin has {@code health}, runs at {@code speed} and
 * hits for {@code attackDamage} when there's nothing to steal. With {@code steals} on, its attack on a player takes one
 * hotbar stack instead. Outside camps it spawns at night in {@code #cozychristmas:spawns_present_goblins} with
 * {@code spawnWeight} (0 = only in camps), in groups of {@code minGroup}..{@code maxGroup}.
 */
public record PresentGoblinSettings(double health, double attackDamage, double speed, boolean steals, int spawnWeight,
                                    int minGroup, int maxGroup) {

    public static final PresentGoblinSettings DEFAULTS = new PresentGoblinSettings(14.0, 2.0, 0.32, true, 6, 1, 2);

    public static final Codec<PresentGoblinSettings> CODEC = RecordCodecBuilder.<PresentGoblinSettings>create(i -> i.group(
            ConfigCodecs.field(Codec.doubleRange(1.0, 1024.0), "health", DEFAULTS.health)
                    .forGetter(PresentGoblinSettings::health),
            ConfigCodecs.field(Codec.doubleRange(0.0, 100.0), "attack_damage", DEFAULTS.attackDamage)
                    .forGetter(PresentGoblinSettings::attackDamage),
            ConfigCodecs.field(Codec.doubleRange(0.0, 1.0), "speed", DEFAULTS.speed)
                    .forGetter(PresentGoblinSettings::speed),
            ConfigCodecs.field(Codec.BOOL, "steals", DEFAULTS.steals).forGetter(PresentGoblinSettings::steals),
            ConfigCodecs.field(Codec.intRange(0, 1000), "spawn_weight", DEFAULTS.spawnWeight)
                    .forGetter(PresentGoblinSettings::spawnWeight),
            ConfigCodecs.field(Codec.intRange(1, 16), "min_group", DEFAULTS.minGroup)
                    .forGetter(PresentGoblinSettings::minGroup),
            ConfigCodecs.field(Codec.intRange(1, 16), "max_group", DEFAULTS.maxGroup)
                    .forGetter(PresentGoblinSettings::maxGroup)
    ).apply(i, PresentGoblinSettings::new)).validate(PresentGoblinSettings::validate);

    private static DataResult<PresentGoblinSettings> validate(PresentGoblinSettings settings) {
        return settings.minGroup <= settings.maxGroup
                ? DataResult.success(settings)
                : DataResult.error(() -> "min_group must not be above max_group");
    }
}
