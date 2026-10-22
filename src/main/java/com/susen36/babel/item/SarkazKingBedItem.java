package com.susen36.babel.item;

import com.susen36.babel.collectible.CollectibleActivation;
import com.susen36.babel.collectible.CollectibleItem;
import com.susen36.babel.collectible.CollectibleTiers;
import com.susen36.babel.util.LifePointUtils;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;

public class SarkazKingBedItem extends CollectibleItem.CustomCollectibleItem {

    public SarkazKingBedItem() {
        super(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON), 25, false, CollectibleTiers.NORMAL,
                new CollectibleItem.Levels(0, 1, 0),
                CollectibleActivation.builder()
                        .sound(SoundEvents.PLAYER_LEVELUP, 2F, 1F)
                        .particle(ParticleTypes.HAPPY_VILLAGER, 72)
                        .showOverlay(true)
                        .build());
        addConditionalAttributeModifier(Attributes.ARMOR, 3.0, AttributeModifier.Operation.ADDITION,
                (player, self) -> LifePointUtils.getLifePoint(player) >= LifePointUtils.getMaxLifePoint(player) ? 1 : 0);
        addConditionalAttributeModifier(Attributes.ARMOR_TOUGHNESS, 2.0, AttributeModifier.Operation.ADDITION,
                (player, self) -> LifePointUtils.getLifePoint(player) >= LifePointUtils.getMaxLifePoint(player) ? 1 : 0);
    }
}
