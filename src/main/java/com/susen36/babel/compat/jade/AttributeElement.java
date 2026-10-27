package com.susen36.babel.compat.jade;

import com.mojang.blaze3d.systems.RenderSystem;
import com.susen36.babel.BabelMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec2;
import snownee.jade.api.theme.IThemeHelper;
import snownee.jade.api.ui.Element;
import snownee.jade.overlay.DisplayHelper;
import snownee.jade.overlay.OverlayRenderer;

import java.util.ArrayList;
import java.util.List;

public class AttributeElement extends Element {
    private static final ResourceLocation ELEMENTAL_ICON = new ResourceLocation(BabelMod.MODID, "textures/gui/overlay/elemental_resistance.png");
    private static final ResourceLocation MAGIC_ICON = new ResourceLocation(BabelMod.MODID, "textures/gui/overlay/magic_resistance.png");
    private static final ResourceLocation BARRIER_ICON = new ResourceLocation(BabelMod.MODID, "textures/gui/overlay/defense.png");

    private record Segment(ResourceLocation icon, String text) {}
    private final List<Segment> segments = new ArrayList<>();
    private int[] offsets = new int[0];
    private int totalWidth;

    public AttributeElement(double elemental, double resistance, double barrier) {
        Font font = Minecraft.getInstance().font;
        if (elemental > 0) segments.add(new Segment(ELEMENTAL_ICON, formatPercent(elemental)));
        if (resistance > 0) segments.add(new Segment(MAGIC_ICON, formatPercent(resistance)));
        if (barrier > 0) segments.add(new Segment(BARRIER_ICON, formatValue(barrier)));
        offsets = new int[segments.size()];
        for (int i = 0; i < segments.size(); i++) {
            offsets[i] = totalWidth;
            totalWidth += font.width(segments.get(i).text()) + 17;
        }
    }

    private static String formatPercent(double v) {
        if (v > 2147483647) return "Too Large";
        return DisplayHelper.dfCommas.format(Math.ceil(v)) + "%";
    }

    private static String formatValue(double v) {
        if (v > 2147483647) return "Too Large";
        return DisplayHelper.dfCommas.format(Math.ceil(v));
    }

    @Override
    public Vec2 getSize() {
        return new Vec2((float) totalWidth, 11.0F);
    }

    @Override
    public void render(GuiGraphics guiGraphics, float x, float y, float maxX, float maxY) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, OverlayRenderer.alpha);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        for (int i = 0; i < segments.size(); i++) {
            Segment segment = segments.get(i);
            float px = x + offsets[i];
            guiGraphics.blit(segment.icon(), (int) px, (int) y, 0, 0, 9, 9, 9, 9);
            DisplayHelper.INSTANCE.drawText(guiGraphics, segment.text(), px + 13F, y, IThemeHelper.get().getNormalColor());
        }
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }
}