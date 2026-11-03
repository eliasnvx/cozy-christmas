package dev.eliasnvx.cozychristmas.registry;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import dev.eliasnvx.cozychristmas.CozyChristmas;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

/**
 * Sound events. {@code sounds.json} points them at vanilla sound files with our own pitch and volume, so no audio is
 * shipped (nothing to license); resource packs can replace them one by one.
 */
public final class CozySounds {

    public static final DeferredRegister<SoundEvent> REGISTER = DeferredRegister.create(CozyChristmas.MOD_ID, Registries.SOUND_EVENT);

    public static final RegistrySupplier<SoundEvent> PRESENT_OPEN = event("block.present.open");
    public static final RegistrySupplier<SoundEvent> PRESENT_COAL = event("block.present.coal");
    /** The soft tick of a present that's going to go boom, while someone stands close. */
    public static final RegistrySupplier<SoundEvent> PRESENT_TICK = event("block.present.tick");
    public static final RegistrySupplier<SoundEvent> PRESENT_BOOM = event("block.present.boom");
    public static final RegistrySupplier<SoundEvent> PRESENT_CONFETTI = event("block.present.confetti");
    public static final RegistrySupplier<SoundEvent> WRAPPING_TABLE_WRAP = event("block.wrapping_table.wrap");

    private CozySounds() {
    }

    private static RegistrySupplier<SoundEvent> event(String name) {
        Identifier id = CozyChristmas.id(name);
        return REGISTER.register(id, () -> SoundEvent.createVariableRangeEvent(id));
    }
}
