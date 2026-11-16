package dev.eliasnvx.cozychristmas.krampus;

import dev.eliasnvx.cozychristmas.registry.CozyBlocks;
import dev.eliasnvx.cozychristmas.registry.CozyEffects;
import dev.eliasnvx.cozychristmas.registry.CozyEntities;
import dev.eliasnvx.cozychristmas.registry.CozySounds;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/**
 * Krampus's sack over a victim, for as long as it is {@link SackedEffect sacked}: it sits on the victim, wobbles while
 * they struggle and bursts open when the effect ends. Purely the visible part of the effect; it isn't saved, can't be
 * hit and blocks nothing.
 */
public class KrampusSack extends Entity {

    private static final EntityDataAccessor<Integer> DATA_VICTIM = SynchedEntityData.defineId(KrampusSack.class,
            EntityDataSerializers.INT);
    /** Never outlives the effect by more than this, whatever happens to the victim. */
    private static final int GRACE_TICKS = 10;
    private static final int BURST_PARTICLES = 20;

    private int maxAge;

    public KrampusSack(EntityType<? extends KrampusSack> type, Level level) {
        super(type, level);
        noPhysics = true;
    }

    /** A sack over {@code victim} for {@code ticks}; the caller gives the effect and adds the entity. */
    public static KrampusSack over(LivingEntity victim, int ticks) {
        KrampusSack sack = new KrampusSack(CozyEntities.KRAMPUS_SACK.get(), victim.level());
        sack.entityData.set(DATA_VICTIM, victim.getId());
        sack.maxAge = ticks + GRACE_TICKS;
        sack.setPos(victim.position());
        return sack;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_VICTIM, -1);
    }

    public @Nullable LivingEntity victim() {
        return level().getEntity(entityData.get(DATA_VICTIM)) instanceof LivingEntity living ? living : null;
    }

    @Override
    public void tick() {
        super.tick();
        LivingEntity victim = victim();
        if (victim != null) {
            setPos(victim.position());
        }
        if (level() instanceof ServerLevel server && (victim == null || !victim.isAlive()
                || !victim.hasEffect(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(CozyEffects.SACKED.get())) || tickCount > maxAge)) {
            burst(server);
        }
    }

    private void burst(ServerLevel server) {
        server.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, CozyBlocks.SACK.get().defaultBlockState()),
                getX(), getY() + getBbHeight() * 0.6, getZ(), BURST_PARTICLES, 0.35, 0.5, 0.35, 0.05);
        server.playSound(null, getX(), getY(), getZ(), CozySounds.KRAMPUS_SACK_OPEN.get(), SoundSource.HOSTILE, 0.8F, 1.0F);
        discard();
    }

    @Override
    public boolean shouldBeSaved() {
        return false;
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        return false;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
    }
}
