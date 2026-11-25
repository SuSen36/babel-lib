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

/**
 * 物品栏渲染装饰：为处于冷却的收藏品叠加白色进度遮罩，仅客户端生效。
 */
public class CollectiblesDecorator implements IItemDecorator {
    private static final int SLOT_SIZE = 16;
    private static final int COOLDOWN_OVERLAY_COLOR = 0x80FFFFFF;

    @Override
    public boolean render(@NotNull GuiGraphics guiGraphics, @NotNull Font font, @NotNull ItemStack stack, int x, int y) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        Collectibles data = player == null ? null : Collectibles.getData(player);
        if (data != null) {
            Collectibles.Cooldown cd = data.cooldown;
            Item item = stack.getItem();
            long maxTicks = cd.getCooldownDuration(item);
            if (maxTicks > 0) {
                long remainingTicks = cd.getRemainingTicks(item, CooldownUtils.getServerCurrentTick());
                if (remainingTicks > 0) {
                    float progress = Mth.clamp(
                            ((float) remainingTicks + mc.getFrameTime()) / (float) maxTicks,
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
        }
        return false;
    }
}