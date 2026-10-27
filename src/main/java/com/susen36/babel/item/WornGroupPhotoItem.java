package com.susen36.babel.item;

import com.susen36.babel.collectible.CollectibleActivation;
import com.susen36.babel.collectible.CollectibleItem;
import com.susen36.babel.collectible.CollectibleTiers;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

import java.util.UUID;

public class WornGroupPhotoItem extends CollectibleItem.CustomCollectibleItem {

    private static final AttributeModifier ARMOR_DEBUFF = new AttributeModifier(
            UUID.fromString("5b2c8e1a-7d4f-43a9-b6e2-9c1d0f3a4b5e"), "babel:worn_group_photo", -0.12, AttributeModifier.Operation.MULTIPLY_BASE);

    public WornGroupPhotoItem() {
        super(new Item.Properties().stacksTo(1).rarity(Rarity.COMMON), 25, false, CollectibleTiers.NORMAL,
                new CollectibleItem.Levels(0, 1, 0),
                CollectibleActivation.builder()
                        .sound(SoundEvents.PLAYER_LEVELUP, 2F, 1F)
                        .particle(ParticleTypes.HAPPY_VILLAGER, 72)
                        .showOverlay(true)
                        .build());
        addRangeEffect(Monster.class, (monster, player, layer) -> {
            AttributeInstance armor = monster.getAttribute(Attributes.ARMOR);
            if (armor != null && armor.getModifier(UUID.fromString("5b2c8e1a-7d4f-43a9-b6e2-9c1d0f3a4b5e")) == null) {
                armor.addTransientModifier(ARMOR_DEBUFF);
            }
        });
    }
}