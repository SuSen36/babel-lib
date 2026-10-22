package com.susen36.babel.effect;

import com.susen36.babel.BabelMod;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraftforge.client.extensions.common.IClientMobEffectExtensions;
import net.minecraftforge.common.ForgeMod;

import java.util.function.Consumer;

public class AddReachMobEffect extends MobEffect {
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

    public AddReachMobEffect() {
        super(MobEffectCategory.BENEFICIAL, -10066432);
        this.addAttributeModifier(ForgeMod.BLOCK_REACH.get(), "5f9a3c7e-1d2b-4c8a-9e0f-000000000001", 0.2, AttributeModifier.Operation.MULTIPLY_BASE);
        this.addAttributeModifier(ForgeMod.ENTITY_REACH.get(), "5f9a3c7e-1d2b-4c8a-9e0f-000000000002", 0.2, AttributeModifier.Operation.MULTIPLY_BASE);
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
