package com.susen36.babel.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.susen36.babel.item.HandOfFlowingWaterItem;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(CrossbowItem.class)
public abstract class CrossbowItemMixin {

    @WrapOperation(method = "loadProjectile", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;split(I)Lnet/minecraft/world/item/ItemStack;"))
    private static ItemStack maybeSkipBoltConsumption(
            ItemStack bolts, int count, Operation<ItemStack> original,
            @Local(argsOnly = true) LivingEntity shooter) {
        if (shooter instanceof Player player && HandOfFlowingWaterItem.isFlowing(player) && player.getRandom().nextBoolean()) {
            return bolts.copyWithCount(1);
        }
        return original.call(bolts, count);
    }
}