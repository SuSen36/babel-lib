package com.susen36.babel.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.susen36.babel.collectible.CollectibleItem;
import com.susen36.babel.collectible.Collectibles;
import com.susen36.babel.network.BabelNetwork;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CollectibleCommand {

    private static final DynamicCommandExceptionType ERROR_UNKNOWN_COLLECTIBLE = new DynamicCommandExceptionType(obj -> Component.translatable("command.collectible.unknown", obj.toString()));

    private static final LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal("collectible")
            .then(Commands.literal("give").requires(s -> s.hasPermission(2))
                    .then(Commands.argument("targets", EntityArgument.players())
                            .then(Commands.argument("collectible", ResourceLocationArgument.id())
                                    .suggests((ctx, builder) ->
                                            SharedSuggestionProvider.suggestResource(Collectibles.getRegistry().keySet(), builder))
                                    .executes(ctx -> give(ctx, 0))
                                    .then(Commands.argument("layer", IntegerArgumentType.integer(0))
                                            .executes(ctx -> give(ctx, IntegerArgumentType.getInteger(ctx, "layer")))))))
            .then(Commands.literal("clear").requires(s -> s.hasPermission(2))
                    .then(Commands.argument("targets", EntityArgument.players())
                            .executes(CollectibleCommand::clearCollectibles)
                            .then(Commands.argument("collectible", ResourceLocationArgument.id())
                                    .suggests((ctx, builder) ->
                                            SharedSuggestionProvider.suggestResource(Collectibles.getRegistry().keySet(), builder))
                                    .executes(CollectibleCommand::clearCollectible))));

    public static LiteralArgumentBuilder<CommandSourceStack> get() {
        return command;
    }

    private static Item resolveCollectible(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ResourceLocation id = ResourceLocationArgument.getId(ctx, "collectible");
        var entry = Collectibles.getRegistry().get(id);
        if (entry != null && entry.get() != null) {
            return entry.get();
        }
        throw ERROR_UNKNOWN_COLLECTIBLE.create(id);
    }

    private static int give(CommandContext<CommandSourceStack> ctx, int layer) throws CommandSyntaxException {
        Item item = resolveCollectible(ctx);
        Collection<ServerPlayer> targets = EntityArgument.getPlayers(ctx, "targets");
        List<ServerPlayer> written = new ArrayList<>();
        for (ServerPlayer player : targets) {
            Collectibles data = Collectibles.getData(player);
            if (data != null && item instanceof CollectibleItem) {
                if (layer > 0) {
                    Collectibles.addLayerAndSync(player, item, layer);
                }
                if (Collectibles.getData(player) == data) {
                    boolean activated = Collectibles.activateAndSync(player, item);
                    if (activated) {
                        written.add(player);
                    }
                }
            }
        }
        sendFeedback(ctx, "command.collectible.give", written, item);
        return written.size();
    }

    private static int clearCollectible(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        Item item = resolveCollectible(ctx);
        Collection<ServerPlayer> targets = EntityArgument.getPlayers(ctx, "targets");
        List<ServerPlayer> written = new ArrayList<>();
        for (ServerPlayer player : targets) {
            Collectibles data = Collectibles.getData(player);
            if (data != null) {
                boolean removed = Collectibles.removeAndSync(player, item);
                if (removed && Collectibles.getData(player) == data) {
                    written.add(player);
                }
            }
        }
        sendFeedback(ctx, "command.collectible.remove", written, item);
        return written.size();
    }

    private static int clearCollectibles(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        Collection<ServerPlayer> targets = EntityArgument.getPlayers(ctx, "targets");
        List<ServerPlayer> written = new ArrayList<>();
        for (ServerPlayer player : targets) {
            Collectibles data = Collectibles.getData(player);
            if (data != null) {
                for (Item item : new ArrayList<>(data.getUsed())) {
                    if (Collectibles.getData(player) != data) {
                        break;
                    }
                    data.remove(item);
                }
                if (Collectibles.getData(player) == data) {
                    BabelNetwork.syncCollectibles(player);
                    written.add(player);
                }
            }
        }
        sendFeedback(ctx, "command.collectible.clear", written, null);
        return written.size();
    }

    private static void sendFeedback(CommandContext<CommandSourceStack> ctx, String key, Collection<ServerPlayer> targets, @Nullable Item item) {
        if (!targets.isEmpty()) {
            boolean single = targets.size() == 1;
            String feedbackKey = key + (single ? ".single" : ".mult");
            Object target = single ? targets.iterator().next().getDisplayName() : targets.size();
            Component feedback = item == null ? Component.translatable(feedbackKey, target)
                    : Component.translatable(feedbackKey, target, item.getDescription());
            ctx.getSource().sendSuccess(() -> feedback, true);
        }
    }
}