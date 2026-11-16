package dev.eliasnvx.cozychristmas.stocking;

import dev.eliasnvx.cozychristmas.config.CozyConfig;
import dev.eliasnvx.cozychristmas.karma.KarmaManager;
import dev.eliasnvx.cozychristmas.present.PresentContents;
import dev.eliasnvx.cozychristmas.present.PresentDesign;
import dev.eliasnvx.cozychristmas.present.PresentOutcome;
import dev.eliasnvx.cozychristmas.registry.CozyBlocks;
import dev.eliasnvx.cozychristmas.registry.CozyComponents;
import dev.eliasnvx.cozychristmas.registry.CozyItems;
import dev.eliasnvx.cozychristmas.registry.CozySounds;
import dev.eliasnvx.cozychristmas.registry.CozyTags;
import dev.eliasnvx.cozychristmas.season.ChristmasSeason;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

/**
 * A hung stocking: whose it is, what it holds, what was hung (our stocking or a sock, given back when it's taken
 * down) and the last morning it was filled. Every couple of seconds the server checks whether a new morning has come
 * (the day-night clock: sleeping brings it) and fills it for its owner, if the owner is online, it hangs over a
 * fireplace, it's empty, it's the Christmas season ({@code stockings.season_only}) and none of the owner's stockings
 * was filled that morning yet.
 */
public class StockingBlockEntity extends BlockEntity {

    private static final String OWNER = "owner";
    private static final String OWNER_NAME = "owner_name";
    private static final String CONTENTS = "contents";
    private static final String HUNG = "hung";
    private static final String LAST_MORNING = "last_morning";
    private static final int CHECK_INTERVAL = 40;
    private static final long TICKS_PER_DAY = 24000L;
    private static final long NEVER = Long.MIN_VALUE;

    private @Nullable UUID owner;
    /** Kept for the look-at tooltip (Jade), so it shows while the owner is offline; empty for old stockings. */
    private String ownerName = "";
    private ItemStack contents = ItemStack.EMPTY;
    private ItemStack hung = ItemStack.EMPTY;
    private long lastMorning = NEVER;

    public StockingBlockEntity(BlockPos pos, BlockState state) {
        super(CozyBlocks.STOCKING_ENTITY.get(), pos, state);
    }

    /** The morning of the day-night clock: the day number turns at sunrise. */
    public static long morning(Level level) {
        return Math.floorDiv(level.getDayTime(), TICKS_PER_DAY);
    }

    /** Hung by {@code player}: theirs, filled from the next morning on. */
    public void hang(Player player, ItemStack item, long today) {
        this.owner = player.getUUID();
        this.ownerName = player.getScoreboardName();
        this.hung = item.copyWithCount(1);
        this.lastMorning = today;
        setChanged();
    }

    public @Nullable UUID owner() {
        return owner;
    }

    public String ownerName() {
        return ownerName;
    }

    public ItemStack contents() {
        return contents;
    }

    public boolean isOwner(Player player) {
        return owner == null || owner.equals(player.getUUID());
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, StockingBlockEntity stocking) {
        if ((level.getGameTime() + pos.asLong()) % CHECK_INTERVAL == 0 && level instanceof ServerLevel server) {
            stocking.tryFill(server);
        }
    }

    /** Fills it if a new morning has come and the rules allow; true if it did. */
    public boolean tryFill(ServerLevel level) {
        long today = morning(level);
        if (today < lastMorning) {
            lastMorning = today; // the clock was turned back: wait for the next morning
            setChanged();
            return false;
        }
        if (today == lastMorning || owner == null || !contents.isEmpty() || !hangsOverFireplace(level, worldPosition, getBlockState())) {
            return false;
        }
        if (CozyConfig.common().stockings().seasonOnly() && !ChristmasSeason.isActive()) {
            return false;
        }
        ServerPlayer player = level.getServer().getPlayerList().getPlayer(owner);
        if (player == null || !KarmaManager.takeStockingGift(player, today)) {
            return false;
        }
        StockingGift gift = StockingGift.forTier(KarmaManager.tier(player));
        fill(level, gift);
        lastMorning = today;
        setChanged();
        return true;
    }

    private void fill(ServerLevel level, StockingGift gift) {
        StockingSettings settings = CozyConfig.common().stockings();
        if (gift == StockingGift.COAL) {
            contents = new ItemStack(Items.COAL, settings.coalMin() + level.getRandom().nextInt(settings.coalMax() - settings.coalMin() + 1));
        } else {
            PresentDesign[] designs = PresentDesign.values();
            contents = CozyItems.present(designs[level.getRandom().nextInt(designs.length)]);
            if (gift == StockingGift.GIFT) {
                contents.set(CozyComponents.PRESENT_CONTENTS.get(), new PresentContents.Surprise(Optional.of(PresentOutcome.LOOT)));
            }
        }
        level.setBlock(worldPosition, getBlockState().setValue(StockingBlock.FILL, gift.fill()), StockingBlock.UPDATE_CLIENTS);
        level.playSound(null, worldPosition, CozySounds.SANTA_GIFT.get(), SoundSource.BLOCKS, 0.6F, 1.0F);
    }

    /** Hands what it holds over and empties it; empty if there's nothing. */
    public ItemStack takeContents() {
        ItemStack taken = contents;
        contents = ItemStack.EMPTY;
        if (level != null && !taken.isEmpty()) {
            level.setBlock(worldPosition, getBlockState().setValue(StockingBlock.FILL, StockingFill.EMPTY), StockingBlock.UPDATE_CLIENTS);
        }
        setChanged();
        return taken;
    }

    /** Hanging on the wall above a fireplace, or two above (a taller hearth). */
    public static boolean hangsOverFireplace(LevelReader level, BlockPos pos, BlockState state) {
        BlockPos wall = pos.relative(state.getValue(StockingBlock.FACING).getOpposite());
        return level.getBlockState(wall.below()).is(CozyTags.FIREPLACES) || level.getBlockState(wall.below(2)).is(CozyTags.FIREPLACES);
    }

    /**
     * Taking the stocking down: what it holds and the stocking itself (unless a creative player took it down).
     * Called by {@link StockingBlock#onRemove} on the server, before the block entity goes.
     */
    void dropContents() {
        if (level != null) {
            Containers.dropItemStack(level, worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5, contents);
            Containers.dropItemStack(level, worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5, hung);
            contents = ItemStack.EMPTY;
            hung = ItemStack.EMPTY;
        }
    }

    /** A creative player taking it down gets nothing back (as with any block); what's inside still drops. */
    public void forgetHungItem() {
        hung = ItemStack.EMPTY;
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        owner = tag.hasUUID(OWNER) ? tag.getUUID(OWNER) : null;
        ownerName = tag.getString(OWNER_NAME);
        contents = ItemStack.parseOptional(registries, tag.getCompound(CONTENTS));
        hung = ItemStack.parseOptional(registries, tag.getCompound(HUNG));
        lastMorning = tag.contains(LAST_MORNING) ? tag.getLong(LAST_MORNING) : NEVER;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (owner != null) {
            tag.putUUID(OWNER, owner);
        }
        tag.putString(OWNER_NAME, ownerName);
        if (!contents.isEmpty()) {
            tag.put(CONTENTS, contents.save(registries));
        }
        if (!hung.isEmpty()) {
            tag.put(HUNG, hung.save(registries));
        }
        tag.putLong(LAST_MORNING, lastMorning);
    }
}
