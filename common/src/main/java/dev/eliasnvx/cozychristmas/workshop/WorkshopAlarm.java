package dev.eliasnvx.cozychristmas.workshop;

import dev.eliasnvx.cozychristmas.config.CozyConfig;
import dev.eliasnvx.cozychristmas.karma.KarmaManager;
import dev.eliasnvx.cozychristmas.registry.CozySounds;
import dev.eliasnvx.cozychristmas.util.Players;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Trouble at the workshop: someone hurt Santa or an elf, or stole from the warehouse. Every Santa within
 * {@code workshop.santa.alarm_radius} goes after the culprit and the elves around run from them. Santa's people never
 * blame each other, and players in creative or spectator are left alone.
 */
public final class WorkshopAlarm {

    private WorkshopAlarm() {
    }

    /** A present of the warehouse was opened or broken: costs {@code karma.steal_present} and raises the alarm. */
    public static void theft(ServerLevel level, BlockPos pos, ServerPlayer thief) {
        if (!canBeBlamed(thief)) {
            return;
        }
        KarmaManager.add(thief, CozyConfig.common().karma().stealPresent());
        thief.sendOverlayMessage(Component.translatable("message.cozychristmas.workshop.theft"));
        raise(level, Vec3.atCenterOf(pos), thief);
    }

    /** Santas near {@code at} get angry at {@code culprit}, elves flee from them. */
    public static void raise(ServerLevel level, Vec3 at, LivingEntity culprit) {
        if (!canBeBlamed(culprit)) {
            return;
        }
        double radius = CozyConfig.common().workshop().santa().alarmRadius();
        AABB area = AABB.ofSize(at, radius * 2, radius * 2, radius * 2);
        boolean heard = false;
        for (Santa santa : level.getEntitiesOfClass(Santa.class, area, LivingEntity::isAlive)) {
            heard |= santa.angerAt(culprit);
        }
        for (Elf elf : level.getEntitiesOfClass(Elf.class, area, LivingEntity::isAlive)) {
            elf.fleeFrom(culprit);
        }
        if (heard) {
            level.playSound(null, at.x, at.y, at.z, CozySounds.ELF_ALARM.get(), SoundSource.NEUTRAL, 1.0F, 1.0F);
        }
    }

    /** Not one of Santa's own, and not a player out of the game's reach: only survival and adventure count. */
    static boolean canBeBlamed(LivingEntity culprit) {
        if (culprit instanceof Santa || culprit instanceof Elf || !culprit.isAlive()) {
            return false;
        }
        return !(culprit instanceof Player player) || Players.inPlay(player);
    }
}
