package dev.eliasnvx.cozychristmas.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.eliasnvx.cozychristmas.CozyChristmas;
import dev.eliasnvx.cozychristmas.config.CozyConfig;
import dev.eliasnvx.cozychristmas.karma.Karma;
import dev.eliasnvx.cozychristmas.karma.KarmaManager;
import dev.eliasnvx.cozychristmas.season.ChristmasSeason;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;
import java.util.function.ToIntFunction;

/**
 * {@code /cozychristmas karma get|set|add} and {@code /cozychristmas season}: operator tools for servers and testing.
 * Karma is hidden from players, so this is the only place its number shows.
 */
public final class CozyCommands {

    private CozyCommands() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context,
                                Commands.CommandSelection selection) {
        dispatcher.register(Commands.literal(CozyChristmas.MOD_ID)
                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(Commands.literal("karma")
                        .then(Commands.literal("get")
                                .then(Commands.argument("targets", EntityArgument.players())
                                        .executes(ctx -> forEach(ctx, KarmaManager::get))))
                        .then(Commands.literal("set")
                                .then(Commands.argument("targets", EntityArgument.players())
                                        .then(Commands.argument("value", IntegerArgumentType.integer(Karma.MIN, Karma.MAX))
                                                .executes(ctx -> forEach(ctx, player -> {
                                                    KarmaManager.set(player, IntegerArgumentType.getInteger(ctx, "value"));
                                                    return KarmaManager.get(player);
                                                })))))
                        .then(Commands.literal("add")
                                .then(Commands.argument("targets", EntityArgument.players())
                                        .then(Commands.argument("delta", IntegerArgumentType.integer(-2 * Karma.MAX, 2 * Karma.MAX))
                                                .executes(ctx -> forEach(ctx, player ->
                                                        KarmaManager.add(player, IntegerArgumentType.getInteger(ctx, "delta"))))))))
                .then(Commands.literal("season").executes(CozyCommands::season)));
    }

    /** Runs {@code action} on every target and reports the resulting karma. Returns how many players were affected. */
    private static int forEach(CommandContext<CommandSourceStack> ctx, ToIntFunction<ServerPlayer> action)
            throws CommandSyntaxException {
        Collection<ServerPlayer> targets = EntityArgument.getPlayers(ctx, "targets");
        for (ServerPlayer player : targets) {
            int karma = action.applyAsInt(player);
            Component tier = Component.translatable(CozyConfig.common().karma().tier(karma).translationKey());
            ctx.getSource().sendSuccess(() -> Component.translatable("commands.cozychristmas.karma.value",
                    player.getDisplayName(), karma, tier), false);
        }
        return targets.size();
    }

    private static int season(CommandContext<CommandSourceStack> ctx) {
        var settings = CozyConfig.common().season();
        String key = ChristmasSeason.isWorkshopDelivery() ? "delivery" : ChristmasSeason.isActive() ? "active" : "inactive";
        ctx.getSource().sendSuccess(() -> Component.translatable("commands.cozychristmas.season." + key,
                ChristmasSeason.today().toString(), settings.source().getSerializedName()), false);
        return ChristmasSeason.isActive() ? 1 : 0;
    }
}
