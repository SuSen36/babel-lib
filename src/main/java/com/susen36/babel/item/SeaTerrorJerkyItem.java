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

public class SeaTerrorJerkyItem extends CollectibleItem.CustomCollectibleItem {

    private static final AttributeModifier SEA_TERROR_DEBUFF = new AttributeModifier(
            UUID.fromString("7f3a9e2c-5b4d-4a1e-8c6f-1d2e3f4a5b6c"), "babel:sea_terror_jerky", -0.21, AttributeModifier.Operation.MULTIPLY_BASE);

    public SeaTerrorJerkyItem() {
        super(new Item.Properties().stacksTo(1).rarity(Rarity.RARE), 25, false, CollectibleTiers.RARE,
                new CollectibleItem.Levels(0, 1, 0),
                CollectibleActivation.builder()
                        .sound(SoundEvents.PLAYER_LEVELUP, 2F, 1F)
                        .particle(ParticleTypes.HAPPY_VILLAGER, 72)
                        .showOverlay(true)
                        .build());
        addRangeEffect(Monster.class, (monster, player, layer) -> {
            AttributeInstance armor = monster.getAttribute(Attributes.ARMOR);
            if (armor != null && armor.getModifier(UUID.fromString("7f3a9e2c-5b4d-4a1e-8c6f-1d2e3f4a5b6c")) == null) {
                armor.addTransientModifier(SEA_TERROR_DEBUFF);
            }
        });
    }
}
