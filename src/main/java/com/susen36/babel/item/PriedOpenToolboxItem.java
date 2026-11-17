package com.susen36.babel.item;

import com.susen36.babel.collectible.CollectibleActivation;
import com.susen36.babel.collectible.CollectibleItem;
import com.susen36.babel.collectible.CollectibleTiers;
import com.susen36.babel.difficulty.NDifficulty;
import com.susen36.babel.init.BabelItems;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.ItemHandlerHelper;

public class PriedOpenToolboxItem extends CollectibleItem.CustomCollectibleItem {

    private static final int EXP_LEVELS = 4;
    private static final int[] INGOT_QUANTITY = {0, 3, 4, 5};

    public PriedOpenToolboxItem() {
        super(new Item.Properties().stacksTo(1), 25, false, CollectibleTiers.NORMAL, new CollectibleItem.Levels(0, 1, 0),
                CollectibleActivation.builder()
                        .sound(SoundEvents.CHEST_OPEN, 2F, 1F)
                        .particle(ParticleTypes.ENCHANT, 72)
                        .showOverlay(true)
                        .build());
    }

    @Override
    public void onEffectStarted(ItemStack stack, Level level, Player player) {
        player.giveExperienceLevels(EXP_LEVELS);

        if (!level.isClientSide()) {
            int ingotQuantityND = NDifficulty.setValue(level, INGOT_QUANTITY[0], INGOT_QUANTITY);
            if (ingotQuantityND > 0) {
                ItemHandlerHelper.giveItemToPlayer(player, new ItemStack(BabelItems.ORIGINIUM_INGOT.get(), ingotQuantityND));
            }
        }
    }
}