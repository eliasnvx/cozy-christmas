package dev.eliasnvx.cozychristmas.present;

import dev.eliasnvx.cozychristmas.config.CozyConfig;
import dev.eliasnvx.cozychristmas.karma.KarmaManager;
import dev.eliasnvx.cozychristmas.registry.CozyBlocks;
import dev.eliasnvx.cozychristmas.registry.CozySounds;
import dev.eliasnvx.cozychristmas.workshop.WorkshopAlarm;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
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
 * <p>Breaking keeps the contents: the block loot table copies {@code contents} into the item's
 * {@code cozychristmas:present_contents} NBT, and placing hands them back ({@link PresentItem}).
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
    public void load(CompoundTag tag) {
        super.load(tag);
        contents = tag.contains(CONTENTS)
                ? PresentContents.CODEC.parse(NbtOps.INSTANCE, tag.get(CONTENTS)).result().orElse(PresentContents.SURPRISE)
                : PresentContents.SURPRISE;
        workshopStock = tag.getBoolean(WORKSHOP_STOCK);
    }

    /**
     * A plain surprise isn't written: it's the default, and the loot table copies {@code contents} into the dropped
     * item's NBT, where a written surprise would keep it from stacking with fresh presents.
     */
    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (!contents.equals(PresentContents.SURPRISE)) {
            PresentContents.CODEC.encodeStart(NbtOps.INSTANCE, contents).result().ifPresent(encoded -> tag.put(CONTENTS, encoded));
        }
        if (workshopStock) {
            tag.putBoolean(WORKSHOP_STOCK, true);
        }
    }
}
