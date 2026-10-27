package com.susen36.babel.init;

import com.mojang.brigadier.CommandDispatcher;
import com.susen36.babel.BabelMod;
import com.susen36.babel.command.EPCommand;
import com.susen36.babel.command.LifeCommand;
import net.minecraft.commands.CommandSourceStack;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = BabelMod.MODID)
public class BabelCommands {

    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        dispatcher.register(EPCommand.get());
        dispatcher.register(LifeCommand.get());
    }
}