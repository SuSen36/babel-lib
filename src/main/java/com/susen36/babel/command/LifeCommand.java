package com.susen36.babel.command;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.susen36.babel.BabelConfig;
import com.susen36.babel.capability.BabelCapability;
import com.susen36.babel.capability.health.HealthCapability;
import com.susen36.babel.init.BabelAttributes;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;

public class LifeCommand {

    private static final LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal("lifepoint").requires(s -> s.hasPermission(2))
            .then(Commands.argument("name", EntityArgument.players())
                    .then(Commands.literal("life_point").then(Commands.argument("life", DoubleArgumentType.doubleArg(0))
                            .executes(LifeCommand::setLife)))
                    .then(Commands.literal("max_life_point").then(Commands.argument("life", DoubleArgumentType.doubleArg(1))
                            .executes(LifeCommand::setMaxLife)))
                    .then(Commands.literal("shield").then(Commands.argument("shield", DoubleArgumentType.doubleArg(0))
                            .executes(LifeCommand::setShield))));

    public static LiteralArgumentBuilder<CommandSourceStack> get() {
        return command;
    }

    private static double getAmount(CommandContext<CommandSourceStack> argument, String name, double limit) throws CommandSyntaxException {
        double value = DoubleArgumentType.getDouble(argument, name);
        if (value > limit) {
            throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.doubleTooHigh().create(value, limit);
        }
        // Keep the command's whole-point semantics without imposing an int ceiling.
        return Math.floor(value);
    }

    private static int setLife(CommandContext<CommandSourceStack> argument) throws CommandSyntaxException {
        double value = getAmount(argument, "life", BabelConfig.LIFE_POINT_LIMIT.get());
        int changed = 0;
        for (ServerPlayer player : EntityArgument.getPlayers(argument, "name")) {
            HealthCapability health = BabelCapability.getHealth(player);
            if (health != null && value >= 1) {
                double before = health.getLives();
                health.setLives(Math.min(value, Math.floor(health.getMaxLives())));
                health.sync();
                if (health.getLives() != before) {
                    sendSuccess(argument, player, "command.set_life", health.getLives());
                    changed++;
                }
            }
        }
        return changed;
    }

    private static int setMaxLife(CommandContext<CommandSourceStack> argument) throws CommandSyntaxException {
        double value = getAmount(argument, "life", BabelConfig.LIFE_POINT_LIMIT.get());
        int changed = 0;
        for (ServerPlayer player : EntityArgument.getPlayers(argument, "name")) {
            HealthCapability health = BabelCapability.getHealth(player);
            AttributeInstance attribute = player.getAttribute(BabelAttributes.MAX_LIFE_POINT.get());
            if (health != null && attribute != null) {
                double before = attribute.getBaseValue();
                health.setMaxLives(value);
                health.sync();
                if (attribute.getBaseValue() != before) {
                    sendSuccess(argument, player, "command.set_maxlife", health.getMaxLives());
                    changed++;
                }
            }
        }
        return changed;
    }

    private static int setShield(CommandContext<CommandSourceStack> argument) throws CommandSyntaxException {
        double value = getAmount(argument, "shield", BabelConfig.SHIELD_LIMIT.get());
        int changed = 0;
        for (ServerPlayer player : EntityArgument.getPlayers(argument, "name")) {
            HealthCapability health = BabelCapability.getHealth(player);
            if (health != null) {
                double before = health.getShield();
                health.setShield(value);
                health.sync();
                if (health.getShield() != before) {
                    sendSuccess(argument, player, "command.set_shield", health.getShield());
                    changed++;
                }
            }
        }
        return changed;
    }

    private static void sendSuccess(CommandContext<CommandSourceStack> argument, Entity entity, String key, double value) {
        argument.getSource().sendSuccess(() -> Component.translatable(key, entity.getDisplayName(), value), true);
    }
}