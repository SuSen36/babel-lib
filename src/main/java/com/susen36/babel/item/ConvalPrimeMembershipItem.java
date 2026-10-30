package com.susen36.babel.item;

import com.susen36.babel.collectible.CollectibleActivation;
import com.susen36.babel.collectible.CollectibleItem;
import com.susen36.babel.collectible.CollectibleTiers;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

// Conval 为 Convalescence（疗养）的非标准缩写
public class ConvalPrimeMembershipItem extends CollectibleItem.CustomCollectibleItem {

    public ConvalPrimeMembershipItem() {
        super(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON), 25, false, CollectibleTiers.NORMAL,
                new CollectibleItem.Levels(0, 1, 0),
                CollectibleActivation.builder()
                        .sound(SoundEvents.PLAYER_LEVELUP, 2F, 1F)
                        .particle(ParticleTypes.HAPPY_VILLAGER, 72)
                        .showOverlay(true)
                        .build());
        addConditionalAttributeModifier(Attributes.ATTACK_SPEED, 0.45D, AttributeModifier.Operation.MULTIPLY_BASE,
                (player, self) -> player.hasEffect(MobEffects.REGENERATION) ? 1 : 0);
    }
}
