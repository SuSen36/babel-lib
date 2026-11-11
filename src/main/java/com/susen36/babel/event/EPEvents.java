package com.susen36.babel.event;

import com.susen36.babel.BabelMod;
import com.susen36.babel.api.event.ElementEvent;
import com.susen36.babel.capability.BabelCapability;
import com.susen36.babel.capability.ep.EPCapability;
import com.susen36.babel.elemental.FrenzyInjury;
import com.susen36.babel.elemental.base.AbstractEPCapability;
import com.susen36.babel.elemental.base.EPManager;
import com.susen36.babel.init.BabelDamageTypes;
import com.susen36.babel.init.BabelMobEffects;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.WeakHashMap;

@Mod.EventBusSubscriber(modid = BabelMod.MODID)
public class EPEvents {

    private static final Map<LivingEntity, CacheEntry> EP_CACHE = new WeakHashMap<>();
    private static final int CACHE_TTL = 60;

    private record CacheEntry(EPCapability ep, long expireAt) {}

    @Nullable
    private static EPCapability cachedEP(LivingEntity living) {
        long tick = living.level().getGameTime();
        CacheEntry entry = EP_CACHE.get(living);
        EPCapability ep;
        if (entry != null && tick < entry.expireAt) {
            ep = entry.ep;
        } else {
            ep = BabelCapability.getEP(living);
            if (ep != null) {
                EP_CACHE.put(living, new CacheEntry(ep, tick + CACHE_TTL));
            }
        }
        return ep;
    }

    @SubscribeEvent
    public static void onEntityTick(LivingEvent.LivingTickEvent event) {
        LivingEntity living = event.getEntity();
        EPCapability ep = living.level().isClientSide() ? null : cachedEP(living);
        if (ep != null) {
            boolean burstJustEnded = ep.tick();
            int syncInterval = living instanceof Player ? 5 : 20;
            if (burstJustEnded || living.tickCount % syncInterval == 0) {
                ep.syncDirty();
            }
        }
    }

    @SubscribeEvent
    public static void onLivingDamageResetRegen(LivingDamageEvent event) {
        if (event.getAmount() > 0) {
            EPCapability ep = cachedEP(event.getEntity());
            if (ep != null) {
                ep.resetRegenDelay();
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void handleElementalAttackDamage(LivingDamageEvent event) {
        LivingEntity target = event.getEntity();
        Entity sourceEntity = event.getSource().getEntity();
        LivingEntity attacker = sourceEntity instanceof Projectile projectile && projectile.getOwner() instanceof LivingEntity owner
                ? owner : sourceEntity instanceof LivingEntity living ? living : null;

        if (attacker != null) {
            EPManager.ElementalAttackConfig attackConfig = EPManager.getElementalAttackConfig(attacker);
            AbstractEPCapability.EPType epType = attackConfig.type();
            double rate = attackConfig.rate();
            double injuryDamage = attackConfig.injuryDamage();
            EPManager.ElementalDefenseConfig defenseConfig = EPManager.getElementalDefenseConfig(target);
            if (epType == AbstractEPCapability.EPType.NERVOUS && defenseConfig.type() == epType) {
                rate *= defenseConfig.baseModifier() * defenseConfig.totalModifier();
                injuryDamage *= defenseConfig.baseModifier() * defenseConfig.totalModifier();
            }
            double elementalDamage = injuryDamage + event.getAmount() * rate;
            if (elementalDamage > 0) {
                EPManager.hurtElemental(target, epType, attacker, Mth.floor(elementalDamage));
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onBurnDamageCancel(LivingDamageEvent event) {
        LivingEntity entity = event.getEntity();
        DamageSource source = event.getSource();
        float damage = event.getAmount();
        if (!entity.level().isClientSide() && entity instanceof Player && source.is(DamageTypeTags.IS_FIRE)) {
            EPCapability ep = cachedEP(entity);
            AbstractEPCapability burn = ep == null ? null : ep.getEP(AbstractEPCapability.EPType.BURN);
            if (burn != null && burn.underBurst()) {
                event.setAmount(damage * 1.2F);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onWitherDamageCancel(LivingDamageEvent event) {
        LivingEntity entity = event.getEntity();
        DamageSource source = event.getSource();
        float damage = event.getAmount();
        if (!entity.level().isClientSide() && entity instanceof Player && source.is(DamageTypes.WITHER)) {
            EPCapability ep = cachedEP(entity);
            AbstractEPCapability necrosis = ep == null ? null : ep.getEP(AbstractEPCapability.EPType.NECROSIS);
            if (necrosis != null && necrosis.underBurst()) {
                event.setAmount(damage * 1.1F);
            }
        }
    }

    @SubscribeEvent
    public static void onFrenzyMeleeHit(LivingHurtEvent event) {
        if (!event.isCanceled() && !event.getEntity().level().isClientSide()
                && event.getSource().getDirectEntity() == event.getSource().getEntity()
                && event.getSource().getEntity() instanceof LivingEntity attacker) {
            EPCapability ep = cachedEP(attacker);
            AbstractEPCapability frenzy = ep == null ? null : ep.getEP(AbstractEPCapability.EPType.FRENZY);
            if (frenzy instanceof FrenzyInjury fi && fi.underBurst()) {
                fi.addFrenzyStack();
            }
        }
    }

    @SubscribeEvent
    public static void onElementalInjuryResist(ElementEvent.Hurt event) {
        MobEffectInstance effect = event.getEntity().getEffect(BabelMobEffects.ESSENCE_RESISTANCE.get());
        if (effect != null) {
            double reduction = Mth.clamp(0.15 * (effect.getAmplifier() + 1), 0.0, 1.0);
            event.setAmount(Mth.floor(event.getAmount() * (1.0 - reduction)));
        }
    }

    @SubscribeEvent
    public static void onElementBurstResist(LivingHurtEvent event) {
        LivingEntity target = event.getEntity();
        MobEffectInstance effect = target.getEffect(BabelMobEffects.ESSENCE_RESISTANCE.get());
        if (effect != null && !target.level().isClientSide() && event.getSource().is(BabelDamageTypes.ELEMENT_BREAK)) {
            double reduction = Mth.clamp(0.25 * (effect.getAmplifier() + 1), 0.0, 1.0);
            event.setAmount(event.getAmount() * (float) (1.0 - reduction));
        }
    }
}