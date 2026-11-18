package com.susen36.babel.item;

import com.susen36.babel.collectible.CollectibleActivation;
import com.susen36.babel.collectible.CollectibleItem;
import com.susen36.babel.collectible.CollectibleTiers;
import com.susen36.babel.effect.FearMobEffect;
import com.susen36.babel.init.BabelEntityTypeTags;
import com.susen36.babel.init.BabelMobEffects;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;

import java.util.Map;
import java.util.WeakHashMap;

public class NorthwindConstructItem extends CollectibleItem.CustomCollectibleItem {
    private static final String MACHINA_TRIGGERED_KEY = "babel:northwind_machina_triggered";
    private static final String APPARITION_TRIGGERED_KEY = "babel:northwind_apparition_triggered";
    private static final int FREEZE_DURATION_TICKS = 300;
    private static final int FEAR_DURATION_TICKS = 100;
    private static final int TICKS_PER_SECOND = 20;

    // 最近一次范围扫描命中的 tick，用于攻击事件触发时判定敌人是否仍在持有者范围内
    private static final Map<LivingEntity, Integer> SCANNED_TICKS = new WeakHashMap<>();

    public NorthwindConstructItem() {
        super(new Item.Properties().stacksTo(1), 25, false, CollectibleTiers.ADVANCED,
                new CollectibleItem.Levels(0, 1, 0),
                CollectibleActivation.builder()
                        .sound(SoundEvents.PLAYER_LEVELUP, 2F, 1F)
                        .particle(ParticleTypes.SNOWFLAKE, 36)
                        .build());
        addRangeEffect(LivingEntity.class, (enemy, player, layer) ->
                SCANNED_TICKS.put(enemy, enemy.tickCount), (entity, player) -> !ALLIED_WITH_PLAYER.test(entity, player));
    }

    // 范围扫描每 20 tick 一次，按一个周期内的命中判定。
    public static boolean isInRange(LivingEntity entity) {
        Integer scanned = SCANNED_TICKS.get(entity);
        return scanned != null && entity.tickCount - scanned <= TICKS_PER_SECOND;
    }

    // 每只敌人仅触发一次，触发标记持久化在实体数据中。
    public static void onMobAttack(LivingEntity attacker) {
        CompoundTag data = attacker.getPersistentData();
        if (attacker.getType().is(BabelEntityTypeTags.MACHINA) || attacker.getType().is(BabelEntityTypeTags.ORIGINIUM_CREATION)) {
            if (!data.getBoolean(MACHINA_TRIGGERED_KEY)) {
                data.putBoolean(MACHINA_TRIGGERED_KEY, true);
                attacker.addEffect(new MobEffectInstance(BabelMobEffects.FROZEN.get(), FREEZE_DURATION_TICKS, 0));
            }
        } else if (attacker.getType().is(BabelEntityTypeTags.APPARITION)) {
            if (!data.getBoolean(APPARITION_TRIGGERED_KEY)) {
                data.putBoolean(APPARITION_TRIGGERED_KEY, true);
                FearMobEffect.apply(attacker, FEAR_DURATION_TICKS);
            }
        }
    }
}