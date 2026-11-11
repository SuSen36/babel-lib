package com.susen36.babel.item;

import com.susen36.babel.api.event.LifePointConsumeEvent;
import com.susen36.babel.collectible.CollectibleActivation;
import com.susen36.babel.collectible.CollectibleItem;
import com.susen36.babel.collectible.CollectibleTiers;
import com.susen36.babel.init.BabelAttributes;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class LivingWoodplateItem extends CollectibleItem.CustomCollectibleItem {

    private static final double BARRIER_RATIO = 0.5;

    public LivingWoodplateItem() {
        super(new Item.Properties().stacksTo(1), 25, false, CollectibleTiers.RARE, new CollectibleItem.Levels(0, 1, 0),
                CollectibleActivation.builder()
                        .sound(SoundEvents.WOOD_PLACE, 2F, 1F)
                        .particle(ParticleTypes.COMPOSTER, 72)
                        .showOverlay(true)
                        .build());
    }

    @Override
    public void onEffectStarted(ItemStack stack, Level level, Player player, int layer) {
        grantBarrier(player);
    }

    @Override
    public void onLifePointLoss(LifePointConsumeEvent event, Player player, int layer) {
        grantBarrier(player);
    }

    private void grantBarrier(Player player) {
        AttributeInstance instance = player.getAttribute(BabelAttributes.BARRIER.get());
        if (instance != null) {
            instance.setBaseValue(instance.getBaseValue() + player.getMaxHealth() * BARRIER_RATIO);
        }
    }
}