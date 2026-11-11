package com.susen36.babel.event;

import com.susen36.babel.BabelMod;
import com.susen36.babel.effect.PalsyMobEffect;
import com.susen36.babel.init.BabelMobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = BabelMod.MODID)
public class PalsyEvents {

    @SubscribeEvent
    public static void onPalsyingCancel(LivingHurtEvent event) {
        LivingEntity target = event.getEntity();
        if (!event.isCanceled() && !target.level().isClientSide()
                && event.getSource().getEntity() instanceof LivingEntity attacker
                && attacker.hasEffect(BabelMobEffects.PALSY.get())) {
            PalsyMobEffect.onAttackBlocked(attacker, target);
            event.setCanceled(true);
        }
    }
}