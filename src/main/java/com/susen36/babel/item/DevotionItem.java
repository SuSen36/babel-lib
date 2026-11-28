package com.susen36.babel.item;

import com.susen36.babel.collectible.CollectibleActivation;
import com.susen36.babel.collectible.CollectibleItem;
import com.susen36.babel.collectible.CollectibleTiers;
import com.susen36.babel.util.DamageUtils;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DevotionItem extends CollectibleItem.CustomCollectibleItem {

    private static final float MAGIC_DAMAGE = 3.0F;
    private final RangeTargetSelector<LivingEntity> enemiesInRange;

    public DevotionItem() {
        super(new Item.Properties().stacksTo(1), 25, false, CollectibleTiers.RARE,
                new CollectibleItem.Levels(0, 1, 0),
                CollectibleActivation.builder()
                        .sound(SoundEvents.PLAYER_LEVELUP, 2F, 1F)
                        .particle(ParticleTypes.WAX_OFF, 72)
                        .showOverlay(true)
                        .build());
        this.enemiesInRange = addRangeEffect(LivingEntity.class, 12.0,
                (entity, owner) -> entity.isAlive() && ENEMY_OF_PLAYER.test(entity, owner));
    }

    @Override
    public void onAttackHit(LivingHurtEvent event, Player player, LivingEntity target, int layer) {
        if (!player.level().isClientSide() && event.getSource().getEntity() == player) {
            hurtRandomEnemy(player, target);
        }
    }

    @Override
    public void onOwnerHeal(LivingHealEvent event, Player player, int layer) {
        if (!player.level().isClientSide()) {
            hurtRandomEnemy(player, null);
        }
    }

    private void hurtRandomEnemy(Player player, @Nullable LivingEntity attackTarget) {
        List<LivingEntity> enemies = this.enemiesInRange.findTargets(player);
        if (!enemies.isEmpty()) {
            LivingEntity enemy = enemies.get(player.getRandom().nextInt(enemies.size()));
            if (enemy == attackTarget) {
                DamageUtils.hurtIndependently(enemy, player.damageSources().magic(), MAGIC_DAMAGE);
            } else {
                enemy.hurt(player.damageSources().magic(), MAGIC_DAMAGE);
            }
        }
    }
}