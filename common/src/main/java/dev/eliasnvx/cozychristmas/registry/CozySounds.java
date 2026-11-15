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

    public static final RegistrySupplier<SoundEvent> ELF_AMBIENT = event("entity.elf.ambient");
    public static final RegistrySupplier<SoundEvent> ELF_HURT = event("entity.elf.hurt");
    public static final RegistrySupplier<SoundEvent> ELF_DEATH = event("entity.elf.death");
    /** The tap of an elf at its workbench. */
    public static final RegistrySupplier<SoundEvent> ELF_WORK = event("entity.elf.work");
    /** Elves call for Santa: someone hurt one of them or stole. */
    public static final RegistrySupplier<SoundEvent> ELF_ALARM = event("entity.elf.alarm");

    public static final RegistrySupplier<SoundEvent> SANTA_AMBIENT = event("entity.santa.ambient");
    public static final RegistrySupplier<SoundEvent> SANTA_HURT = event("entity.santa.hurt");
    public static final RegistrySupplier<SoundEvent> SANTA_DEATH = event("entity.santa.death");
    public static final RegistrySupplier<SoundEvent> SANTA_YES = event("entity.santa.yes");
    public static final RegistrySupplier<SoundEvent> SANTA_NO = event("entity.santa.no");
    /** Santa hands out a daily present. */
    public static final RegistrySupplier<SoundEvent> SANTA_GIFT = event("entity.santa.gift");

    public static final RegistrySupplier<SoundEvent> REINDEER_AMBIENT = event("entity.reindeer.ambient");
    public static final RegistrySupplier<SoundEvent> REINDEER_HURT = event("entity.reindeer.hurt");
    public static final RegistrySupplier<SoundEvent> REINDEER_DEATH = event("entity.reindeer.death");
    public static final RegistrySupplier<SoundEvent> REINDEER_EAT = event("entity.reindeer.eat");
    public static final RegistrySupplier<SoundEvent> REINDEER_ANGRY = event("entity.reindeer.angry");
    /** Sleigh bells, jingling while a reindeer flies. */
    public static final RegistrySupplier<SoundEvent> REINDEER_BELLS = event("entity.reindeer.bells");

    public static final RegistrySupplier<SoundEvent> PRESENT_GOBLIN_AMBIENT = event("entity.present_goblin.ambient");
    public static final RegistrySupplier<SoundEvent> PRESENT_GOBLIN_HURT = event("entity.present_goblin.hurt");
    public static final RegistrySupplier<SoundEvent> PRESENT_GOBLIN_DEATH = event("entity.present_goblin.death");
    /** A snatch and a cackle: something just left your hotbar. */
    public static final RegistrySupplier<SoundEvent> PRESENT_GOBLIN_STEAL = event("entity.present_goblin.steal");

    public static final RegistrySupplier<SoundEvent> GINGERBREAD_MAN_AMBIENT = event("entity.gingerbread_man.ambient");
    public static final RegistrySupplier<SoundEvent> GINGERBREAD_MAN_HURT = event("entity.gingerbread_man.hurt");
    /** It crumbles. */
    public static final RegistrySupplier<SoundEvent> GINGERBREAD_MAN_DEATH = event("entity.gingerbread_man.death");
    public static final RegistrySupplier<SoundEvent> GINGERBREAD_MAN_STEP = event("entity.gingerbread_man.step");

    /** A low, crunchy grumble. */
    public static final RegistrySupplier<SoundEvent> EVIL_SNOWMAN_AMBIENT = event("entity.evil_snowman.ambient");
    public static final RegistrySupplier<SoundEvent> EVIL_SNOWMAN_HURT = event("entity.evil_snowman.hurt");
    public static final RegistrySupplier<SoundEvent> EVIL_SNOWMAN_DEATH = event("entity.evil_snowman.death");
    public static final RegistrySupplier<SoundEvent> EVIL_SNOWMAN_STEP = event("entity.evil_snowman.step");
    public static final RegistrySupplier<SoundEvent> EVIL_SNOWMAN_THROW = event("entity.evil_snowman.throw");
    /** Fire or heat on snow: a hiss. */
    public static final RegistrySupplier<SoundEvent> EVIL_SNOWMAN_MELT = event("entity.evil_snowman.melt");

    private CozySounds() {
    }

    private static RegistrySupplier<SoundEvent> event(String name) {
        Identifier id = CozyChristmas.id(name);
        return REGISTER.register(id, () -> SoundEvent.createVariableRangeEvent(id));
    }
}
