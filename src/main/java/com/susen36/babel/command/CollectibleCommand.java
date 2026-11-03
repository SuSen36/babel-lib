package com.susen36.babel.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.susen36.babel.collectible.Collectibles;
import com.susen36.babel.network.BabelNetwork;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;

public class CollectibleCommand {

    private static final DynamicCommandExceptionType ERROR_UNKNOWN_COLLECTIBLE = new DynamicCommandExceptionType(obj -> Component.literal("Unknown collectible: " + obj));

    private static final LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal("collectible")
            .then(Commands.literal("give").requires(s -> s.hasPermission(2))
                    .then(Commands.argument("targets", EntityArgument.players())
                            .then(Commands.argument("collectible", StringArgumentType.word())
                                    .suggests((ctx, builder) ->
                                            SharedSuggestionProvider.suggest(Collectibles.Collectibles.keySet(), builder))
                                    .executes(ctx -> give(ctx, 0))
                                    .then(Commands.argument("layer", IntegerArgumentType.integer(0))
                                            .executes(ctx -> give(ctx, IntegerArgumentType.getInteger(ctx, "layer")))))))
            .then(Commands.literal("clear").requires(s -> s.hasPermission(2))
                    .then(Commands.argument("targets", EntityArgument.players())
                            .executes(CollectibleCommand::clearCollectibles)
                            .then(Commands.argument("collectible", StringArgumentType.word())
                                    .suggests((ctx, builder) ->
                                            SharedSuggestionProvider.suggest(Collectibles.Collectibles.keySet(), builder))
                                    .executes(CollectibleCommand::clearCollectible))));

    public static LiteralArgumentBuilder<CommandSourceStack> get() {
        return command;
    }

    private static Item resolveCollectible(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        String id = StringArgumentType.getString(ctx, "collectible");
        var entry = Collectibles.Collectibles.get(id);
        if (entry != null && entry.get() != null) {
            return entry.get();
        }
        throw ERROR_UNKNOWN_COLLECTIBLE.create(id);
    }

    private static int give(CommandContext<CommandSourceStack> ctx, int layer) throws CommandSyntaxException {
        Item item = resolveCollectible(ctx);
        Collection<ServerPlayer> targets = EntityArgument.getPlayers(ctx, "targets");
        for (ServerPlayer player : targets) {
            if (layer > 0) {
                Collectibles.addLayerAndSync(player, item, layer);
            }
            Collectibles.activateAndSync(player, item);
        }
        sendFeedback(ctx, "command.collectible.give", targets, item);
        return targets.size();
    }

    private static int clearCollectible(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        Item item = resolveCollectible(ctx);
        Collection<ServerPlayer> targets = EntityArgument.getPlayers(ctx, "targets");
        for (ServerPlayer player : targets) {
            Collectibles.deactivateAndSync(player, item);
        }
        sendFeedback(ctx, "command.collectible.remove", targets, item);
        return targets.size();
    }

    private static int clearCollectibles(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        Collection<ServerPlayer> targets = EntityArgument.getPlayers(ctx, "targets");
        for (ServerPlayer player : targets) {
            var data = Collectibles.getData(player);
            for (Item item : new ArrayList<>(data.getUsed())) {
                data.remove(item);
            }
            BabelNetwork.syncCollectibles(player);
        }
        sendFeedback(ctx, "command.collectible.clear", targets, null);
        return targets.size();
    }

    private static void sendFeedback(CommandContext<CommandSourceStack> ctx, String key, Collection<ServerPlayer> targets, @Nullable Item item) {
        String collectible = item == null ? "" : Component.translatable(item.getDescriptionId()).getString();
        String info;
        if (targets.size() == 1) {
            info = Component.translatable(key + ".single").getString()
                    .replace("<player>", targets.iterator().next().getGameProfile().getName())
                    .replace("<collectible>", collectible);
        } else {
            info = Component.translatable(key + ".mult").getString()
                    .replace("<num>", String.valueOf(targets.size()))
                    .replace("<collectible>", collectible);
        }
        ctx.getSource().sendSuccess(() -> Component.literal(info), true);
    }
}