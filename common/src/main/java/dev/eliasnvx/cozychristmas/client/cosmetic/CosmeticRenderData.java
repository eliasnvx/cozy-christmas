package dev.eliasnvx.cozychristmas.client.cosmetic;

import dev.eliasnvx.cozychristmas.compat.catgirl.CatgirlEars;
import dev.eliasnvx.cozychristmas.cosmetic.CosmeticInventory;
import dev.eliasnvx.cozychristmas.cosmetic.CosmeticSlot;
import dev.eliasnvx.cozychristmas.cosmetic.CosmeticsManager;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Per-player render data, captured by the cosmetic layer each frame. One instance per entity is reused across frames:
 * the worn list is rebuilt only when the (immutable) inventory snapshot changes, and the smoothed motion is updated in
 * place, so capturing does not allocate.
 */
public final class CosmeticRenderData {

    /** Higher = snappier smoothing (per tick). */
    private static final float SMOOTHING = 0.2F;
    private static final float GOLDEN_ANGLE = 2.39996F;

    /** Neutral values for states without data (a player that wears nothing). */
    public static final CosmeticRenderData STILL = new CosmeticRenderData(0);

    /** @param dye 24-bit dyed color, or -1 if undyed */
    public record Worn(CosmeticSlot slot, Item item, int dye) {
    }

    private static final Map<Player, CosmeticRenderData> CACHE = new WeakHashMap<>();

    private @Nullable CosmeticInventory source;
    private List<Worn> worn = List.of();
    private final float phase;
    private float walkAmount;
    private boolean catEars;
    private float flatten;
    private float lastAge = Float.NaN;

    private CosmeticRenderData(int entityId) {
        this.phase = (entityId * GOLDEN_ANGLE) % Mth.TWO_PI;
    }

    /** Called from the cosmetic layer once per frame per player. Returns null if nothing is worn. */
    public static @Nullable CosmeticRenderData capture(Player player, PlayerPose state) {
        CosmeticInventory inventory = CosmeticsManager.looks(player);
        if (inventory.isEmpty()) {
            return null;
        }
        CosmeticRenderData data = CACHE.computeIfAbsent(player, p -> new CosmeticRenderData(p.getId()));
        if (data.source != inventory) {
            data.rebuild(inventory);
        }
        data.catEars = CatgirlEars.wears(player);
        data.updateMotion(state);
        return data;
    }

    /** The data a model's {@code setupAnim} reads; never null. */
    public static CosmeticRenderData of(PlayerPose state) {
        return state.cosmetics;
    }

    private void rebuild(CosmeticInventory inventory) {
        List<Worn> list = new ArrayList<>();
        for (CosmeticSlot slot : CosmeticSlot.values()) {
            ItemStack stack = inventory.get(slot);
            if (!stack.isEmpty()) {
                list.add(new Worn(slot, stack.getItem(), dyeOf(stack)));
            }
        }
        this.worn = List.copyOf(list);
        this.source = inventory;
    }

    /** 24-bit dyed color, or -1 if undyed. */
    public static int dyeOf(ItemStack stack) {
        return stack.getItem() instanceof DyeableLeatherItem dyeable && dyeable.hasCustomColor(stack)
                ? dyeable.getColor(stack) & 0xFFFFFF : -1;
    }

    private void updateMotion(PlayerPose state) {
        if (Float.isNaN(lastAge)) {
            lastAge = state.ageInTicks;
            return;
        }
        float dt = state.ageInTicks - lastAge;
        if (dt <= 0) {
            return; // same frame (inventory doll) or paused
        }
        float alpha = 1.0F - (float) Math.exp(-dt * SMOOTHING);
        walkAmount += (Math.min(state.walkAnimationSpeed, 1.0F) - walkAmount) * alpha;
        float flattenRate = state.hasRedOverlay ? CatgirlEars.FLATTEN_IN : CatgirlEars.FLATTEN_OUT;
        flatten += ((state.hasRedOverlay ? 1.0F : 0.0F) - flatten) * (1.0F - (float) Math.exp(-dt * flattenRate));
        lastAge = state.ageInTicks;
    }

    public List<Worn> worn() {
        return worn;
    }

    /** 0..1 how fast the player is walking, smoothed. */
    public float walkAmount() {
        return walkAmount;
    }

    /** Whether the wearer has Catgirl &amp; Catboy Mod's cat ears on: the ear warmers go over them. */
    public boolean catEars() {
        return catEars;
    }

    /** 0..1 how flat cat ears are pressed back (the wearer just took damage), as Catgirl's ears are. */
    public float flatten() {
        return flatten;
    }

    /** Per-player phase offset in radians, so the scarves of different players do not flutter in sync. */
    public float phase() {
        return phase;
    }
}
