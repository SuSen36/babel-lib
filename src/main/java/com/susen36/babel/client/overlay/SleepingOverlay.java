package com.susen36.babel.client.overlay;

import com.susen36.babel.BabelMod;
import com.susen36.babel.init.BabelMobEffects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = BabelMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class SleepingOverlay {
    private static final int FADE_IN_TICKS = 20;
    private static final int FADE_OUT_STEP = 2;
    private static final int FILTER_COLOR = 1052704;
    private static final int MAX_ALPHA = 220;

    private static int fadeTicks;

    @SubscribeEvent
    public static void registerOverlay(RegisterGuiOverlaysEvent event) {
        event.registerAboveAll("sleep_filter",
                (gui, guiGraphics, partialTick, width, height) -> renderSleepFilter(guiGraphics, width, height));
    }

    private static void renderSleepFilter(GuiGraphics guiGraphics, int width, int height) {
        if (fadeTicks > 0) {
            int alpha = MAX_ALPHA * fadeTicks / FADE_IN_TICKS;
            guiGraphics.fill(RenderType.guiOverlay(), 0, 0, width, height, alpha << 24 | FILTER_COLOR);
        }
    }

    @Mod.EventBusSubscriber(value = Dist.CLIENT, modid = BabelMod.MODID)
    public static class TickHandler {
        @SubscribeEvent
        public static void onClientTick(TickEvent.ClientTickEvent event) {
            if (event.phase == TickEvent.Phase.END) {
                Player player = Minecraft.getInstance().player;
                if (player != null && player.hasEffect(BabelMobEffects.SLEEP.get())) {
                    fadeTicks = Math.min(FADE_IN_TICKS, fadeTicks + 1);
                } else if (fadeTicks > 0) {
                    fadeTicks = Math.max(0, fadeTicks - FADE_OUT_STEP);
                }
            }
        }
    }
}