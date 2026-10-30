package com.susen36.babel.item;

import com.susen36.babel.collectible.CollectibleActivation;
import com.susen36.babel.collectible.CollectibleItem;
import com.susen36.babel.collectible.CollectibleTiers;
import com.susen36.babel.init.BabelEntityTypeTags;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

import java.util.function.BiPredicate;

public class FrozenClawItem extends CollectibleItem.CustomCollectibleItem {

    private static final BiPredicate<LivingEntity, Player> BOSS = (entity, player) ->
            entity.getType().is(BabelEntityTypeTags.FORGE_BOSSES);

    public FrozenClawItem() {
        super(new Item.Properties().stacksTo(1).rarity(Rarity.RARE), 25, false, CollectibleTiers.CURSED,
                new CollectibleItem.Levels(0, 1, 0),
                CollectibleActivation.builder()
                        .sound(SoundEvents.AMBIENT_SOUL_SAND_VALLEY_MOOD.value(), 2F, 1F)
                        .particle(ParticleTypes.PORTAL, 99)
                        .autoUseOnGain(true)
                        .build());
        addRangeAttributeModifier(Monster.class, Attributes.ATTACK_DAMAGE, 0.12D, AttributeModifier.Operation.MULTIPLY_BASE);
        addRangeAttributeModifier(Monster.class, Attributes.ARMOR, 0.12D, AttributeModifier.Operation.MULTIPLY_BASE);
        addRangeAttributeModifier(Monster.class, Attributes.MAX_HEALTH, 0.12D, AttributeModifier.Operation.MULTIPLY_BASE);
        // 首领为「还会+16%/+20%」，与基础 12% 合并申报；末影龙与凋灵不是 Monster 子类，故走 LivingEntity 通道
        addRangeAttributeModifier(LivingEntity.class, Attributes.ATTACK_DAMAGE, 0.16D, AttributeModifier.Operation.MULTIPLY_BASE, BOSS, "boss");
        addRangeAttributeModifier(LivingEntity.class, Attributes.ARMOR, 0.16D, AttributeModifier.Operation.MULTIPLY_BASE, BOSS, "boss");
        addRangeAttributeModifier(LivingEntity.class, Attributes.MAX_HEALTH, 0.20D, AttributeModifier.Operation.MULTIPLY_BASE, BOSS, "boss");
    }
}