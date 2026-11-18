package com.susen36.babel.item;

import com.susen36.babel.api.event.LifePointConsumeEvent;
import com.susen36.babel.collectible.CollectibleActivation;
import com.susen36.babel.collectible.CollectibleItem;
import com.susen36.babel.collectible.CollectibleTiers;
import com.susen36.babel.init.BabelDamageTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.phys.AABB;

public class MemoriesTheShardItem extends CollectibleItem.CustomCollectibleItem {

    private static final float TRUE_DAMAGE = 19.0F;
    private static final double EFFECT_RANGE = 16.0D;

    public MemoriesTheShardItem() {
        super(new Item.Properties().stacksTo(1), 25, false, CollectibleTiers.ADVANCED,
                new CollectibleItem.Levels(0, 1, 0),
                CollectibleActivation.builder()
                        .sound(SoundEvents.GLASS_BREAK, 1.5F, 0.8F)
                        .showOverlay(true)
                        .build());
    }

    @Override
    public void onLifePointLoss(LifePointConsumeEvent event, Player player, int layer) {
        for (Mob mob : player.level().getEntitiesOfClass(Mob.class,
                new AABB(player.position(), player.position()).inflate(EFFECT_RANGE),
                entity -> ENEMY_OF_PLAYER.test(entity, player))) {
            mob.hurt(BabelDamageTypes.source(player.level(), BabelDamageTypes.TRUE_DAMAGE), TRUE_DAMAGE);
        }
    }
}