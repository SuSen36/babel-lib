package com.susen36.babel.client.overlay;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.susen36.babel.BabelMod;
import com.susen36.babel.init.BabelMobEffects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.client.event.RenderGuiEvent;

@Mod.EventBusSubscriber(value = {Dist.CLIENT}, modid = BabelMod.MODID)
public class WeakArmorOverlay {
	@SubscribeEvent(priority = EventPriority.NORMAL)
	public static void eventHandler(RenderGuiEvent.Pre event) {
		Player player = Minecraft.getInstance().player;
		ResourceLocation texture = null;
		boolean hasArmor = player.hasEffect(BabelMobEffects.LESS_ARMOR.get());
		boolean hasMagic = player.hasEffect(BabelMobEffects.WEAK_MAGIC_RESISTANCE.get());
		int amplifier;
		if (hasArmor && hasMagic) {
			amplifier = (player.getEffect(BabelMobEffects.LESS_ARMOR.get()).getAmplifier()
					+ player.getEffect(BabelMobEffects.WEAK_MAGIC_RESISTANCE.get()).getAmplifier()) / 2;
		} else if (hasArmor) {
			amplifier = player.getEffect(BabelMobEffects.LESS_ARMOR.get()).getAmplifier();
		} else if (hasMagic) {
			amplifier = player.getEffect(BabelMobEffects.WEAK_MAGIC_RESISTANCE.get()).getAmplifier();
		} else {
			amplifier = 0;
		}
		boolean least = amplifier > 9;
		if (hasArmor && hasMagic) {
			texture = new ResourceLocation(BabelMod.MODID,
					"textures/gui/overlay/" + (least ? "least" : "less") + "_armor_ui.png");
		} else if (hasArmor) {
			texture = new ResourceLocation(BabelMod.MODID,
					"textures/gui/overlay/" + (least ? "least" : "less") + "_armor_ui_blue.png");
		} else if (hasMagic) {
			texture = new ResourceLocation(BabelMod.MODID,
					"textures/gui/overlay/" + (least ? "least" : "less") + "_armor_ui_purple.png");
		}
		if (texture != null) {
			int width = Minecraft.getInstance().getWindow().getGuiScaledWidth();
			int height = Minecraft.getInstance().getWindow().getGuiScaledHeight();
			RenderSystem.disableDepthTest();
			RenderSystem.depthMask(false);
			RenderSystem.enableBlend();
			RenderSystem.setShader(GameRenderer::getPositionTexShader);
			RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
			RenderSystem.setShaderColor(1, 1, 1, 1);
			event.getGuiGraphics().blit(texture, 0, 0, 0, 0, width, height, width, height);
			RenderSystem.depthMask(true);
			RenderSystem.defaultBlendFunc();
			RenderSystem.enableDepthTest();
			RenderSystem.disableBlend();
			RenderSystem.setShaderColor(1, 1, 1, 1);
		}
	}
}