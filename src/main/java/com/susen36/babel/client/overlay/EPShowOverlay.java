package com.susen36.babel.client.overlay;

import com.mojang.blaze3d.systems.RenderSystem;
import com.susen36.babel.BabelConfig;
import com.susen36.babel.BabelMod;
import com.susen36.babel.capability.BabelCapability;
import com.susen36.babel.capability.ep.EPCapability;
import com.susen36.babel.elemental.base.AbstractEPCapability;
import com.susen36.babel.init.BabelAttributes;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

@EventBusSubscriber(value = {Dist.CLIENT}, modid = BabelMod.MODID)
public class EPShowOverlay {
    private static final ResourceLocation EMPTY_ICON = rl("empty_icon");
    private static final ResourceLocation SANITY_ICON = rl("sanity_icon");
    private static final ResourceLocation WATER_ICON = rl("water_icon");
    private static final ResourceLocation FIRE_ICON = rl("fire_icon");
    private static final ResourceLocation DARK_ICON = rl("dark_icon");
    private static final ResourceLocation FRENZY_ICON = rl("anger_icon");
    private static final ResourceLocation WHITE_ICON = rl("whiter_icon");
    private static final ResourceLocation SANITY_RING = rl("sanity");
    private static final ResourceLocation WATER_RING = rl("water");
    private static final ResourceLocation FIRE_RING = rl("fire");
    private static final ResourceLocation DARK_RING = rl("dark");
    private static final ResourceLocation FRENZY_RING = rl("anger");

    private static final RandomSource RANDOM = RandomSource.create();
    private static float bufferProgress = 0.0F;
    private static AbstractEPCapability.EPType lastType = null;
    private static final float TICK_RETENTION = 0.774F;
    private static final float BLINK_THRESHOLD = 0.02F;

    private static ResourceLocation rl(String name) {
        return ResourceLocation.fromNamespaceAndPath(BabelMod.MODID, "textures/gui/ep/" + name + ".png");
    }

    @SubscribeEvent
    public static void registerOverlay(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.AIR_LEVEL,
                ResourceLocation.fromNamespaceAndPath(BabelMod.MODID, "ep_bar"),
                EPShowOverlay::renderEpBar);
    }

    // 每游戏 tick 更新一次缓冲值（PVZ 写法），渲染帧只读，使白闪持续稳定
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) {
            return;
        }
        EPCapability ep = BabelCapability.getEP(player);
        AbstractEPCapability currentElement = ep.getCurrentElement();
        float progress = currentElement == null ? 0.0F : currentElement.getInjuryProgress();
        AbstractEPCapability.EPType type = currentElement == null ? null : currentElement.getType();
        if (lastType != type) {
            lastType = type;
            bufferProgress = progress;
        }
        if (type != null) {
            bufferProgress = progress * (1 - TICK_RETENTION) + bufferProgress * TICK_RETENTION;
        }
    }

    private static void renderEpBar(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;

        if (player != null && mc.gameMode != null && mc.gameMode.canHurtPlayer()) {
            EPCapability ep = BabelCapability.getEP(player);
            float threshold = BabelAttributes.getMaxElementalValue(player);
            AbstractEPCapability currentElement = ep.getCurrentElement();
            float progress = currentElement == null ? 0.0F : currentElement.getInjuryProgress();

            if (threshold > 0 && currentElement != null && progress < 1.0F) {
                int dx = (int) Math.round(BabelConfig.epXOffset);
                int dy = (int) Math.round(BabelConfig.epYOffset);
                AbstractEPCapability.EPType type = currentElement.getType();

                mc.getProfiler().push("ep");
                RenderSystem.enableBlend();
                if (BabelConfig.epBarStyle) {
                    int left = mc.getWindow().getGuiScaledWidth() / 2 + 91 + dx;
                    int top = mc.getWindow().getGuiScaledHeight() - mc.gui.rightHeight + dy;
                    renderIcons(guiGraphics, player, left, top, type, progress, currentElement.underBurst(), mc.gui.getGuiTicks());
                    mc.gui.rightHeight += 10;
                } else {
                    int w = mc.getWindow().getGuiScaledWidth();
                    int h = mc.getWindow().getGuiScaledHeight();
                    renderRing(guiGraphics, w, h, dx, dy, type, progress);
                }
                RenderSystem.disableBlend();
                mc.getProfiler().pop();
            }
        }
    }

    private static void renderIcons(GuiGraphics guiGraphics, Player player, int left, int top,
                                    AbstractEPCapability.EPType type, float progress, boolean burst, int guiTicks) {
        ResourceLocation icon = getIcon(type);
        int filled = Mth.clamp(Math.round(bufferProgress * 10.0F), 0, 10);
        boolean blink = Math.abs(progress - bufferProgress) > BLINK_THRESHOLD;
        boolean depleted = burst || progress <= 0.0F;
        boolean shake = guiTicks % 10 <= 3 && (depleted || player.hasEffect(MobEffects.DARKNESS));

        for (int i = 0; i < 10; i++) {
            int x = left - i * 8 - 10;
            int y = top;
            if (shake) {
                y += RANDOM.nextInt(3) - 1;
            }
            if (i < filled) {
                guiGraphics.blit(icon, x, y, 10, 10, 0, 0, 9, 9, 9, 9);
                if (blink) {
                    guiGraphics.blit(WHITE_ICON, x, y, 10, 10, 0, 0, 9, 9, 9, 9);
                }
            } else {
                guiGraphics.blit(EMPTY_ICON, x, y, 10, 10, 0, 0, 9, 9, 9, 9);
            }
        }
    }

    private static void renderRing(GuiGraphics guiGraphics, int w, int h, int dx, int dy,
                                   AbstractEPCapability.EPType type, float progress) {
        ResourceLocation ring = getRing(type);
        int frame = Mth.clamp(Mth.ceil(progress * 20.0F) * 16, 0, 304);
        guiGraphics.blit(ring, w / 2 + 92 + dx, h - 19 + dy,
                frame, 0, 16, 16, 320, 16);
    }

    private static ResourceLocation getIcon(AbstractEPCapability.EPType type) {
        return switch (type) {
            case NERVOUS -> SANITY_ICON;
            case CORROSION -> WATER_ICON;
            case BURN -> FIRE_ICON;
            case NECROSIS -> DARK_ICON;
            case FRENZY -> FRENZY_ICON;
            default -> SANITY_ICON;
        };
    }

    private static ResourceLocation getRing(AbstractEPCapability.EPType type) {
        return switch (type) {
            case NERVOUS -> SANITY_RING;
            case CORROSION -> WATER_RING;
            case BURN -> FIRE_RING;
            case NECROSIS -> DARK_RING;
            case FRENZY -> FRENZY_RING;
            default -> SANITY_RING;
        };
    }
}