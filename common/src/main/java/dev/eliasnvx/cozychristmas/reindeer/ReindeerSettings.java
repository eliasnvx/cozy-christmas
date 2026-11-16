package dev.eliasnvx.cozychristmas.reindeer;

import com.mojang.serialization.Codec;
import dev.eliasnvx.cozychristmas.config.ConfigCodecs;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.ExtraCodecs;

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

    public static final Codec<ReindeerSettings> CODEC = ExtraCodecs.validate(RecordCodecBuilder.<ReindeerSettings>create(i -> i.group(
            ConfigCodecs.field(FlightMode.CODEC, "flight", DEFAULTS.flight).forGetter(ReindeerSettings::flight),
            ConfigCodecs.field(Codec.doubleRange(0.0, 0.5), "fly_speed", DEFAULTS.flySpeed)
                    .forGetter(ReindeerSettings::flySpeed),
            ConfigCodecs.field(Codec.doubleRange(0.0, 1.0), "climb_speed", DEFAULTS.climbSpeed)
                    .forGetter(ReindeerSettings::climbSpeed),
            ConfigCodecs.field(Codec.doubleRange(1.0, 1024.0), "health", DEFAULTS.health)
                    .forGetter(ReindeerSettings::health),
            ConfigCodecs.field(Codec.doubleRange(0.0, 1.0), "speed", DEFAULTS.speed).forGetter(ReindeerSettings::speed),
            ConfigCodecs.field(Codec.doubleRange(0.0, 2.0), "jump_strength", DEFAULTS.jumpStrength)
                    .forGetter(ReindeerSettings::jumpStrength),
            ConfigCodecs.field(Codec.doubleRange(0.0, 1.0), "tame_chance", DEFAULTS.tameChance)
                    .forGetter(ReindeerSettings::tameChance),
            ConfigCodecs.field(Codec.intRange(0, 1000), "spawn_weight", DEFAULTS.spawnWeight)
                    .forGetter(ReindeerSettings::spawnWeight),
            ConfigCodecs.field(Codec.intRange(1, 16), "min_group", DEFAULTS.minGroup)
                    .forGetter(ReindeerSettings::minGroup),
            ConfigCodecs.field(Codec.intRange(1, 16), "max_group", DEFAULTS.maxGroup)
                    .forGetter(ReindeerSettings::maxGroup)
    ).apply(i, ReindeerSettings::new)), ReindeerSettings::validate);

    private static DataResult<ReindeerSettings> validate(ReindeerSettings settings) {
        return settings.minGroup <= settings.maxGroup
                ? DataResult.success(settings)
                : DataResult.error(() -> "min_group must not be above max_group");
    }
}
