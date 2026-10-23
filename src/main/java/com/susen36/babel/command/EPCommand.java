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

    private static final DynamicCommandExceptionType ERROR_UNKNOWN_EP_TYPE = new DynamicCommandExceptionType(obj -> Component.literal("Unknown elemental injury type: " + obj));

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
            argument.getSource().sendSuccess(() -> Component.literal(Component.translatable("command.ep.hurt.single").getString()
                            .replace("{name}", finalSingleCandidate.getDisplayName().getString())
                            .replace("{amount}", "" + amount)
                            .replace("{type}", type.description().getString())),
                    true);
        } else if (executedCount > 0) {
            final int finalExecutedCount = executedCount;
            argument.getSource().sendSuccess(() -> Component.literal(Component.translatable("command.ep.hurt.mult").getString()
                            .replace("{num}", "" + finalExecutedCount)
                            .replace("{amount}", "" + amount)
                            .replace("{type}", type.description().getString())),
                    true);
        } else {
            argument.getSource().sendFailure(Component.literal(Component.translatable("command.ep.not_found").getString()));
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
                    ep.heal(type, amount);
                }
                if (executedCount == 0) singleCandidate = living;
                executedCount++;
            }
        }
        if (executedCount == 1 && singleCandidate != null) {
            final LivingEntity finalSingleCandidate = singleCandidate;
            argument.getSource().sendSuccess(() -> Component.literal(Component.translatable("command.ep.heal.single").getString()
                            .replace("{name}", finalSingleCandidate.getDisplayName().getString())
                            .replace("{amount}", "" + amount)
                            .replace("{type}", type.description().getString())),
                    true);
        } else if (executedCount > 0) {
            final int finalExecutedCount = executedCount;
            argument.getSource().sendSuccess(() -> Component.literal(Component.translatable("command.ep.heal.mult").getString()
                            .replace("{num}", "" + finalExecutedCount)
                            .replace("{amount}", "" + amount)
                            .replace("{type}", type.description().getString())),
                    true);
        } else {
            argument.getSource().sendFailure(Component.literal(Component.translatable("command.ep.not_found").getString()));
        }
        return executedCount;
    }

    private static int sendEPValue(CommandContext<CommandSourceStack> argument) throws CommandSyntaxException {
        AbstractEPCapability.EPType type = resolveType(argument);
        Entity[] entities = EntityArgument.getEntities(argument, "entities").toArray(new Entity[0]);
        for (Entity entity : entities) {
            if (entity instanceof LivingEntity living) {
                EPCapability ep = BabelCapability.getEP(living);
                if (ep != null) {
                    if (entity instanceof Player player && !player.level().isClientSide()) {
                        player.displayClientMessage(Component.literal(Component.translatable("command.ep.check").getString()
                                        .replace("{name}", player.getDisplayName().getString())
                                        .replace("{type}", type.description().getString())
                                        .replace("{num}", "" + ep.getValue(type))
                                        .replace("{max}", "" + (int) BabelAttributes.getMaxElementalValue(living))),
                                false);
                    } else {
                        argument.getSource().sendSystemMessage(Component.literal(Component.translatable("command.ep.check").getString()
                                .replace("{name}", living.getDisplayName().getString())
                                .replace("{type}", type.description().getString())
                                .replace("{num}", "" + ep.getValue(type))
                                .replace("{max}", "" + (int) BabelAttributes.getMaxElementalValue(living))));
                    }
                }
            }
        }
        return 0;
    }
}