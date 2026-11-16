package dev.eliasnvx.cozychristmas.registry;

import dev.eliasnvx.cozychristmas.CozyChristmas;
import dev.eliasnvx.cozychristmas.cosmetic.Cosmetic;
import dev.eliasnvx.cozychristmas.item.ItemData;
import dev.eliasnvx.cozychristmas.present.PresentContents;

/** Item data of the mod: in a stack's NBT on 1.20.1 (later versions use data components with the same ids). */
public final class CozyComponents {

    public static final ItemData<Cosmetic> COSMETIC = new ItemData<>(CozyChristmas.MOD_ID + ":cosmetic", Cosmetic.CODEC);

    /** What's inside a present item; the block entity carries the same while it's placed. */
    public static final ItemData<PresentContents> PRESENT_CONTENTS =
            new ItemData<>(CozyChristmas.MOD_ID + ":present_contents", PresentContents.CODEC);

    private CozyComponents() {
    }
}
