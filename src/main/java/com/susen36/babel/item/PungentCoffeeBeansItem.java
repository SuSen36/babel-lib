package com.susen36.babel.item;

import com.susen36.babel.collectible.CollectibleActivation;
import com.susen36.babel.collectible.CollectibleItem;
import com.susen36.babel.collectible.CollectibleTiers;
import com.susen36.babel.init.BabelAttributes;
import com.susen36.babel.util.LifePointUtils;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class PungentCoffeeBeansItem extends CollectibleItem.CustomCollectibleItem {

    private static final int EXPERIENCE_LEVELS = 4;

    public PungentCoffeeBeansItem() {
        super(new Item.Properties().stacksTo(1), 25, false, CollectibleTiers.RARE,
                new CollectibleItem.Levels(0, 1, 0),
                CollectibleActivation.builder()
                        .showOverlay(true)
                        .build());
    }

    @Override
    public void onEffectStarted(@NotNull ItemStack stack, @NotNull Level level, @NotNull Player player) {
        LifePointUtils.addShieldPoint(player,  1);
        if (!player.level().isClientSide()) {
            player.giveExperienceLevels(EXPERIENCE_LEVELS);
        }
    }
}