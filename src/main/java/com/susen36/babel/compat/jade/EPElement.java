package com.susen36.babel.compat.jade;

import com.mojang.blaze3d.systems.RenderSystem;
import com.susen36.babel.BabelMod;
import com.susen36.babel.elemental.base.AbstractEPCapability;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;
import snownee.jade.api.theme.IThemeHelper;
import snownee.jade.api.ui.Element;
import snownee.jade.overlay.DisplayHelper;
import snownee.jade.overlay.OverlayRenderer;

public class EPElement extends Element {
    private final String text;
    private final float process;
    private final ResourceLocation icon;
    private final ResourceLocation bar;

    private static final ResourceLocation SANITY_ICON = rl("sanity_icon");
    private static final ResourceLocation SANITY_BAR = rl("sanity_bar");
    private static final ResourceLocation WATER_ICON = rl("water_icon");
    private static final ResourceLocation WATER_BAR = rl("water_bar");
    private static final ResourceLocation FIRE_ICON = rl("fire_icon");
    private static final ResourceLocation FIRE_BAR = rl("fire_bar");
    private static final ResourceLocation DARK_ICON = rl("dark_icon");
    private static final ResourceLocation DARK_BAR = rl("dark_bar");

    private static ResourceLocation rl(String name) {
        return ResourceLocation.fromNamespaceAndPath(BabelMod.MODID, "textures/overlay/ep/" + name + ".png");
    }

    public EPElement(AbstractEPCapability.EPType type, float value, float max) {
        value = (float) Math.ceil(value);
        max = (float) Math.ceil(max);
        if (max <= 0) this.process = 1.0F;
        else this.process = Mth.clamp(value / max, 0.0F, 1.0F);
        if (max < -1) this.text = "Infinity";
        else if (max > Integer.MAX_VALUE) this.text = "Too Large";
        else this.text = String.format("%s / %s",
                DisplayHelper.dfCommas.format(value),
                DisplayHelper.dfCommas.format(max));

        switch (type) {
            case CORROSION -> { this.icon = WATER_ICON; this.bar = WATER_BAR; }
            case BURN -> { this.icon = FIRE_ICON; this.bar = FIRE_BAR; }
            case NECROSIS -> { this.icon = DARK_ICON; this.bar = DARK_BAR; }
            default -> { this.icon = SANITY_ICON; this.bar = SANITY_BAR; }
        }
    }

    @Override
    public Vec2 getSize() {
        Font font = Minecraft.getInstance().font;
        return new Vec2(17.0F + (float) font.width(this.text), 12.0F);
    }

    @Override
    public void render(GuiGraphics guiGraphics, float x, float y, float maxX, float maxY) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, OverlayRenderer.alpha);
        RenderSystem.setShaderTexture(0, this.icon);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        guiGraphics.blit(this.icon, (int) x, (int) y, 0, 0, 10, 10, 10, 10);
        guiGraphics.blit(this.bar, (int) x + 11, (int) y + 8, 0, 2, 51, 2, 51, 4);
        guiGraphics.blit(this.bar, (int) x + 12, (int) y + 8, 0, 0, (int) (50 * this.process), 2, 51, 4);
        DisplayHelper.INSTANCE.drawText(guiGraphics, this.text, x + 13F, y, IThemeHelper.get().getNormalColor());
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }
}