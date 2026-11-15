package dev.eliasnvx.cozychristmas.goblin;

import com.mojang.serialization.Codec;
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
            Codec.doubleRange(1.0, 1024.0).fieldOf("health").orElse(DEFAULTS.health).forGetter(PresentGoblinSettings::health),
            Codec.doubleRange(0.0, 100.0).fieldOf("attack_damage").orElse(DEFAULTS.attackDamage).forGetter(PresentGoblinSettings::attackDamage),
            Codec.doubleRange(0.0, 1.0).fieldOf("speed").orElse(DEFAULTS.speed).forGetter(PresentGoblinSettings::speed),
            Codec.BOOL.fieldOf("steals").orElse(DEFAULTS.steals).forGetter(PresentGoblinSettings::steals),
            Codec.intRange(0, 1000).fieldOf("spawn_weight").orElse(DEFAULTS.spawnWeight).forGetter(PresentGoblinSettings::spawnWeight),
            Codec.intRange(1, 16).fieldOf("min_group").orElse(DEFAULTS.minGroup).forGetter(PresentGoblinSettings::minGroup),
            Codec.intRange(1, 16).fieldOf("max_group").orElse(DEFAULTS.maxGroup).forGetter(PresentGoblinSettings::maxGroup)
    ).apply(i, PresentGoblinSettings::new)).validate(PresentGoblinSettings::validate);

    private static DataResult<PresentGoblinSettings> validate(PresentGoblinSettings settings) {
        return settings.minGroup <= settings.maxGroup
                ? DataResult.success(settings)
                : DataResult.error(() -> "min_group must not be above max_group");
    }
}
