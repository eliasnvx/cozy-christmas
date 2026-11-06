package dev.eliasnvx.cozychristmas.reindeer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * The {@code reindeer} config section. A reindeer has {@code health}, walks at {@code speed} and jumps with
 * {@code jumpStrength}. Each carrot fed to a wild one tames it with {@code tameChance}. Saddled and ridden it flies when
 * {@code flight} allows: {@code flySpeed} is added per tick toward where the rider steers (the air drag makes about ten
 * times that the top speed), holding jump climbs at {@code climbSpeed} blocks per tick. Wild herds of
 * {@code minGroup}..{@code maxGroup} spawn in {@code #cozychristmas:spawns_reindeer} with {@code spawnWeight} (0 = off).
 */
public record ReindeerSettings(FlightMode flight, double flySpeed, double climbSpeed, double health, double speed,
                               double jumpStrength, double tameChance, int spawnWeight, int minGroup, int maxGroup) {

    public static final ReindeerSettings DEFAULTS = new ReindeerSettings(FlightMode.SEASON, 0.07, 0.3, 24.0, 0.25, 0.6,
            0.33, 2, 2, 3);

    public static final Codec<ReindeerSettings> CODEC = RecordCodecBuilder.<ReindeerSettings>create(i -> i.group(
            FlightMode.CODEC.fieldOf("flight").orElse(DEFAULTS.flight).forGetter(ReindeerSettings::flight),
            Codec.doubleRange(0.0, 0.5).fieldOf("fly_speed").orElse(DEFAULTS.flySpeed).forGetter(ReindeerSettings::flySpeed),
            Codec.doubleRange(0.0, 1.0).fieldOf("climb_speed").orElse(DEFAULTS.climbSpeed).forGetter(ReindeerSettings::climbSpeed),
            Codec.doubleRange(1.0, 1024.0).fieldOf("health").orElse(DEFAULTS.health).forGetter(ReindeerSettings::health),
            Codec.doubleRange(0.0, 1.0).fieldOf("speed").orElse(DEFAULTS.speed).forGetter(ReindeerSettings::speed),
            Codec.doubleRange(0.0, 2.0).fieldOf("jump_strength").orElse(DEFAULTS.jumpStrength).forGetter(ReindeerSettings::jumpStrength),
            Codec.doubleRange(0.0, 1.0).fieldOf("tame_chance").orElse(DEFAULTS.tameChance).forGetter(ReindeerSettings::tameChance),
            Codec.intRange(0, 1000).fieldOf("spawn_weight").orElse(DEFAULTS.spawnWeight).forGetter(ReindeerSettings::spawnWeight),
            Codec.intRange(1, 16).fieldOf("min_group").orElse(DEFAULTS.minGroup).forGetter(ReindeerSettings::minGroup),
            Codec.intRange(1, 16).fieldOf("max_group").orElse(DEFAULTS.maxGroup).forGetter(ReindeerSettings::maxGroup)
    ).apply(i, ReindeerSettings::new)).validate(ReindeerSettings::validate);

    private static DataResult<ReindeerSettings> validate(ReindeerSettings settings) {
        return settings.minGroup <= settings.maxGroup
                ? DataResult.success(settings)
                : DataResult.error(() -> "min_group must not be above max_group");
    }
}
