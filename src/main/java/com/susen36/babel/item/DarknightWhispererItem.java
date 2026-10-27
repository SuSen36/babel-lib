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

public class DarknightWhispererItem extends CollectibleItem.CustomCollectibleItem {

    private static final AttributeModifier DARKNIGHT_DEBUFF = new AttributeModifier(
            UUID.fromString("2c6b0f5e-8a1d-4c3e-9f7a-6d5e4b3a2c1b"), "babel:darknight_whisperer", -0.10, AttributeModifier.Operation.MULTIPLY_BASE);

    public DarknightWhispererItem() {
        super(new Item.Properties().stacksTo(1).rarity(Rarity.RARE), 25, false, CollectibleTiers.RARE,
                new CollectibleItem.Levels(0, 1, 0),
                CollectibleActivation.builder()
                        .sound(SoundEvents.PLAYER_LEVELUP, 2F, 1F)
                        .particle(ParticleTypes.HAPPY_VILLAGER, 72)
                        .showOverlay(true)
                        .build());
        addRangeEffect(Monster.class, (monster, player, layer) -> {
            AttributeInstance maxHealth = monster.getAttribute(Attributes.MAX_HEALTH);
            if (maxHealth != null && maxHealth.getModifier(UUID.fromString("2c6b0f5e-8a1d-4c3e-9f7a-6d5e4b3a2c1b")) == null) {
                maxHealth.addTransientModifier(DARKNIGHT_DEBUFF);
                if (monster.getHealth() > monster.getMaxHealth()) {
                    monster.setHealth(monster.getMaxHealth());
                }
            }
        });
    }
}