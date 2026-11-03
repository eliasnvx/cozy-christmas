package dev.eliasnvx.cozychristmas.registry;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import dev.eliasnvx.cozychristmas.CozyChristmas;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;

public final class CozyParticles {

    public static final DeferredRegister<ParticleType<?>> REGISTER = DeferredRegister.create(CozyChristmas.MOD_ID, Registries.PARTICLE_TYPE);

    /** Paper confetti from a present that went boom: colored flakes that tumble and drift down. */
    public static final RegistrySupplier<SimpleParticleType> CONFETTI = REGISTER.register("confetti", () -> simple(false));

    private CozyParticles() {
    }

    /** Its constructor is protected (vanilla calls it from its own package), so an empty subclass. */
    private static SimpleParticleType simple(boolean overrideLimiter) {
        return new SimpleParticleType(overrideLimiter) {
        };
    }
}
