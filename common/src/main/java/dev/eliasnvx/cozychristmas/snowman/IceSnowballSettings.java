package dev.eliasnvx.cozychristmas.snowman;

import com.mojang.serialization.Codec;
import dev.eliasnvx.cozychristmas.config.ConfigCodecs;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * The {@code evil_snowman.ice_snowball} config section, for every ice snowball, thrown by a snowman, a player or a
 * dispenser. A hit deals {@code damage}, gives Slowness ({@code slownessAmplifier} 0 = Slowness I) for
 * {@code slownessTicks} and adds {@code chillTicks} of powder-snow freezing, which a warm target (leather boots and
 * the like, the Toasty set) shrugs off. Freezing thaws 2 ticks a tick, so one snowman never freezes anyone solid; a few
 * of them can.
 */
public record IceSnowballSettings(double damage, int slownessTicks, int slownessAmplifier, int chillTicks) {

    public static final IceSnowballSettings DEFAULTS = new IceSnowballSettings(2.0, 80, 0, 60);

    public static final Codec<IceSnowballSettings> CODEC = RecordCodecBuilder.create(i -> i.group(
            ConfigCodecs.field(Codec.doubleRange(0.0, 100.0), "damage", DEFAULTS.damage)
                    .forGetter(IceSnowballSettings::damage),
            ConfigCodecs.field(Codec.intRange(0, 1200), "slowness_ticks", DEFAULTS.slownessTicks)
                    .forGetter(IceSnowballSettings::slownessTicks),
            ConfigCodecs.field(Codec.intRange(0, 4), "slowness_amplifier", DEFAULTS.slownessAmplifier)
                    .forGetter(IceSnowballSettings::slownessAmplifier),
            ConfigCodecs.field(Codec.intRange(0, 1000), "chill_ticks", DEFAULTS.chillTicks)
                    .forGetter(IceSnowballSettings::chillTicks)
    ).apply(i, IceSnowballSettings::new));
}
