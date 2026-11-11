package com.susen36.babel.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.susen36.babel.collectible.Collectibles;
import com.susen36.babel.init.BabelCollectible;
import com.susen36.babel.init.BabelDamageTypes;
import com.susen36.babel.init.BabelMobEffects;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Player.class)
public abstract class PlayerMixin {

    @WrapOperation(
            method = "attack",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/damagesource/DamageSources;playerAttack(Lnet/minecraft/world/entity/player/Player;)Lnet/minecraft/world/damagesource/DamageSource;"
            )
    )
    private DamageSource dreambindRetag(DamageSources sources, Player attacker, Operation<DamageSource> original, Entity target) {
        if (target instanceof LivingEntity living
                && living.hasEffect(BabelMobEffects.SLEEP.get())
                && Collectibles.isUsed(attacker, BabelCollectible.DREAMBIND_CASTLE_MODEL)) {
            return BabelDamageTypes.source(attacker.level(), BabelDamageTypes.DREAMBIND_STRIKE, attacker);
        }
        return original.call(sources, attacker);
    }
}