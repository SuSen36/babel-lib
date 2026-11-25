package com.susen36.babel.client.overlay;

import com.mojang.blaze3d.systems.RenderSystem;
import com.susen36.babel.BabelConfig;
import com.susen36.babel.BabelMod;
import com.susen36.babel.capability.BabelCapability;
import com.susen36.babel.capability.health.HealthCapability;
import com.susen36.babel.init.BabelAttributes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(value = {Dist.CLIENT}, modid = BabelMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class PlayerStatusOverlay {
    private static final ResourceLocation LIFE_POINT = new ResourceLocation(BabelMod.MODID, "textures/gui/overlay/target_health.png");
    private static final ResourceLocation SHIELD_POINT = new ResourceLocation(BabelMod.MODID, "textures/gui/overlay/target_shield.png");
    private static final ResourceLocation MAGIC_RESISTANCE_ICON = new ResourceLocation(BabelMod.MODID, "textures/gui/overlay/magic_resistance.png");
    private static final ResourceLocation BARRIER_ICON = new ResourceLocation(BabelMod.MODID, "textures/gui/overlay/barrier.png");
    private static final ResourceLocation SHIELD_ICON = new ResourceLocation(BabelMod.MODID, "textures/gui/overlay/shield.png");
    private static final ResourceLocation ELEMENTAL_RESISTANCE_ICON = new ResourceLocation(BabelMod.MODID, "textures/gui/overlay/elemental_resistance.png");
    private static final ResourceLocation MISS_PHYSICAL_ICON = new ResourceLocation(BabelMod.MODID, "textures/gui/overlay/miss_physical.png");
    private static final ResourceLocation MISS_MAGIC_ICON = new ResourceLocation(BabelMod.MODID, "textures/gui/overlay/miss_magic.png");
    private static final int COLOR_WHITE = 0xFFFFFFFF;
    private static final int COLOR_LIFE_MAX = 0xFF66CCFF;

    @SubscribeEvent
    public static void registerOverlay(RegisterGuiOverlaysEvent event) {
        event.registerAbove(VanillaGuiOverlay.AIR_LEVEL.id(),
                "health_bar",
                PlayerStatusOverlay::renderHealthBar);
    }

    private static void renderHealthBar(ForgeGui gui, GuiGraphics guiGraphics, float partialTick, int width, int height) {
        Player player = Minecraft.getInstance().player;
        HealthCapability health = player == null ? null : BabelCapability.getHealth(player);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        if (player != null && !player.isSpectator() && health != null) {
            Font font = Minecraft.getInstance().font;
            double capabilityShield = health.getShield();
            int lifeOffsetX = (int) Math.round(BabelConfig.LIFE_X_OFFSET.get());
            int lifeOffsetY = (int) Math.round(BabelConfig.LIFE_Y_OFFSET.get()) - 16;
            int shieldOffsetX = lifeOffsetX + (int) Math.round(BabelConfig.SHIELD_X_OFFSET.get());
            int shieldOffsetY = lifeOffsetY + (int) Math.round(BabelConfig.SHIELD_Y_OFFSET.get());

            guiGraphics.blit(LIFE_POINT, 6 + lifeOffsetX, height - 24 + lifeOffsetY, 0, 0, 24, 16, 24, 16);

            if (capabilityShield > 0) {
                guiGraphics.blit(SHIELD_POINT, 6 + shieldOffsetX, height - 40 + shieldOffsetY, 0, 0, 24, 16, 24, 16);
                String shield = String.valueOf((int) capabilityShield);
                guiGraphics.drawString(font, shield, 20 + shieldOffsetX, height - 36 + shieldOffsetY, COLOR_WHITE, true);
            }

            String lifeMax = "/" + (int) health.getMaxLives();
            String life = String.valueOf((int) health.getLives());
            int lifeMaxX = 20 + lifeOffsetX + font.width(life);
            guiGraphics.drawString(font, lifeMax, lifeMaxX, height - 21 + lifeOffsetY, COLOR_LIFE_MAX, true);
            guiGraphics.drawString(font, life, 20 + lifeOffsetX, height - 21 + lifeOffsetY, COLOR_WHITE, true);

            if (player.isAlive()) {
                int attributeX = 6 + lifeOffsetX;
                int attributeY = height - 24 + lifeOffsetY;
                int shieldY = height - 40 + shieldOffsetY;
                if (capabilityShield > 0 && shieldY < attributeY) {
                    attributeY = shieldY;
                }
                attributeY -= 12;
                AttributeInstance physicalMiss = player.getAttribute(BabelAttributes.PHYSICAL_MISS_RATE.get());
                double physicalMissValue = physicalMiss == null ? 0.0 : physicalMiss.getValue();
                AttributeInstance magicMiss = player.getAttribute(BabelAttributes.MAGIC_MISS_RATE.get());
                double magicMissValue = magicMiss == null ? 0.0 : magicMiss.getValue();
                if (physicalMissValue > 0.0 || magicMissValue > 0.0) {
                    int missX = attributeX;
                    if (physicalMissValue > 0.0) {
                        String text = Integer.toString(Mth.ceil(physicalMissValue)) + "%";
                        guiGraphics.blit(MISS_PHYSICAL_ICON, missX, attributeY + 3, 0, 0, 16, 6, 16, 6);
                        guiGraphics.drawString(font, text, missX + 18, attributeY, COLOR_WHITE, true);
                        missX += 18 + font.width(text) + 4;
                    }
                    if (magicMissValue > 0.0) {
                        String text = Integer.toString(Mth.ceil(magicMissValue)) + "%";
                        guiGraphics.blit(MISS_MAGIC_ICON, missX, attributeY + 3, 0, 0, 16, 6, 16, 6);
                        guiGraphics.drawString(font, text, missX + 18, attributeY, COLOR_WHITE, true);
                    }
                    attributeY -= 12;
                }
                AttributeInstance magicResistance = player.getAttribute(BabelAttributes.MAGIC_RESISTANCE.get());
                double magicResistValue = magicResistance == null ? 0.0 : magicResistance.getValue();
                if (magicResistValue > 0.0) {
                    String text = Integer.toString(Mth.ceil(magicResistValue)) + "%";
                    guiGraphics.blit(MAGIC_RESISTANCE_ICON, attributeX, attributeY, 0, 0, 9, 9, 9, 9);
                    guiGraphics.drawString(font, text, attributeX + 13, attributeY, COLOR_WHITE, true);
                    attributeY -= 12;
                }
                AttributeInstance barrier = player.getAttribute(BabelAttributes.BARRIER.get());
                double barrierValue = barrier == null ? 0.0 : barrier.getValue();
                AttributeInstance shieldAttr = player.getAttribute(BabelAttributes.SHIELD.get());
                double shieldValue = shieldAttr == null ? 0.0 : shieldAttr.getValue();
                if (barrierValue > 0.0 || shieldValue > 0.0) {
                    int segmentX = attributeX;
                    if (barrierValue > 0.0) {
                        String text = Integer.toString(Mth.ceil(barrierValue));
                        guiGraphics.blit(BARRIER_ICON, segmentX, attributeY, 0, 0, 9, 9, 9, 9);
                        guiGraphics.drawString(font, text, segmentX + 13, attributeY, COLOR_WHITE, true);
                        segmentX += 13 + font.width(text) + 4;
                    }
                    if (shieldValue > 0.0) {
                        String text = Integer.toString(Mth.ceil(shieldValue));
                        guiGraphics.blit(SHIELD_ICON, segmentX, attributeY, 0, 0, 9, 9, 9, 9);
                        guiGraphics.drawString(font, text, segmentX + 13, attributeY, COLOR_WHITE, true);
                    }
                    attributeY -= 12;
                }
                AttributeInstance elementalResistance = player.getAttribute(BabelAttributes.ELEMENTAL_RESISTANCE.get());
                double elementalValue = elementalResistance == null ? 0.0 : elementalResistance.getValue();
                if (elementalValue > 0.0) {
                    String text = Mth.ceil(elementalValue) + "%";
                    guiGraphics.blit(ELEMENTAL_RESISTANCE_ICON, attributeX, attributeY, 0, 0, 9, 9, 9, 9);
                    guiGraphics.drawString(font, text, attributeX + 13, attributeY, COLOR_WHITE, true);
                }
            }
        }
        RenderSystem.disableBlend();
    }
}