package com.susen36.babel.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.susen36.babel.capability.BabelCapability;
import com.susen36.babel.capability.ep.EPCapability;
import com.susen36.babel.elemental.base.AbstractEPCapability;
import com.susen36.babel.elemental.base.ElementalInjurySource;
import com.susen36.babel.init.BabelAttributes;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.Arrays;

public class EPCommand {

    private static final DynamicCommandExceptionType ERROR_UNKNOWN_EP_TYPE = new DynamicCommandExceptionType(obj -> Component.translatable("command.ep.unknown_type", obj));

    private static final LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal("ep").then(
            Commands.argument("entities", EntityArgument.entities())
                    .then(Commands.argument("type", StringArgumentType.word()).suggests((ctx, builder) ->
                                    SharedSuggestionProvider.suggest(Arrays.stream(AbstractEPCapability.EPType.values()).map(Enum::name), builder))
                            .then(Commands.literal("hurt").requires(s -> s.hasPermission(2))
                                    .then(Commands.argument("amount", IntegerArgumentType.integer(0))
                                            .executes(EPCommand::epHurt)))
                            .then(Commands.literal("heal").requires(s -> s.hasPermission(2))
                                    .then(Commands.argument("amount", IntegerArgumentType.integer(0))
                                            .executes(EPCommand::epHeal)))
                            .then(Commands.literal("check").executes(EPCommand::sendEPValue))
                    ));

    public static LiteralArgumentBuilder<CommandSourceStack> get() {
        return command;
    }

    private static AbstractEPCapability.EPType resolveType(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        String name = StringArgumentType.getString(ctx, "type");
        for (AbstractEPCapability.EPType t : AbstractEPCapability.EPType.values()) {
            if (t.name().equalsIgnoreCase(name)) return t;
        }
        throw ERROR_UNKNOWN_EP_TYPE.create(ctx.getInput());
    }

    private static int epHurt(CommandContext<CommandSourceStack> argument) throws CommandSyntaxException {
        AbstractEPCapability.EPType type = resolveType(argument);
        Entity[] entities = EntityArgument.getEntities(argument, "entities").toArray(new Entity[0]);
        int amount = IntegerArgumentType.getInteger(argument, "amount");
        int executedCount = 0;
        LivingEntity singleCandidate = null;
        for (Entity entity : entities) {
            if (entity instanceof LivingEntity living) {
                EPCapability ep = BabelCapability.getEP(living);
                if (ep != null && ep.hurt(type, ElementalInjurySource.fromCommand(argument.getSource()), amount)) {
                    if (executedCount == 0) singleCandidate = living;
                    executedCount++;
                }
            }
        }
        if (executedCount == 1 && singleCandidate != null) {
            final LivingEntity finalSingleCandidate = singleCandidate;
            argument.getSource().sendSuccess(() -> Component.translatable("command.ep.hurt.single",
                    finalSingleCandidate.getDisplayName(), amount, type.description()), true);
        } else if (executedCount > 0) {
            final int finalExecutedCount = executedCount;
            argument.getSource().sendSuccess(() -> Component.translatable("command.ep.hurt.mult",
                    finalExecutedCount, amount, type.description()), true);
        } else {
            argument.getSource().sendFailure(Component.translatable("command.ep.not_found"));
        }
        return executedCount;
    }

    private static int epHeal(CommandContext<CommandSourceStack> argument) throws CommandSyntaxException {
        AbstractEPCapability.EPType type = resolveType(argument);
        Entity[] entities = EntityArgument.getEntities(argument, "entities").toArray(new Entity[0]);
        int amount = IntegerArgumentType.getInteger(argument, "amount");
        int executedCount = 0;
        LivingEntity singleCandidate = null;
        for (Entity entity : entities) {
            if (entity instanceof LivingEntity living) {
                EPCapability ep = BabelCapability.getEP(living);
                if (ep != null) {
                    int before = ep.getValue(type);
                    ep.heal(type, amount);
                    if (ep.getValue(type) != before) {
                        singleCandidate = living;
                        executedCount++;
                    }
                }
            }
        }
        if (executedCount == 1 && singleCandidate != null) {
            final LivingEntity finalSingleCandidate = singleCandidate;
            argument.getSource().sendSuccess(() -> Component.translatable("command.ep.heal.single",
                    finalSingleCandidate.getDisplayName(), amount, type.description()), true);
        } else if (executedCount > 0) {
            final int finalExecutedCount = executedCount;
            argument.getSource().sendSuccess(() -> Component.translatable("command.ep.heal.mult",
                    finalExecutedCount, amount, type.description()), true);
        } else {
            argument.getSource().sendFailure(Component.translatable("command.ep.not_found"));
        }
        return executedCount;
    }

    private static int sendEPValue(CommandContext<CommandSourceStack> argument) throws CommandSyntaxException {
        AbstractEPCapability.EPType type = resolveType(argument);
        Entity[] entities = EntityArgument.getEntities(argument, "entities").toArray(new Entity[0]);
        long total = 0;
        for (Entity entity : entities) {
            if (entity instanceof LivingEntity living) {
                EPCapability ep = BabelCapability.getEP(living);
                if (ep != null) {
                    int value = ep.getValue(type);
                    if (value < 0) {
                        continue;
                    }
                    total += value;
                    Component feedback = Component.translatable("command.ep.check", living.getDisplayName(), type.description(),
                            value, (int) BabelAttributes.getMaxElementalValue(living));
                    if (entity instanceof Player player && !player.level().isClientSide()) {
                        player.displayClientMessage(feedback, false);
                    } else {
                        argument.getSource().sendSystemMessage(feedback);
                    }
                }
            }
        }
        // Multi-target queries sum valid values; Brigadier results are signed ints.
        return (int) Math.min(total, Integer.MAX_VALUE);
    }
}