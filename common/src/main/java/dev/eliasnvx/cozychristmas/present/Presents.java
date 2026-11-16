package dev.eliasnvx.cozychristmas.present;

import dev.eliasnvx.cozychristmas.CozyChristmas;
import dev.eliasnvx.cozychristmas.karma.KarmaManager;
import dev.eliasnvx.cozychristmas.registry.CozySounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

/** Opening a present: what comes out of it. Loot and coal are loot tables, so packs decide what Santa gives. */
public final class Presents {

    /** A real gift: {@code data/cozychristmas/loot_tables/presents/gift.json}. */
    public static final ResourceLocation GIFT_LOOT = CozyChristmas.id("presents/gift");
    /** Santa noticed: {@code data/cozychristmas/loot_tables/presents/coal.json}. */
    public static final ResourceLocation COAL_LOOT = CozyChristmas.id("presents/coal");

    private static final int PAPER_BITS = 12;
    private static final double PAPER_SPREAD = 0.25;
    private static final double PAPER_SPEED = 0.08;

    private Presents() {
    }

    /** The present at {@code pos} is already gone; this hands out what it held. */
    static void unwrap(ServerLevel level, BlockPos pos, PresentContents contents, ServerPlayer opener) {
        if (contents instanceof PresentContents.Surprise surprise) {
            switch (surprise.decided().orElse(PresentOutcome.LOOT)) {
                case LOOT -> dropLoot(level, pos, GIFT_LOOT, opener, CozySounds.PRESENT_OPEN.get());
                case COAL -> dropLoot(level, pos, COAL_LOOT, opener, CozySounds.PRESENT_COAL.get());
                case BOOM -> PresentBoom.explode(level, Vec3.atCenterOf(pos), null);
            }
        } else if (contents instanceof PresentContents.Gift gift) {
            Block.popResource(level, pos, gift.create());
            paperBurst(level, pos, CozySounds.PRESENT_OPEN.get());
            gift.from().ifPresent(giver -> thankGiver(level, giver, opener));
        } else if (contents instanceof PresentContents.Prank prank) {
            PresentBoom.explode(level, Vec3.atCenterOf(pos),
                    prank.from().map(giver -> level.getServer().getPlayerList().getPlayer(giver.id())).orElse(null));
        }
    }

    private static void dropLoot(ServerLevel level, BlockPos pos, ResourceLocation id, ServerPlayer opener, SoundEvent sound) {
        LootTable table = level.getServer().getLootData().getLootTable(id);
        LootParams params = new LootParams.Builder(level)
                .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos))
                .withParameter(LootContextParams.THIS_ENTITY, opener)
                .withLuck(opener.getLuck())
                .create(LootContextParamSets.GIFT);
        table.getRandomItems(params, stack -> Block.popResource(level, pos, stack));
        paperBurst(level, pos, sound);
    }

    /** Torn wrapping paper and the opening sound. */
    private static void paperBurst(ServerLevel level, BlockPos pos, SoundEvent sound) {
        Vec3 center = Vec3.atCenterOf(pos);
        level.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(Items.PAPER)), center.x, center.y, center.z,
                PAPER_BITS, PAPER_SPREAD, PAPER_SPREAD, PAPER_SPREAD, PAPER_SPEED);
        level.playSound(null, pos, sound, SoundSource.BLOCKS, 1.0F, 1.0F);
    }

    /** Someone else opened their gift: the giver earns karma, if they're online (karma lives on the player). */
    private static void thankGiver(ServerLevel level, PresentContents.Giver giver, ServerPlayer opener) {
        if (giver.id().equals(opener.getUUID())) {
            return;
        }
        ServerPlayer player = level.getServer().getPlayerList().getPlayer(giver.id());
        if (player != null) {
            KarmaManager.rewardGift(player);
        }
    }
}
