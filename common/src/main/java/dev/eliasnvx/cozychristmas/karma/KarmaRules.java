package dev.eliasnvx.cozychristmas.karma;

import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.EntityEvent;
import dev.eliasnvx.cozychristmas.config.CozyConfig;
import dev.eliasnvx.cozychristmas.registry.CozyTags;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * Deeds that move karma. Which mobs count is the entity tag {@code #cozychristmas:karma_protected}, so packs decide;
 * the amounts are in the config. The killing blow is a hit too, so a kill costs both. More rules (gifts, feeding
 * reindeer, the workshop) live with their features.
 */
public final class KarmaRules {

    /**
     * Game time each victim was last charged for. The hurt event fires on every hit attempt, also during invulnerability
     * frames, so without it a fast clicker would pay many times for one blow. Server thread only.
     */
    private static final Map<LivingEntity, Long> LAST_CHARGED = new WeakHashMap<>();

    private KarmaRules() {
    }

    public static void register() {
        EntityEvent.LIVING_HURT.register(KarmaRules::onHurt);
        EntityEvent.LIVING_DEATH.register(KarmaRules::onDeath);
    }

    private static EventResult onHurt(LivingEntity victim, DamageSource source, float amount) {
        ServerPlayer player = culprit(victim, source);
        if (player != null && amount > 0 && takeCharge(victim)) {
            KarmaManager.add(player, CozyConfig.common().karma().hurtProtected());
        }
        return EventResult.pass();
    }

    private static EventResult onDeath(LivingEntity victim, DamageSource source) {
        ServerPlayer player = culprit(victim, source);
        if (player != null) {
            KarmaManager.add(player, CozyConfig.common().karma().killProtected());
            LAST_CHARGED.remove(victim);
        }
        return EventResult.pass();
    }

    /** The player to blame for hurting a protected mob, or null. A projectile's damage counts as its shooter's deed. */
    private static @Nullable ServerPlayer culprit(LivingEntity victim, DamageSource source) {
        if (victim.level().isClientSide() || !victim.is(CozyTags.KARMA_PROTECTED)) {
            return null;
        }
        return source.getEntity() instanceof ServerPlayer player ? player : null;
    }

    private static boolean takeCharge(LivingEntity victim) {
        long now = victim.level().getGameTime();
        Long last = LAST_CHARGED.get(victim);
        if (last != null && now - last < CozyConfig.common().karma().hurtCooldownTicks()) {
            return false;
        }
        LAST_CHARGED.put(victim, now);
        return true;
    }
}
