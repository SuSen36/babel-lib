package com.susen36.babel.command;

import com.mojang.brigadier.arguments.FloatArgumentType;
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

    private static final DynamicCommandExceptionType ERROR_UNKNOWN_EP_TYPE = new DynamicCommandExceptionType(obj -> Component.literal("Unknown elemental injury type: " + obj));

    private static final LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal("ep").then(
            Commands.argument("entities", EntityArgument.entities())
                    .then(Commands.argument("type", StringArgumentType.word()).suggests((ctx, builder) ->
                                    SharedSuggestionProvider.suggest(Arrays.stream(AbstractEPCapability.EPType.values()).map(Enum::name), builder))
                            .then(Commands.literal("hurt").requires(s -> s.hasPermission(2))
                                    .then(Commands.argument("amount", FloatArgumentType.floatArg(0))
                                            .executes(EPCommand::epHurt)))
                            .then(Commands.literal("heal").requires(s -> s.hasPermission(2))
                                    .then(Commands.argument("amount", FloatArgumentType.floatArg(0))
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
        float amount = FloatArgumentType.getFloat(argument, "amount");
        int executedCount = 0;
        LivingEntity singleCandidate = null;
        for (Entity entity : entities) {
            if (entity instanceof LivingEntity living) {
                EPCapability ep = BabelCapability.getEP(living);
                if (ep.hurt(type, ElementalInjurySource.fromCommand(argument.getSource()), amount)) {
                    if (executedCount == 0) singleCandidate = living;
                    executedCount++;
                }
            }
        }
        if (executedCount == 1) {
            final LivingEntity finalSingleCandidate = singleCandidate;
            argument.getSource().sendSuccess(() -> Component.literal(String.format("对 %s 造成了 %.1f 点 %s",
                            finalSingleCandidate.getDisplayName().getString(), amount, type.description())),
                    true);
        } else if (executedCount > 0) {
            final int finalExecutedCount = executedCount;
            argument.getSource().sendSuccess(() -> Component.literal(String.format("对 %d 个实体造成了 %.1f 点 %s",
                            finalExecutedCount, amount, type.description())),
                    true);
        } else {
            argument.getSource().sendFailure(Component.literal("未找到实体！"));
        }
        return executedCount;
    }

    private static int epHeal(CommandContext<CommandSourceStack> argument) throws CommandSyntaxException {
        AbstractEPCapability.EPType type = resolveType(argument);
        Entity[] entities = EntityArgument.getEntities(argument, "entities").toArray(new Entity[0]);
        float amount = FloatArgumentType.getFloat(argument, "amount");
        int executedCount = 0;
        LivingEntity singleCandidate = null;
        for (Entity entity : entities) {
            if (entity instanceof LivingEntity living) {
                EPCapability ep = BabelCapability.getEP(living);
                ep.heal(type, ElementalInjurySource.fromCommand(argument.getSource()), amount);
                if (executedCount == 0) singleCandidate = living;
                executedCount++;
            }
        }
        if (executedCount == 1) {
            final LivingEntity finalSingleCandidate = singleCandidate;
            argument.getSource().sendSuccess(() -> Component.literal(String.format("为 %s 治愈了 %.1f 点 %s",
                            finalSingleCandidate.getDisplayName().getString(), amount, type.description())),
                    true);
        } else if (executedCount > 0) {
            final int finalExecutedCount = executedCount;
            argument.getSource().sendSuccess(() -> Component.literal(String.format("为 %d 个实体治愈了 %.1f 点 %s",
                            finalExecutedCount, amount, type.description())),
                    true);
        } else {
            argument.getSource().sendFailure(Component.literal("未找到实体！"));
        }
        return executedCount;
    }

    private static int sendEPValue(CommandContext<CommandSourceStack> argument) throws CommandSyntaxException {
        AbstractEPCapability.EPType type = resolveType(argument);
        Entity[] entities = EntityArgument.getEntities(argument, "entities").toArray(new Entity[0]);
        for (Entity entity : entities) {
            if (entity instanceof LivingEntity living) {
                EPCapability ep = BabelCapability.getEP(living);
                if (entity instanceof Player player && !player.level().isClientSide()) {
                    player.displayClientMessage(Component.literal(
                                    String.format("%s 累积的 %s 数值为： %.1f / %d",
                                            player.getDisplayName().getString(), type.description(), ep.getVale(type),
                                            (int) BabelAttributes.getImpairmentThreshold(living))),
                            false);
                } else {
                    argument.getSource().sendSystemMessage(Component.literal(String.format("%s 累积的 %s 数值为： %.1f / %d",
                            living.getDisplayName().getString(), type.description(), ep.getVale(type),
                            (int) BabelAttributes.getImpairmentThreshold(living))));
                }
            }
        }
        return 0;
    }
}