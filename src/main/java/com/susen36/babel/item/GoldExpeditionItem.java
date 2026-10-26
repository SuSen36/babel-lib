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
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.ItemHandlerHelper;

public class GoldExpeditionItem extends CollectibleItem.CustomCollectibleItem {
    public GoldExpeditionItem() {
        super(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC), 25, false, CollectibleTiers.NORMAL, new CollectibleItem.Levels(0, 1, 0),
                CollectibleActivation.builder()
                        .sound(SoundEvents.PLAYER_LEVELUP, 2F, 1F)
                        .particle(ParticleTypes.HAPPY_VILLAGER, 72)
                        .showOverlay(true)
                        .build());
    }

    @Override
    public void onEffectStarted(ItemStack stack, Level level, Player player, int layer) {
        if (level instanceof ServerLevel serverLevel) {
            RandomSource random = player.getRandom();
            double centerY = player.getY() + player.getBbHeight() * 0.5;
            for (int i = 0; i < 8; i++) {
                serverLevel.addFreshEntity(new ExperienceOrb(
                        serverLevel,
                        player.getX() + Mth.nextDouble(random, -0.8, 0.8),
                        centerY,
                        player.getZ() + Mth.nextDouble(random, -0.8, 0.8),
                        4));
            }
            ItemHandlerHelper.giveItemToPlayer(player, new ItemStack(Items.GOLD_INGOT, 8));
        }
    }
}