package com.susen36.babel.event;

import com.susen36.babel.BabelMod;
import com.susen36.babel.init.BabelAttributes;
import com.susen36.babel.init.BabelSounds;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = BabelMod.MODID)
public class ShieldEvents {

    // LOWEST 保证在 BarrierEvents(LOW) 的屏障按量吸收之后结算
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onShieldDamageBlock(LivingDamageEvent event) {
        DamageSource source = event.getSource();
        LivingEntity entity = event.getEntity();
        if (event.getAmount() > 0.0F && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY) && !source.is(DamageTypeTags.BYPASSES_EFFECTS)) {
            AttributeInstance instance = entity.getAttribute(BabelAttributes.SHIELD.get());
            if (instance != null) {
                double layers = instance.getValue();
                if (layers >= 1.0) {
                    instance.setBaseValue(layers - 1.0);
                    event.setAmount(0.0F);
                    entity.level().playSound(null, entity.blockPosition(), BabelSounds.BARRIER.get(),
                            SoundSource.HOSTILE, 2.0F, (float) Mth.nextDouble(RandomSource.create(), 0.9, 1.1));
                }
            }
        }
    }
}