package com.susen36.babel.item;

import com.susen36.babel.BabelConfig;
import com.susen36.babel.collectible.CollectibleActivation;
import com.susen36.babel.collectible.CollectibleItem;
import com.susen36.babel.collectible.CollectibleTiers;
import com.susen36.babel.init.BabelDamageTypes;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.Tags;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

public class HandStrangleItem extends CollectibleItem.CustomCollectibleItem {

    public HandStrangleItem() {
        super(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC), 25, false, CollectibleTiers.ADVANCED,
                new CollectibleItem.Levels(0, 1, 0),
                CollectibleActivation.builder()
                        .sound(SoundEvents.PLAYER_LEVELUP, 2F, 1F)
                        .particle(ParticleTypes.CLOUD, 72)
                        .showOverlay(true)
                        .build());
    }

    @Override
    public void onAttackHit(LivingHurtEvent event, Player player, LivingEntity target, int layer) {
        if (!event.isCanceled() && event.getSource().getDirectEntity() != player) {
            Level level = player.level();
            ItemStack mainHandItem = player.getMainHandItem();
            String registryName = BuiltInRegistries.ITEM.getKey(mainHandItem.getItem()).toString();
            if (isStrangleWeapon(mainHandItem, registryName)
                    && target.isAlive() && target.getHealth() < target.getMaxHealth() * 0.25F) {
                target.hurt(BabelDamageTypes.source(level, BabelDamageTypes.HAND_OF_CHOKER, player), target.getMaxHealth() * 99);
                if (level instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.GLOW_SQUID_INK, target.getX(), target.getY(), target.getZ(), 128, 1, 1, 1, 0.33);
                }
                level.playSound(null, target.blockPosition(), SoundEvents.WITHER_HURT, SoundSource.NEUTRAL, 2, 1);
            }
        }
    }

    private static boolean isStrangleWeapon(ItemStack stack, String registryName) {
        return stack.is(Tags.Items.TOOLS_CROSSBOWS) || stack.getItem() instanceof CrossbowItem
                || matchesConfigured(BabelConfig.handStrangleRecognizable, registryName);
    }
}
