package com.susen36.babel.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.susen36.babel.item.HandOfFlowingWaterItem;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BowItem.class)
public abstract class BowItemMixin {

    @WrapOperation(method = "releaseUsing", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;shrink(I)V"))
    private static void skipArrowConsumptionWhenFlowing(ItemStack arrows, int count, Operation<Void> original, @Local(argsOnly = true) LivingEntity shooter) {
        if (!(shooter instanceof Player player && HandOfFlowingWaterItem.isFlowing(player))) {
            original.call(arrows, count);
        }
    }

    @WrapOperation(method = "releaseUsing", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"))
    private static boolean makeFlowingArrowCreativeOnly(Level level, Entity entity, Operation<Boolean> original, @Local(argsOnly = true) LivingEntity shooter) {
        if (shooter instanceof Player player && HandOfFlowingWaterItem.isFlowing(player) && entity instanceof AbstractArrow arrow) {
            arrow.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
        }
        return original.call(level, entity);
    }
}