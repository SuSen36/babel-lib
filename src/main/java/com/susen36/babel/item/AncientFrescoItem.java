package com.susen36.babel.item;

import com.susen36.babel.collectible.CollectibleActivation;
import com.susen36.babel.collectible.CollectibleItem;
import com.susen36.babel.collectible.CollectibleTiers;
import com.susen36.babel.collectible.Collectibles;
import com.susen36.babel.init.BabelCollectible;
import com.susen36.babel.init.BabelMobEffects;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;

import java.util.Map;
import java.util.WeakHashMap;

public class AncientFrescoItem extends CollectibleItem.CustomCollectibleItem {

    // 15 秒周期
    private static final int PUNISH_PERIOD_TICKS = 300;

    // 各实体上次被壁画惩罚的 tick，用于周期判定
    private static final Map<LivingEntity, Integer> TRIGGER_TICKS = new WeakHashMap<>();

    public AncientFrescoItem() {
        super(new Item.Properties().stacksTo(1), 25, false, CollectibleTiers.RARE,
                new CollectibleItem.Levels(0, 1, 0),
                CollectibleActivation.builder()
                        .sound(SoundEvents.PLAYER_LEVELUP, 2F, 1F)
                        .particle(ParticleTypes.TOTEM_OF_UNDYING, 72)
                        .showOverlay(true)
                        .build());
        addRangeEffect(Mob.class, (monster, player, layer) -> {
            Integer last = TRIGGER_TICKS.get(monster);
            if ((monster.hasEffect(MobEffects.LEVITATION) || monster.hasEffect(BabelMobEffects.MASS_LOSS.get()))
                    && (last == null || player.tickCount - last >= PUNISH_PERIOD_TICKS)) {
                TRIGGER_TICKS.put(monster, player.tickCount);
                monster.hurt(monster.damageSources().indirectMagic(player, player), 10.0F);
                if (monster.isAlive()) {
                    monster.addEffect(new MobEffectInstance(BabelMobEffects.SILENCED.get(), 200, 0));
                }
            }
        }, ENEMY_OF_PLAYER);
    }

    // 敌人的浮空或失重效果自然到期时惩罚一次，主动移除不经过此方法。
    public static void punishOnExpire(LivingEntity entity, MobEffect effect) {
        if (effect == MobEffects.LEVITATION || effect == BabelMobEffects.MASS_LOSS.get()) {
            TRIGGER_TICKS.remove(entity);
            punish(entity);
        }
    }

    private static void punish(LivingEntity entity) {
        if (!entity.level().isClientSide() && !(entity instanceof Player) && entity.isAlive() && entity.level() instanceof ServerLevel level) {
            for (Player player : level.players()) {
                if (player.isAlive() && Collectibles.isUsed(player, BabelCollectible.ANCIENT_FRESCO)) {
                    TRIGGER_TICKS.put(entity, player.tickCount);
                    entity.hurt(entity.damageSources().indirectMagic(player, player), 10.0F);
                    if (entity.isAlive()) {
                        entity.addEffect(new MobEffectInstance(BabelMobEffects.SILENCED.get(), 200, 0));
                    }
                    break;
                }
            }
        }
    }
}