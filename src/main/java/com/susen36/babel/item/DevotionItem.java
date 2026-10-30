package com.susen36.babel.item;

import com.susen36.babel.collectible.CollectibleActivation;
import com.susen36.babel.collectible.CollectibleItem;
import com.susen36.babel.collectible.CollectibleTiers;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

import java.util.List;

public class DevotionItem extends CollectibleItem.CustomCollectibleItem {

    private static final double EFFECT_RANGE = 12.0;
    private static final float MAGIC_DAMAGE = 5.0F;

    public DevotionItem() {
        super(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON), 25, false, CollectibleTiers.NORMAL,
                new CollectibleItem.Levels(0, 1, 0),
                CollectibleActivation.builder()
                        .sound(SoundEvents.PLAYER_LEVELUP, 2F, 1F)
                        .particle(ParticleTypes.HAPPY_VILLAGER, 72)
                        .showOverlay(true)
                        .build());
    }

    @Override
    public void onAttackHit(LivingHurtEvent event, Player player, LivingEntity target, int layer) {
        if (!player.level().isClientSide() && event.getSource().getEntity() == player) {
            hurtRandomEnemy(player);
        }
    }

    @Override
    public void onOwnerHeal(LivingHealEvent event, Player player, int layer) {
        if (!player.level().isClientSide()) {
            hurtRandomEnemy(player);
        }
    }

    private static void hurtRandomEnemy(Player player) {
        List<LivingEntity> enemies = player.level().getEntitiesOfClass(LivingEntity.class,
                player.getBoundingBox().inflate(EFFECT_RANGE), entity -> entity instanceof Enemy && entity.isAlive());
        if (!enemies.isEmpty()) {
            LivingEntity target = enemies.get(player.getRandom().nextInt(enemies.size()));
            target.hurt(player.damageSources().magic(), MAGIC_DAMAGE);
        }
    }
}