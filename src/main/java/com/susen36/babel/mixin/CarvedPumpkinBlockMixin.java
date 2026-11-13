package com.susen36.babel.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.susen36.babel.collectible.Collectibles;
import com.susen36.babel.init.BabelCollectible;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CarvedPumpkinBlock;
import net.minecraftforge.common.util.FakePlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

@Mixin(CarvedPumpkinBlock.class)
public abstract class CarvedPumpkinBlockMixin {

    @WrapOperation(method = "spawnGolemInWorld", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"))
    private static boolean refundIronBlock(Level level, Entity entity, Operation<Boolean> original) {
        boolean spawned = original.call(level, entity);
        if (spawned && entity instanceof IronGolem golem) {
            // 沿用原版 SUMMONED_ENTITY 进度口径，取傀儡 5 格内最近的有效玩家
            List<ServerPlayer> candidates = golem.level().getEntitiesOfClass(ServerPlayer.class,
                    golem.getBoundingBox().inflate(5.0D),
                    player -> !(player instanceof FakePlayer)
                            && Collectibles.isUsed(player, BabelCollectible.COMPREHENSIVE_OPERATION_FILE.get()));
            ServerPlayer builder = null;
            double nearestSqr = Double.MAX_VALUE;
            for (ServerPlayer candidate : candidates) {
                double distSqr = candidate.distanceToSqr(golem);
                if (distSqr < nearestSqr) {
                    nearestSqr = distSqr;
                    builder = candidate;
                }
            }
            if (builder != null) {
                builder.getInventory().placeItemBackInInventory(new ItemStack(Items.IRON_BLOCK));
            }
        }
        return spawned;
    }
}