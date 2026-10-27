package com.susen36.babel.client.overlay;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.susen36.babel.BabelMod;
import com.susen36.babel.init.BabelMobEffects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(value = {Dist.CLIENT}, modid = BabelMod.MODID)
public class PalsyOverlay {
	@SubscribeEvent(priority = EventPriority.NORMAL)
	public static void eventHandler(RenderGuiEvent.Pre event) {
		int w = Minecraft.getInstance().getWindow().getGuiScaledWidth();
		int h = Minecraft.getInstance().getWindow().getGuiScaledHeight();
		Player entity = Minecraft.getInstance().player;

		RenderSystem.disableDepthTest();
		RenderSystem.depthMask(false);
		RenderSystem.enableBlend();
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		RenderSystem.setShaderColor(1, 1, 1, 1);
        boolean result = false;
        if (entity != null) {
            result = entity.hasEffect(BabelMobEffects.PALSY.get());
        }
        if (result) {
			event.getGuiGraphics().blit(new ResourceLocation(BabelMod.MODID, "textures/gui/overlay/palsy.png"), w / 2 + 5, h / 2 + -8, 0, 0, 16, 16, 16, 16);

			event.getGuiGraphics().drawString(Minecraft.getInstance().font,
					getPalsy(entity), w / 2 + 17, h / 2 + -3, -13421773, false);
			event.getGuiGraphics().drawString(Minecraft.getInstance().font,
					getPalsy(entity), w / 2 + 16, h / 2 + -3, -1, false);
		}
		RenderSystem.depthMask(true);
		RenderSystem.defaultBlendFunc();
		RenderSystem.enableDepthTest();
		RenderSystem.disableBlend();
		RenderSystem.setShaderColor(1, 1, 1, 1);
	}

	private static String getPalsy(Entity entity) {
		if (entity instanceof LivingEntity living) {
			MobEffectInstance effectInstance = living.getEffect(BabelMobEffects.PALSY.get());
			if (effectInstance != null) {
				return String.valueOf(effectInstance.getAmplifier() + 1);
			}
		}
		return "";
	}
}