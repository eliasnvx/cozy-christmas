package dev.eliasnvx.cozychristmas.cold;

import dev.eliasnvx.cozychristmas.cosmetic.ToastySet;
import net.minecraft.world.entity.LivingEntity;

/**
 * The one place the mod asks "is this entity kept warm?". Each Minecraft version maps it to its own cold: on 26.3 and
 * the backports that's freezing in powder snow ({@code LivingEntityMixin} on {@code canFreeze}); 26.4 adds the vanilla
 * Freezing effect, which gets wired here too. Warm things (the Toasty set now, cocoa later) only plug in here.
 */
public final class Cold {

    private Cold() {
    }

    public static boolean isKeptWarm(LivingEntity entity) {
        return ToastySet.keepsWarm(entity);
    }
}
