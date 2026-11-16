package dev.eliasnvx.cozychristmas.present;

import com.mojang.serialization.Codec;
import dev.eliasnvx.cozychristmas.config.ConfigCodecs;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;

/**
 * The {@code presents.boom} config section. A boom hurts everything within {@code radius} blocks: {@code maxDamage}
 * at the present, falling to {@code minDamage} at the edge (SPEC: 4-6), less behind cover. It pushes them away
 * ({@code knockback}, 1 = a vanilla explosion's push) and leaves blocks alone unless {@code breaksBlocks}.
 */
public record BoomSettings(float minDamage, float maxDamage, double radius, float knockback, boolean breaksBlocks) {

    public static final BoomSettings DEFAULTS = new BoomSettings(4.0F, 6.0F, 3.0, 1.0F, false);

    private static final Codec<Float> DAMAGE = Codec.floatRange(0.0F, 100.0F);

    public static final Codec<BoomSettings> CODEC = ExtraCodecs.validate(RecordCodecBuilder.<BoomSettings>create(i -> i.group(
            ConfigCodecs.field(DAMAGE, "min_damage", DEFAULTS.minDamage).forGetter(BoomSettings::minDamage),
            ConfigCodecs.field(DAMAGE, "max_damage", DEFAULTS.maxDamage).forGetter(BoomSettings::maxDamage),
            ConfigCodecs.field(Codec.doubleRange(1.0, 8.0), "radius", DEFAULTS.radius).forGetter(BoomSettings::radius),
            ConfigCodecs.field(Codec.floatRange(0.0F, 4.0F), "knockback", DEFAULTS.knockback)
                    .forGetter(BoomSettings::knockback),
            ConfigCodecs.field(Codec.BOOL, "breaks_blocks", DEFAULTS.breaksBlocks).forGetter(BoomSettings::breaksBlocks)
    ).apply(i, BoomSettings::new)), BoomSettings::validate);

    private static DataResult<BoomSettings> validate(BoomSettings settings) {
        return settings.minDamage <= settings.maxDamage
                ? DataResult.success(settings)
                : DataResult.error(() -> "min_damage must not be above max_damage");
    }

    /** Damage at {@code distance} blocks from the present, before cover: max at 0, min at the radius, 0 beyond. */
    public float damageAt(double distance) {
        if (distance > radius) {
            return 0.0F;
        }
        return (float) Mth.lerp(distance / radius, maxDamage, minDamage);
    }
}
