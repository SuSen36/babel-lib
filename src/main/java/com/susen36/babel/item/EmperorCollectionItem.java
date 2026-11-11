package com.susen36.babel.item;

import com.susen36.babel.collectible.CollectibleActivation;
import com.susen36.babel.collectible.CollectibleItem;
import com.susen36.babel.collectible.CollectibleTiers;
import com.susen36.babel.util.DataUtils;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.Item;

public class EmperorCollectionItem extends CollectibleItem.CustomCollectibleItem {

    public EmperorCollectionItem() {
        super(new Item.Properties().stacksTo(1), 25, false, CollectibleTiers.RARE, new CollectibleItem.Levels(0, 1, 0),
                CollectibleActivation.builder()
                        .sound(SoundEvents.PLAYER_LEVELUP, 2F, 1F)
                        .particle(ParticleTypes.HAPPY_VILLAGER, 72)
                        .showOverlay(true)
                        .build());
        addDamageEffect(Monster.class, (event, player, monster, layer) -> {
            if (DataUtils.isMagic(event.getSource())) {
                event.setAmount(event.getAmount() * 1.3F);
            }
        });
    }
}