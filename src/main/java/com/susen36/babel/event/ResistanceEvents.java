package com.susen36.babel.event;

import com.susen36.babel.BabelMod;
import com.susen36.babel.api.event.ResistDamageEvent;
import com.susen36.babel.init.BabelAttributes;
import com.susen36.babel.util.DataUtils;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = BabelMod.MODID)
public class ResistanceEvents {

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onLivingDamagePreHigh(LivingDamageEvent event) {
        LivingEntity entity = event.getEntity();
        DamageSource source = event.getSource();
        if (DataUtils.isNotReal(source)) {
            float originalAmount = event.getAmount();
            float amount = originalAmount;
            if (DataUtils.isMagic(source)) {
                AttributeInstance magic = entity.getAttribute(BabelAttributes.MAGIC_RESISTANCE.get());
                if (magic != null) {
                    double v = magic.getValue();
                    v = ResistDamageEvent.modifyEffectiveResistanceValue(entity, source, amount, v);
                    if (v > 0) {
                        amount *= (float) ((100.0 - Mth.clamp(v, 0.0, 100.0)) * 0.01);
                    }
                }
            } else if (DataUtils.isPhysical(source)) {
                AttributeInstance defense = entity.getAttribute(BabelAttributes.DEFENSE.get());
                if (defense != null) {
                    double v = defense.getValue();
                    v = ResistDamageEvent.modifyEffectiveDefenseValue(entity, source, amount, v);
                    if (v > 0) {
                        amount -= (float) v;
                    }
                }
            }
            amount = Math.max(originalAmount * 0.05F, amount);
            if (amount < originalAmount) {
                event.setAmount(amount);
            }
        }
    }
}