package com.susen36.babel.item;

import com.susen36.babel.collectible.CollectibleActivation;
import com.susen36.babel.collectible.CollectibleItem;
import com.susen36.babel.collectible.CollectibleTiers;
import com.susen36.babel.init.BabelMobEffects;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingDamageEvent;

public class HandSwordItem extends CollectibleItem.CustomCollectibleItem {

    private static final TagKey<Item> MINECRAFT_SWORDS = ItemTags.create(new ResourceLocation("minecraft:swords"));

    public HandSwordItem() {
        super(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC), 25, false, CollectibleTiers.NORMAL,
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
            if (!target.hasEffect(BabelMobEffects.LESS_ARMOR.get())) {
                target.addEffect(new MobEffectInstance(BabelMobEffects.LESS_ARMOR.get(), 120, 1));
            }
            player.addEffect(new MobEffectInstance(BabelMobEffects.ADD_REACH.get(), 120, 3, false, false));
            player.heal((float) (player.getMaxHealth() * 0.1));
        }
    }
}
