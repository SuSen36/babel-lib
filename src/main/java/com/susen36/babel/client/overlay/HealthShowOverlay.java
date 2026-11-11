package com.susen36.babel.client.overlay;

import com.susen36.babel.BabelConfig;
import com.susen36.babel.BabelMod;
import com.susen36.babel.capability.BabelCapability;
import com.susen36.babel.capability.health.HealthCapability;
import com.susen36.babel.init.BabelAttributes;
import net.minecraft.client.Minecraft;
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

import java.text.NumberFormat;

@Mod.EventBusSubscriber(value = {Dist.CLIENT}, modid = BabelMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class HealthShowOverlay {
    private static final ResourceLocation LIFE_POINT = new ResourceLocation(BabelMod.MODID, "textures/gui/overlay/target_health.png");
    private static final ResourceLocation SHIELD_POINT = new ResourceLocation(BabelMod.MODID, "textures/gui/overlay/target_shield.png");
    private static final ResourceLocation MAGIC_RESISTANCE_ICON = new ResourceLocation(BabelMod.MODID, "textures/gui/overlay/magic_resistance.png");
    private static final ResourceLocation BARRIER_ICON = new ResourceLocation(BabelMod.MODID, "textures/gui/overlay/defense.png");
    private static final ResourceLocation ELEMENTAL_RESISTANCE_ICON = new ResourceLocation(BabelMod.MODID, "textures/gui/overlay/elemental_resistance.png");
    private static final ResourceLocation MISS_PHYSICAL_ICON = new ResourceLocation(BabelMod.MODID, "textures/gui/overlay/miss_physical.png");
    private static final ResourceLocation MISS_MAGIC_ICON = new ResourceLocation(BabelMod.MODID, "textures/gui/overlay/miss_magic.png");
    private static final NumberFormat ATTRIBUTE_FORMAT = NumberFormat.getIntegerInstance();

    @SubscribeEvent
    public static void registerOverlay(RegisterGuiOverlaysEvent event) {
        event.registerAbove(VanillaGuiOverlay.AIR_LEVEL.id(),
                "health_bar",
                HealthShowOverlay::renderHealthBar);
    }

    private static void renderHealthBar(ForgeGui gui, GuiGraphics guiGraphics, float partialTick, int width, int height) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        HealthCapability health = player == null ? null : BabelCapability.getHealth(player);
        if (player != null && !player.isSpectator() && health != null) {
            int h = mc.getWindow().getGuiScaledHeight();
            int lifeDx = (int) Math.round(BabelConfig.lifeXOffset);
            int lifeDy = (int) Math.round(BabelConfig.lifeYOffset) - 16;
            int shieldDx = lifeDx + (int) Math.round(BabelConfig.shieldXOffset);
            int shieldDy = lifeDy + (int) Math.round(BabelConfig.shieldYOffset);

            guiGraphics.blit(LIFE_POINT, 6 + lifeDx, h - 24 + lifeDy, 0, 0, 24, 16, 24, 16);

            if (health.getShield() > 0) {
                guiGraphics.blit(SHIELD_POINT, 6 + shieldDx, h - 40 + shieldDy, 0, 0, 24, 16, 24, 16);
                String shield = String.valueOf((int) health.getShield());
                guiGraphics.drawString(mc.font, shield, 21 + shieldDx, h - 36 + shieldDy, -16777216, false);
                guiGraphics.drawString(mc.font, shield, 20 + shieldDx, h - 36 + shieldDy, -1, false);
            }

            String lifeMax = "/" + (int) health.getMaxLives();
            String life = String.valueOf((int) health.getLives());
            int lifeMaxX = 20 + lifeDx + mc.font.width(life);
            guiGraphics.drawString(mc.font, lifeMax, lifeMaxX + 1, h - 21 + lifeDy, -16764109, false);
            guiGraphics.drawString(mc.font, lifeMax, lifeMaxX, h - 21 + lifeDy, -10040065, false);
            guiGraphics.drawString(mc.font, life, 21 + lifeDx, h - 21 + lifeDy, -16764109, false);
            guiGraphics.drawString(mc.font, life, 20 + lifeDx, h - 21 + lifeDy, -1, false);

            if (player.isAlive()) {
                int attributeX = 6 + lifeDx;
                int attributeY = h - 24 + lifeDy;
                int shieldY = h - 40 + shieldDy;
                if (health.getShield() > 0 && shieldY < attributeY) {
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
                        String text = ATTRIBUTE_FORMAT.format(Mth.ceil(physicalMissValue)) + "%";
                        guiGraphics.blit(MISS_PHYSICAL_ICON, missX, attributeY + 3, 0, 0, 16, 6, 16, 6);
                        guiGraphics.drawString(mc.font, text, missX + 19, attributeY, -16777216, false);
                        guiGraphics.drawString(mc.font, text, missX + 18, attributeY, -1, false);
                        missX += 18 + mc.font.width(text) + 4;
                    }
                    if (magicMissValue > 0.0) {
                        String text = ATTRIBUTE_FORMAT.format(Mth.ceil(magicMissValue)) + "%";
                        guiGraphics.blit(MISS_MAGIC_ICON, missX, attributeY + 3, 0, 0, 16, 6, 16, 6);
                        guiGraphics.drawString(mc.font, text, missX + 19, attributeY, -16777216, false);
                        guiGraphics.drawString(mc.font, text, missX + 18, attributeY, -1, false);
                    }
                    attributeY -= 12;
                }
                AttributeInstance magicResistance = player.getAttribute(BabelAttributes.MAGIC_RESISTANCE.get());
                double magicResistValue = magicResistance == null ? 0.0 : magicResistance.getValue();
                if (magicResistValue > 0.0) {
                    String text = ATTRIBUTE_FORMAT.format(Mth.ceil(magicResistValue)) + "%";
                    guiGraphics.blit(MAGIC_RESISTANCE_ICON, attributeX, attributeY, 0, 0, 9, 9, 9, 9);
                    guiGraphics.drawString(mc.font, text, attributeX + 14, attributeY, -16777216, false);
                    guiGraphics.drawString(mc.font, text, attributeX + 13, attributeY, -1, false);
                    attributeY -= 12;
                }
                AttributeInstance barrier = player.getAttribute(BabelAttributes.BARRIER.get());
                double barrierValue = barrier == null ? 0.0 : barrier.getValue();
                if (barrierValue > 0.0) {
                    String text = ATTRIBUTE_FORMAT.format(Mth.ceil(barrierValue));
                    guiGraphics.blit(BARRIER_ICON, attributeX, attributeY, 0, 0, 9, 9, 9, 9);
                    guiGraphics.drawString(mc.font, text, attributeX + 14, attributeY, -16777216, false);
                    guiGraphics.drawString(mc.font, text, attributeX + 13, attributeY, -1, false);
                    attributeY -= 12;
                }
                AttributeInstance elementalResistance = player.getAttribute(BabelAttributes.ELEMENTAL_RESISTANCE.get());
                double elementalValue = elementalResistance == null ? 0.0 : elementalResistance.getValue();
                if (elementalValue > 0.0) {
                    String text = ATTRIBUTE_FORMAT.format(Mth.ceil(elementalValue)) + "%";
                    guiGraphics.blit(ELEMENTAL_RESISTANCE_ICON, attributeX, attributeY, 0, 0, 9, 9, 9, 9);
                    guiGraphics.drawString(mc.font, text, attributeX + 14, attributeY, -16777216, false);
                    guiGraphics.drawString(mc.font, text, attributeX + 13, attributeY, -1, false);
                }
            }
        }
    }
}