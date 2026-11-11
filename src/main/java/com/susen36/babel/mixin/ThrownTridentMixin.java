package com.susen36.babel.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.susen36.babel.collectible.Collectibles;
import com.susen36.babel.init.BabelCollectible;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ThrownTrident.class)
public abstract class ThrownTridentMixin {

    @WrapOperation(method = "onHitEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;isThundering()Z"))
    private boolean allowRainForChanneling(Level level, Operation<Boolean> original) {
        if (!original.call(level)) {
            Entity owner = ((ThrownTrident) (Object) this).getOwner();
            return level.isRaining() && owner instanceof ServerPlayer player && Collectibles.isUsed(player, BabelCollectible.HAND_OF_DIFFUSION);
        }
        return true;
    }
}