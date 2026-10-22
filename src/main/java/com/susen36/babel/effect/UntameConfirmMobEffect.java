package com.susen36.babel.effect;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraftforge.client.extensions.common.IClientMobEffectExtensions;

import java.util.function.Consumer;

public class UntameConfirmMobEffect extends MobEffect {
    private static final IClientMobEffectExtensions HIDDEN_EFFECT_EXTENSIONS = new IClientMobEffectExtensions() {
        @Override
        public boolean isVisibleInInventory(MobEffectInstance effect) {
            return false;
        }

        @Override
        public boolean renderInventoryText(MobEffectInstance instance, EffectRenderingInventoryScreen<?> screen, GuiGraphics guiGraphics, int x, int y, int blitOffset) {
            return false;
        }

        @Override
        public boolean isVisibleInGui(MobEffectInstance effect) {
            return false;
        }
    };

    public UntameConfirmMobEffect() {
        super(MobEffectCategory.BENEFICIAL, -26215);
    }

    @Override
    public void initializeClient(Consumer<IClientMobEffectExtensions> consumer) {
        consumer.accept(HIDDEN_EFFECT_EXTENSIONS);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }
}
