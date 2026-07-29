package com.susen36.babel;

import com.mojang.logging.LogUtils;
import com.susen36.babel.capability.BabelCapability;
import com.susen36.babel.init.BabelAttributes;
import com.susen36.babel.network.BabelNetwork;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.util.thread.SidedThreadGroups;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.slf4j.Logger;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

@Mod(BabelMod.MODID)
public class BabelMod {

    public static final String MODID = "babel";
    public static final Logger LOGGER = LogUtils.getLogger();

    private static final Collection<AbstractMap.SimpleEntry<Runnable, Integer>> WORK_QUEUE = new ConcurrentLinkedQueue<>();

    public BabelMod(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(BabelConfig::onLoad);
        modEventBus.addListener(BabelConfig::onReloading);
        BabelAttributes.REGISTER.register(modEventBus);
        BabelCapability.register(modEventBus);
        NeoForge.EVENT_BUS.register(this);
        modContainer.registerConfig(ModConfig.Type.COMMON, BabelConfig.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
    }

    public static void queueServerWork(int tick, Runnable action) {
        if (Thread.currentThread().getThreadGroup() == SidedThreadGroups.SERVER) {
            WORK_QUEUE.add(new AbstractMap.SimpleEntry<>(action, tick));
        }
    }

    @SubscribeEvent
    public void onServerTick(ServerTickEvent.Post event) {
        List<AbstractMap.SimpleEntry<Runnable, Integer>> actions = new ArrayList<>();
        WORK_QUEUE.forEach(work -> {
            work.setValue(work.getValue() - 1);
            if (work.getValue() == 0) actions.add(work);
        });
        actions.forEach(e -> e.getKey().run());
        WORK_QUEUE.removeAll(actions);
    }

    @EventBusSubscriber(modid = MODID, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
        }
    }
}