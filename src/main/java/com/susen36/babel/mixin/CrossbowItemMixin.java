package com.susen36.babel.mixin;

import com.susen36.babel.item.HandOfFlowingWaterItem;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(CrossbowItem.class)
public abstract class CrossbowItemMixin {
    @Redirect(method = "loadProjectile",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;split(I)Lnet/minecraft/world/item/ItemStack;"))
    private static ItemStack maybeSkipBoltConsumption(ItemStack bolts, int count, LivingEntity shooter, ItemStack crossbow, ItemStack projectile, boolean multishotExtra, boolean creative) {
        if (shooter instanceof Player player && HandOfFlowingWaterItem.isFlowing(player) && player.getRandom().nextBoolean()) {
            return bolts.copyWithCount(1);
        }
        return bolts.split(count);
    }
}
