package com.susen36.babel.item;

import com.susen36.babel.collectible.CollectibleActivation;
import com.susen36.babel.collectible.CollectibleItem;
import com.susen36.babel.collectible.CollectibleTiers;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.event.entity.living.LivingDamageEvent;

public class StandardRiotGearItem extends CollectibleItem.CustomCollectibleItem {
    /** 附加的法术伤害会再次触发 onDealDamage，用重入标记让它在第一层停住，不影响任何伤害类型的加成。 */
    private static final ThreadLocal<Boolean> APPLYING_EXTRA = ThreadLocal.withInitial(() -> Boolean.FALSE);

    public StandardRiotGearItem() {
        super(new Item.Properties().stacksTo(1).rarity(Rarity.COMMON), 25, false, CollectibleTiers.NORMAL, new CollectibleItem.Levels(0, 1, 0),
                CollectibleActivation.builder()
                        .sound(SoundEvents.PLAYER_LEVELUP, 2F, 1F)
                        .particle(ParticleTypes.CLOUD, 72)
                        .showOverlay(true)
                        .build());
    }

    @Override
    public void onDealDamage(LivingDamageEvent event, Player player, LivingEntity target, int layer) {
        if (target instanceof Monster && !APPLYING_EXTRA.get()) {
            APPLYING_EXTRA.set(Boolean.TRUE);
            try {
                target.hurt(target.level().damageSources().indirectMagic(player, player), event.getAmount() * 0.2F);
            } finally {
                APPLYING_EXTRA.set(Boolean.FALSE);
            }
        }
    }
}