package dev.eliasnvx.cozychristmas.compat.catgirl;

import dev.architectury.platform.Platform;
import dev.eliasnvx.cozychristmas.CozyChristmas;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

/**
 * Catgirl &amp; Catboy Mod (mod id {@code catgirl}, same author): whether a player wears its cat ears, and how they
 * are posed, so the ear warmers sit over them. That mod isn't published yet, so there is nothing to compile against:
 * its {@code CosmeticsManager.worn(LivingEntity)} and {@code CosmeticInventory.get(CatSlot.EARS)} are looked up once
 * by name. If they can't be found the ear warmers fall back to the sides of the head, with one warning in the log.
 *
 * <p>The pose constants mirror Catgirl's {@code CatModels.EarsModel} (geometry, twitch, flattening when hurt, the
 * lift above a helmet). Its per-player phase uses the same formula as ours, so the muffs twitch with the ears.
 */
public final class CatgirlEars {

    public static final String MOD_ID = "catgirl";

    /** Ear pivots: centres at x = ±{@link #SPACING}, base at {@link #BASE_Y}, a little in front of the middle. */
    public static final float SPACING = 2.3F;
    public static final float BASE_Y = -7.8F;
    public static final float BASE_Z = -0.2F;
    /** Outward tilt at rest. */
    public static final float TILT = 0.24F;
    /** A visible helmet lifts the ears this much, so they poke out on top of it. */
    public static final float HELMET_LIFT = 1.0F;

    private static final float TWITCH = 0.35F;
    private static final float TWITCH_SPEED = 0.35F;
    private static final float TWITCH_SHARPNESS = 12.0F;
    private static final float WALK_BOUNCE = 0.08F;
    private static final float FLAT_BACK = 0.9F;
    private static final float FLAT_OUT = 0.5F;
    /** Ears flatten fast when hurt and come back up slowly (per tick). */
    public static final float FLATTEN_IN = 1.2F;
    public static final float FLATTEN_OUT = 0.12F;

    private static final @Nullable Lookup LOOKUP = Platform.isModLoaded(MOD_ID) ? Lookup.find() : null;

    private CatgirlEars() {
    }

    /** Whether Catgirl's ears slot holds something on this entity (its own slot or one of the mods it reads). */
    public static boolean wears(LivingEntity entity) {
        return LOOKUP != null && LOOKUP.wearsEars(entity);
    }

    /** One ear's rotation: z (outward tilt) and x (folding back). {@code side} is -1 for the left ear, 1 for the right. */
    public static float zRot(int side, float ageInTicks, float phase, float walkPos, float walkAmount, float flatten) {
        return side * (TILT + twitch(side, ageInTicks, phase) + bounce(walkPos, walkAmount) + flatten * FLAT_OUT);
    }

    public static float xRot(int side, float ageInTicks, float phase, float flatten) {
        return -twitch(side, ageInTicks, phase) * 0.5F - flatten * FLAT_BACK;
    }

    /** Ears flick now and then, each on its own beat. */
    private static float twitch(int side, float ageInTicks, float phase) {
        float t = ageInTicks * TWITCH_SPEED / Mth.PI + phase;
        float wave = side < 0 ? Mth.sin(t) : Mth.sin(t * 0.83F + 1.7F);
        return (float) Math.pow(Math.max(wave, 0.0F), TWITCH_SHARPNESS) * TWITCH;
    }

    private static float bounce(float walkPos, float walkAmount) {
        return Mth.sin(walkPos * 0.6662F * 2) * WALK_BOUNCE * walkAmount;
    }

    private record Lookup(MethodHandle worn, MethodHandle get, Object earsSlot) {

        static @Nullable Lookup find() {
            try {
                ClassLoader loader = CatgirlEars.class.getClassLoader();
                Class<?> manager = Class.forName("dev.eliasnvx.catgirl.cosmetic.CosmeticsManager", false, loader);
                Class<?> inventory = Class.forName("dev.eliasnvx.catgirl.cosmetic.CosmeticInventory", false, loader);
                Class<?> slot = Class.forName("dev.eliasnvx.catgirl.cosmetic.CatSlot", false, loader);
                MethodHandles.Lookup lookup = MethodHandles.publicLookup();
                MethodHandle worn = lookup.findStatic(manager, "worn", MethodType.methodType(inventory, LivingEntity.class))
                        .asType(MethodType.methodType(Object.class, LivingEntity.class));
                MethodHandle get = lookup.findVirtual(inventory, "get", MethodType.methodType(ItemStack.class, slot))
                        .asType(MethodType.methodType(ItemStack.class, Object.class, Object.class));
                Object ears = slot.getField("EARS").get(null);
                CozyChristmas.LOGGER.info("Ear warmers go over Catgirl & Catboy Mod's cat ears");
                return new Lookup(worn, get, ears);
            } catch (ReflectiveOperationException | ClassCastException e) {
                CozyChristmas.LOGGER.warn("Catgirl & Catboy Mod is loaded, but its cosmetics couldn't be read; "
                        + "ear warmers stay on the sides of the head", e);
                return null;
            }
        }

        boolean wearsEars(LivingEntity entity) {
            try {
                Object worn = worn().invokeExact(entity);
                return !((ItemStack) get().invokeExact(worn, earsSlot())).isEmpty();
            } catch (Throwable e) {
                return false;
            }
        }
    }
}
