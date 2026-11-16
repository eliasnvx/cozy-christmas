package dev.eliasnvx.cozychristmas.krampus;

import dev.eliasnvx.cozychristmas.registry.CozyEffects;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * In Krampus's sack (SPEC: "puts you in his sack, 2 seconds unable to move"): no walking, no jumping, no getting on
 * a mount or a boat, no gliding off. The sack itself is a {@link KrampusSack} entity over the victim, so everyone sees
 * it; the victim sees burlap. 1.20.1 has no jump strength attribute for every entity (only the horses'), so the jump is
 * taken away by {@code LivingEntityMixin} through {@link #blocksJump}.
 */
public class SackedEffect extends MobEffect {

    public static final int COLOR = 0x8A6A3C;
    /** 1.20.1 attribute modifiers are keyed by UUID: stable ones, from 26.3's modifier ids. */
    private static final String SPEED_MODIFIER = modifierId("cozychristmas:sacked_speed");

    public SackedEffect() {
        super(MobEffectCategory.HARMFUL, COLOR);
        addAttributeModifier(Attributes.MOVEMENT_SPEED, SPEED_MODIFIER, -1.0, AttributeModifier.Operation.MULTIPLY_TOTAL);
    }

    private static String modifierId(String name) {
        return UUID.nameUUIDFromBytes(name.getBytes(StandardCharsets.UTF_8)).toString();
    }

    /** Whether {@code entity} can't jump now: it's in the sack. Both sides: a player's client jumps for them. */
    public static boolean blocksJump(LivingEntity entity) {
        return CozyEffects.SACKED.isPresent() && entity.hasEffect(CozyEffects.SACKED.get());
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (!(entity.level() instanceof ServerLevel)) {
            return; // 1.20.1 ticks effects on the client too: the server decides, riding and gliding are synced
        }
        if (entity.isPassenger()) {
            entity.stopRiding();
        }
        // on 1.20.1 only players glide (the elytra flag is set by the player's packet), and only they can stop it
        if (entity instanceof Player player && player.isFallFlying()) {
            player.stopFallFlying();
        }
    }
}
