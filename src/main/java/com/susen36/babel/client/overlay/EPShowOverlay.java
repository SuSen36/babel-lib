package com.susen36.babel.client.overlay;

import com.susen36.babel.BabelConfig;
import com.susen36.babel.BabelMod;
import com.susen36.babel.capability.BabelCapability;
import com.susen36.babel.capability.ep.EPCapability;
import com.susen36.babel.elemental.base.AbstractEPCapability;
import com.susen36.babel.init.BabelAttributes;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

@EventBusSubscriber(value = {Dist.CLIENT}, modid = BabelMod.MODID)
public class EPShowOverlay {
    private static final ResourceLocation SANITY_BAR = rl("sanity_player_bar");
    private static final ResourceLocation CORROSION_BAR = rl("water_player_bar");
    private static final ResourceLocation FIRE_BAR = rl("fire_player_bar");
    private static final ResourceLocation DARK_BAR = rl("dark_player_bar");
    private static final ResourceLocation FRENZY_BAR = rl("anger_player_bar");
    private static final ResourceLocation SANITY_RING = rl("sanity");
    private static final ResourceLocation WATER_RING = rl("water");
    private static final ResourceLocation FIRE_RING = rl("fire");
    private static final ResourceLocation DARK_RING = rl("dark");
    private static final ResourceLocation FRENZY_RING = rl("anger");

    private static ResourceLocation rl(String name) {
        return ResourceLocation.fromNamespaceAndPath(BabelMod.MODID, "textures/overlay/ep/" + name + ".png");
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRenderGui(RenderGuiEvent.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) return;

        EPCapability ep = BabelCapability.getEP(player);
        float threshold = BabelAttributes.getMaxElementalValue(player);
        if (threshold <= 0) return;

        int w = mc.getWindow().getGuiScaledWidth();
        int h = mc.getWindow().getGuiScaledHeight();
        int dx = (int) Math.round(BabelConfig.epXOffset);
        int dy = (int) Math.round(BabelConfig.epYOffset);

        AbstractEPCapability currentElement = ep.getCurrentElement();
        if (currentElement == null) return;

        float progress = currentElement.getInjuryProgress();
        if (progress >= 1.0F) return;

        AbstractEPCapability.EPType type = currentElement.getType();
        if (BabelConfig.epBarStyle) {
            ResourceLocation bar = getBar(type);
            event.getGuiGraphics().blit(bar, w / 2 + 93 + dx, h - 12 + dy,
                    0, 4, 62, 8, 62, 12);
            event.getGuiGraphics().blit(bar, w / 2 + 103 + dx, h - 9 + dy,
                    0, 0, (int) (50 * progress), 4, 62, 12);
        } else {
            ResourceLocation ring = getRing(type);
            int frame = Mth.clamp(Mth.ceil(progress * 20.0F) * 16, 0, 304);
            event.getGuiGraphics().blit(ring, w / 2 + 92 + dx, h - 19 + dy,
                    frame, 0, 16, 16, 320, 16);
        }
    }

    private static ResourceLocation getBar(AbstractEPCapability.EPType type) {
        return switch (type) {
            case NERVOUS -> SANITY_BAR;
            case CORROSION -> CORROSION_BAR;
            case BURN -> FIRE_BAR;
            case NECROSIS -> DARK_BAR;
            case FRENZY -> FRENZY_BAR;
            default -> SANITY_BAR;
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