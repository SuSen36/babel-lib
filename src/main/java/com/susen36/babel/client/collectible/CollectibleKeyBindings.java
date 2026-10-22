package com.susen36.babel.client.collectible;

import com.susen36.babel.BabelMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = BabelMod.MODID)
public class CollectibleKeyBindings {
    public static final KeyMapping OPEN_COLLECTIBLES = new KeyMapping("key.babel.open_collectibles", GLFW.GLFW_KEY_C, "key.categories.babel");

    private CollectibleKeyBindings() {
    }

    @SubscribeEvent
    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(OPEN_COLLECTIBLES);
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            while (OPEN_COLLECTIBLES.consumeClick()) {
                Minecraft minecraft = Minecraft.getInstance();
                if (minecraft.screen instanceof CollectiblesScreen) {
                    minecraft.setScreen(null);
                } else if (minecraft.screen == null && minecraft.player != null) {
                    minecraft.setScreen(new CollectiblesScreen(minecraft.player));
                }
            }
        }
    }
}
