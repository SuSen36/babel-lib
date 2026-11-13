package com.susen36.babel.mixin;

import com.susen36.babel.collectible.Collectibles;
import com.susen36.babel.init.BabelCollectible;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.trading.MerchantOffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Villager.class)
public abstract class VillagerMixin {

    private static final double VILLAGER_DISCOUNT_RATE = 1.0D / 3.0D;
    private static final int REGIONAL_ACTION_PLAN_MAX_LEVEL = 3;
    private static final int VILLAGER_MAX_LEVEL = 5;

    @Inject(method = "updateSpecialPrices(Lnet/minecraft/world/entity/player/Player;)V", at = @At("TAIL"))
    private void applyVillagerDiscount(Player player, CallbackInfo ci) {
        Villager villager = (Villager) (Object) this;
        if (!villager.level().isClientSide()) {
            int villagerLevel = villager.getVillagerData().getLevel();
            if (Collectibles.isUsed(player, BabelCollectible.SPEECH_DRAFT)) {
                babelDiscountAllOffers(villager);
            }
            if (villagerLevel <= REGIONAL_ACTION_PLAN_MAX_LEVEL && Collectibles.isUsed(player, BabelCollectible.REGIONAL_ACTION_PLAN)) {
                babelDiscountAllOffers(villager);
            }
            if (villagerLevel >= VILLAGER_MAX_LEVEL && Collectibles.isUsed(player, BabelCollectible.SECRET_HR_LETTER)) {
                babelDiscountAllOffers(villager);
            }
        }
    }

    private void babelDiscountAllOffers(Villager villager) {
        for (MerchantOffer offer : villager.getOffers()) {
            int discount = Mth.floor(VILLAGER_DISCOUNT_RATE * offer.getBaseCostA().getCount());
            offer.addToSpecialPriceDiff(-Math.max(discount, 1));
        }
    }
}
