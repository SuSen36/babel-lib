package com.susen36.babel.util;

import com.susen36.babel.BabelMod;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Mod.EventBusSubscriber(modid = BabelMod.MODID)
public final class ServerTasks {
    private static final Queue<AbstractMap.SimpleEntry<Runnable, Integer>> QUEUE = new ConcurrentLinkedQueue<>();

    private ServerTasks() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static void schedule(int delayTicks, Runnable action) {
        QUEUE.add(new AbstractMap.SimpleEntry<>(action, delayTicks));
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && !QUEUE.isEmpty()) {
            List<Runnable> pending = new ArrayList<>();
            QUEUE.removeIf(work -> {
                work.setValue(work.getValue() - 1);
                if (work.getValue() <= 0) {
                    pending.add(work.getKey());
                    return true;
                }
                return false;
            });
            for (Runnable action : pending) {
                action.run();
            }
        }
    }
}
