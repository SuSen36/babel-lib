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

public class AbyssalWyrdmaskItem extends CollectibleItem.CustomCollectibleItem {

    private static final AttributeModifier ATTACK_DEBUFF = new AttributeModifier(
            UUID.fromString("9e4b2d7c-6a3f-4e8b-8d1c-5f2a7b9c0d3e"), "babel:abyssal_wyrdmask", -0.12, AttributeModifier.Operation.MULTIPLY_BASE);

    public AbyssalWyrdmaskItem() {
        super(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON), 25, false, CollectibleTiers.NORMAL,
                new CollectibleItem.Levels(0, 1, 0),
                CollectibleActivation.builder()
                        .sound(SoundEvents.PLAYER_LEVELUP, 2F, 1F)
                        .particle(ParticleTypes.HAPPY_VILLAGER, 72)
                        .showOverlay(true)
                        .build());
        addRangeEffect(Monster.class, (monster, player, layer) -> {
            AttributeInstance attackDamage = monster.getAttribute(Attributes.ATTACK_DAMAGE);
            if (attackDamage != null && attackDamage.getModifier(UUID.fromString("9e4b2d7c-6a3f-4e8b-8d1c-5f2a7b9c0d3e")) == null) {
                attackDamage.addTransientModifier(ATTACK_DEBUFF);
            }
        });
    }
}