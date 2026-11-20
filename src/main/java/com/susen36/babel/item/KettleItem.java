package com.susen36.babel.item;

import com.susen36.babel.block.KingPedestalBlockEntity;
import com.susen36.babel.collectible.CollectibleActivation;
import com.susen36.babel.collectible.CollectibleItem;
import com.susen36.babel.collectible.CollectibleTiers;
import com.susen36.babel.init.BabelAttributes;
import com.susen36.babel.init.BabelBlocks;
import com.susen36.babel.init.BabelItems;
import com.susen36.babel.util.BlockUtils;
import com.susen36.babel.util.LifePointUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

//Water Boiler
public class KettleItem extends CollectibleItem.CustomCollectibleItem {
    public KettleItem() {
        super(new Item.Properties().stacksTo(1), 25, false, CollectibleTiers.NORMAL, new CollectibleItem.Levels(0, 1, 0),
                CollectibleActivation.builder()
                        .sound(SoundEvents.PLAYER_LEVELUP, 2F, 1F)
                        .particle(ParticleTypes.WAX_OFF, 72)
                        .showOverlay(true)
                        .build());
        addAttributeModifier(BabelAttributes.MAX_LIFE_POINT.get(), 1, AttributeModifier.Operation.ADDITION);
    }

    @Override
    public void onEffectStarted(ItemStack stack, Level level, Player player) {
        LifePointUtils.addLifePoint(player, 1);
        player.giveExperienceLevels(1);
    }

    @Override
    protected InteractionResultHolder<ItemStack> onSneakUse(Level level, Player player, InteractionHand hand) {
        return BlockUtils.setBlockByUse(level,player,hand,BabelBlocks.BLOCK_KETTLE);
    }
}