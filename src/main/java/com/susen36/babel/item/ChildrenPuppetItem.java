package com.susen36.babel.item;

import com.susen36.babel.collectible.CollectibleActivation;
import com.susen36.babel.collectible.CollectibleItem;
import com.susen36.babel.collectible.CollectibleTiers;
import com.susen36.babel.init.BabelMobEffects;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;

public class ChildrenPuppetItem extends CollectibleItem.CustomCollectibleItem {

    public ChildrenPuppetItem() {
        super(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON), 25, false, CollectibleTiers.RARE,
                new CollectibleItem.Levels(0, 1, 0),
                CollectibleActivation.builder()
                        .sound(SoundEvents.ENCHANTMENT_TABLE_USE, 2F, 1F)
                        .particle(ParticleTypes.ENCHANT, 72)
                        .showOverlay(true)
                        .build());
        addRangeEffect(Monster.class, (monster, player, layer) -> {
            Level level = monster.level();
            if (monster.hasEffect(BabelMobEffects.STUN.get())
                    || monster.hasEffect(BabelMobEffects.FROZEN.get())
                    || monster.hasEffect(BabelMobEffects.BIND.get())) {
                monster.hurt(level.damageSources().indirectMagic(player, player), 7.5F);
            }
        });
    }
}