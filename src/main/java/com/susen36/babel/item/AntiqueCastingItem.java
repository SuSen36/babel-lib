package com.susen36.babel.item;

import com.susen36.babel.collectible.CollectibleActivation;
import com.susen36.babel.collectible.CollectibleItem;
import com.susen36.babel.collectible.CollectibleTiers;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.random.WeightedRandom;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.Level;

import java.util.List;

public class AntiqueCastingItem extends CollectibleItem.CustomCollectibleItem {

    private static final int ENCHANT_COUNT = 3;
    private static final int ENCHANT_LEVEL = 30;

    public AntiqueCastingItem() {
        super(new Item.Properties().stacksTo(1), 25, false, CollectibleTiers.ADVANCED,
                new CollectibleItem.Levels(0, 1, 0),
                CollectibleActivation.builder()
                        .sound(SoundEvents.ENCHANTMENT_TABLE_USE, 2F, 1F)
                        .particle(ParticleTypes.ENCHANT, 72)
                        .showOverlay(true)
                        .build());
    }

    @Override
    public void onEffectStarted(ItemStack stack, Level level, Player player, int layer) {
        if (level instanceof ServerLevel serverLevel) {
            int enchanted = 0;
            for (ItemStack candidate : player.getInventory().items) {
                if (enchanted >= ENCHANT_COUNT) {
                    break;
                }
                if (!candidate.isEmpty() && candidate.isEnchantable() && !candidate.isEnchanted()
                        && enchantWithoutCurse(serverLevel, candidate)) {
                    enchanted++;
                }
            }
            player.getInventory().setChanged();
        }
    }

    // 与附魔台同等的加权抽取，但过滤诅咒附魔；无可选附魔的物品不计入次数
    private static boolean enchantWithoutCurse(ServerLevel level, ItemStack stack) {
        List<EnchantmentInstance> options = EnchantmentHelper.getAvailableEnchantmentResults(ENCHANT_LEVEL, stack, true);
        options.removeIf(option -> option.enchantment.isCurse());
        if (options.isEmpty()) {
            return false;
        }
        WeightedRandom.getRandomItem(level.getRandom(), options).ifPresent(option ->
                stack.enchant(option.enchantment, option.level));
        return true;
    }
}
