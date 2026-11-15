package dev.eliasnvx.cozychristmas.goblin;

import dev.eliasnvx.cozychristmas.config.CozyConfig;
import dev.eliasnvx.cozychristmas.registry.CozySounds;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * A gingerbread man: small, quick, weak alone, in packs of four to six around goblin camps (the camp structure's spawn
 * overrides). Hit one and the whole pack comes. Water makes them soggy ({@code goblins.gingerbread_man.soggy}): it hurts
 * like it hurts endermen. They crumble into gingerbread cookies.
 */
public class GingerbreadMan extends Monster {

    private static final int HOME_RADIUS = 16;
    private static final double FOLLOW_RANGE = 20.0;
    private static final double CHASE = 1.2;
    private static final float LOOK_DISTANCE = 8.0F;
    private static final float STEP_VOLUME = 0.15F;

    public GingerbreadMan(EntityType<? extends GingerbreadMan> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes(GingerbreadManSettings settings) {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, settings.health())
                .add(Attributes.ATTACK_DAMAGE, settings.attackDamage())
                .add(Attributes.MOVEMENT_SPEED, settings.speed())
                .add(Attributes.FOLLOW_RANGE, FOLLOW_RANGE);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(2, new MeleeAttackGoal(this, CHASE, false));
        goalSelector.addGoal(4, new MoveTowardsRestrictionGoal(this, 1.0));
        goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.9));
        goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, LOOK_DISTANCE));
        goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers());
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                                  EntitySpawnReason reason, @Nullable SpawnGroupData groupData) {
        if (reason == EntitySpawnReason.STRUCTURE) {
            setHomeTo(blockPosition(), HOME_RADIUS);
            setPersistenceRequired();
        }
        return super.finalizeSpawn(level, difficulty, reason, groupData);
    }

    @Override
    public boolean isSensitiveToWater() {
        return CozyConfig.common().goblins().gingerbreadMan().soggy();
    }

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return CozySounds.GINGERBREAD_MAN_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return CozySounds.GINGERBREAD_MAN_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return CozySounds.GINGERBREAD_MAN_DEATH.get();
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        playSound(CozySounds.GINGERBREAD_MAN_STEP.get(), STEP_VOLUME, 1.0F);
    }
}
