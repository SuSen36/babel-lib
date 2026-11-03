package com.susen36.babel.block;

import com.susen36.babel.init.BabelBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class VampireBedUpperBlock extends Block {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    public VampireBedUpperBlock() {
        super(BlockBehaviour.Properties.of().sound(SoundType.WOOD).strength(0.5f, 1f).jumpFactor(1.3f).noOcclusion().isRedstoneConductor((bs, br, bp) -> false));
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter reader, BlockPos pos) {
        return true;
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
        return switch (state.getValue(FACING)) {
            case NORTH -> Shapes.or(box(0, 0, 14, 16, 12, 16), box(0, 0, 0, 2, 12, 14), box(14, 0, 0, 16, 12, 14), box(2, 0, 0, 14, 2, 14), box(2, 2, 0, 14, 5, 14));
            case EAST -> Shapes.or(box(0, 0, 0, 2, 12, 16), box(2, 0, 0, 16, 12, 2), box(2, 0, 14, 16, 12, 16), box(2, 0, 2, 16, 2, 14), box(2, 2, 2, 16, 5, 14));
            case WEST -> Shapes.or(box(14, 0, 0, 16, 12, 16), box(0, 0, 14, 14, 12, 16), box(0, 0, 0, 14, 12, 2), box(0, 0, 2, 14, 2, 14), box(0, 2, 2, 14, 5, 14));
            default -> Shapes.or(box(0, 0, 0, 16, 12, 2), box(14, 0, 2, 16, 12, 16), box(0, 0, 2, 2, 12, 16), box(2, 0, 2, 14, 2, 16), box(2, 2, 2, 14, 5, 16));
        };
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return super.getStateForPlacement(context).setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    public BlockState rotate(BlockState state, Rotation rot) {
        return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
    }

    public BlockState mirror(BlockState state, Mirror mirrorIn) {
        return state.rotate(mirrorIn.getRotation(state.getValue(FACING)));
    }

    private static Direction getFacing(BlockState bs) {
        return bs.getValue(FACING);
    }

    @Override
    public boolean canSurvive(BlockState blockstate, LevelReader worldIn, BlockPos pos) {
        if (worldIn instanceof LevelAccessor world) {
            int x = pos.getX();
            int y = pos.getY();
            int z = pos.getZ();
            return world.getBlockState(BlockPos.containing((double) x + getFacing(blockstate).getStepX(), y, (double) z + getFacing(blockstate).getStepZ())).getBlock() == BabelBlocks.VAMPIRES_BED.get();
        }
        return super.canSurvive(blockstate, worldIn, pos);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor world, BlockPos currentPos, BlockPos facingPos) {
        return !state.canSurvive(world, currentPos) ? Blocks.AIR.defaultBlockState() : super.updateShape(state, facing, facingState, world, currentPos, facingPos);
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter world, BlockPos pos, BlockState state) {
        return new ItemStack(BabelBlocks.VAMPIRES_BED.get());
    }
}