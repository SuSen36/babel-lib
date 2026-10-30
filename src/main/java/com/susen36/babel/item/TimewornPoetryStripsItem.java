package com.susen36.babel.item;

import com.susen36.babel.collectible.CollectibleActivation;
import com.susen36.babel.collectible.CollectibleItem;
import com.susen36.babel.collectible.CollectibleTiers;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.event.entity.living.LivingDamageEvent;

public class TimewornPoetryStripsItem extends CollectibleItem.CustomCollectibleItem {

    public TimewornPoetryStripsItem() {
        super(new Item.Properties().stacksTo(1).rarity(Rarity.RARE), 25, false, CollectibleTiers.RARE,
                new CollectibleItem.Levels(0, 1, 0),
                CollectibleActivation.builder()
                        .sound(SoundEvents.BOOK_PAGE_TURN, 2F, 1F)
                        .particle(ParticleTypes.ENCHANT, 72)
                        .showOverlay(true)
                        .build());
        addRangeAttributeModifier(LivingEntity.class, Attributes.MAX_HEALTH, 0.30D, AttributeModifier.Operation.MULTIPLY_BASE, OWNED_BY_PLAYER);
        addRangeAttributeModifier(LivingEntity.class, Attributes.ATTACK_DAMAGE, 0.30D, AttributeModifier.Operation.MULTIPLY_BASE, OWNED_BY_PLAYER);
    }

    @Override
    public void onDealDamage(LivingDamageEvent event, Player player, LivingEntity target, int layer) {
        // 主人对自家宠物的伤害按最终伤害削减 99%
        if (target instanceof OwnableEntity ownable && ownable.getOwner() == player) {
            event.setAmount(event.getAmount() * 0.01F);
        }
    }
}