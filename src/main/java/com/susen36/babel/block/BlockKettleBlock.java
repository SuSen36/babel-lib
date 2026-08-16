package com.susen36.babel.block;

import com.susen36.babel.BabelMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BlockKettleBlock extends Block implements SimpleWaterloggedBlock {
    public static final IntegerProperty BLOCKSTATE = IntegerProperty.create("blockstate", 0, 1);
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final BooleanProperty WATERED = BooleanProperty.create("watered");
    public static final BooleanProperty BOILING = BooleanProperty.create("boiling");
    public static final BooleanProperty NOODLED = BooleanProperty.create("noodled");

    public BlockKettleBlock() {
        super(BlockBehaviour.Properties.of().sound(SoundType.METAL).strength(0.5f, 2f).noOcclusion().isRedstoneConductor((bs, br, bp) -> false));
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(WATERED, false).setValue(BOILING, false).setValue(NOODLED, false).setValue(WATERLOGGED, false));
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
        return switch (state.getValue(FACING)) {
            case NORTH -> Shapes.or(box(2.5, 3, 2.5, 13.5, 6, 13.5), box(3.5, 6, 3.5, 12.5, 9, 12.5), box(3.5, 2, 3.5, 12.5, 3, 12.5), box(4.5, 10, 4.5, 11.5, 12, 11.5), box(5.5, 12, 5.5, 10.5, 13, 10.5), box(7.5, 13, 7.5, 8.5, 14, 8.5),
                    box(4.5, 1, 4.5, 11.5, 2, 11.5), box(3.5, 0, 3.5, 12.5, 1, 12.5), box(4.5, 9, 4.5, 11.5, 10, 11.5));
            case EAST -> Shapes.or(box(2.5, 3, 2.5, 13.5, 6, 13.5), box(3.5, 6, 3.5, 12.5, 9, 12.5), box(3.5, 2, 3.5, 12.5, 3, 12.5), box(4.5, 10, 4.5, 11.5, 12, 11.5), box(5.5, 12, 5.5, 10.5, 13, 10.5), box(7.5, 13, 7.5, 8.5, 14, 8.5),
                    box(4.5, 1, 4.5, 11.5, 2, 11.5), box(3.5, 0, 3.5, 12.5, 1, 12.5), box(4.5, 9, 4.5, 11.5, 10, 11.5));
            case WEST -> Shapes.or(box(2.5, 3, 2.5, 13.5, 6, 13.5), box(3.5, 6, 3.5, 12.5, 9, 12.5), box(3.5, 2, 3.5, 12.5, 3, 12.5), box(4.5, 10, 4.5, 11.5, 12, 11.5), box(5.5, 12, 5.5, 10.5, 13, 10.5), box(7.5, 13, 7.5, 8.5, 14, 8.5),
                    box(4.5, 1, 4.5, 11.5, 2, 11.5), box(3.5, 0, 3.5, 12.5, 1, 12.5), box(4.5, 9, 4.5, 11.5, 10, 11.5));
            default -> Shapes.or(box(2.5, 3, 2.5, 13.5, 6, 13.5), box(3.5, 6, 3.5, 12.5, 9, 12.5), box(3.5, 2, 3.5, 12.5, 3, 12.5), box(4.5, 10, 4.5, 11.5, 12, 11.5), box(5.5, 12, 5.5, 10.5, 13, 10.5), box(7.5, 13, 7.5, 8.5, 14, 8.5),
                    box(4.5, 1, 4.5, 11.5, 2, 11.5), box(3.5, 0, 3.5, 12.5, 1, 12.5), box(4.5, 9, 4.5, 11.5, 10, 11.5));
        };
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING, WATERED, BOILING, NOODLED, WATERLOGGED, BLOCKSTATE);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        boolean flag = context.getLevel().getFluidState(context.getClickedPos()).getType() == Fluids.WATER;
        return super.getStateForPlacement(context).setValue(FACING, context.getHorizontalDirection().getOpposite()).setValue(WATERED, false).setValue(BOILING, false).setValue(NOODLED, false).setValue(WATERLOGGED, flag);
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
    public void onPlace(BlockState blockstate, Level world, BlockPos pos, BlockState oldState, boolean moving) {
        super.onPlace(blockstate, world, pos, oldState, moving);
        world.scheduleTick(pos, this, 40);
    }

    @Override
    public void neighborChanged(BlockState blockstate, Level world, BlockPos pos, Block neighborBlock, BlockPos fromPos, boolean moving) {
        super.neighborChanged(blockstate, world, pos, neighborBlock, fromPos, moving);
        setBoiling(world, pos);
    }

    @Override
    public void tick(BlockState blockstate, ServerLevel world, BlockPos pos, RandomSource random) {
        super.tick(blockstate, world, pos, random);
        setBoiling(world, pos);
        world.scheduleTick(pos, this, 40);
    }

    private void setBoiling(LevelAccessor world, BlockPos pos) {
        boolean valid = false;
        BlockState lower = world.getBlockState(pos.below());
        if (lower.is(BlockTags.create(ResourceLocation.fromNamespaceAndPath(BabelMod.MODID, "heat")))) {
            if (lower.getBlock() == Blocks.CAMPFIRE || lower.getBlock() == Blocks.SOUL_CAMPFIRE) {
                valid = lower.getBlock().getStateDefinition().getProperty("lit") instanceof BooleanProperty litProperty && lower.getValue(litProperty);
            } else if (lower.getBlock() == Blocks.SMOKER) {
                valid = lower.getBlock().getStateDefinition().getProperty("lit") instanceof BooleanProperty litProperty && lower.getValue(litProperty);
            } else {
                valid = true;
            }
        }
        BlockState state = world.getBlockState(pos);
        int blockStateValue = valid ? 1 : 0;
        if (state.getBlock().getStateDefinition().getProperty("blockstate") instanceof IntegerProperty integerProperty && integerProperty.getPossibleValues().contains(blockStateValue)) {
            state = state.setValue(integerProperty, blockStateValue);
        }
        if (state.getBlock().getStateDefinition().getProperty("boiling") instanceof BooleanProperty booleanProperty) {
            state = state.setValue(booleanProperty, valid);
        }
        world.setBlock(pos, state, 3);
    }
}