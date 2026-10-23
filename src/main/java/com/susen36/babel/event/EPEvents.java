package com.susen36.babel.event;

import com.susen36.babel.BabelMod;
import com.susen36.babel.manager.EPManager;
import com.susen36.babel.api.event.ResistDamageEvent;
import com.susen36.babel.capability.BabelCapability;
import com.susen36.babel.capability.ep.EPCapability;
import com.susen36.babel.elemental.base.AbstractEPCapability;
import com.susen36.babel.elemental.FrenzyInjury;
import com.susen36.babel.effect.PalsyMobEffect;
import com.susen36.babel.init.BabelAttributes;
import com.susen36.babel.init.BabelMobEffects;
import com.susen36.babel.init.BabelParticles;
import com.susen36.babel.init.BabelSounds;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

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

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onLivingDamagePreHigh(LivingDamageEvent event) {
        LivingEntity entity = event.getEntity();
        DamageSource source = event.getSource();
        if (isNotReal(source)) {
            float originalAmount = event.getAmount();
            float amount = originalAmount;
            if (isMagic(source)) {
                AttributeInstance magic = entity.getAttribute(BabelAttributes.MAGIC_RESISTANCE.get());
                if (magic != null) {
                    double v = magic.getValue();
                    v = ResistDamageEvent.modifyEffectiveResistanceValue(entity, source, amount, v);
                    if (v > 0) {
                        amount *= (float) ((100.0 - Mth.clamp(v, 0.0, 100.0)) * 0.01);
                    }
                }
            } else if (isPhysical(source)) {
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

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void handleBarrierFunc(LivingDamageEvent event) {
        DamageSource source = event.getSource();
        LivingEntity entity = event.getEntity();
        if (!source.is(DamageTypeTags.BYPASSES_INVULNERABILITY) && !source.is(DamageTypeTags.BYPASSES_EFFECTS)) {
            AttributeInstance instance = entity.getAttribute(BabelAttributes.BARRIER.get());
            if (instance != null) {
                float amount = event.getAmount();
                double brr = instance.getValue();
                if (brr > 0.0) {
                    double disp;
                    if (brr >= amount) {
                        instance.setBaseValue(brr - amount);
                        disp = amount;
                        event.setAmount(0.0F);
                    } else {
                        instance.setBaseValue(0.0);
                        disp = brr;
                        event.setAmount(amount - (float) brr);
                    }
                    entity.level().playSound(null, entity.blockPosition(), BabelSounds.BARRIER.get(),
                            SoundSource.HOSTILE, 2.0F, (float) Mth.nextDouble(RandomSource.create(), 0.9, 1.1));
                    if (entity.level() instanceof ServerLevel level) {
                        level.sendParticles(BabelParticles.BARRIER_SHOW.get(), entity.getX(), entity.getY() + 0.75, entity.getZ(),
                                (int) Math.min(disp * 0.5, 24), 0.75, 0.75, 0.75, 0.1);
                    }
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void handleElementalAttackDamage(LivingDamageEvent event) {
        LivingEntity target = event.getEntity();
        Entity sourceEntity = event.getSource().getEntity();
        LivingEntity attacker = sourceEntity instanceof Projectile projectile && projectile.getOwner() instanceof LivingEntity owner
                ? owner
                : sourceEntity instanceof LivingEntity living ? living : null;

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
    public static void onPalsyingCancel(LivingHurtEvent event) {
        LivingEntity target = event.getEntity();
        if (!event.isCanceled() && !target.level().isClientSide()
                && event.getSource().getEntity() instanceof LivingEntity attacker
                && attacker.hasEffect(BabelMobEffects.PALSY.get())) {
            PalsyMobEffect.onAttackBlocked(attacker, target);
            event.setCanceled(true);
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

    public static final TagKey<DamageType> FORGE_MAGIC = TagKey.create(
            Registries.DAMAGE_TYPE, new ResourceLocation("forge", "is_magic"));

    public static boolean isMagic(DamageSource source) {
        return isNotReal(source) && !source.is(DamageTypeTags.BYPASSES_ENCHANTMENTS) &&
                (source.is(FORGE_MAGIC) || source.is(DamageTypeTags.BYPASSES_ARMOR) || source.is(DamageTypeTags.WITCH_RESISTANT_TO));
    }

    public static boolean isNotReal(DamageSource source) {
        return !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY) && !source.is(DamageTypeTags.BYPASSES_EFFECTS);
    }

    public static boolean isPhysical(DamageSource source) {
        return isNotReal(source) && !isMagic(source) && !source.is(DamageTypeTags.BYPASSES_RESISTANCE);
    }
}
