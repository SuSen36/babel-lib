package com.susen36.babel.item;

import com.susen36.babel.collectible.CollectibleActivation;
import com.susen36.babel.collectible.CollectibleItem;
import com.susen36.babel.collectible.CollectibleTiers;
import com.susen36.babel.util.DataUtils;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;

public class FrozenClawItem extends CollectibleItem.CustomCollectibleItem {

    public FrozenClawItem() {
        super(new Item.Properties().stacksTo(1), 25, false, CollectibleTiers.CURSED,
                new CollectibleItem.Levels(0, 1, 0),
                CollectibleActivation.builder()
                        .sound(SoundEvents.AMBIENT_SOUL_SAND_VALLEY_MOOD.value(), 2F, 1F)
                        .particle(ParticleTypes.PORTAL, 99)
                        .autoUseOnGain(true)
                        .build());
        addRangeAttributeModifier(Mob.class, Attributes.ATTACK_DAMAGE, 0.12D, AttributeModifier.Operation.MULTIPLY_BASE, ENEMY_OF_PLAYER);
        addRangeAttributeModifier(Mob.class, Attributes.ARMOR, 0.12D, AttributeModifier.Operation.MULTIPLY_BASE, ENEMY_OF_PLAYER);
        addRangeAttributeModifier(Mob.class, Attributes.MAX_HEALTH, 0.12D, AttributeModifier.Operation.MULTIPLY_BASE, ENEMY_OF_PLAYER);
        // 首领额外加成；
        addRangeAttributeModifier(Mob.class, Attributes.ATTACK_DAMAGE, 0.16D, AttributeModifier.Operation.MULTIPLY_BASE, (entity2, player2) -> DataUtils.isBoss(entity2), "boss");
        addRangeAttributeModifier(Mob.class, Attributes.ARMOR, 0.16D, AttributeModifier.Operation.MULTIPLY_BASE, (entity1, player1) -> DataUtils.isBoss(entity1), "boss");
        addRangeAttributeModifier(Mob.class, Attributes.MAX_HEALTH, 0.20D, AttributeModifier.Operation.MULTIPLY_BASE, (entity, player) -> DataUtils.isBoss(entity), "boss");
    }
}