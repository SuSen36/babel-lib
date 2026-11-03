package com.susen36.babel.event;

import com.susen36.babel.BabelConfig;
import com.susen36.babel.BabelMod;
import com.susen36.babel.init.BabelMobEffects;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = BabelMod.MODID)
public class ColdEvents {

    @SubscribeEvent
    public static void onColdTrigger(LivingHurtEvent event) {
        LivingEntity target = event.getEntity();
        DamageSource source = event.getSource();
        if (!target.level().isClientSide() && BabelConfig.coldToFrozen
                && source.is(DamageTypes.FREEZE) && target.canFreeze()) {
            if (target.getTicksFrozen() > 0) {
                // 融化速率为每 tick -2，冻结时长取触发时寒冷的剩余时间
                int duration = Mth.ceil(target.getTicksFrozen() / 2.0F);
                target.addEffect(new MobEffectInstance(BabelMobEffects.FROZEN.get(), duration));
                target.setTicksFrozen(0);
            } else {
                // 标记为寒冷但低于原版冻结阈值，避免完全冻结后原版周期冻结伤害抢先触发转化
                target.setTicksFrozen(target.getTicksRequiredToFreeze() - 1);
            }
        }
    }
}