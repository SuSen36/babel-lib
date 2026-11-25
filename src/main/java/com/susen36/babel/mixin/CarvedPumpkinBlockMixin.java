package com.susen36.babel.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.susen36.babel.item.OperationFileItem;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CarvedPumpkinBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

@Mixin(CarvedPumpkinBlock.class)
public abstract class CarvedPumpkinBlockMixin {

    @WrapOperation(method = "spawnGolemInWorld", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"))
    private static boolean captureSpawnedGolem(Level level, Entity entity, Operation<Boolean> original,
                                                     @Share("spawnedGolem") LocalRef<IronGolem> spawnedGolem) {
        boolean spawned = original.call(level, entity);
        if (spawned && entity instanceof IronGolem golem) {
            spawnedGolem.set(golem);
        }
        return spawned;
    }

    @ModifyExpressionValue(method = "spawnGolemInWorld", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getEntitiesOfClass(Ljava/lang/Class;Lnet/minecraft/world/phys/AABB;)Ljava/util/List;"))
    private static List<ServerPlayer> onSummonPlayers(List<ServerPlayer> players,
                                                            @Share("spawnedGolem") LocalRef<IronGolem> spawnedGolem) {
        IronGolem golem = spawnedGolem.get();
        if (golem != null) {
            OperationFileItem.onIronGolemBuilt(golem, players);
        }
        return players;
    }
}