package com.susen36.babel.item;

import com.susen36.babel.collectible.CollectibleActivation;
import com.susen36.babel.collectible.CollectibleItem;
import com.susen36.babel.collectible.CollectibleTiers;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item;

public class StandardRiotGearItem extends CollectibleItem.CustomCollectibleItem {
    /** 附加的法术伤害会再次触发 onDealDamage，用重入标记让它在第一层停住，不影响任何伤害类型的加成。 */
    private static final ThreadLocal<LivingEntity> EXTRA_DAMAGE_TARGET = new ThreadLocal<>();

    public StandardRiotGearItem() {
        super(new Item.Properties().stacksTo(1), 25, false, CollectibleTiers.NORMAL, new CollectibleItem.Levels(0, 1, 0),
                CollectibleActivation.builder()
                        .sound(SoundEvents.PLAYER_LEVELUP, 2F, 1F)
                        .particle(ParticleTypes.CLOUD, 72)
                        .showOverlay(true)
                        .build());
        addDamageEffect(Mob.class, (event, player, target, layer) -> {
            if (!event.isCanceled() && target.isAlive() && EXTRA_DAMAGE_TARGET.get() != target) {
                EXTRA_DAMAGE_TARGET.set(target);
                int savedInvulnerable = target.invulnerableTime;
                target.invulnerableTime = 0;
                try {
                    target.hurt(target.level().damageSources().indirectMagic(player, player), event.getAmount() * 0.2F);
                } finally {
                    EXTRA_DAMAGE_TARGET.remove();
                    target.invulnerableTime = Math.max(savedInvulnerable, target.invulnerableTime);
                }
            }
        }, ENEMY_OF_PLAYER);
    }
}