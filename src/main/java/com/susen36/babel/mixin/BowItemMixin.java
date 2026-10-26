package com.susen36.babel.mixin;

import com.susen36.babel.item.HandOfFlowingWaterItem;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
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
    private void maybeSkipArrowConsumption(ItemStack arrows, int count, ItemStack bow, Level level, LivingEntity shooter, int remainingTicks) {
        if (!(shooter instanceof Player player && HandOfFlowingWaterItem.isFlowing(player) && HandOfFlowingWaterItem.rollArrowPreservation(player, level))) {
            arrows.shrink(count);
        }
    }
}
