package com.susen36.babel.mixin;

import com.susen36.babel.collectible.Collectibles;
import com.susen36.babel.init.BabelCollectible;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.item.trading.MerchantOffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractVillager.class)
public abstract class AbstractVillagerMixin {

    @Inject(method = "notifyTrade(Lnet/minecraft/world/item/trading/MerchantOffer;)V", at = @At("TAIL"))
    private void babelGrantTradeLevel(MerchantOffer offer, CallbackInfo ci) {
        AbstractVillager trader = (AbstractVillager) (Object) this;
        if (!trader.level().isClientSide() && trader.getTradingPlayer() instanceof ServerPlayer player && Collectibles.isUsed(player, BabelCollectible.CANNOT_MARK)) {
            player.giveExperienceLevels(1);
        }
    }
}