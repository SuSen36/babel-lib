package com.susen36.babel.item;

import com.susen36.babel.collectible.CollectibleActivation;
import com.susen36.babel.collectible.CollectibleItem;
import com.susen36.babel.collectible.CollectibleTiers;
import com.susen36.babel.collectible.Collectibles;
import com.susen36.babel.init.BabelCollectible;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraftforge.common.Tags;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

public class HandOfFlowingWaterItem extends CollectibleItem.CustomCollectibleItem {

    private static final int BUFF_DURATION_TICKS = 160;
    private static final int MAX_STACKS = 4;

    private static final Map<Player, Buff> ACTIVE_BUFFS = Collections.synchronizedMap(new WeakHashMap<>());

    private record Buff(int stacks, int expireTick) {
    }

    public HandOfFlowingWaterItem() {
        super(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC), 25, false, CollectibleTiers.ADVANCED,
                new CollectibleItem.Levels(0, 1, 0),
                CollectibleActivation.builder()
                        .sound(SoundEvents.PLAYER_LEVELUP, 2F, 1F)
                        .particle(ParticleTypes.CLOUD, 72)
                        .showOverlay(true)
                        .build());
    }

    public static boolean isFlowing(Player player) {
        return Collectibles.getData(player).isUsed(BabelCollectible.HAND_OF_FLOWING_WATER.get());
    }

    public static boolean isBowOrCrossbow(ItemStack stack) {
        return stack.is(Tags.Items.TOOLS_BOWS) || stack.getItem() instanceof BowItem
                || stack.is(Tags.Items.TOOLS_CROSSBOWS) || stack.getItem() instanceof CrossbowItem;
    }

    public static int activeStacks(Player player) {
        Buff buff = ACTIVE_BUFFS.get(player);
        return buff != null && player.tickCount < buff.expireTick() ? buff.stacks() : 0;
    }

    @Override
    public void onUseItemEnd(LivingEntityUseItemEvent event, Player player, int layer) {
        if (event instanceof LivingEntityUseItemEvent.Stop stop) {
            ItemStack usedItem = stop.getItem();
            boolean fullyUsed = usedItem.getItem() instanceof CrossbowItem ? CrossbowItem.isCharged(usedItem)
                    : usedItem.getItem() instanceof BowItem && BowItem.getPowerForTime(player.getTicksUsingItem()) >= 1.0F;
            if (fullyUsed) {
                int stacks = Mth.clamp(activeStacks(player) + 1, 0, MAX_STACKS);
                ACTIVE_BUFFS.put(player, new Buff(stacks, player.tickCount + BUFF_DURATION_TICKS));
            }
        }
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int tickCount, int layer) {
        return tickCount % 10 == 0;
    }

    @Override
    public void applyEffectTick(Player player, int layer) {
        Buff buff = ACTIVE_BUFFS.get(player);
        if (buff != null && player.tickCount > buff.expireTick()) {
            ACTIVE_BUFFS.remove(player);
        }
    }
}