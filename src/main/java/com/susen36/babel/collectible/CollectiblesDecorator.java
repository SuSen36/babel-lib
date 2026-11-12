package com.susen36.babel.collectible;

import com.susen36.babel.util.CooldownUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.IItemDecorator;
import org.jetbrains.annotations.NotNull;

public class CollectiblesDecorator implements IItemDecorator {
    private static final int SLOT_SIZE = 16;
    private static final int COOLDOWN_OVERLAY_COLOR = 0x80FFFFFF;

    @Override
    public boolean render(@NotNull GuiGraphics guiGraphics, @NotNull Font font, @NotNull ItemStack stack, int x, int y) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player != null) {
            var cd = Collectibles.getCooldownData(player);
            Item item = stack.getItem();
            long lastTicks = cd.getCooldownEndTick(item) - CooldownUtils.getServerCurrentTick();
            long maxTicks = cd.getCooldownDuration(item);
            if (lastTicks > 0 && maxTicks > 0) {
                float progress = Mth.clamp(
                        ((float) lastTicks + mc.getFrameTime()) / (float) maxTicks,
                        0.0F, 1.0F);
                int height = Math.round(SLOT_SIZE * progress);
                int topY = y + (SLOT_SIZE - height);
                guiGraphics.fill(
                        RenderType.guiOverlay(),
                        x, topY,
                        x + SLOT_SIZE, topY + height,
                        COOLDOWN_OVERLAY_COLOR);
            }
        }
        return false;
    }
}