package com.susen36.babel.item;

import com.susen36.babel.api.event.ElementEvent;
import com.susen36.babel.collectible.CollectibleActivation;
import com.susen36.babel.collectible.CollectibleItem;
import com.susen36.babel.collectible.CollectibleTiers;
import com.susen36.babel.util.DataUtils;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;

public class DisastersOriginItem extends CollectibleItem.CustomCollectibleItem {

    private static final float MAGIC_DAMAGE_MULTIPLIER = 1.2F;
    private static final int ELEMENTAL_INJURY_MULTIPLIER = 2;

    public DisastersOriginItem() {
        super(new Item.Properties().stacksTo(1), 25, false, CollectibleTiers.RARE,
                new CollectibleItem.Levels(0, 1, 0),
                CollectibleActivation.builder()
                        .sound(SoundEvents.PLAYER_LEVELUP, 2F, 1F)
                        .particle(ParticleTypes.HAPPY_VILLAGER, 72)
                        .showOverlay(true)
                        .build());
        addDamageEffect(Mob.class, (event, player, monster, layer) -> {
            if (DataUtils.isMagic(event.getSource())) {
                event.setAmount(event.getAmount() * MAGIC_DAMAGE_MULTIPLIER);
            }
        }, ENEMY_OF_PLAYER);
    }

    @Override
    public void onElementalDamage(ElementEvent.Hurt event, Player player, LivingEntity target, int layer) {
        if (ENEMY_OF_PLAYER.test(target, player)) {
            event.setAmount(event.getAmount() * ELEMENTAL_INJURY_MULTIPLIER);
        }
    }
}