package com.susen36.babel.item;

import com.susen36.babel.collectible.CollectibleActivation;
import com.susen36.babel.collectible.CollectibleItem;
import com.susen36.babel.collectible.CollectibleTiers;
import com.susen36.babel.init.BabelDamageTypes;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingDamageEvent;

public class HandFertilityItem extends CollectibleItem.CustomCollectibleItem {

    private static final TagKey<Item> MINECRAFT_HOES = ItemTags.create(new ResourceLocation("minecraft:hoes"));

    public HandFertilityItem() {
        super(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC), 25, false, CollectibleTiers.ADVANCED,
                new CollectibleItem.Levels(0, 1, 0),
                CollectibleActivation.builder()
                        .sound(SoundEvents.PLAYER_LEVELUP, 2F, 1F)
                        .particle(ParticleTypes.CLOUD, 72)
                        .showOverlay(true)
                        .build());
    }

    @Override
    public void onDealMeleeDamage(LivingDamageEvent event, Player player, LivingEntity target, int layer) {
        Level level = target.level();
        ItemStack mainHandItem = player.getMainHandItem();
        if (mainHandItem.getItem() instanceof HoeItem || mainHandItem.is(MINECRAFT_HOES)) {
            target.hurt(BabelDamageTypes.source(level, BabelDamageTypes.HAND_OF_CHOKER, event.getSource().getEntity()),
                    (float) (target.getHealth() * 0.075));
            if (level instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.SQUID_INK, target.getX(), target.getY(), target.getZ(), 8, 0.75, 0.9, 0.75, 0.1);
            }
        }
    }
}
