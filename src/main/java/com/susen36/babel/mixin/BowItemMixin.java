package com.susen36.babel.mixin;

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
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(BowItem.class)
public abstract class BowItemMixin {
    @Redirect(method = "releaseUsing",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;shrink(I)V"))
    private void skipArrowConsumptionWhenFlowing(ItemStack arrows, int count, ItemStack bow, Level level, LivingEntity shooter, int remainingTicks) {
        if (!(shooter instanceof Player player && HandOfFlowingWaterItem.isFlowing(player))) {
            arrows.shrink(count);
        }
    }

    @Redirect(method = "releaseUsing",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"))
    private boolean makeFlowingArrowCreativeOnly(Level level, Entity entity, ItemStack bow, Level levelArg, LivingEntity shooter, int remainingTicks) {
        if (shooter instanceof Player player && HandOfFlowingWaterItem.isFlowing(player) && entity instanceof AbstractArrow arrow) {
            arrow.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
        }
        return level.addFreshEntity(entity);
    }
}