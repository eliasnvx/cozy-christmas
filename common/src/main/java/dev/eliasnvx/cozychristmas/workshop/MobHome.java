package dev.eliasnvx.cozychristmas.workshop;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.Mob;

/**
 * Saves a mob's home (its restriction: center and radius). 26.3's {@code Mob} saves {@code home_pos} and
 * {@code home_radius} itself; 1.20.1's forgets the restriction on reload, so the workshop's Santa, elves and stable
 * reindeer would wander off after a restart. Same keys as 26.3; the position is 1.20.1's {@code NbtUtils} compound
 * ({@code X}, {@code Y}, {@code Z}).
 */
public final class MobHome {

    private static final String HOME_POS = "home_pos";
    private static final String HOME_RADIUS = "home_radius";

    private MobHome() {
    }

    public static void save(Mob mob, CompoundTag tag) {
        if (mob.hasRestriction()) {
            tag.putInt(HOME_RADIUS, (int) mob.getRestrictRadius());
            tag.put(HOME_POS, NbtUtils.writeBlockPos(mob.getRestrictCenter()));
        }
    }

    public static void load(Mob mob, CompoundTag tag) {
        if (tag.contains(HOME_RADIUS, Tag.TAG_INT) && tag.contains(HOME_POS, Tag.TAG_COMPOUND)) {
            mob.restrictTo(NbtUtils.readBlockPos(tag.getCompound(HOME_POS)), tag.getInt(HOME_RADIUS));
        }
    }
}
