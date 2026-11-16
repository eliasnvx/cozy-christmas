package dev.eliasnvx.cozychristmas.workshop;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.eliasnvx.cozychristmas.CozyChristmas;
import dev.eliasnvx.cozychristmas.config.CozyConfig;
import dev.eliasnvx.cozychristmas.karma.KarmaManager;
import dev.eliasnvx.cozychristmas.karma.KarmaTier;
import dev.eliasnvx.cozychristmas.present.PresentContents;
import dev.eliasnvx.cozychristmas.present.PresentDesign;
import dev.eliasnvx.cozychristmas.present.PresentOutcome;
import dev.eliasnvx.cozychristmas.registry.CozyComponents;
import dev.eliasnvx.cozychristmas.registry.CozyItems;
import dev.eliasnvx.cozychristmas.registry.CozySounds;
import dev.eliasnvx.cozychristmas.season.ChristmasSeason;
import dev.eliasnvx.cozychristmas.sleigh.SantasSleigh;
import dev.eliasnvx.cozychristmas.sleigh.SleighFlight;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.stats.Stats;
import net.minecraft.util.Prediction;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.InteractGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.LookAtTradingPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsRestrictionGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.TradeWithPlayerGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.item.trading.TradeSet;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Santa, at home in his workshop. He trades for emeralds (trade sets {@code cozychristmas:santa/goods} and
 * {@code santa/buying}, restocked every game day) and reads the player's karma, which is how players learn it: nice ones
 * pay {@code workshop.santa.nice_discount} less and in season get a present once a day; naughty ones he turns away.
 * He's neutral: whoever hurts him or an elf, or steals from the warehouse, he goes after ({@link WorkshopAlarm}) for
 * {@code anger_ticks}, and won't trade with them meanwhile.
 */
public class Santa extends AbstractVillager {

    public static final ResourceKey<TradeSet> GOODS = tradeSet("santa/goods");
    public static final ResourceKey<TradeSet> BUYING = tradeSet("santa/buying");

    /** Santa placed by the workshop stays around it. */
    private static final int HOME_RADIUS = 20;
    private static final int UNHAPPY_TICKS = 40;
    private static final double FOLLOW_RANGE = 32.0;
    private static final double KNOCKBACK_RESISTANCE = 0.6;
    private static final double CHASE = 1.2;
    private static final double STROLL = 0.6;
    private static final String RESTOCK_DAY = "restock_day";
    private static final String OWED_THANKS = "owed_thanks";
    private static final String THANKED = "thanked";
    private static final String THANKED_DAY = "day";
    private static final String THANKED_PLAYER = "player";
    /** On delivery days Santa stays this close to his sleigh, and looks for it this far. */
    private static final double BY_THE_SLEIGH = 3.5;
    private static final double SLEIGH_SEARCH = 32.0;

    private long restockDay = Long.MIN_VALUE;
    /**
     * Who Santa is angry at and until when (game time). Kept apart from the attack target: on peaceful he can't attack
     * a player, but still won't trade with them. Not saved, like the target.
     */
    private @Nullable LivingEntity angryAt;
    private long angryUntil;
    /** Players who helped load the sleigh and haven't been thanked yet. */
    private final Set<UUID> owedThanks = new HashSet<>();
    /** The real day each helper last got a thank-you present: one a day, however often the sleigh flies. */
    private final Map<UUID, Long> thankedOn = new HashMap<>();

    public Santa(EntityType<? extends Santa> type, Level level) {
        super(type, level);
    }

    private static ResourceKey<TradeSet> tradeSet(String path) {
        return ResourceKey.create(Registries.TRADE_SET, CozyChristmas.id(path));
    }

    public static AttributeSupplier.Builder createAttributes(SantaSettings settings) {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, settings.health())
                .add(Attributes.ATTACK_DAMAGE, settings.attackDamage())
                .add(Attributes.MOVEMENT_SPEED, settings.speed())
                .add(Attributes.FOLLOW_RANGE, FOLLOW_RANGE)
                .add(Attributes.KNOCKBACK_RESISTANCE, KNOCKBACK_RESISTANCE);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(1, new TradeWithPlayerGoal(this));
        goalSelector.addGoal(1, new LookAtTradingPlayerGoal(this));
        goalSelector.addGoal(2, new MeleeAttackGoal(this, CHASE, true));
        goalSelector.addGoal(3, new StayBySleighGoal());
        goalSelector.addGoal(4, new MoveTowardsRestrictionGoal(this, STROLL));
        goalSelector.addGoal(8, new WaterAvoidingRandomStrollGoal(this, STROLL));
        goalSelector.addGoal(9, new InteractGoal(this, Player.class, 3.0F, 1.0F));
        goalSelector.addGoal(10, new LookAtPlayerGoal(this, Mob.class, 8.0F));
        goalSelector.addGoal(11, new RandomLookAroundGoal(this));
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                                  EntitySpawnReason reason, @Nullable SpawnGroupData groupData) {
        if (reason == EntitySpawnReason.STRUCTURE) {
            setHomeTo(blockPosition(), HOME_RADIUS);
        }
        return super.finalizeSpawn(level, difficulty, reason, groupData);
    }

    @Override
    public boolean removeWhenFarAway(double distanceSquared) {
        return false;
    }

    @Override
    public @Nullable AgeableMob getBreedOffspring(ServerLevel level, AgeableMob partner) {
        return null;
    }

    @Override
    public boolean showProgressBar() {
        return false;
    }

    // --- anger -------------------------------------------------------------------------------------------------

    /**
     * Is angry at {@code culprit} for {@code workshop.santa.anger_ticks} (again from now if he already was) and goes after
     * them where the game allows it (not players on peaceful).
     * @return false if Santa doesn't blame them (one of his own, creative, spectator)
     */
    public boolean angerAt(LivingEntity culprit) {
        if (!WorkshopAlarm.canBeBlamed(culprit)) {
            return false;
        }
        if (getTradingPlayer() == culprit) {
            stopTrading();
        }
        angryAt = culprit;
        angryUntil = level().getGameTime() + CozyConfig.common().workshop().santa().angerTicks();
        setTarget(culprit);
        return true;
    }

    public boolean isAngryAt(LivingEntity entity) {
        return angryAt == entity && level().getGameTime() < angryUntil;
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        if (angryAt != null && (level.getGameTime() >= angryUntil || !WorkshopAlarm.canBeBlamed(angryAt))) {
            if (getTarget() == angryAt) {
                setTarget(null);
            }
            angryAt = null;
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide() && getUnhappyCounter() > 0) {
            setUnhappyCounter(getUnhappyCounter() - 1);
        }
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        boolean hurt = super.hurtServer(level, source, damage);
        if (hurt && source.getEntity() instanceof LivingEntity attacker) {
            WorkshopAlarm.raise(level, position(), attacker);
        }
        return hurt;
    }

    // --- trading -------------------------------------------------------------------------------------------------

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        if (held.is(CozyItems.SANTA_SPAWN_EGG.get()) || !isAlive() || isTrading() || isBaby()) {
            return super.mobInteract(player, hand);
        }
        if (hand == InteractionHand.MAIN_HAND) {
            player.awardStat(Stats.TALKED_TO_VILLAGER);
        }
        if (level() instanceof ServerLevel level && player instanceof ServerPlayer visitor) {
            visit(level, visitor);
        }
        return InteractionResult.SUCCESS_SERVER;
    }

    /** A player talks to Santa: refused, given today's present, or let in to trade. */
    private void visit(ServerLevel level, ServerPlayer visitor) {
        KarmaTier tier = KarmaManager.tier(visitor);
        if (isAngryAt(visitor) || tier == KarmaTier.NAUGHTY) {
            refuse(visitor, isAngryAt(visitor) ? "angry" : "naughty");
            return;
        }
        if (thankForHelping(level, visitor) || tier == KarmaTier.NICE && giveDailyPresent(level, visitor)) {
            return;
        }
        restockIfNewDay(level);
        if (getOffers().isEmpty()) {
            return;
        }
        if (tier == KarmaTier.NICE) {
            SantaSettings settings = CozyConfig.common().workshop().santa();
            for (MerchantOffer offer : getOffers()) {
                offer.addToSpecialPriceDiff(-settings.discount(offer.getBaseCostA().getCount()));
            }
        }
        setTradingPlayer(visitor);
        openTradingScreen(visitor, getDisplayName(), 1);
    }

    private void refuse(ServerPlayer visitor, String why) {
        setUnhappyCounter(UNHAPPY_TICKS);
        makeSound(CozySounds.SANTA_NO.get());
        visitor.sendOverlayMessage(Component.translatable("message.cozychristmas.santa." + why));
    }

    /**
     * In season, with {@code daily_gifts} on, a nice player gets one present a game day: a surprise already decided to
     * be a gift (never coal or a boom from Santa himself).
     */
    private boolean giveDailyPresent(ServerLevel level, ServerPlayer visitor) {
        if (!CozyConfig.common().workshop().santa().dailyGifts() || !ChristmasSeason.isActive()
                || !KarmaManager.takeSantaGift(visitor)) {
            return false;
        }
        PresentDesign[] designs = PresentDesign.values();
        ItemStack present = CozyItems.present(designs[getRandom().nextInt(designs.length)]);
        present.set(CozyComponents.PRESENT_CONTENTS.get(), new PresentContents.Surprise(Optional.of(PresentOutcome.LOOT)));
        visitor.getInventory().placeItemBackInInventory(present, Prediction.SERVER_ONLY); // drops it if full
        makeSound(CozySounds.SANTA_GIFT.get());
        level.sendParticles(ParticleTypes.HAPPY_VILLAGER, getX(), getY() + 1.8, getZ(), 8, 0.4, 0.4, 0.4, 0.0);
        visitor.sendOverlayMessage(Component.translatable("message.cozychristmas.santa.gift"));
        return true;
    }

    /** Back from a delivery flight: these players helped load the sleigh. */
    public void thank(Collection<UUID> helpers) {
        owedThanks.addAll(helpers);
    }

    public boolean owesThanks(UUID player) {
        return owedThanks.contains(player);
    }

    /**
     * A player who helped load the sleigh gets a present (a gift, like the daily one) and a thank-you on their next
     * visit, at most one a real day: loading takes any present, so this mustn't become a present mill.
     */
    private boolean thankForHelping(ServerLevel level, ServerPlayer visitor) {
        if (!owedThanks.remove(visitor.getUUID())) {
            return false;
        }
        long today = ChristmasSeason.today().toEpochDay();
        if (thankedOn.getOrDefault(visitor.getUUID(), Long.MIN_VALUE) != today) {
            thankedOn.put(visitor.getUUID(), today);
            PresentDesign[] designs = PresentDesign.values();
            ItemStack present = CozyItems.present(designs[getRandom().nextInt(designs.length)]);
            present.set(CozyComponents.PRESENT_CONTENTS.get(), new PresentContents.Surprise(Optional.of(PresentOutcome.LOOT)));
            visitor.getInventory().placeItemBackInInventory(present, Prediction.SERVER_ONLY);
            level.sendParticles(ParticleTypes.HAPPY_VILLAGER, getX(), getY() + 1.8, getZ(), 8, 0.4, 0.4, 0.4, 0.0);
        }
        makeSound(CozySounds.SANTA_GIFT.get());
        visitor.sendOverlayMessage(Component.translatable("message.cozychristmas.santa.thanks"));
        return true;
    }

    private void restockIfNewDay(ServerLevel level) {
        long today = KarmaManager.today(level);
        if (restockDay != today) {
            for (MerchantOffer offer : getOffers()) {
                offer.resetUses();
            }
            restockDay = today;
        }
    }

    @Override
    protected void updateTrades(ServerLevel level) {
        MerchantOffers offers = getOffers();
        addOffersFromTradeSet(level, offers, GOODS);
        addOffersFromTradeSet(level, offers, BUYING);
    }

    /** The nice discount lasts one visit, like a villager's hero-of-the-village prices. */
    @Override
    public void setTradingPlayer(@Nullable Player player) {
        boolean stopping = getTradingPlayer() != null && player == null;
        super.setTradingPlayer(player);
        if (stopping && !level().isClientSide()) {
            for (MerchantOffer offer : getOffers()) {
                offer.resetSpecialPriceDiff();
            }
        }
    }

    @Override
    protected void stopTrading() {
        setTradingPlayer(null);
    }

    @Override
    protected void rewardTradeXp(MerchantOffer offer) {
        if (offer.shouldRewardExp()) {
            level().addFreshEntity(new ExperienceOrb(level(), getX(), getY() + 0.5, getZ(), 3 + getRandom().nextInt(4)));
        }
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putLong(RESTOCK_DAY, restockDay);
        output.store(OWED_THANKS, UUIDUtil.CODEC.listOf(), List.copyOf(owedThanks));
        output.store(THANKED, Thanked.CODEC.listOf(), thankedOn.entrySet().stream()
                .map(entry -> new Thanked(entry.getKey(), entry.getValue())).toList());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        restockDay = input.getLongOr(RESTOCK_DAY, Long.MIN_VALUE);
        owedThanks.clear();
        input.read(OWED_THANKS, UUIDUtil.CODEC.listOf()).ifPresent(owedThanks::addAll);
        thankedOn.clear();
        input.read(THANKED, Thanked.CODEC.listOf()).ifPresent(list -> list.forEach(entry -> thankedOn.put(entry.player(), entry.day())));
    }

    /** A helper's last thank-you day, as saved. */
    private record Thanked(UUID player, long day) {
        static final Codec<Thanked> CODEC = RecordCodecBuilder.create(i -> i.group(
                UUIDUtil.CODEC.fieldOf(THANKED_PLAYER).forGetter(Thanked::player),
                Codec.LONG.fieldOf(THANKED_DAY).forGetter(Thanked::day)
        ).apply(i, Thanked::new));
    }

    /** On delivery days Santa stands by his parked sleigh while the elves load it, ready to get in at nightfall. */
    final class StayBySleighGoal extends Goal {

        private static final int RECHECK = 40;

        private @Nullable SantasSleigh sleigh;
        private int recheck;

        StayBySleighGoal() {
            setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            if (--recheck > 0 || isPassenger() || !ChristmasSeason.isWorkshopDelivery()) {
                return false;
            }
            recheck = reducedTickDelay(RECHECK);
            sleigh = level().getEntitiesOfClass(SantasSleigh.class, getBoundingBox().inflate(SLEIGH_SEARCH),
                            candidate -> candidate.phase() == SleighFlight.Phase.PARKED).stream()
                    .min(Comparator.comparingDouble(Santa.this::distanceToSqr)).orElse(null);
            return sleigh != null && distanceTo(sleigh) > BY_THE_SLEIGH;
        }

        @Override
        public boolean canContinueToUse() {
            return sleigh != null && sleigh.isAlive() && !isPassenger() && distanceTo(sleigh) > BY_THE_SLEIGH - 1.0
                    && !getNavigation().isDone();
        }

        @Override
        public void start() {
            if (sleigh != null) {
                getNavigation().moveTo(sleigh, STROLL);
            }
        }

        @Override
        public void stop() {
            sleigh = null;
        }
    }

    // --- sounds --------------------------------------------------------------------------------------------------

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return CozySounds.SANTA_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return CozySounds.SANTA_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return CozySounds.SANTA_DEATH.get();
    }

    @Override
    public SoundEvent getNotifyTradeSound() {
        return CozySounds.SANTA_YES.get();
    }

    @Override
    protected SoundEvent getTradeUpdatedSound(boolean validTrade) {
        return validTrade ? CozySounds.SANTA_YES.get() : CozySounds.SANTA_NO.get();
    }
}
