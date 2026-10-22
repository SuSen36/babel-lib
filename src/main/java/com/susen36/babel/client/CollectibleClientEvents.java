package com.susen36.babel.client;

import com.susen36.babel.BabelMod;
import com.susen36.babel.collectible.Collectibles;
import com.susen36.babel.collectible.CollectiblesDecorator;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterItemDecorationsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD, modid = BabelMod.MODID)
public class CollectibleClientEvents {

    @SubscribeEvent
    public static void onRegisterItemDecorationsEvent(@NotNull RegisterItemDecorationsEvent event) {
        var items = Collectibles.Collectibles.values();
        for (var item : items) {
            event.register(item.get(), new CollectiblesDecorator());
        }
    }
}
