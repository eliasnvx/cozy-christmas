package dev.eliasnvx.cozychristmas.krampus;

import dev.architectury.event.events.common.TickEvent;
import dev.eliasnvx.cozychristmas.config.CozyConfig;
import dev.eliasnvx.cozychristmas.karma.KarmaManager;
import dev.eliasnvx.cozychristmas.registry.CozyEntities;
import dev.eliasnvx.cozychristmas.registry.CozySounds;
import dev.eliasnvx.cozychristmas.season.ChristmasSeason;
import dev.eliasnvx.cozychristmas.util.Players;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * SPEC "Krampus comes to a player with very bad karma himself" ({@link KrampusVisitSettings}): at night in the
 * Overworld a very naughty survival player may hear chains, and he steps out of the dark nearby. He leaves at dawn
 * if nobody beat him; beating him clears the bad karma. Nothing ticks per player: one check per level every
 * {@code check_ticks}.
 */
public final class KrampusVisits {

    private KrampusVisits() {
    }

    public static void register() {
        TickEvent.SERVER_LEVEL_POST.register(KrampusVisits::tick);
    }

    private static void tick(ServerLevel level) {
        KrampusVisitSettings visits = CozyConfig.common().krampus().visits();
        if (!visits.enabled() || level.dimension() != Level.OVERWORLD || level.getGameTime() % visits.checkTicks() != 0) {
            return;
        }
        for (ServerPlayer player : level.players()) {
            if (mayVisit(level, player) && level.getRandom().nextDouble() < visits.chance()) {
                visit(level, player);
            }
        }
    }

    /** Whether {@code player} may get a visit right now (chance aside). */
    public static boolean mayVisit(ServerLevel level, ServerPlayer player) {
        KrampusVisitSettings visits = CozyConfig.common().krampus().visits();
        return Players.inPlay(player) && level.dimension() == Level.OVERWORLD
                && visits.mayVisit(KarmaManager.get(player), level.isDarkOutside(), ChristmasSeason.isActive(),
                KarmaManager.today(level), KarmaManager.lastKrampusVisit(player));
    }

    /** Sends him to {@code player} now, whatever the rules say. Returns him, or null if there was no room nearby. */
    public static @Nullable Krampus visit(ServerLevel level, ServerPlayer player) {
        KrampusVisitSettings visits = CozyConfig.common().krampus().visits();
        Krampus krampus = CozyEntities.KRAMPUS.get().create(level, EntitySpawnReason.EVENT);
        if (krampus == null) {
            return null;
        }
        Vec3 spot = Krampus.visitSpot(level, krampus, player.blockPosition(), visits.minDistance(), visits.maxDistance());
        if (spot == null) {
            return null;
        }
        krampus.snapTo(spot.x, spot.y, spot.z, level.getRandom().nextFloat() * 360.0F, 0.0F);
        krampus.finalizeSpawn(level, level.getCurrentDifficultyAt(krampus.blockPosition()), EntitySpawnReason.EVENT, null);
        krampus.visit(player);
        level.addFreshEntity(krampus);
        KarmaManager.markKrampusVisit(player);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), CozySounds.KRAMPUS_CHAINS.get(), SoundSource.HOSTILE,
                1.0F, 0.8F);
        player.sendSystemMessage(Component.translatable("message.cozychristmas.krampus.visit"));
        return krampus;
    }
}
