package com.susen36.babel.block;

import com.susen36.babel.init.BabelCollectible;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.List;

public class VampireBedBlock extends BedBlock {
    public VampireBedBlock() {
        super(DyeColor.RED, BlockBehaviour.Properties.of().sound(SoundType.WOOD).strength(0.5f, 1f).jumpFactor(1.3f).noOcclusion().pushReaction(PushReaction.DESTROY).isRedstoneConductor((bs, br, bp) -> false));
    }

    /**
     * 创造模式中键取床时返回收藏品物品，而不是方块对应的 BlockItem。
     */
    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter level, BlockPos pos, Player player) {
        return new ItemStack(BabelCollectible.VAMPIRES_BED.get());
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
        // 模型默认对应 facing=south（脚在北、头在南），碰撞箱分支按模型局部方向书写，因此取反匹配。
        Direction facing = state.getValue(FACING).getOpposite();
        if (state.getValue(PART) == BedPart.HEAD) {
            return switch (facing) {
                case NORTH -> Shapes.or(box(0, 0, 14, 16, 12, 16), box(0, 0, 0, 2, 12, 14), box(14, 0, 0, 16, 12, 14), box(2, 0, 0, 14, 2, 14), box(2, 2, 0, 14, 5, 14));
                case EAST -> Shapes.or(box(0, 0, 0, 2, 12, 16), box(2, 0, 0, 16, 12, 2), box(2, 0, 14, 16, 12, 16), box(2, 0, 2, 16, 2, 14), box(2, 2, 2, 16, 5, 14));
                case WEST -> Shapes.or(box(14, 0, 0, 16, 12, 16), box(0, 0, 14, 14, 12, 16), box(0, 0, 0, 14, 12, 2), box(0, 0, 2, 14, 2, 14), box(0, 2, 2, 14, 5, 14));
                default -> Shapes.or(box(0, 0, 0, 16, 12, 2), box(14, 0, 2, 16, 12, 16), box(0, 0, 2, 2, 12, 16), box(2, 0, 2, 14, 2, 16), box(2, 2, 2, 14, 5, 16));
            };
        }
        return switch (facing) {
            case NORTH -> Shapes.or(box(0, 0, 0, 16, 12, 2), box(0, 0, 2, 2, 12, 16), box(14, 0, 2, 16, 12, 16), box(2, 0, 2, 14, 2, 16), box(2, 2, 2, 14, 5, 16));
            case EAST -> Shapes.or(box(14, 0, 0, 16, 12, 16), box(0, 0, 0, 14, 12, 2), box(0, 0, 14, 14, 12, 16), box(0, 0, 2, 14, 2, 14), box(0, 2, 2, 14, 5, 14));
            case WEST -> Shapes.or(box(0, 0, 0, 2, 12, 16), box(2, 0, 14, 16, 12, 16), box(2, 0, 0, 16, 12, 2), box(2, 0, 2, 16, 2, 14), box(2, 2, 2, 16, 5, 14));
            default -> Shapes.or(box(0, 0, 14, 16, 12, 16), box(14, 0, 0, 16, 12, 14), box(0, 0, 0, 2, 12, 14), box(2, 0, 0, 14, 2, 14), box(2, 2, 0, 14, 5, 14));
        };
    }

    /**
     * 右击脚部或枕部都换算到枕部，再交给原版睡眠流程处理夜间判定、被阻挡与刷怪检查。
     */
    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) {
            return InteractionResult.CONSUME;
        }
        BlockPos headPos = state.getValue(PART) == BedPart.HEAD ? pos : pos.relative(state.getValue(FACING));
        BlockState headState = level.getBlockState(headPos);
        if (!headState.is(this) || headState.getValue(PART) != BedPart.HEAD) {
            return InteractionResult.CONSUME;
        }
        player.startSleepInBed(headPos).ifLeft(problem -> {
            Component message = problem.getMessage();
            if (message != null) {
                player.displayClientMessage(message, true);
            }
        });
        return InteractionResult.SUCCESS;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        //棺木使用普通方块模型渲染，不需要原版床方块实体。
        return null;
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean moving) {
        super.onPlace(state, level, pos, oldState, moving);
        if (state.getValue(PART) == BedPart.FOOT) {
            level.scheduleTick(pos, this, 20);
        }
    }

    @Override
    public void tick(BlockState blockstate, ServerLevel world, BlockPos pos, RandomSource random) {
        super.tick(blockstate, world, pos, random);
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        final Vec3 center = new Vec3(x, y, z);
        List<Entity> entfound = world.getEntitiesOfClass(Entity.class, new AABB(center, center).inflate(64 / 2d), e -> true).stream().sorted(Comparator.comparingDouble(entcnd -> entcnd.distanceToSqr(center))).toList();
        for (Entity entityiterator : entfound) {
            if ((entityiterator instanceof Bat || (entityiterator instanceof LivingEntity livEnt1 && livEnt1.getMobType() == MobType.UNDEAD))
                    && new Vec3((double) x + 0.5, (double) y + 1, (double) z + 0.5).distanceTo(new Vec3(entityiterator.getX(), entityiterator.getY(), entityiterator.getZ())) > 1) {
                if (Math.random() < 0.2) {
                    if (entityiterator instanceof Mob mob)
                        mob.getNavigation().moveTo(((double) x + Mth.nextDouble(RandomSource.create(), -3, 4)), y, ((double) z + Mth.nextDouble(RandomSource.create(), -3, 4)), 1);
                }
            }
        }
        world.scheduleTick(pos, this, 20);
    }

}