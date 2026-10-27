package com.susen36.babel.item;

import com.susen36.babel.collectible.CollectibleActivation;
import com.susen36.babel.collectible.CollectibleItem;
import com.susen36.babel.collectible.CollectibleTiers;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.event.entity.player.CriticalHitEvent;

import java.util.Map;
import java.util.WeakHashMap;

public class BlackTulipItem extends CollectibleItem.CustomCollectibleItem {

    /** 充能时长 60 秒（1200 tick），加成随经过 tick 线性增长。 */
    private static final int CHARGE_TICKS = 1200;
    private static final double MAX_CRIT_BONUS = 0.6D;

    /** 以 Player 实例弱引用计时，不写入 NBT：重登或重启后充能清零。 */
    private static final Map<Player, Integer> CHARGE_STARTS = new WeakHashMap<>();

    public BlackTulipItem() {
        super(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC), 25, false, CollectibleTiers.ADVANCED,
                new CollectibleItem.Levels(0, 1, 0),
                CollectibleActivation.builder()
                        .sound(SoundEvents.PLAYER_LEVELUP, 2F, 1F)
                        .particle(ParticleTypes.WITCH, 72)
                        .showOverlay(true)
                        .build());
    }

    private static double critBonus(Player player) {
        Integer start = CHARGE_STARTS.get(player);
        if (start == null) {
            return 0.0D;
        }
        double progress = Mth.clamp((player.tickCount - start) / (double) CHARGE_TICKS, 0.0D, 1.0D);
        return progress * MAX_CRIT_BONUS;
    }

    @Override
    public void onCriticalHit(CriticalHitEvent event, Player player, int layer) {
        if (event.isVanillaCritical() && !player.level().isClientSide()) {
            event.setDamageModifier(event.getDamageModifier() * (float) (1.0D + critBonus(player)));
            CHARGE_STARTS.put(player, player.tickCount);
        }
    }
}