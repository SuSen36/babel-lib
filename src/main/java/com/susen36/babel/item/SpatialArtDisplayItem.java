package com.susen36.babel.item;

import com.susen36.babel.collectible.CollectibleActivation;
import com.susen36.babel.collectible.CollectibleItem;
import com.susen36.babel.collectible.CollectibleTiers;
import com.susen36.babel.util.LifePointUtils;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingDeathEvent;

public class SpatialArtDisplayItem extends CollectibleItem.CustomCollectibleItem {
    public SpatialArtDisplayItem() {
        super(new Item.Properties().stacksTo(1), 25, false, CollectibleTiers.RARE, new CollectibleItem.Levels(0, 1, 0),
                CollectibleActivation.builder()
                        .sound(SoundEvents.AMETHYST_BLOCK_CHIME, 2F, 1F)
                        .particle(ParticleTypes.ENCHANT, 72)
                        .showOverlay(true)
                        .build());
    }

    @Override
    public void onEffectStarted(ItemStack stack, Level level, Player player) {
        LifePointUtils.healLifePoint(player, 7);
    }

    @Override
    public void onPlayerKill(LivingDeathEvent event, Player player, Entity victim, int layer) {
        if (victim instanceof LivingEntity living && ENEMY_OF_PLAYER.test(living, player) && Math.random() < 0.02) {
            LifePointUtils.healLifePoint(player, 1);
        }
    }
}