package dev.eliasnvx.cozychristmas.compat.snowyspirit;

import dev.architectury.platform.Platform;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.phys.AABB;

import java.util.Optional;

/**
 * Snowy Spirit (SPEC compat): its gingerbread golems stand guard at Santa's workshop. Santa calls them once, when he's
 * first at home in a generated workshop, and keeps them around it. Snowy Spirit is looked up by entity id, so nothing
 * of it is needed to compile or to run without it.
 */
public final class SnowySpiritGuards {

    public static final String MOD_ID = "snowyspirit";
    public static final ResourceLocation GINGERBREAD_GOLEM = ResourceLocation.fromNamespaceAndPath(MOD_ID, "gingerbread_golem");
    /** Guards stand within this many blocks of Santa's home, and are kept within {@link #GUARD_RANGE} of it. */
    private static final int SPREAD = 4;
    private static final int GUARD_RANGE = 16;
    private static final int TRIES_PER_GUARD = 6;
    private static final int GROUND_SEARCH = 3;

    private SnowySpiritGuards() {
    }

    public static boolean available() {
        return Platform.isModLoaded(MOD_ID) && golemType().isPresent();
    }

    /** Spawns up to {@code count} guards round {@code home}; returns how many came. */
    public static int call(ServerLevel level, BlockPos home, int count) {
        Optional<EntityType<?>> type = golemType();
        if (count <= 0 || type.isEmpty() || !Platform.isModLoaded(MOD_ID)) {
            return 0;
        }
        RandomSource random = level.getRandom();
        int spawned = 0;
        for (int i = 0; i < count * TRIES_PER_GUARD && spawned < count; i++) {
            BlockPos spot = standingSpot(level, home.offset(random.nextIntBetweenInclusive(-SPREAD, SPREAD), 0,
                    random.nextIntBetweenInclusive(-SPREAD, SPREAD)));
            if (spot == null) {
                continue;
            }
            Entity entity = type.get().create(level);
            if (!(entity instanceof Mob guard)) {
                continue;
            }
            guard.moveTo(spot.getX() + 0.5, spot.getY(), spot.getZ() + 0.5, random.nextFloat() * 360.0F, 0.0F);
            guard.finalizeSpawn(level, level.getCurrentDifficultyAt(spot), MobSpawnType.STRUCTURE, null);
            guard.setPersistenceRequired();
            keepNear(guard, home);
            level.addFreshEntity(guard);
            spawned++;
        }
        return spawned;
    }

    /** Guards near {@code home} that wandered off its leash (1.21.1 mobs don't save their home) get it back. */
    public static void keepGuardsNear(ServerLevel level, BlockPos home) {
        Optional<EntityType<?>> type = golemType();
        if (type.isEmpty()) {
            return;
        }
        for (Entity entity : level.getEntities(type.get(), new AABB(home).inflate(GUARD_RANGE * 2), Entity::isAlive)) {
            if (entity instanceof PathfinderMob guard && !guard.hasRestriction()) {
                keepNear(guard, home);
            }
        }
    }

    private static void keepNear(Mob guard, BlockPos home) {
        if (guard instanceof PathfinderMob pathfinder) {
            pathfinder.restrictTo(home, GUARD_RANGE);
        }
    }

    private static Optional<EntityType<?>> golemType() {
        return BuiltInRegistries.ENTITY_TYPE.getOptional(GINGERBREAD_GOLEM);
    }

    /** Two free blocks over solid ground, near {@code around}'s height (inside a house too, not on its roof). */
    private static BlockPos standingSpot(ServerLevel level, BlockPos around) {
        for (int dy = GROUND_SEARCH; dy >= -GROUND_SEARCH; dy--) {
            BlockPos feet = around.above(dy);
            BlockPos ground = feet.below();
            if (level.getBlockState(feet).isAir() && level.getBlockState(feet.above()).isAir()
                    && level.getBlockState(ground).isFaceSturdy(level, ground, Direction.UP)) {
                return feet;
            }
        }
        return null;
    }
}
