package com.susen36.babel.item;

import com.susen36.babel.collectible.CollectibleActivation;
import com.susen36.babel.collectible.CollectibleItem;
import com.susen36.babel.collectible.CollectibleTiers;
import com.susen36.babel.init.BabelAttributes;
import com.susen36.babel.util.LifePointUtils;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class SpecialSuppressorItem extends CollectibleItem.CustomCollectibleItem {
    public SpecialSuppressorItem() {
        super(new Item.Properties().stacksTo(1), 25, false, CollectibleTiers.NORMAL, new CollectibleItem.Levels(0, 1, 0),
                CollectibleActivation.builder()
                        .sound(SoundEvents.PLAYER_LEVELUP, 3.5F, 1F)
                        .particle(ParticleTypes.SMOKE, 72)
                        .showOverlay(true)
                        .build());
        addAttributeModifier(BabelAttributes.MAX_LIFE_POINT.get(), 2, AttributeModifier.Operation.ADDITION);
    }

    @Override
    public void onEffectStarted(ItemStack stack, Level level, Player player, int layer) {
        LifePointUtils.addLifePoint(player, 2);
        LifePointUtils.healLifePoint(player, 2);
    }
}