package com.susen36.babel.item;

import com.susen36.babel.collectible.CollectibleActivation;
import com.susen36.babel.collectible.CollectibleItem;
import com.susen36.babel.collectible.CollectibleTiers;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

public class OriginiumIrisFlowerItem extends CollectibleItem.CustomCollectibleItem {
    public OriginiumIrisFlowerItem() {
        super(new Item.Properties().stacksTo(16).rarity(Rarity.EPIC), 20, false, CollectibleTiers.NORMAL, new CollectibleItem.Levels(0, 1, 0),
                CollectibleActivation.builder()
                        .sound(SoundEvents.PLAYER_LEVELUP, 2F, 1F)
                        .particle(ParticleTypes.HAPPY_VILLAGER, 72)
                        .showOverlay(true)
                        .build());
    }

    @Override
    public UseAnim getUseAnimation(ItemStack itemstack) {
        return UseAnim.BOW;
    }

    @Override
    public void onEffectStarted(ItemStack stack, Level level, Player player, int layer) {
        if (level instanceof ServerLevel serverLevel) {
            double x = player.getX();
            double y = player.getY();
            double z = player.getZ();
            RandomSource random = level.random;
            for (int index0 = 0; index0 < 16; index0++) {
                serverLevel.addFreshEntity(new ExperienceOrb(serverLevel, x + Mth.nextDouble(random, -1, 1), y + Mth.nextDouble(random, 0.6, 0.75), z + Mth.nextDouble(random, -1, 1), 4));
            }
        }
    }
}