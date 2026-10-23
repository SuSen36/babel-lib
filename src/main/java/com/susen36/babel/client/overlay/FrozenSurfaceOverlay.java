package com.susen36.babel.client.overlay;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.susen36.babel.BabelMod;
import com.susen36.babel.init.BabelMobEffects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(value = {Dist.CLIENT}, modid = BabelMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class FrozenSurfaceOverlay {
    private static final ResourceLocation FROZEN_SURFACE = new ResourceLocation(BabelMod.MODID, "textures/gui/overlay/frozen_surface.png");

    @SubscribeEvent
    public static void registerOverlay(RegisterGuiOverlaysEvent event) {
        event.registerAbove(VanillaGuiOverlay.AIR_LEVEL.id(),
                "frozen_surface",
                (gui, guiGraphics, partialTick, width, height) -> renderFrozenSurface(guiGraphics, width, height));
    }

    private static void renderFrozenSurface(GuiGraphics guiGraphics, int w, int h) {
        Player player = Minecraft.getInstance().player;
        boolean frozen = player != null && player.hasEffect(BabelMobEffects.FROZEN.get());
        if (frozen) {
            RenderSystem.disableDepthTest();
            RenderSystem.depthMask(false);
            RenderSystem.enableBlend();
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            RenderSystem.setShaderColor(1, 1, 1, 1);
            guiGraphics.blit(FROZEN_SURFACE, 0, 0, 0, 0, w, h, w, h);
            RenderSystem.depthMask(true);
            RenderSystem.defaultBlendFunc();
            RenderSystem.enableDepthTest();
            RenderSystem.disableBlend();
            RenderSystem.setShaderColor(1, 1, 1, 1);
        }
    }
}