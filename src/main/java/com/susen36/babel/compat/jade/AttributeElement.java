package com.susen36.babel.compat.jade;

import com.mojang.blaze3d.systems.RenderSystem;
import com.susen36.babel.BabelMod;
import net.minecraft.client.Minecraft;
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
    private static final ResourceLocation BARRIER_ICON = new ResourceLocation(BabelMod.MODID, "textures/gui/overlay/barrier.png");
    private static final ResourceLocation SHIELD_ICON = new ResourceLocation(BabelMod.MODID, "textures/gui/overlay/shield.png");
    private static final ResourceLocation MISS_PHYSICAL_ICON = new ResourceLocation(BabelMod.MODID, "textures/gui/overlay/miss_physical.png");
    private static final ResourceLocation MISS_MAGIC_ICON = new ResourceLocation(BabelMod.MODID, "textures/gui/overlay/miss_magic.png");
    private static final ResourceLocation NUMB_ICON = new ResourceLocation(BabelMod.MODID, "textures/gui/overlay/numb.png");

   private record Segment(ResourceLocation icon, String text, int width, int u, int drawH, int tw, int th, int iconY, int textX) {}

    private final List<Segment> segments = new ArrayList<>();
    private int[] offsets = new int[0];
    private int totalWidth;

    public AttributeElement(double elemental, double resistance, double physicalMiss, double magicMiss, int numb, double barrier, double shield) {
        if (elemental > 0) segments.add(attributeSegment(ELEMENTAL_ICON, formatPercent(elemental)));
        if (resistance > 0) segments.add(attributeSegment(MAGIC_ICON, formatPercent(resistance)));
        if (physicalMiss > 0) segments.add(missSegment(MISS_PHYSICAL_ICON, formatPercent(physicalMiss)));
        if (magicMiss > 0) segments.add(missSegment(MISS_MAGIC_ICON, formatPercent(magicMiss)));
        if (numb > 0) segments.add(numbSegment(numb));
        if (barrier > 0) segments.add(attributeSegment(BARRIER_ICON, formatValue(barrier)));
        if (shield > 0) segments.add(attributeSegment(SHIELD_ICON, formatValue(shield)));
        offsets = new int[segments.size()];
        for (int i = 0; i < segments.size(); i++) {
            offsets[i] = totalWidth;
            totalWidth += segments.get(i).width();
        }
    }

    private static Segment attributeSegment(ResourceLocation icon, String text) {
        return new Segment(icon, text, 17 + Minecraft.getInstance().font.width(text), 0, 9, 9, 9, 0, 13);
    }

    private static Segment missSegment(ResourceLocation icon, String text) {
        return new Segment(icon, text, 18 + Minecraft.getInstance().font.width(text), 0, 6, 16, 6, 1, 18);
    }

    private static Segment numbSegment(int level) {
        int width = level <= 3 ? level * 5 + 5 : 26;
        return new Segment(NUMB_ICON, "", width, 9 * (level - 1), 9, 25, 36, 0, 0);
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
            guiGraphics.blit(segment.icon(), (int) px, (int) y + segment.iconY(),
                    segment.u(), 0, segment.tw(), segment.drawH(), segment.tw(), segment.th());
            if (!segment.text().isEmpty()) {
                DisplayHelper.INSTANCE.drawText(guiGraphics, segment.text(), px + segment.textX(), y, IThemeHelper.get().getNormalColor());
            }
        }
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }
}