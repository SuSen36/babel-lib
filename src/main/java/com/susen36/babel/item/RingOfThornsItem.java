package com.susen36.babel.item;

import com.susen36.babel.collectible.CollectibleActivation;
import com.susen36.babel.collectible.CollectibleItem;
import com.susen36.babel.collectible.CollectibleTiers;
import com.susen36.babel.init.BabelMobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.eventbus.api.Event;

public class RingOfThornsItem extends CollectibleItem.CustomCollectibleItem {

    private static final double FOOD_CHANCE = 0.35D;

    public RingOfThornsItem() {
        super(new Item.Properties().stacksTo(1), 25, false, CollectibleTiers.ADVANCED,
                new CollectibleItem.Levels(0, 1, 0),
                CollectibleActivation.builder()
                        .showOverlay(true)
                        .build());
    }

    @Override
    public void onOwnerEffectApplicable(MobEffectEvent.Applicable event, Player player, int layer) {
        if (event.getEffectInstance().getEffect() == BabelMobEffects.FEAR.get()) {
            event.setResult(Event.Result.DENY);
        }
    }

    @Override
    public void onOwnerHeal(LivingHealEvent event, Player player, int layer) {
        if (!player.level().isClientSide() && Math.random() < FOOD_CHANCE) {
            player.getFoodData().eat(1, 0.0F);
        }
    }
}