package com.susen36.babel.item;

import com.susen36.babel.collectible.CollectibleActivation;
import com.susen36.babel.collectible.CollectibleItem;
import com.susen36.babel.collectible.CollectibleTiers;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

public class YaneseDictionaryItem extends CollectibleItem.CustomCollectibleItem {

    private static final float RESTORE_THRESHOLD = 0.3F;

    private static final Map<Player, BuffState> BUFF_STATES = Collections.synchronizedMap(new WeakHashMap<>());

    private record BuffState(float granted, int amplifier, int lastDuration, boolean restored) {
    }

    public YaneseDictionaryItem() {
        super(new Item.Properties().stacksTo(1), 25, false, CollectibleTiers.NORMAL,
                new CollectibleItem.Levels(0, 1, 0),
                CollectibleActivation.forTier(CollectibleTiers.NORMAL));
    }

    // 原版会在吸收心耗尽的同一 tick 移除效果，降频检查会漏掉触发窗口
    @Override
    public boolean shouldApplyEffectTickThisTick(int tickCount, int layer) {
        return true;
    }

    @Override
    public void applyEffectTick(Player player, int layer) {
        if (!player.level().isClientSide()) {
            MobEffectInstance absorption = player.getEffect(MobEffects.ABSORPTION);
            if (absorption == null) {
                BUFF_STATES.remove(player);
            } else {
                BuffState state = BUFF_STATES.get(player);
                boolean reapplied = state == null
                        || absorption.getAmplifier() != state.amplifier()
                        || absorption.getDuration() > state.lastDuration();
                float granted;
                boolean restored;
                if (reapplied) {
                    granted = player.getAbsorptionAmount();
                    restored = false;
                } else {
                    granted = state.granted();
                    restored = state.restored();
                }
                if (!restored && player.getAbsorptionAmount() < granted * RESTORE_THRESHOLD) {
                    player.setAbsorptionAmount(granted);
                    restored = true;
                }
                BUFF_STATES.put(player, new BuffState(granted, absorption.getAmplifier(), absorption.getDuration(), restored));
            }
        }
    }
}