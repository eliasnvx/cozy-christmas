package dev.eliasnvx.cozychristmas.workshop;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.eliasnvx.cozychristmas.compat.snowyspirit.SnowySpiritGuards;
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
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.stats.Stats;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
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
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
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
 * Santa, at home in his workshop. He trades for emeralds ({@link SantaTrades#GOODS} and {@link SantaTrades#BUYING},
 * restocked every game day) and reads the player's karma, which is how players learn it: nice ones
 * pay {@code workshop.santa.nice_discount} less and in season get a present once a day; naughty ones he turns away.
 * He's neutral: whoever hurts him or an elf, or steals from the warehouse, he goes after ({@link WorkshopAlarm}) for
 * {@code anger_ticks}, and won't trade with them meanwhile.
 */
public class Santa extends AbstractVillager {

    /** Santa placed by the workshop stays around it. */
    private static final int HOME_RADIUS = 20;
    private static final int UNHAPPY_TICKS = 40;
    private static final double FOLLOW_RANGE = 32.0;
    private static final double KNOCKBACK_RESISTANCE = 0.6;
    private static final double CHASE = 1.2;
    private static final double STROLL = 0.6;
    private static final String RESTOCK_DAY = "restock_day";
    private static final String GUARDS_CALLED = "guards_called";
    /** How often Santa checks his Snowy Spirit guards are still kept round the workshop. */
    private static final int GUARD_CHECK = 200;
    private static final String OWED_THANKS = "owed_thanks";
    private static final String THANKED = "thanked";
    private static final String THANKED_DAY = "day";
    private static final String THANKED_PLAYER = "player";
    /** On delivery days Santa stays this close to his sleigh, and looks for it this far. */
    private static final double BY_THE_SLEIGH = 3.5;
    private static final double SLEIGH_SEARCH = 32.0;

    /** Whether he called his Snowy Spirit guards (once, in a generated workshop, with Snowy Spirit installed). */
    private boolean guardsCalled;
    private long restockDay = Long.MIN_VALUE;
    /**
     * Who Santa is angry at and until when (game time). Kept apart from the attack target: on peaceful he can't attack
     * a player, but still won't trade with them. Not saved, like the target.
     */
    private @Nullable UUID angryAt;
    private long angryUntil;
    /** Players who helped load the sleigh and haven't been thanked yet. */
    private final Set<UUID> owedThanks = new HashSet<>();
    /** The real day each helper last got a thank-you present: one a day, however often the sleigh flies. */
    private final Map<UUID, Long> thankedOn = new HashMap<>();

    public Santa(EntityType<? extends Santa> type, Level level) {
        super(type, level);
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
                                                  MobSpawnType reason, @Nullable SpawnGroupData groupData) {
        if (reason == MobSpawnType.STRUCTURE) {
            restrictTo(blockPosition(), HOME_RADIUS);
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
        angryAt = culprit.getUUID();
        angryUntil = level().getGameTime() + CozyConfig.common().workshop().santa().angerTicks();
        setTarget(culprit);
        return true;
    }

    /** By UUID: a player who logs out and back in (or dies) is still the one he's angry at. */
    public boolean isAngryAt(LivingEntity entity) {
        return entity.getUUID().equals(angryAt) && level().getGameTime() < angryUntil;
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if (level() instanceof ServerLevel server) {
            guardWorkshop(server);
        }
        if (angryAt == null || !(level() instanceof ServerLevel level)) {
            return;
        }
        LivingEntity culprit = level.getEntity(angryAt) instanceof LivingEntity living ? living : null;
        if (level.getGameTime() >= angryUntil || culprit != null && culprit.isAlive() && !WorkshopAlarm.canBeBlamed(culprit)) {
            if (getTarget() != null && angryAt.equals(getTarget().getUUID())) {
                setTarget(null);
            }
            angryAt = null;
        } else if (culprit != null && culprit.isAlive() && getTarget() == null) {
            setTarget(culprit); // back from a relog or a respawn
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
    public boolean hurt(DamageSource source, float damage) {
        boolean hurt = super.hurt(source, damage);
        if (hurt && level() instanceof ServerLevel level && source.getEntity() instanceof LivingEntity attacker) {
            WorkshopAlarm.raise(level, position(), attacker);
        }
        return hurt;
    }

    // --- trading -------------------------------------------------------------------------------------------------

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (isPassenger()) {
            return InteractionResult.PASS; // aboard his sleigh: no trading, no presents in the air
        }
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
        return InteractionResult.sidedSuccess(level().isClientSide());
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
        visitor.displayClientMessage(Component.translatable("message.cozychristmas.santa." + why), true);
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
        visitor.getInventory().placeItemBackInInventory(present); // drops it if full
        makeSound(CozySounds.SANTA_GIFT.get());
        level.sendParticles(ParticleTypes.HAPPY_VILLAGER, getX(), getY() + 1.8, getZ(), 8, 0.4, 0.4, 0.4, 0.0);
        visitor.displayClientMessage(Component.translatable("message.cozychristmas.santa.gift"), true);
        return true;
    }

    /**
     * Snowy Spirit (SPEC compat): in his workshop (where he has a home) Santa calls {@code workshop.santa.snowy_spirit_guards}
     * gingerbread golems once, and keeps them near it.
     */
    public void guardWorkshop(ServerLevel level) {
        if (!hasRestriction() || !SnowySpiritGuards.available()) {
            return;
        }
        if (!guardsCalled) {
            guardsCalled = true;
            SnowySpiritGuards.call(level, getRestrictCenter(), CozyConfig.common().workshop().santa().snowySpiritGuards());
        } else if (tickCount % GUARD_CHECK == 0) {
            SnowySpiritGuards.keepGuardsNear(level, getRestrictCenter());
        }
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
            visitor.getInventory().placeItemBackInInventory(present);
            level.sendParticles(ParticleTypes.HAPPY_VILLAGER, getX(), getY() + 1.8, getZ(), 8, 0.4, 0.4, 0.4, 0.0);
        }
        makeSound(CozySounds.SANTA_GIFT.get());
        visitor.displayClientMessage(Component.translatable("message.cozychristmas.santa.thanks"), true);
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
    protected void updateTrades() {
        if (level() instanceof ServerLevel level) { // offers only exist on the server
            MerchantOffers offers = getOffers();
            SantaTrades.GOODS.addOffers(level, offers);
            SantaTrades.BUYING.addOffers(level, offers);
        }
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
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        MobHome.save(this, tag);
        tag.putLong(RESTOCK_DAY, restockDay);
        tag.putBoolean(GUARDS_CALLED, guardsCalled);
        store(tag, OWED_THANKS, UUIDUtil.CODEC.listOf(), List.copyOf(owedThanks));
        store(tag, THANKED, Thanked.CODEC.listOf(), thankedOn.entrySet().stream()
                .map(entry -> new Thanked(entry.getKey(), entry.getValue())).toList());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        MobHome.load(this, tag);
        restockDay = tag.contains(RESTOCK_DAY, Tag.TAG_LONG) ? tag.getLong(RESTOCK_DAY) : Long.MIN_VALUE;
        guardsCalled = tag.getBoolean(GUARDS_CALLED);
        owedThanks.clear();
        read(tag, OWED_THANKS, UUIDUtil.CODEC.listOf()).ifPresent(owedThanks::addAll);
        thankedOn.clear();
        read(tag, THANKED, Thanked.CODEC.listOf()).ifPresent(list -> list.forEach(entry -> thankedOn.put(entry.player(), entry.day())));
    }

    private static <T> void store(CompoundTag tag, String key, Codec<T> codec, T value) {
        codec.encodeStart(NbtOps.INSTANCE, value).result().ifPresent(encoded -> tag.put(key, encoded));
    }

    private static <T> Optional<T> read(CompoundTag tag, String key, Codec<T> codec) {
        return tag.contains(key) ? codec.parse(NbtOps.INSTANCE, tag.get(key)).result() : Optional.empty();
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

    /** Flying through a hillside or a tree aboard his sleigh doesn't hurt him. */
    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        return source.is(DamageTypes.IN_WALL) && getVehicle() instanceof SantasSleigh || super.isInvulnerableTo(source);
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
