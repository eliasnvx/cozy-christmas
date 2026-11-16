package dev.eliasnvx.cozychristmas.cosmetic;

import dev.architectury.event.events.common.TickEvent;
import dev.eliasnvx.cozychristmas.config.CozyConfig;
import dev.eliasnvx.cozychristmas.registry.CozyTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

import java.util.List;
import java.util.UUID;

/**
 * The Toasty set: something from {@code #cozychristmas:toasty_set} in each of {@link #SLOTS} (a sweater, a hat, a
 * scarf and mittens by default). Worn whole, it keeps the wearer warm (see {@code cold.Cold}) and adds
 * {@link ToastySettings#snowSpeedBonus()} to the walking speed on snow.
 */
public final class ToastySet {

    public static final List<CosmeticSlot> SLOTS = List.of(CosmeticSlot.HEAD, CosmeticSlot.NECK, CosmeticSlot.BODY,
            CosmeticSlot.HANDS);

    /**
     * The speed bonus modifier. Transient: it's recomputed every tick, never saved. 1.20.1 names modifiers by UUID
     * (later versions use the id {@code cozychristmas:toasty_snow_speed}; this is its name-based UUID).
     */
    public static final UUID SNOW_SPEED_ID = UUID.fromString("2641b459-159e-3bf2-8ec4-7d65b48dcda6");
    private static final String SNOW_SPEED_NAME = "cozychristmas:toasty_snow_speed";

    private ToastySet() {
    }

    public static void register() {
        TickEvent.PLAYER_POST.register(player -> {
            // the server owns the modifier; vanilla's attribute sync brings it to the client for movement prediction
            if (!player.level().isClientSide()) {
                updateSnowSpeed(player);
            }
        });
    }

    /** How many of the set's slots hold a piece of it (the tooltip shows this). */
    public static int piecesWorn(CosmeticInventory worn) {
        int pieces = 0;
        for (CosmeticSlot slot : SLOTS) {
            if (worn.get(slot).is(CozyTags.TOASTY_SET)) {
                pieces++;
            }
        }
        return pieces;
    }

    public static boolean isWorn(LivingEntity entity) {
        return piecesWorn(CosmeticsManager.worn(entity)) == SLOTS.size();
    }

    /** Whether the set stops this entity from freezing (config {@code keeps_warm}). */
    public static boolean keepsWarm(LivingEntity entity) {
        return settings().keepsWarm() && isWorn(entity);
    }

    /**
     * Adds or removes the snow speed modifier. It's decided on the ground only: in the air walking speed doesn't apply,
     * and keeping the modifier through a jump saves a sync packet each hop. Taking a piece off removes it at once.
     */
    public static void updateSnowSpeed(Player player) {
        AttributeInstance speed = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed == null) {
            return;
        }
        double bonus = settings().snowSpeedBonus();
        boolean active = speed.getModifier(SNOW_SPEED_ID) != null;
        boolean wanted = bonus > 0.0 && isWorn(player) && (player.onGround() ? onSnow(player) : active);
        if (wanted && !active) {
            speed.addTransientModifier(new AttributeModifier(SNOW_SPEED_ID, SNOW_SPEED_NAME, bonus,
                    AttributeModifier.Operation.MULTIPLY_BASE));
        } else if (!wanted && active) {
            speed.removeModifier(SNOW_SPEED_ID);
        }
    }

    /** Standing on a snow block, or in a snow layer (it fills the block at the feet). */
    private static boolean onSnow(Player player) {
        return player.getBlockStateOn().is(CozyTags.TOASTY_SNOW)
                || player.level().getBlockState(player.blockPosition()).is(CozyTags.TOASTY_SNOW);
    }

    private static ToastySettings settings() {
        return CozyConfig.common().toastySet();
    }
}
