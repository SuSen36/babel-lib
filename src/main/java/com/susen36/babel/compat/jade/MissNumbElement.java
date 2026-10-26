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

public class MissNumbElement extends Element {
    private final String physical;
    private final String magic;
    private final int style;
    private final boolean showPhysical;
    private final boolean showMagic;

    private static final ResourceLocation MISS_ICON = new ResourceLocation(BabelMod.MODID, "textures/gui/overlay/miss_physical.png");
    private static final ResourceLocation MISS_MAGIC_ICON = new ResourceLocation(BabelMod.MODID, "textures/gui/overlay/miss_magic.png");
    private static final ResourceLocation NUMB_ICON = new ResourceLocation(BabelMod.MODID, "textures/gui/overlay/numb.png");

    public MissNumbElement(int physical, int magic, int numb) {
        this.physical = physical > 0 ? "" + physical : "";
        this.magic = magic > 0 ? "" + magic : "";
        this.style = numb;
        this.showPhysical = physical > 0;
        this.showMagic = magic > 0;
    }

    @Override
    public Vec2 getSize() {
        return new Vec2((float) (getPhysicalPartLen() + getMagicPartLen() + getNumbPartLen()), 11.0F);
    }

    public int getPhysicalPartLen() {
        if (!showPhysical) return 0;
        return Minecraft.getInstance().font.width(this.physical) + 18;
    }

    public int getMagicPartLen() {
        if (!showMagic) return 0;
        return Minecraft.getInstance().font.width(this.magic) + 18;
    }

    public int getNumbPartLen() {
        if (style <= 0) return 0;
        if (style <= 3) return style * 5 + 5;
        else return 26;
    }

    @Override
    public void render(GuiGraphics guiGraphics, float x, float y, float maxX, float maxY) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, OverlayRenderer.alpha);
        RenderSystem.setShaderTexture(0, MISS_ICON);
        RenderSystem.setShaderTexture(0, MISS_MAGIC_ICON);
        RenderSystem.setShaderTexture(0, NUMB_ICON);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        float cursor = x;
        if (showPhysical) {
            guiGraphics.blit(MISS_ICON, (int) cursor, (int) y + 1, 0, 0, 16, 6, 16, 6);
            DisplayHelper.INSTANCE.drawText(guiGraphics, this.physical, cursor + 18F, y, IThemeHelper.get().getNormalColor());
            cursor += getPhysicalPartLen();
        }
        if (showMagic) {
            guiGraphics.blit(MISS_MAGIC_ICON, (int) cursor, (int) y + 1, 0, 0, 16, 6, 16, 6);
            DisplayHelper.INSTANCE.drawText(guiGraphics, this.magic, cursor + 18F, y, IThemeHelper.get().getNormalColor());
            cursor += getMagicPartLen();
        }
        if (style > 0) guiGraphics.blit(NUMB_ICON, (int) cursor, (int) y, 0, 9 * (style - 1), 25, 9, 25, 36);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }
}