package com.susen36.babel.item;

import com.susen36.babel.collectible.CollectibleActivation;
import com.susen36.babel.collectible.CollectibleItem;
import com.susen36.babel.collectible.CollectibleTiers;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.event.entity.living.LivingDeathEvent;

public class HandOfSnatcherItem extends CollectibleItem.CustomCollectibleItem {

    private static final double EMERALD_CHANCE = 0.1;

    public HandOfSnatcherItem() {
        super(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC), 25, false, CollectibleTiers.ADVANCED,
                new CollectibleItem.Levels(0, 1, 0),
                CollectibleActivation.builder()
                        .sound(SoundEvents.PLAYER_LEVELUP, 2F, 1F)
                        .particle(ParticleTypes.CLOUD, 72)
                        .showOverlay(true)
                        .build());
        addConditionalAttributeModifier(Attributes.MOVEMENT_SPEED, 0.1, AttributeModifier.Operation.MULTIPLY_TOTAL,
                (player, self) -> player.hasEffect(MobEffects.HERO_OF_THE_VILLAGE) ? 1 : 0);
    }

    @Override
    public void onPlayerKill(LivingDeathEvent event, Player player, Entity victim, int layer) {
        if (Math.random() < EMERALD_CHANCE && victim.level() instanceof ServerLevel level) {
            ItemEntity emerald = new ItemEntity(level, victim.getX(), victim.getY(), victim.getZ(), new ItemStack(Items.EMERALD));
            emerald.setPickUpDelay(10);
            level.addFreshEntity(emerald);
        }
    }
}