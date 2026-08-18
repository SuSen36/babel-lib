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

    /*
    	@SubscribeEvent
	public static void registerCommand(RegisterCommandsEvent event) {
		event.getDispatcher().register(Commands.literal("caerula_arbor:sanity").requires(s -> s.hasPermission(2)).then(Commands.literal("check").then(Commands.argument("name", EntityArgument.entity()).executes(arguments -> {
			Level world = arguments.getSource().getUnsidedLevel();
			Entity entity = arguments.getSource().getEntity();
			if (entity == null && world instanceof ServerLevel servLevel)
				entity = FakePlayerFactory.getMinecraft(servLevel);
			Direction direction = Direction.DOWN;
			if (entity != null)
                entity.getDirection();

            Entity ent;
            String info;
            ent = new Object() {
                public Entity getEntity() {
                    try {
                        return EntityArgument.getEntity(arguments, "name");
                    } catch (CommandSyntaxException e) {
                        e.printStackTrace();
                        return null;
                    }
                }
            }.getEntity();
            if (ent == null) {
                arguments.getSource().sendFailure(Component.literal((Component.translatable("command.sanity.check.fail").getString())));
            }
            info = Component.translatable("command.sanity.check.success").getString();
            info = info.replace("{name}", ent.getDisplayName().getString());
            info = info.replace("{num}",
                    "" + (ent instanceof LivingEntity livingEntity ? EPManager.getEP(livingEntity).getEP(AbstractEPCapability.EPType.NERVOUS).getValue() : 0));
            {
                final String success = info;
                final boolean informAdmins = true;
                arguments.getSource().sendSuccess(() -> Component.literal(success), informAdmins);
            }
            return 0;
		}))).then(Commands.literal("set").then(Commands.argument("name", EntityArgument.entities()).then(Commands.argument("amount", DoubleArgumentType.doubleArg(-1, 1000)).executes(arguments -> {
			Level world = arguments.getSource().getUnsidedLevel();
			double x = arguments.getSource().getPosition().x();
			double y = arguments.getSource().getPosition().y();
			double z = arguments.getSource().getPosition().z();
			Entity entity = arguments.getSource().getEntity();
			if (entity == null && world instanceof ServerLevel servLevel)
				entity = FakePlayerFactory.getMinecraft(servLevel);
			Direction direction = Direction.DOWN;
			if (entity != null)
                entity.getDirection();

            Entity ent;
            String info = "";
            double num = 0;
            try {
                for (Entity entityiterator : EntityArgument.getEntities(arguments, "name")) {
                    ent = entityiterator;
                    if (ent instanceof LivingEntity) {
                        num = num + 1;
                        CompoundTag sanityData = EPManager.getEP((LivingEntity) ent).getEP(AbstractEPCapability.EPType.NERVOUS).serializeNBT(ent.registryAccess());
                        sanityData.putInt("nervous.injury", Mth.floor(DoubleArgumentType.getDouble(arguments, "amount")));
                        sanityData.putInt("nervous.leftReviveTick", 0);
                        sanityData.putInt("nervous.immunityTick", 0);
                        EPManager.getEP((LivingEntity) ent).getEP(AbstractEPCapability.EPType.NERVOUS).deserializeNBT(ent.registryAccess(), sanityData);
                        if (num == 1) {
                            info = Component.translatable("command.sanity.set.single").getString();
                            info = info.replace("{name}", ent.getDisplayName().getString());
                        } else {
                            info = Component.translatable("command.sanity.set.mult").getString();
                        }
                    }
                }
            } catch (CommandSyntaxException e) {
                e.printStackTrace();
            }
            info = info.replace("{num}", "" + Math.round(num));
            info = info.replace("{amount}", "" + Math.round(Math.pow(10, 1) * (DoubleArgumentType.getDouble(arguments, "amount"))) / Math.pow(10, 1));
            {
                final String success = info;
                final boolean informAdmins = true;
                arguments.getSource().sendSuccess(() -> Component.literal(success), informAdmins);
            }
            return 0;
		})))).then(Commands.literal("hurt").then(Commands.argument("name", EntityArgument.entities()).then(Commands.argument("amount", DoubleArgumentType.doubleArg(0, 2147483647)).executes(arguments -> {
			Level world = arguments.getSource().getUnsidedLevel();
			double x = arguments.getSource().getPosition().x();
			double y = arguments.getSource().getPosition().y();
			double z = arguments.getSource().getPosition().z();
			Entity entity = arguments.getSource().getEntity();
			if (entity == null && world instanceof ServerLevel servLevel)
				entity = FakePlayerFactory.getMinecraft(servLevel);
			Direction direction = Direction.DOWN;
			if (entity != null)
                entity.getDirection();

            Entity ent;
            String info = "";
            double num = 0;
            try {
                for (Entity entityiterator : EntityArgument.getEntities(arguments, "name")) {
                    ent = entityiterator;
                    if (ent instanceof LivingEntity) {
                        num = num + 1;
                        EPManager.getEP((LivingEntity) ent).getEP(AbstractEPCapability.EPType.NERVOUS).hurt(ElementalInjurySource.fromNothing(), Mth.floor(DoubleArgumentType.getDouble(arguments, "amount")));
                        if (num == 1) {
                            info = Component.translatable("command.sanity.hurt.single").getString();
                            info = info.replace("{name}", ent.getDisplayName().getString());
                        } else {
                            info = Component.translatable("command.sanity.hurt.mult").getString();
                        }
                    }
                }
            } catch (CommandSyntaxException e) {
                e.printStackTrace();
            }
            info = info.replace("{num}", "" + Math.round(num));
            info = info.replace("{amount}", "" + Math.round(Math.pow(10, 1) * (DoubleArgumentType.getDouble(arguments, "amount"))) / Math.pow(10, 1));
            {
                final String success = info;
                final boolean informAdmins = true;
                arguments.getSource().sendSuccess(() -> Component.literal(success), informAdmins);
            }
            return 0;
		})))).then(Commands.literal("heal").then(Commands.argument("name", EntityArgument.entities()).then(Commands.argument("amount", DoubleArgumentType.doubleArg(0, 2147483647)).executes(arguments -> {
			Level world = arguments.getSource().getUnsidedLevel();
			double x = arguments.getSource().getPosition().x();
			double y = arguments.getSource().getPosition().y();
			double z = arguments.getSource().getPosition().z();
			Entity entity = arguments.getSource().getEntity();
			if (entity == null && world instanceof ServerLevel servLevel)
				entity = FakePlayerFactory.getMinecraft(servLevel);
			Direction direction = Direction.DOWN;
			if (entity != null)
                entity.getDirection();

            Entity ent;
            String info = "";
            double num = 0;
            try {
                for (Entity entityiterator : EntityArgument.getEntities(arguments, "name")) {
                    ent = entityiterator;
                    if (ent instanceof LivingEntity) {
                        num = num + 1;
                        EPManager.getEP((LivingEntity) ent).getEP(AbstractEPCapability.EPType.NERVOUS).heal(Mth.floor(DoubleArgumentType.getDouble(arguments, "amount")));
                        if (num == 1) {
                            info = Component.translatable("command.sanity.heal.single").getString();
                            info = info.replace("{name}", ent.getDisplayName().getString());
                        } else {
                            info = Component.translatable("command.sanity.heal.mult").getString();
                        }
                    }
                }
            } catch (CommandSyntaxException e) {
                e.printStackTrace();
            }
            info = info.replace("{num}", "" + Math.round(num));
            info = info.replace("{amount}", "" + Math.round(Math.pow(10, 1) * (DoubleArgumentType.getDouble(arguments, "amount"))) / Math.pow(10, 1));
            {
                final String success = info;
                final boolean informAdmins = true;
                arguments.getSource().sendSuccess(() -> Component.literal(success), informAdmins);
            }
            return 0;
		})))));
	}
     */
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
                if (ep.hurt(type, ElementalInjurySource.fromCommand(argument.getSource()), amount)) {
                    if (executedCount == 0) singleCandidate = living;
                    executedCount++;
                }
            }
        }
        if (executedCount == 1) {
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
                ep.heal(type, amount);
                if (executedCount == 0) singleCandidate = living;
                executedCount++;
            }
        }
        if (executedCount == 1) {
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
        return 0;
    }
}