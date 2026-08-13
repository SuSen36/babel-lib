package com.susen36.babel.client.overlay;

import com.mojang.blaze3d.systems.RenderSystem;
import com.susen36.babel.BabelConfig;
import com.susen36.babel.BabelMod;
import com.susen36.babel.capability.BabelCapability;
import com.susen36.babel.capability.ep.EPCapability;
import com.susen36.babel.elemental.base.AbstractEPCapability;
import com.susen36.babel.init.BabelAttributes;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

@EventBusSubscriber(value = {Dist.CLIENT}, modid = BabelMod.MODID)
public class EPShowOverlay {
    private static final ResourceLocation EMPTY_ICON = rl("empty_icon");
    private static final ResourceLocation SANITY_ICON = rl("sanity_icon");
    private static final ResourceLocation WATER_ICON = rl("water_icon");
    private static final ResourceLocation FIRE_ICON = rl("fire_icon");
    private static final ResourceLocation DARK_ICON = rl("dark_icon");
    private static final ResourceLocation FRENZY_ICON = rl("anger_icon");
    private static final ResourceLocation SANITY_RING = rl("sanity");
    private static final ResourceLocation WATER_RING = rl("water");
    private static final ResourceLocation FIRE_RING = rl("fire");
    private static final ResourceLocation DARK_RING = rl("dark");
    private static final ResourceLocation FRENZY_RING = rl("anger");

    private static final RandomSource RANDOM = RandomSource.create();
    private static float bufferProgress = -1.0F;
    private static final float TICK_LERP = (float) Math.pow(0.95, 5.0D);

    private static ResourceLocation rl(String name) {
        return ResourceLocation.fromNamespaceAndPath(BabelMod.MODID, "textures/gui/ep/" + name + ".png");
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRenderGui(RenderGuiEvent.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;

        if (player != null) {
            EPCapability ep = BabelCapability.getEP(player);
            float threshold = BabelAttributes.getMaxElementalValue(player);
            AbstractEPCapability currentElement = ep.getCurrentElement();
            float progress = currentElement == null ? 0.0F : currentElement.getInjuryProgress();

            if (threshold > 0 && currentElement != null && progress < 1.0F) {
                int w = mc.getWindow().getGuiScaledWidth();
                int h = mc.getWindow().getGuiScaledHeight();
                int dx = (int) Math.round(BabelConfig.epXOffset);
                int dy = (int) Math.round(BabelConfig.epYOffset);
                AbstractEPCapability.EPType type = currentElement.getType();

                mc.getProfiler().push("ep");
                RenderSystem.enableBlend();
                if (BabelConfig.epBarStyle) {
                    renderIcons(event, player, w, h, dx, dy, type, progress, mc.gui.getGuiTicks());
                } else {
                    renderRing(event, w, h, dx, dy, type, progress);
                }
                RenderSystem.disableBlend();
                mc.getProfiler().pop();
            }
        }
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) return;

        EPCapability ep = BabelCapability.getEP(player);
        AbstractEPCapability currentElement = ep.getCurrentElement();
        float progress = currentElement == null ? 0.0F : currentElement.getInjuryProgress();
        if (progress >= 1.0F) {
            progress = 0.0F;
        }

        if (bufferProgress < 0.0F) {
            bufferProgress = progress;
        } else {
            bufferProgress = progress * (1.0F - TICK_LERP) + TICK_LERP * bufferProgress;
        }
    }

    private static void renderIcons(RenderGuiEvent.Pre event, Player player, int w, int h, int dx, int dy,
                                    AbstractEPCapability.EPType type, float progress, int guiTicks) {
        ResourceLocation icon = getIcon(type);
        int filled = Mth.clamp(Math.round(progress * 10.0F), 0, 10);
        boolean shake = guiTicks % 10 <= 3 && (progress <= 0.05F || player.hasEffect(MobEffects.DARKNESS));
        boolean flash = bufferProgress >= 0 && Math.abs(progress - bufferProgress) > 0.05F;

        for (int i = 0; i < 10; i++) {
            int x = w / 2 + 91 + dx + i * 9;
            int y = h - 26 + dy;
            if (shake) {
                y += RANDOM.nextInt(3) - 1;
            }
            if (i < filled) {
                event.getGuiGraphics().blit(icon, x, y, 0, 0, 10, 10, 10, 10);
            } else {
                event.getGuiGraphics().blit(EMPTY_ICON, x, y, 0, 0, 10, 10, 10, 10);
            }
            if (flash) {
                event.getGuiGraphics().fill(x, y, x + 10, y + 10, 0x66FFFFFF);
            }
        }
    }

    private static void renderRing(RenderGuiEvent.Pre event, int w, int h, int dx, int dy,
                                   AbstractEPCapability.EPType type, float progress) {
        ResourceLocation ring = getRing(type);
        int frame = Mth.clamp(Mth.ceil(progress * 20.0F) * 16, 0, 304);
        event.getGuiGraphics().blit(ring, w / 2 + 92 + dx, h - 19 + dy,
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