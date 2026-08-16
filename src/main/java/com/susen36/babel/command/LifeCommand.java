package com.susen36.babel.command;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.susen36.babel.util.HealthUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;

public class LifeCommand {

    private static final LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal("babel:player_data").requires(s -> s.hasPermission(2))
            .then(Commands.argument("name", EntityArgument.players())
                    .then(Commands.literal("life_point").then(Commands.argument("life", DoubleArgumentType.doubleArg(0, 255))
                            .executes(LifeCommand::setLife)))
                    .then(Commands.literal("max_life_point").then(Commands.argument("life", DoubleArgumentType.doubleArg(1, 255))
                            .executes(LifeCommand::setMaxLife)))
                    .then(Commands.literal("shield").then(Commands.argument("shield", DoubleArgumentType.doubleArg(0, 999))
                            .executes(LifeCommand::setShield))));

    public static LiteralArgumentBuilder<CommandSourceStack> get() {
        return command;
    }

    private static int setLife(CommandContext<CommandSourceStack> argument) throws CommandSyntaxException {
        double value = DoubleArgumentType.getDouble(argument, "life");
        for (Entity entity : EntityArgument.getEntities(argument, "name")) {
            int setval = (int) Math.min(value, HealthUtils.getMaxLifePoint(entity));
            HealthUtils.setLifePoint(entity, setval);
            sendSuccess(argument, entity, "command.set_life", setval);
        }
        return 0;
    }

    private static int setMaxLife(CommandContext<CommandSourceStack> argument) throws CommandSyntaxException {
        double value = DoubleArgumentType.getDouble(argument, "life");
        for (Entity entity : EntityArgument.getEntities(argument, "name")) {
            int setval = (int) value;
            HealthUtils.setMaxLifePoint(entity, setval);
            sendSuccess(argument, entity, "command.set_maxlife", setval);
        }
        return 0;
    }

    private static int setShield(CommandContext<CommandSourceStack> argument) throws CommandSyntaxException {
        double value = DoubleArgumentType.getDouble(argument, "shield");
        for (Entity entity : EntityArgument.getEntities(argument, "name")) {
            int setval = (int) value;
            HealthUtils.setShieldPoint(entity, setval);
            sendSuccess(argument, entity, "command.set_shield", setval);
        }
        return 0;
    }

    private static void sendSuccess(CommandContext<CommandSourceStack> argument, Entity entity, String key, int value) {
        String info = Component.translatable(key).getString();
        info = info.replace("<player>", entity.getDisplayName().getString());
        info = info.replace("<num>", "" + Math.round(value));
        final String success = info;
        argument.getSource().sendSuccess(() -> Component.literal(success), true);
    }
}
