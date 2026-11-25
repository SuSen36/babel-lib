package com.susen36.babel.util;

import com.susen36.babel.block.KingPedestalBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.registries.RegistryObject;

public class BlockUtils {

    private BlockUtils() {}

    /**
     * 方块放置流程
     */
    public static InteractionResultHolder<ItemStack> setBlockByUse(Level level, Player player, InteractionHand hand, RegistryObject<? extends Block> blocks) {
        ItemStack itemstack = player.getItemInHand(hand);
        HitResult hit = player.pick(player.getBlockReach(), 1.0F, false);
        //点击非方块时返回，包含空点击（目标为AIR）
        if (!(hit instanceof BlockHitResult blockHit) || hit.getType() != HitResult.Type.BLOCK) {
            return InteractionResultHolder.pass(itemstack);
        }

        UseOnContext context = new UseOnContext(player, hand, blockHit);
        BlockPlaceContext blockContext = new BlockPlaceContext(context);

        if (!blockContext.canPlace()) {
            return InteractionResultHolder.pass(itemstack);
        }

        BlockState blockState = blocks.get().getStateForPlacement(blockContext);
        if (blockState == null) {
            return InteractionResultHolder.pass(itemstack);
        }

        BlockPos pos = blockContext.getClickedPos();

        //原版校验修正
        if (!blockState.canSurvive(level, pos)) {
            return InteractionResultHolder.pass(itemstack);
        }
        if (!level.mayInteract(player, pos)) {
            return InteractionResultHolder.pass(itemstack);
        }

        //放置检查
        if (!level.setBlock(pos, blockState, 3)) {
            return InteractionResultHolder.pass(itemstack);
        }
        //与原版 BlockItem 一致，触发床等双格方块的另一半放置。
        blockState.getBlock().setPlacedBy(level, pos, blockState, player, itemstack);

        //检查放置
        if (level.getBlockEntity(pos) instanceof KingPedestalBlockEntity pedestal) {
            pedestal.setItem(itemstack.copy());
        }
        //消耗物品
        if (!player.getAbilities().instabuild) {
            itemstack.shrink(1);
        }

        return InteractionResultHolder.sidedSuccess(itemstack, level.isClientSide());
    }
}