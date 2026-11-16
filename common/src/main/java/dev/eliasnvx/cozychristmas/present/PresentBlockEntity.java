package dev.eliasnvx.cozychristmas.present;

import dev.eliasnvx.cozychristmas.config.CozyConfig;
import dev.eliasnvx.cozychristmas.karma.KarmaManager;
import dev.eliasnvx.cozychristmas.registry.CozyBlocks;
import dev.eliasnvx.cozychristmas.registry.CozyComponents;
import dev.eliasnvx.cozychristmas.registry.CozySounds;
import dev.eliasnvx.cozychristmas.workshop.WorkshopAlarm;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * Holds the {@link PresentContents} of a placed present. Contents stay on the server: the client only sees the block
 * state ({@link PresentBlock#TICKING}).
 *
 * <p>A surprise is decided by the karma of the first player who comes within {@code presents.warning_radius} (or opens
 * it from farther away), so the ticking is honest: a boom ticks before it's opened, and breaking it keeps the decision.
 *
 * <p>Presents in the warehouse of Santa's workshop are {@linkplain #isWorkshopStock() stock} (set by the structure
 * template): opening or breaking one is theft ({@code WorkshopAlarm#theft}). The mark isn't part of the item, so a
 * stolen present is just a present afterwards.
 */
public class PresentBlockEntity extends BlockEntity {

    /** Players nearby are looked for this often; each present checks on its own tick, so they're spread out. */
    private static final int CHECK_INTERVAL = 10;
    private static final float TICK_VOLUME = 0.6F;
    private static final String CONTENTS = "contents";
    private static final String WORKSHOP_STOCK = "workshop_stock";

    private PresentContents contents = PresentContents.SURPRISE;
    private boolean workshopStock;

    public PresentBlockEntity(BlockPos pos, BlockState state) {
        super(CozyBlocks.PRESENT_ENTITY.get(), pos, state);
    }

    public PresentContents contents() {
        return contents;
    }

    public void setContents(PresentContents contents) {
        this.contents = contents;
        setChanged();
    }

    /** Santa's: part of the workshop warehouse. */
    public boolean isWorkshopStock() {
        return workshopStock;
    }

    public void setWorkshopStock(boolean workshopStock) {
        this.workshopStock = workshopStock;
        setChanged();
    }

    static void serverTick(Level level, BlockPos pos, BlockState state, PresentBlockEntity present) {
        if (Math.floorMod(level.getGameTime() + pos.asLong(), CHECK_INTERVAL) != 0) {
            return;
        }
        double radius = CozyConfig.common().presents().warningRadius();
        Vec3 center = Vec3.atCenterOf(pos);
        Player near = radius > 0.0
                ? level.getNearestPlayer(center.x, center.y, center.z, radius, EntitySelector.NO_SPECTATORS)
                : null;
        // Santa's stock waits for whoever opens it: the theft comes first, then the thief's own karma decides
        if (near instanceof ServerPlayer player && !present.workshopStock) {
            present.decideFor(player);
        }
        boolean ticking = near != null && present.contents.isBoom();
        if (state.getValue(PresentBlock.TICKING) != ticking) {
            level.setBlock(pos, state.setValue(PresentBlock.TICKING, ticking), Block.UPDATE_CLIENTS);
        }
        if (ticking) {
            level.playSound(null, pos, CozySounds.PRESENT_TICK.get(), SoundSource.BLOCKS, TICK_VOLUME, 1.0F);
        }
    }

    /** Server: removes the present and gives the opener what's inside. */
    public void open(ServerPlayer opener) {
        if (!(level instanceof ServerLevel serverLevel) || isRemoved()) {
            return;
        }
        if (workshopStock) {
            WorkshopAlarm.theft(serverLevel, worldPosition, opener); // first: stealing makes the thief naughtier
        }
        decideFor(opener);
        PresentContents opened = contents;
        BlockPos pos = worldPosition;
        serverLevel.removeBlock(pos, false);
        Presents.unwrap(serverLevel, pos, opened, opener);
    }

    /** An undecided surprise rolls its outcome on this player's karma; anything else stays as it is. */
    private void decideFor(ServerPlayer player) {
        if (contents instanceof PresentContents.Surprise surprise && surprise.decided().isEmpty()) {
            PresentOdds odds = CozyConfig.common().presents().odds(KarmaManager.tier(player));
            setContents(new PresentContents.Surprise(odds.roll(player.getRandom())));
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        contents = tag.contains(CONTENTS)
                ? PresentContents.CODEC.parse(registries.createSerializationContext(NbtOps.INSTANCE), tag.get(CONTENTS))
                        .resultOrPartial().orElse(PresentContents.SURPRISE)
                : PresentContents.SURPRISE;
        workshopStock = tag.getBoolean(WORKSHOP_STOCK);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        RegistryOps<Tag> ops = registries.createSerializationContext(NbtOps.INSTANCE);
        PresentContents.CODEC.encodeStart(ops, contents).ifSuccess(encoded -> tag.put(CONTENTS, encoded));
        if (workshopStock) {
            tag.putBoolean(WORKSHOP_STOCK, true);
        }
    }

    @Override
    protected void applyImplicitComponents(DataComponentInput components) {
        super.applyImplicitComponents(components);
        contents = components.getOrDefault(CozyComponents.PRESENT_CONTENTS.get(), PresentContents.SURPRISE);
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder components) {
        super.collectImplicitComponents(components);
        components.set(CozyComponents.PRESENT_CONTENTS.get(), contents);
        if (contents instanceof PresentContents.Surprise surprise && surprise.decided().isPresent()) {
            // decided ones don't stack: stacking with a known one would tell loot from coal before opening
            components.set(DataComponents.MAX_STACK_SIZE, 1);
        }
    }

    @Override
    @SuppressWarnings("deprecation") // Mojang marks it override-only; vanilla's decorated pot overrides it the same way
    public void removeComponentsFromTag(CompoundTag tag) {
        super.removeComponentsFromTag(tag);
        tag.remove(CONTENTS);
    }
}
