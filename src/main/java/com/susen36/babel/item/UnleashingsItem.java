package com.susen36.babel.item;

import com.susen36.babel.api.event.ElementEvent;
import com.susen36.babel.collectible.CollectibleActivation;
import com.susen36.babel.collectible.CollectibleItem;
import com.susen36.babel.collectible.CollectibleTiers;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraftforge.event.entity.living.LivingDamageEvent;

import java.util.ArrayList;
import java.util.List;

public class UnleashingsItem extends CollectibleItem.CustomCollectibleItem {

    /** 药水延长时长上限：5 分钟 = 6000 tick */
    private static final int MAX_EFFECT_DURATION_TICKS = 6000;
    private static final int EXTEND_TICKS = 20;
    private static final float CHANCE_FOOD = 0.25F;
    private static final float CHANCE_SATURATION = 0.50F;
    private static final float CHANCE_HEAL = 0.70F;

    public UnleashingsItem() {
        super(new Item.Properties().stacksTo(1), 25, false, CollectibleTiers.ADVANCED,
                new CollectibleItem.Levels(0, 1, 0),
                CollectibleActivation.builder()
                        .sound(SoundEvents.PLAYER_LEVELUP, 2F, 1F)
                        .particle(ParticleTypes.HAPPY_VILLAGER, 72)
                        .showOverlay(true)
                        .build());
    }

    @Override
    public void onMobHurt(LivingDamageEvent event, Player player, int layer) {
        if (event.getAmount() > 0 && !player.level().isClientSide() && player.isAlive()) {
            applyRandomBenefit(player);
        }
    }

    @Override
    public void onElementalHurt(ElementEvent.PostHurt event, Player player, int layer) {
        if (!player.level().isClientSide() && player.isAlive()) {
            applyRandomBenefit(player);
        }
    }

    // 受击收益：25% 饥饿 / 25% 饱和 / 20% 回血 / 40% 延长增益药水
    private void applyRandomBenefit(Player player) {
        float roll = player.getRandom().nextFloat();
        if (roll < CHANCE_FOOD) {
            player.getFoodData().eat(1, 0.0F);
        } else if (roll < CHANCE_SATURATION) {
            float foodLevel = player.getFoodData().getFoodLevel();
            float saturation = Math.min(player.getFoodData().getSaturationLevel() + 1.0F, foodLevel);
            player.getFoodData().setSaturation(saturation);
        } else if (roll < CHANCE_HEAL) {
            player.heal(1.0F);
        } else {
            // 随机挑选一个增益且非瞬时的药水效果延长 1 秒，总时长封顶 5 分钟；无可延长效果时判定落空
            List<MobEffectInstance> extendable = new ArrayList<>();
            for (MobEffectInstance instance : player.getActiveEffects()) {
                if (instance.getEffect().isBeneficial() && !instance.getEffect().isInstantenous()) {
                    extendable.add(instance);
                }
            }
            if (!extendable.isEmpty()) {
                MobEffectInstance picked = extendable.get(player.getRandom().nextInt(extendable.size()));
                int newDuration = Math.min(picked.getDuration() + EXTEND_TICKS, MAX_EFFECT_DURATION_TICKS);
                player.addEffect(new MobEffectInstance(picked.getEffect(), newDuration, picked.getAmplifier(),
                        picked.isAmbient(), picked.isVisible(), picked.showIcon()));
            }
        }
    }
}
