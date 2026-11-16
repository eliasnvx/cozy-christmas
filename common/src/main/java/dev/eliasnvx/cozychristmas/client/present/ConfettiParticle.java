package dev.eliasnvx.cozychristmas.client.present;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;

/**
 * A flake of paper confetti: thrown out fast, slowed hard by the air, then it tumbles and sways down. The sprites are
 * white shapes tinted with a festive color each, so one texture set gives every color.
 */
public class ConfettiParticle extends TextureSheetParticle {

    private static final int[] COLORS = {0xE8323C, 0xF2C230, 0x2FA84F, 0x2F7DE1, 0xF27AB8, 0xFFFFFF, 0x9B59D9};
    private static final float CHANNEL = 255.0F;
    private static final float GRAVITY = 0.25F;
    private static final float AIR_DRAG = 0.88F;
    private static final int MIN_LIFETIME = 60;
    private static final int EXTRA_LIFETIME = 40;
    private static final int FADE_TICKS = 10;
    private static final float MIN_SIZE = 0.05F;
    private static final float EXTRA_SIZE = 0.035F;
    private static final float MAX_SPIN = 0.35F;
    private static final double SWAY = 0.004;

    private final float spin;
    private final float swayPhase;

    ConfettiParticle(ClientLevel level, double x, double y, double z, double xd, double yd, double zd, SpriteSet sprites) {
        super(level, x, y, z);
        // the velocity constructor would randomize and renormalize the burst; take it as sent
        this.xd = xd;
        this.yd = yd;
        this.zd = zd;
        pickSprite(sprites);
        this.gravity = GRAVITY;
        this.friction = AIR_DRAG;
        this.lifetime = MIN_LIFETIME + random.nextInt(EXTRA_LIFETIME);
        this.quadSize = MIN_SIZE + random.nextFloat() * EXTRA_SIZE;
        int color = COLORS[random.nextInt(COLORS.length)];
        setColor(FastColor.ARGB32.red(color) / CHANNEL, FastColor.ARGB32.green(color) / CHANNEL,
                FastColor.ARGB32.blue(color) / CHANNEL);
        this.roll = random.nextFloat() * Mth.TWO_PI;
        this.oRoll = roll;
        this.spin = (random.nextFloat() * 2.0F - 1.0F) * MAX_SPIN;
        this.swayPhase = random.nextFloat() * Mth.TWO_PI;
    }

    @Override
    public void tick() {
        super.tick();
        oRoll = roll;
        if (onGround) {
            return; // landed flat: it stops turning and swaying
        }
        roll += spin;
        xd += Mth.sin(age * 0.25F + swayPhase) * SWAY;
        zd += Mth.cos(age * 0.21F + swayPhase) * SWAY;
        int left = lifetime - age;
        if (left < FADE_TICKS) {
            setAlpha(left / (float) FADE_TICKS);
        }
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public record Provider(SpriteSet sprites) implements ParticleProvider<SimpleParticleType> {
        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z,
                                       double xd, double yd, double zd) {
            return new ConfettiParticle(level, x, y, z, xd, yd, zd, sprites);
        }
    }
}
