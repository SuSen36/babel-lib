package com.susen36.babel.event;

import com.susen36.babel.BabelMod;
import com.susen36.babel.api.event.DodgeEvent;
import com.susen36.babel.init.BabelAttributes;
import com.susen36.babel.init.BabelDamageTags;
import com.susen36.babel.init.BabelMobEffects;
import com.susen36.babel.init.BabelParticles;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = BabelMod.MODID)
public class DodgeEvents {

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingAttack(LivingAttackEvent event) {
        LivingEntity target = event.getEntity();
        DamageSource source = event.getSource();
        AttributeInstance dodgeRate = target.getAttribute(EPEvents.isMagic(source) ? BabelAttributes.MAGIC_MISS_RATE.get() : BabelAttributes.PHYSICAL_MISS_RATE.get());
        if (!event.isCanceled() && dodgeRate != null && dodgeRate.getValue() > 0
                && !source.is(BabelDamageTags.BYPASS_MISS)
                && !target.hasEffect(BabelMobEffects.SILENCED.get())
                && (source.is(BabelDamageTags.SKIP_SOURCE_CHECK) || source.getEntity() != null)
                && target.getRandom().nextDouble() * 100 < dodgeRate.getValue()) {
            if (target.level() instanceof ServerLevel level) {
                SimpleParticleType particle = EPEvents.isMagic(source) ? BabelParticles.MISS_MAGIC.get() : BabelParticles.MISS_PHYSICAL.get();
                level.sendParticles(particle, target.getX(), target.getY(), target.getZ(), 6, 1, 1, 1, 0.1);
            }
            DodgeEvent.postDodge(target, source);
            event.setCanceled(true);
        }
    }
}