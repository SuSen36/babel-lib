package com.susen36.babel.mixin;

import com.susen36.babel.init.BabelDamageTypes;
import com.susen36.babel.init.BabelMobEffects;
import com.susen36.babel.item.DreambindCastleModelItem;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Player.class)
public abstract class PlayerMixin {

    @Redirect(method = "attack",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/damagesource/DamageSources;playerAttack(Lnet/minecraft/world/entity/player/Player;)Lnet/minecraft/world/damagesource/DamageSource;"))
    private DamageSource dreambindRetag(DamageSources sources, Player attacker, Entity target) {
        if (target instanceof LivingEntity living
                && living.hasEffect(BabelMobEffects.SLEEP.get())
                && DreambindCastleModelItem.isUsed(attacker)) {
            return BabelDamageTypes.source(attacker.level(), BabelDamageTypes.DREAMBIND_STRIKE, attacker);
        }
        return sources.playerAttack(attacker);
    }
}