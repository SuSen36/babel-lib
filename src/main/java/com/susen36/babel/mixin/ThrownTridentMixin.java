package com.susen36.babel.mixin;

import com.susen36.babel.collectible.Collectibles;
import com.susen36.babel.init.BabelCollectible;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ThrownTrident.class)
public abstract class ThrownTridentMixin {
    @Redirect(method = "onHitEntity",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;isThundering()Z"))
    private boolean allowRainForChanneling(Level level) {
        if (!level.isThundering()) {
            Entity owner = ((ThrownTrident) (Object) this).getOwner();
            return level.isRaining() && owner instanceof ServerPlayer player
                    && Collectibles.getData(player).isUsed(BabelCollectible.HAND_OF_DIFFUSION.get());
        }
        return true;
    }
}