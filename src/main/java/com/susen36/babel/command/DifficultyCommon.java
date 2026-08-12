package com.susen36.babel.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.ParsedCommandNode;
import com.susen36.babel.difficulty.Difficulty;
import com.susen36.babel.init.BabelGameRules;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.CommandEvent;

@EventBusSubscriber
public class DifficultyCommon {
	@SubscribeEvent
	public static void onCommand(CommandEvent event) {
		CommandContext<CommandSourceStack> commandContext = event.getParseResults().getContext().build(event.getParseResults().getReader().getString());
		Entity commandExecutor = commandContext.getSource().getEntity();
        if (commandExecutor instanceof Player player && !player.level().isClientSide() && commandContext.getSource().getServer().getDefaultGameType() != GameType.SURVIVAL) {
            boolean hasGameRuleNode = false;
            boolean hasSurgingWavesNode = false;
            boolean hasValueNode = false;
            for (ParsedCommandNode<CommandSourceStack> parsedNode : commandContext.getNodes()) {
                String nodeName = parsedNode.getNode().getName();
                if ("gamerule".equals(nodeName)) {
                    hasGameRuleNode = true;
                } else if (BabelGameRules.DIFFICULTY_LEVEL.getId().equals(nodeName)) {
                    hasSurgingWavesNode = true;
                } else if ("value".equals(nodeName)) {
                    hasValueNode = true;
                }
            }

            if (!hasGameRuleNode || !hasSurgingWavesNode || !hasValueNode) {
                return;
            }

            int targetSurgingWavesLevel = IntegerArgumentType.getInteger(commandContext, "value");
            int currentSurgingWavesLevel = Difficulty.difficultyLevel(player.level()).value();
            if (targetSurgingWavesLevel <= currentSurgingWavesLevel) {
                return;
            }

            Component warningMessage = null;
            if (targetSurgingWavesLevel >= 12) {
                warningMessage = Component.translatable("gameplay.babel.n_12");
            } else if (currentSurgingWavesLevel < 6 && targetSurgingWavesLevel >= 9) {
                warningMessage = Component.translatable("gameplay.babel.n_6");
            }

            if (warningMessage != null) {
                player.displayClientMessage(warningMessage, false);
            }
        }
    }
}