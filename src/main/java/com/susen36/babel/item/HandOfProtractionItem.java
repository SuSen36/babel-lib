package com.susen36.babel.item;

import com.susen36.babel.collectible.CollectibleActivation;
import com.susen36.babel.collectible.CollectibleAttributes;
import com.susen36.babel.collectible.CollectibleItem;
import com.susen36.babel.collectible.CollectibleTiers;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public class HandOfProtractionItem extends CollectibleItem.CustomCollectibleItem {

    private static final int STACK_GAIN_INTERVAL_TICKS = 600;
    private static final int DECAY_GRACE_TICKS = 160;
    private static final int DECAY_INTERVAL_TICKS = 100;
    private static final int MAX_STACKS = 5;
    // applyEffects 每 80 tick 心跳一次；留少量余量，超时未刷新即视为已离开范围
    private static final long STALE_TICKS = 100;

    private static final Map<Player, Stacks> ACTIVE_STACKS = new WeakHashMap<>();
    private static final Map<Player, Long> LAST_BEACON_TICKS = new WeakHashMap<>();

    private record Stacks(int count, long lastGainTick, long lastInRangeTick, long lastDecayTick) {
    }

    public HandOfProtractionItem() {
        super(new Item.Properties().stacksTo(1), 25, false, CollectibleTiers.ADVANCED,
                new CollectibleItem.Levels(0, 1, 0),
                CollectibleActivation.builder()
                        .sound(SoundEvents.PLAYER_LEVELUP, 2F, 1F)
                        .particle(ParticleTypes.GLOW, 72)
                        .showOverlay(true)
                        .build());
        addConditionalAttributeModifier(Attributes.ATTACK_DAMAGE, 0.25, AttributeModifier.Operation.MULTIPLY_BASE,
                (player, self) -> activeStacks(player));
    }

    // 由 BeaconBlockEntityMixin 捕获原版 applyEffects 实际选中的玩家写入
    public static void markBeaconAffected(Level level, List<Player> affectedPlayers) {
        if (!level.isClientSide()) {
            long gameTime = level.getGameTime();
            for (Player player : affectedPlayers) {
                LAST_BEACON_TICKS.put(player, gameTime);
            }
        }
    }

    private static boolean isInActiveBeaconRange(Player player) {
        Long lastBeaconTick = LAST_BEACON_TICKS.get(player);
        return lastBeaconTick != null && player.level().getGameTime() - lastBeaconTick <= STALE_TICKS;
    }

    private static int activeStacks(Player player) {
        Stacks stacks = ACTIVE_STACKS.get(player);
        return stacks != null ? stacks.count() : 0;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int tickCount, int layer) {
        return tickCount % 20 == 0;
    }

    @Override
    public void applyEffectTick(Player player, int layer) {
        if (!player.level().isClientSide()) {
            long tick = player.tickCount;
            Stacks stacks = ACTIVE_STACKS.get(player);
            Stacks updated = stacks == null ? new Stacks(0, tick, tick, tick) : stacks;
            boolean inRange = isInActiveBeaconRange(player);
            if (inRange && updated.count() < MAX_STACKS && tick - updated.lastGainTick() >= STACK_GAIN_INTERVAL_TICKS) {
                ACTIVE_STACKS.put(player, new Stacks(updated.count() + 1, tick, tick, tick));
                CollectibleAttributes.reconcile(player);
            } else if (inRange) {
                ACTIVE_STACKS.put(player, new Stacks(updated.count(), updated.lastGainTick(), tick, tick));
            } else if (updated.count() > 0 && tick - updated.lastInRangeTick() >= DECAY_GRACE_TICKS
                    && tick - updated.lastDecayTick() >= DECAY_INTERVAL_TICKS) {
                int remaining = updated.count() - 1;
                if (remaining == 0) {
                    ACTIVE_STACKS.remove(player);
                } else {
                    ACTIVE_STACKS.put(player, new Stacks(remaining, updated.lastGainTick(), updated.lastInRangeTick(), tick));
                }
                CollectibleAttributes.reconcile(player);
            }
        }
    }
}