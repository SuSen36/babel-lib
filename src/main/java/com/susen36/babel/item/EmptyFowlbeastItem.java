package com.susen36.babel.item;

import com.susen36.babel.collectible.CollectibleActivation;
import com.susen36.babel.collectible.CollectibleItem;
import com.susen36.babel.collectible.CollectibleTiers;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class EmptyFowlbeastItem extends CollectibleItem.CustomCollectibleItem {


    public EmptyFowlbeastItem() {
        super(new Item.Properties().stacksTo(1).rarity(Rarity.RARE), 25, false, CollectibleTiers.RARE,
                new CollectibleItem.Levels(0, 1, 0),
                CollectibleActivation.builder()
                        .sound(SoundEvents.ALLAY_ITEM_GIVEN, 2F, 1F)
                        .particle(ParticleTypes.SNOWFLAKE, 72)
                        .showOverlay(true)
                        .build());
        addRangeAttributeModifier(LivingEntity.class, Attributes.MAX_HEALTH, 0.30D, AttributeModifier.Operation.MULTIPLY_BASE, OWNED_BY_PLAYER);
        addRangeAttributeModifier(LivingEntity.class, Attributes.ATTACK_DAMAGE, 0.30D, AttributeModifier.Operation.MULTIPLY_BASE, OWNED_BY_PLAYER);
        addConditionalAttributeModifier(Attributes.ATTACK_DAMAGE, 0.60D, AttributeModifier.Operation.MULTIPLY_BASE, OWNED_PET_IN_RANGE);
    }

    // 仅当范围内存在主人本人的宠物时返回 1 层
    private static final AttributeStacks OWNED_PET_IN_RANGE = (player, self) -> {
        int stacks = 0;
        if (player.level() instanceof ServerLevel serverLevel) {
            List<? extends LivingEntity> entities = serverLevel.getEntitiesOfClass(LivingEntity.class,
                    new AABB(player.position(), player.position()).inflate(MONSTER_RANGE));
            for (LivingEntity living : entities) {
                if (living instanceof OwnableEntity ownable && ownable.getOwner() == player) {
                    stacks = 1;
                    break;
                }
            }
        }
        return stacks;
    };
}