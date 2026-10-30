package com.susen36.babel.collectible;

import com.susen36.babel.util.CooldownUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.IItemDecorator;
import org.jetbrains.annotations.NotNull;

public class CollectiblesDecorator implements IItemDecorator {
    @Override
    public boolean render(@NotNull GuiGraphics guiGraphics, @NotNull Font font, @NotNull ItemStack stack, int x, int y) {
        var mc = Minecraft.getInstance();
        var player = mc.player;
        if (player == null) return false;

        var cd = Collectibles.getCooldownData(player);

        long lastTicks = cd.getCooldownEndTick(stack.getItem()) - CooldownUtils.getServerCurrentTick();
        long maxTicks = CooldownUtils.getLastMaxCooldownTick(stack.getItem());

        if (lastTicks <= 0) {
            if (maxTicks >= 0) CooldownUtils.resetLastMaxCooldownTick(stack.getItem());
            return false;
        }
        if (maxTicks <= 0) CooldownUtils.setLastMaxCooldownTick(stack.getItem(), lastTicks);

        float partialTick = mc.getFrameTime();
        float progress = (lastTicks + partialTick) / (float) maxTicks;
        progress = Mth.clamp(progress, 0.0F, 1.0F);

        int color = 0x80FFFFFF;
        int height = Math.round(16.0F * progress);
        int topY = y + (16 - height);
        guiGraphics.fill(RenderType.guiOverlay(), x, topY, x + 16, topY + height, color);

        return false;
    }
}