package dev.eliasnvx.cozychristmas.goblin;

import dev.eliasnvx.cozychristmas.config.CozyConfig;
import dev.eliasnvx.cozychristmas.karma.KarmaManager;
import dev.eliasnvx.cozychristmas.present.PresentDesign;
import dev.eliasnvx.cozychristmas.registry.CozyItems;
import dev.eliasnvx.cozychristmas.registry.CozySounds;
import dev.eliasnvx.cozychristmas.util.Players;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsRestrictionGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jetbrains.annotations.Nullable;

/**
 * A present goblin, the Sock Goblin's Christmas cousin (Femboy Mod). It runs at players, and its "attack" snatches a
 * whole stack from a random hotbar slot ({@link GoblinLoot}); then it runs away with it held up in its hand. Kill it to
 * get the stack back: it drops the loot, a surprise present on top, and the killer earns {@code karma.return_stolen}.
 * With nothing to take (or {@code goblins.present_goblin.steals} off) it just scratches.
 *
 * <p>A goblin holding loot never despawns, not even in peaceful (the stack is someone's), and doesn't attack anyone.
 * Goblins placed by a camp stay around it.
 */
public class PresentGoblin extends Monster {

    /** Eye height (blocks): 1.20.1 has none in the entity type. */
    private static final float EYE_HEIGHT = 0.8F;

    private static final int HOME_RADIUS = 16;
    private static final double FOLLOW_RANGE = 24.0;
    private static final double CHASE = 1.15;
    private static final double FLEE = 1.3;
    private static final double FLEE_SPRINT = 1.55;
    private static final float FLEE_DISTANCE = 20.0F;
    private static final float LOOK_DISTANCE = 8.0F;
    private static final float STEAL_PITCH = 1.2F;

    public PresentGoblin(EntityType<? extends PresentGoblin> type, Level level) {
        super(type, level);
        setCanPickUpLoot(false);
    }

    /** 1.20.1 has no eye height in the entity type: 26.3's. */
    @Override
    protected float getStandingEyeHeight(Pose pose, EntityDimensions dimensions) {
        return EYE_HEIGHT;
    }

    public static AttributeSupplier.Builder createAttributes(PresentGoblinSettings settings) {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, settings.health())
                .add(Attributes.ATTACK_DAMAGE, settings.attackDamage())
                .add(Attributes.MOVEMENT_SPEED, settings.speed())
                .add(Attributes.FOLLOW_RANGE, FOLLOW_RANGE);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(1, new AvoidEntityGoal<>(this, Player.class, FLEE_DISTANCE, FLEE, FLEE_SPRINT,
                player -> hasLoot() && !player.isSpectator()));
        goalSelector.addGoal(2, new MeleeAttackGoal(this, CHASE, false) {
            @Override
            public boolean canUse() {
                return !hasLoot() && super.canUse();
            }

            @Override
            public boolean canContinueToUse() {
                return !hasLoot() && super.canContinueToUse();
            }
        });
        goalSelector.addGoal(4, new MoveTowardsRestrictionGoal(this, 1.0));
        goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.9));
        goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, LOOK_DISTANCE));
        goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this) {
            @Override
            public boolean canUse() {
                return !hasLoot() && super.canUse();
            }
        }.setAlertOthers());
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true,
                target -> !hasLoot()));
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                                  MobSpawnType reason, @Nullable SpawnGroupData groupData,
                                                  @Nullable CompoundTag spawnTag) {
        if (reason == MobSpawnType.STRUCTURE) {
            restrictTo(blockPosition(), HOME_RADIUS);
            setPersistenceRequired();
        }
        return super.finalizeSpawn(level, difficulty, reason, groupData, spawnTag);
    }

    /** Holding someone's stolen stack. */
    public boolean hasLoot() {
        return !getMainHandItem().isEmpty();
    }

    /** Whether this goblin would rob this player now: empty-handed, theft on, a survival or adventure player. */
    public boolean canRob(Player player) {
        return !hasLoot() && CozyConfig.common().goblins().presentGoblin().steals() && player.isAlive()
                && Players.inPlay(player) && GoblinLoot.pickSlot(player.getInventory(), getRandom()) >= 0;
    }

    /** Takes one hotbar stack from the player into its hand (server); false if there was nothing to take. */
    public boolean steal(ServerPlayer player) {
        if (!canRob(player)) {
            return false;
        }
        int slot = GoblinLoot.pickSlot(player.getInventory(), getRandom());
        ItemStack loot = player.getInventory().removeItem(slot, player.getInventory().getItem(slot).getCount());
        setItemSlot(EquipmentSlot.MAINHAND, loot);
        setGuaranteedDrop(EquipmentSlot.MAINHAND);
        setPersistenceRequired();
        setTarget(null);
        swing(InteractionHand.MAIN_HAND);
        playSound(CozySounds.PRESENT_GOBLIN_STEAL.get(), 1.0F, STEAL_PITCH);
        player.displayClientMessage(Component.translatable("message.cozychristmas.present_goblin.stole", loot.getHoverName()), true);
        return true;
    }

    /** The attack is a snatch when there's something to snatch. */
    @Override
    public boolean doHurtTarget(Entity target) {
        if (target instanceof ServerPlayer player && steal(player)) {
            return true;
        }
        return super.doHurtTarget(target);
    }

    /**
     * The stolen stack is the player's: it drops whatever the gamerules (mob loot off) or its curses (vanishing) say,
     * so it's dropped here, not as equipment. A player's kill earns {@code karma.return_stolen} and a surprise present
     * once a game day.
     */
    @Override
    public void die(DamageSource source) {
        if (hasLoot() && !isRemoved() && !dead && level() instanceof ServerLevel) {
            ItemStack loot = getMainHandItem();
            setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
            spawnAtLocation(loot);
            if (source.getEntity() instanceof ServerPlayer killer && KarmaManager.takeReturnReward(killer)) {
                KarmaManager.add(killer, CozyConfig.common().karma().returnStolen());
                PresentDesign[] designs = PresentDesign.values();
                spawnAtLocation(CozyItems.present(designs[getRandom().nextInt(designs.length)]));
                killer.displayClientMessage(Component.translatable("message.cozychristmas.present_goblin.returned"), true);
            }
        }
        super.die(source);
    }

    /** Peaceful removes monsters; a goblin with someone's things stays (and can't attack there anyway). */
    @Override
    public void checkDespawn() {
        if (hasLoot() && level().getDifficulty() == Difficulty.PEACEFUL) {
            return;
        }
        super.checkDespawn();
    }

    @Override
    public boolean removeWhenFarAway(double distanceSquared) {
        return !hasLoot() && super.removeWhenFarAway(distanceSquared);
    }

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return CozySounds.PRESENT_GOBLIN_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return CozySounds.PRESENT_GOBLIN_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return CozySounds.PRESENT_GOBLIN_DEATH.get();
    }
}
