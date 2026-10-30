package com.susen36.babel.block;

import com.susen36.babel.init.BabelSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.List;

public class GoldenChaliceBlock extends Block implements SimpleWaterloggedBlock, EntityBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final IntegerProperty BLOCKSTATE = IntegerProperty.create("blockstate", 0, 2);

    public GoldenChaliceBlock() {
        super(BlockBehaviour.Properties.of().sound(SoundType.METAL).strength(8f, 1200f).noOcclusion().isRedstoneConductor((bs, br, bp) -> false));
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(WATERLOGGED, false)
                .setValue(BLOCKSTATE, 0));
    }

    @Override
    public void appendHoverText(ItemStack itemstack, BlockGetter context, List<Component> list, TooltipFlag flag) {
        list.add(Component.translatable("block.babel.golden_chalice.description_0"));
        list.add(Component.translatable("block.babel.golden_chalice.description_1"));
        list.add(Component.translatable("block.babel.golden_chalice.description_2"));
        list.add(Component.translatable("block.babel.golden_chalice.description_3"));
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter reader, BlockPos pos) {
        return state.getFluidState().isEmpty();
    }

    @Override
    public int getLightBlock(BlockState state, BlockGetter worldIn, BlockPos pos) {
        return 0;
    }

    @Override
    public VoxelShape getVisualShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return box(4.5, 0, 4.5, 11.5, 10, 11.5);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, WATERLOGGED, BLOCKSTATE);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new GoldenChaliceBlockEntity(pos, state);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        boolean flag = context.getLevel().getFluidState(context.getClickedPos()).getType() == Fluids.WATER;
        return super.getStateForPlacement(context)
                .setValue(FACING, context.getHorizontalDirection().getOpposite())
                .setValue(WATERLOGGED, flag);
    }

    public BlockState rotate(BlockState state, Rotation rot) {
        return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
    }

    public BlockState mirror(BlockState state, Mirror mirrorIn) {
        return state.rotate(mirrorIn.getRotation(state.getValue(FACING)));
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor world, BlockPos currentPos, BlockPos facingPos) {
        if (state.getValue(WATERLOGGED)) {
            world.scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(world));
        }
        return super.updateShape(state, facing, facingState, world, currentPos, facingPos);
    }

    @Override
    public InteractionResult use(BlockState blockstate, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack itemstack = player.getItemInHand(hand);
        if (world.getBlockEntity(pos) instanceof GoldenChaliceBlockEntity chalice) {
            if (player.isShiftKeyDown()) {
                if (!world.isClientSide()) {
                    player.displayClientMessage(Component.literal(Component.translatable("block.golden_chalice.inquiry").getString() + chalice.getAmount() + " /64"), true);
                }
                return InteractionResult.sidedSuccess(world.isClientSide());
            }

            if (itemstack.getItem() == Items.GOLD_INGOT) {
                int maxAdd = Math.min(itemstack.getCount(), 64 - chalice.getAmount());
                if (maxAdd > 0) {
                    chalice.setAmount(chalice.getAmount() + maxAdd);
                    int visual = chalice.getAmount() == 0 ? 0 : chalice.getAmount() < 32 ? 1 : 2;
                    world.setBlock(pos, blockstate.setValue(BLOCKSTATE, visual), 3);
                    itemstack.shrink(maxAdd);
                    world.playSound(null, pos, BabelSounds.MONEY_IN.get(), SoundSource.BLOCKS, 1, 1);
                    return InteractionResult.sidedSuccess(world.isClientSide());
                }
                return InteractionResult.PASS;
            }

            if (itemstack.isEmpty()) {
                if (chalice.getAmount() > 0) {
                    chalice.setAmount(chalice.getAmount() - 1);
                    int visual = chalice.getAmount() == 0 ? 0 : chalice.getAmount() < 32 ? 1 : 2;
                    world.setBlock(pos, blockstate.setValue(BLOCKSTATE, visual), 3);
                    if (world instanceof ServerLevel level) {
                        ItemEntity entityToSpawn = new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 0.75, pos.getZ() + 0.5, new ItemStack(Items.GOLD_INGOT));
                        entityToSpawn.setPickUpDelay(10);
                        level.addFreshEntity(entityToSpawn);
                    }
                    world.playSound(null, pos, BabelSounds.MONEY_OUT.get(), SoundSource.BLOCKS, 1, 1);
                    return InteractionResult.sidedSuccess(world.isClientSide());
                }
            }
        }

        return InteractionResult.PASS;
    }

    @Override
    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock()) && world instanceof ServerLevel level && world.getBlockEntity(pos) instanceof GoldenChaliceBlockEntity chalice && chalice.getAmount() > 0) {
            ItemEntity itemEntity = new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, new ItemStack(Items.GOLD_INGOT, chalice.getAmount()));
            itemEntity.setPickUpDelay(10);
            level.addFreshEntity(itemEntity);
        }
        super.onRemove(state, world, pos, newState, isMoving);
    }
}