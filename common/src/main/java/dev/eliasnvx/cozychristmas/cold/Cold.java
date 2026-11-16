package dev.eliasnvx.cozychristmas.cold;

import dev.eliasnvx.cozychristmas.cosmetic.ToastySet;
import dev.eliasnvx.cozychristmas.registry.CozyEffects;
import net.minecraft.world.entity.LivingEntity;

/**
 * The one place the mod asks "is this entity kept warm?" and makes things cold. Each Minecraft version maps it to its
 * own cold: on 26.3 and the backports that's freezing in powder snow ({@code LivingEntityMixin} on {@code canFreeze});
 * 26.4 adds the vanilla Freezing effect, which gets wired here too. Warm things (the Toasty set, hot cocoa's warmth) and
 * cold things (ice snowballs) only plug in here.
 */
public final class Cold {

    private Cold() {
    }

    public static boolean isKeptWarm(LivingEntity entity) {
        return ToastySet.keepsWarm(entity) || entity.hasEffect(CozyEffects.WARM.get());
    }

    /**
     * Adds {@code ticks} of powder-snow freezing, unless the entity can't freeze (warm, or immune like strays and
     * snowmen). Vanilla thaws it again, 2 ticks a tick, once the entity is out of powder snow.
     */
    public static void chill(LivingEntity entity, int ticks) {
        if (ticks > 0 && entity.canFreeze()) {
            entity.setTicksFrozen(chilled(entity.getTicksFrozen(), ticks, entity.getTicksRequiredToFreeze()));
        }
    }

    /** Freezing after a chill: up to frozen solid, never past it, and never less than it was. */
    public static int chilled(int frozen, int ticks, int solid) {
        return Math.max(frozen, Math.min(solid, frozen + ticks));
    }
}
