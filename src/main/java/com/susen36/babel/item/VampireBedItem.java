package com.susen36.babel.item;

import com.susen36.babel.collectible.CollectibleActivation;
import com.susen36.babel.collectible.CollectibleItem;
import com.susen36.babel.collectible.CollectibleTiers;
import com.susen36.babel.init.BabelAttributes;
import com.susen36.babel.init.BabelBlocks;
import com.susen36.babel.util.BlockUtils;
import com.susen36.babel.util.LifePointUtils;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class VampireBedItem extends CollectibleItem.CustomCollectibleItem {
    public VampireBedItem() {
        super(new Item.Properties().stacksTo(1), 25, false, CollectibleTiers.RARE, new CollectibleItem.Levels(0, 1, 0),
                CollectibleActivation.builder()
                        .sound(SoundEvents.ALLAY_AMBIENT_WITHOUT_ITEM, 3.5F, 1F)
                        .particle(ParticleTypes.ASH, 72)
                        .showOverlay(true)
                        .build());
        addAttributeModifier(BabelAttributes.MAX_LIFE_POINT.get(), 4, AttributeModifier.Operation.ADDITION);
    }

    @Override
    public void onEffectStarted(ItemStack stack, Level level, Player player) {
        LifePointUtils.setLifePoint(player, LifePointUtils.getLifePoint(player) + 4);
    }

    @Override
    protected InteractionResultHolder<ItemStack> onSneakUse(Level level, Player player, InteractionHand hand) {
        return BlockUtils.setBlockByUse(level,player,hand,BabelBlocks.VAMPIRES_BED);
    }
}