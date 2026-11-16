package dev.eliasnvx.cozychristmas.tree;

import dev.eliasnvx.cozychristmas.config.CozyConfig;
import dev.eliasnvx.cozychristmas.karma.KarmaManager;
import dev.eliasnvx.cozychristmas.present.PresentBlock;
import dev.eliasnvx.cozychristmas.present.PresentBlockEntity;
import dev.eliasnvx.cozychristmas.present.PresentContents;
import dev.eliasnvx.cozychristmas.present.PresentDesign;
import dev.eliasnvx.cozychristmas.registry.CozyBlocks;
import dev.eliasnvx.cozychristmas.registry.CozySounds;
import dev.eliasnvx.cozychristmas.season.ChristmasSeason;
import dev.eliasnvx.cozychristmas.stocking.StockingBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * The star on top of a Christmas tree: whose tree it is and the last morning a present came. Every couple of seconds
 * the server checks whether a new morning has come (the day-night clock, as for stockings) and puts a present under
 * the tree for its owner, if the owner is online, it's the Christmas season ({@code tree.season_only}), the tree is
 * decorated, there's room under it and none of the owner's trees had one that morning. Karma decides what's inside:
 * the owner's, rolled when the present appears (Santa knows whose tree it is). A star without an owner (one placed by
 * a structure) never brings any.
 */
public class TreeStarBlockEntity extends BlockEntity {

    private static final String OWNER = "owner";
    private static final String OWNER_NAME = "owner_name";
    private static final String LAST_MORNING = "last_morning";
    private static final int CHECK_INTERVAL = 40;
    private static final long NEVER = Long.MIN_VALUE;
    private static final int SPARKLES = 12;

    private @Nullable UUID owner;
    private String ownerName = "";
    private long lastMorning = NEVER;

    public TreeStarBlockEntity(BlockPos pos, BlockState state) {
        super(CozyBlocks.TREE_STAR_ENTITY.get(), pos, state);
    }

    /** Put up by {@code player}: their tree, with presents from the next morning on. */
    public void putUp(Player player, long today) {
        owner = player.getUUID();
        ownerName = player.getScoreboardName();
        lastMorning = today;
        setChanged();
    }

    public @Nullable UUID owner() {
        return owner;
    }

    public String ownerName() {
        return ownerName;
    }

    public boolean isOwner(Player player) {
        return owner == null || owner.equals(player.getUUID());
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, TreeStarBlockEntity star) {
        if ((level.getGameTime() + pos.asLong()) % CHECK_INTERVAL == 0 && level instanceof ServerLevel server) {
            star.tryGift(server);
        }
    }

    /** Puts a present under the tree if a new morning has come and the rules allow; returns where, or null. */
    public @Nullable BlockPos tryGift(ServerLevel level) {
        long today = StockingBlockEntity.morning(level);
        if (today < lastMorning) {
            lastMorning = today; // the clock was turned back: wait for the next morning
            setChanged();
            return null;
        }
        TreeSettings settings = CozyConfig.common().tree();
        if (today == lastMorning || owner == null || (settings.seasonOnly() && !ChristmasSeason.isActive())) {
            return null;
        }
        ServerPlayer player = level.getServer().getPlayerList().getPlayer(owner);
        if (player == null) {
            return null;
        }
        if (KarmaManager.hadTreeGift(player, today)) {
            lastMorning = today; // another of their trees had this morning's present
            setChanged();
            return null;
        }
        ChristmasTree.Scan scan = ChristmasTree.scan(level, worldPosition, settings);
        if (ChristmasTree.status(scan, settings) != ChristmasTree.Status.READY
                || !settings.hasRoomFor(ChristmasTree.presentsAround(level, scan.base(), settings))) {
            return null; // tried again later this morning: the owner may still decorate it or pick presents up
        }
        BlockPos spot = ChristmasTree.pickSpot(level, scan.base(), settings, level.getRandom());
        if (spot == null || !KarmaManager.takeTreeGift(player, today)) {
            return null;
        }
        place(level, spot, player);
        lastMorning = today;
        setChanged();
        return spot;
    }

    private void place(ServerLevel level, BlockPos spot, ServerPlayer owner) {
        PresentDesign[] designs = PresentDesign.values();
        BlockState present = CozyBlocks.PRESENT.get().defaultBlockState()
                .setValue(PresentBlock.DESIGN, designs[level.getRandom().nextInt(designs.length)]);
        level.setBlock(spot, present, Block.UPDATE_ALL);
        if (level.getBlockEntity(spot) instanceof PresentBlockEntity entity) {
            entity.setContents(new PresentContents.Surprise(
                    CozyConfig.common().presents().odds(KarmaManager.tier(owner)).roll(level.getRandom())));
        }
        level.sendParticles(ParticleTypes.SNOWFLAKE, spot.getX() + 0.5, spot.getY() + 0.6, spot.getZ() + 0.5,
                SPARKLES, 0.3, 0.3, 0.3, 0.02);
        level.playSound(null, spot, CozySounds.SANTA_GIFT.get(), SoundSource.BLOCKS, 0.6F, 1.0F);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        owner = tag.hasUUID(OWNER) ? tag.getUUID(OWNER) : null;
        ownerName = tag.getString(OWNER_NAME);
        lastMorning = tag.contains(LAST_MORNING) ? tag.getLong(LAST_MORNING) : NEVER;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (owner != null) {
            tag.putUUID(OWNER, owner);
        }
        tag.putString(OWNER_NAME, ownerName);
        tag.putLong(LAST_MORNING, lastMorning);
    }
}
