package dev.eliasnvx.cozychristmas.snowman;

import com.mojang.serialization.Codec;
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
            Codec.doubleRange(0.0, 100.0).fieldOf("damage").orElse(DEFAULTS.damage).forGetter(IceSnowballSettings::damage),
            Codec.intRange(0, 1200).fieldOf("slowness_ticks").orElse(DEFAULTS.slownessTicks).forGetter(IceSnowballSettings::slownessTicks),
            Codec.intRange(0, 4).fieldOf("slowness_amplifier").orElse(DEFAULTS.slownessAmplifier).forGetter(IceSnowballSettings::slownessAmplifier),
            Codec.intRange(0, 1000).fieldOf("chill_ticks").orElse(DEFAULTS.chillTicks).forGetter(IceSnowballSettings::chillTicks)
    ).apply(i, IceSnowballSettings::new));
}
