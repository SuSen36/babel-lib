package com.susen36.babel.item;

import com.susen36.babel.collectible.CollectibleActivation;
import com.susen36.babel.collectible.CollectibleItem;
import com.susen36.babel.collectible.CollectibleTiers;
import com.susen36.babel.collectible.Collectibles;
import com.susen36.babel.init.BabelCollectible;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingDamageEvent;

public class HandOfSwordItem extends CollectibleItem.CustomCollectibleItem {

    private static final TagKey<Item> MINECRAFT_SWORDS = ItemTags.create(new ResourceLocation("minecraft:swords"));

    public HandOfSwordItem() {
        super(new Item.Properties().stacksTo(1), 25, false, CollectibleTiers.ADVANCED,
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
        if ((mainHandItem.getItem() instanceof SwordItem || mainHandItem.is(MINECRAFT_SWORDS))
                && !level.isClientSide()) {
            player.heal((float) (player.getMaxHealth() * 0.075));
        }
    }

    public static boolean isArmorPenetrating(DamageSource source) {
        boolean penetrating = false;
        if (source.getEntity() instanceof Player player && source.is(DamageTypes.PLAYER_ATTACK)
                && !player.level().isClientSide() && !Collectibles.isEffectSuppressed(player)) {
            ItemStack mainHandItem = player.getMainHandItem();
            penetrating = (mainHandItem.getItem() instanceof SwordItem || mainHandItem.is(MINECRAFT_SWORDS))
                    && Collectibles.isUsed(player, BabelCollectible.HAND_OF_SWORD);
        }
        return penetrating;
    }
}