package com.susen36.babel.item;

import com.susen36.babel.collectible.CollectibleActivation;
import com.susen36.babel.collectible.CollectibleItem;
import com.susen36.babel.collectible.CollectibleTiers;
import com.susen36.babel.init.BabelAttributes;
import com.susen36.babel.util.DataUtils;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

public class CastlesOffspringItem extends CollectibleItem.CustomCollectibleItem {

    // boss 战判定范围与 bossbar 可见范围一致（参照 CannotEntity 的 64 格）
    private static final double BOSS_RANGE = 64.0D;
    // 50 秒进入状态
    private static final int REQUIRED_TICKS = 1000;
    // 离开 boss 战后每秒流失总进度的 5%
    private static final int DECAY_PER_SECOND = (int) (REQUIRED_TICKS * 0.05);
    private static final int TICKS_PER_SECOND = 20;

    private static final Map<Player, Integer> PROGRESS = Collections.synchronizedMap(new WeakHashMap<>());
    // 最近一次扫描命中 boss 的 tick，用于判定当前周期是否处于 boss 战
    private static final Map<Player, Integer> BOSS_SIGHT_TICKS = new WeakHashMap<>();

    public CastlesOffspringItem() {
        super(new Item.Properties().stacksTo(1), 25, false, CollectibleTiers.ADVANCED,
                new CollectibleItem.Levels(0, 1, 0),
                CollectibleActivation.builder()
                        .sound(SoundEvents.PLAYER_LEVELUP, 2F, 1F)
                        .particle(ParticleTypes.PORTAL, 72)
                        .showOverlay(true)
                        .build());
        addConditionalAttributeModifier(BabelAttributes.DEFENSE.get(), 6.0, AttributeModifier.Operation.ADDITION,
                (player, ignoredStacks) -> stageStacks(player));
        addConditionalAttributeModifier(BabelAttributes.MAGIC_RESISTANCE.get(), 30.0, AttributeModifier.Operation.ADDITION,
                (player, ignoredStacks) -> stageStacks(player));
        addRangeEffect(LivingEntity.class, BOSS_RANGE, (boss, player, layer) ->
                BOSS_SIGHT_TICKS.put(player, player.tickCount), CastlesOffspringItem::aliveBoss);
    }

    private static boolean aliveBoss(LivingEntity entity, Player player) {
        return entity.isAlive() && DataUtils.isBoss(entity);
    }

    private static int stageStacks(Player player) {
        Integer progress = PROGRESS.get(player);
        return progress != null && progress >= REQUIRED_TICKS ? 1 : 0;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int tickCount, int layer) {
        return tickCount % TICKS_PER_SECOND == 0;
    }

    @Override
    public void applyEffectTick(Player player, int layer) {
        boolean wasReady = stageStacks(player) > 0;
        Integer sightTick = BOSS_SIGHT_TICKS.get(player);
        boolean inBossFight = sightTick != null && player.tickCount - sightTick <= TICKS_PER_SECOND;
        int progress = PROGRESS.getOrDefault(player, 0);
        if (inBossFight) {
            progress = Math.min(progress + TICKS_PER_SECOND, REQUIRED_TICKS);
        } else if (progress > 0) {
            progress = Math.max(progress - DECAY_PER_SECOND, 0);
        }
        if (progress == 0) {
            PROGRESS.remove(player);
        } else {
            PROGRESS.put(player, progress);
        }
        if (!wasReady && stageStacks(player) > 0 && player.level() instanceof ServerLevel level) {
            level.sendParticles(ParticleTypes.WAX_ON, player.getX(), player.getY() + 1.0, player.getZ(), 16, 0.5, 0.8, 0.5, 0.1);
        }
    }
}