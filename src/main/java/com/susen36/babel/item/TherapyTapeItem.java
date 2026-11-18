package com.susen36.babel.item;

import com.susen36.babel.collectible.CollectibleActivation;
import com.susen36.babel.collectible.CollectibleItem;
import com.susen36.babel.collectible.CollectibleTiers;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.item.Item;

import java.util.Map;
import java.util.WeakHashMap;

public class TherapyTapeItem extends CollectibleItem.CustomCollectibleItem {

    private static final double EFFECT_RANGE = 16.0D;
    private static final int TICKS_PER_SECOND = 20;

    // 最近一次范围扫描命中的 tick，用于事件触发时判定敌人是否仍在持有者范围内
    private static final Map<LivingEntity, Integer> SCANNED_TICKS = new WeakHashMap<>();

    public TherapyTapeItem() {
        super(new Item.Properties().stacksTo(1), 25, false, CollectibleTiers.ADVANCED,
                new CollectibleItem.Levels(0, 1, 0),
                CollectibleActivation.builder()
                        .sound(SoundEvents.PLAYER_LEVELUP, 2F, 1F)
                        .particle(ParticleTypes.NOTE, 72)
                        .showOverlay(true)
                        .build());
        addRangeEffect(LivingEntity.class, EFFECT_RANGE, (enemy, player, layer) ->
                SCANNED_TICKS.put(enemy, enemy.tickCount), (entity, player) -> entity instanceof Enemy && entity.isAlive());
    }

    // 范围扫描每 20 tick 一次，按一个周期内的命中判定。
    public static boolean isInRange(LivingEntity entity) {
        Integer scanned = SCANNED_TICKS.get(entity);
        return scanned != null && entity.tickCount - scanned <= TICKS_PER_SECOND;
    }
}