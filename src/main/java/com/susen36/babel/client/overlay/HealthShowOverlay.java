package com.susen36.babel.client.overlay;

import com.susen36.babel.BabelConfig;
import com.susen36.babel.BabelMod;
import com.susen36.babel.util.LifePointUtils;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

@EventBusSubscriber(value = {Dist.CLIENT}, modid = BabelMod.MODID)
public class HealthShowOverlay {
    private static final ResourceLocation LIFE_POINT = ResourceLocation.fromNamespaceAndPath(BabelMod.MODID, "textures/gui/overlay/target_health.png");
    private static final ResourceLocation SHIELD_POINT = ResourceLocation.fromNamespaceAndPath(BabelMod.MODID, "textures/gui/overlay/target_shield.png");

    @SubscribeEvent
    public static void registerOverlay(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.AIR_LEVEL,
                ResourceLocation.fromNamespaceAndPath(BabelMod.MODID, "health_bar"),
                HealthShowOverlay::renderHealthBar);
    }

    private static void renderHealthBar(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || player.isSpectator()) {
            return;
        }

        int h = mc.getWindow().getGuiScaledHeight();
        int lifeDx = (int) Math.round(BabelConfig.lifeXOffset);
        int lifeDy = (int) Math.round(BabelConfig.lifeYOffset) - 16;
        int shieldDx = lifeDx + (int) Math.round(BabelConfig.shieldXOffset);
        int shieldDy = lifeDy + (int) Math.round(BabelConfig.shieldYOffset);

        guiGraphics.blit(LIFE_POINT, 6 + lifeDx, h - 24 + lifeDy, 0, 0, 24, 16, 24, 16);

        if (LifePointUtils.getShieldPoint(player) > 0) {
            guiGraphics.blit(SHIELD_POINT, 6 + shieldDx, h - 40 + shieldDy, 0, 0, 24, 16, 24, 16);
            String shield = String.valueOf(LifePointUtils.getShieldPoint(player));
            guiGraphics.drawString(mc.font, shield, 21 + shieldDx, h - 36 + shieldDy, -16777216, false);
            guiGraphics.drawString(mc.font, shield, 20 + shieldDx, h - 36 + shieldDy, -1, false);
        }

        String lifeMax = "/" + LifePointUtils.getMaxLifePoint(player);
        String life = String.valueOf(LifePointUtils.getLifePoint(player));
        guiGraphics.drawString(mc.font, lifeMax, 37 + lifeDx, h - 21 + lifeDy, -16764109, false);
        guiGraphics.drawString(mc.font, lifeMax, 36 + lifeDx, h - 21 + lifeDy, -10040065, false);
        guiGraphics.drawString(mc.font, life, 21 + lifeDx, h - 21 + lifeDy, -16764109, false);
        guiGraphics.drawString(mc.font, life, 20 + lifeDx, h - 21 + lifeDy, -1, false);
    }
}
