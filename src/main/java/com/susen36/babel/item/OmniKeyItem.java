package com.susen36.babel.item;

import com.susen36.babel.collectible.CollectibleActivation;
import com.susen36.babel.collectible.CollectibleItem;
import com.susen36.babel.collectible.CollectibleTiers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public class OmniKeyItem extends CollectibleItem.CustomCollectibleItem {
    public OmniKeyItem() {
        super(new Item.Properties().durability(64).rarity(Rarity.UNCOMMON), 25, false, CollectibleTiers.NORMAL, new CollectibleItem.Levels(0, 1, 0),
                CollectibleActivation.builder()
                        .sound(SoundEvents.PLAYER_LEVELUP, 2F, 1F)
                        .particle(ParticleTypes.HAPPY_VILLAGER, 72)
                        .showOverlay(true)
                        .build());
    }

    @Override
    public void onEffectStarted(ItemStack stack, Level level, Player player, int layer) {
        player.giveExperienceLevels(3);
    }

    @Override
    protected InteractionResultHolder<ItemStack> onSneakUse(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        HitResult hit = player.pick(5.0D, 1.0F, false);
        if (hit instanceof BlockHitResult blockHit) {
            BlockPos pos = blockHit.getBlockPos();
            BlockState state = level.getBlockState(pos);
            if (state.getBlock() == Blocks.IRON_DOOR || state.getBlock() == Blocks.IRON_TRAPDOOR) {
                if (!state.getValue(BlockStateProperties.OPEN)) {
                    level.setBlock(pos, state.setValue(BlockStateProperties.OPEN, true), 3);
                    level.scheduleTick(pos, state.getBlock(), 22);
                    level.playSound(null, pos, SoundEvents.CHAIN_STEP, SoundSource.NEUTRAL, 1, 1);
                    if (level instanceof ServerLevel serverLevel && !player.getAbilities().instabuild) {
                        if (itemstack.hurt(1, serverLevel.getRandom(), null)) {
                            itemstack.shrink(1);
                        }
                    }
                }
                return InteractionResultHolder.sidedSuccess(itemstack, level.isClientSide());
            }
        }
        return InteractionResultHolder.pass(itemstack);
    }
}