package com.susen36.babel.collectible;

import com.susen36.babel.util.CooldownUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.IItemDecorator;
import org.jetbrains.annotations.NotNull;

public class CollectiblesDecorator implements IItemDecorator {
    @Override
    public boolean render(@NotNull GuiGraphics guiGraphics, @NotNull Font font, @NotNull ItemStack stack, int x, int y) {
        var mc = Minecraft.getInstance();
        var player = mc.player;
        if (player == null) return false;

        var cd = player.getData(Collectibles.ATTACHMENT_COLLECTIBLE_COOLDOWN.get());

        long lastTicks = cd.getCooldownEndTick(stack.getItem()) - CooldownUtils.getServerCurrentTick();
        long maxTicks = CooldownUtils.getLastMaxCooldownTick(stack.getItem());

        if (lastTicks <= 0) {
            if (maxTicks >= 0) CooldownUtils.resetLastMaxCooldownTick(stack.getItem());
            return false;
        }
        if (maxTicks <= 0) CooldownUtils.setLastMaxCooldownTick(stack.getItem(), lastTicks);

        float partialTick = mc.getTimer().getGameTimeDeltaPartialTick(true);
        float progress = (lastTicks + partialTick) / (float) maxTicks;
        progress = Math.clamp(progress, 0.0F, 1.0F);

        // render overlay
        int color = 0x80FFFFFF;
        int height = Math.round(16.0F * progress);
        int topY = y + (16 - height);
        guiGraphics.fill(RenderType.guiOverlay(), x, topY, x + 16, topY + height, color);

        return false;
    }
}
