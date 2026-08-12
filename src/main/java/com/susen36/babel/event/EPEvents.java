package com.susen36.babel.event;

import com.susen36.babel.BabelMod;
import com.susen36.babel.manager.EPManager;
import com.susen36.babel.api.event.ResistDamageEvent;
import com.susen36.babel.capability.BabelCapability;
import com.susen36.babel.capability.ep.EPCapability;
import com.susen36.babel.elemental.base.AbstractEPCapability;
import com.susen36.babel.effect.PalsyMobEffect;
import com.susen36.babel.init.BabelAttributes;
import com.susen36.babel.init.BabelMobEffects;
import com.susen36.babel.network.BabelNetwork;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import java.util.Map;
import java.util.WeakHashMap;

@EventBusSubscriber(modid = BabelMod.MODID)
public class EPEvents {

    private static final Map<LivingEntity, CacheEntry> EP_CACHE = new WeakHashMap<>();
    private static final int CACHE_TTL = 60;

    private record CacheEntry(EPCapability ep, long expireAt) {}

    private static EPCapability cachedEP(LivingEntity living) {
        long tick = living.level().getGameTime();
        CacheEntry entry = EP_CACHE.get(living);
        if (entry != null && tick < entry.expireAt) return entry.ep;
        EPCapability ep = BabelCapability.getEP(living);
        EP_CACHE.put(living, new CacheEntry(ep, tick + CACHE_TTL));
        return ep;
    }

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof LivingEntity living)) return;
        if (living.level().isClientSide()) return;
        EPCapability ep = cachedEP(living);
        boolean burstJustEnded = ep.tick();
        int syncInterval = living instanceof Player ? 10 : 20;
        if (burstJustEnded || living.tickCount % syncInterval == 0) {
            BabelNetwork.syncEP(living, ep);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onLivingDamagePreHigh(LivingDamageEvent.Pre event) {
        LivingEntity entity = event.getEntity();
        DamageSource source = event.getSource();
        if (isNotReal(source)) {
            float originalAmount = event.getNewDamage();
            float amount = originalAmount;
            if (isMagic(source)) {
                AttributeInstance magic = entity.getAttribute(BabelAttributes.MAGIC_RESISTANCE);
                if (magic != null) {
                    double v = magic.getValue();
                    v = ResistDamageEvent.modifyEffectiveResistanceValue(entity, source, amount, v);
                    if (v > 0) {
                        amount *= (float) ((100.0 - Mth.clamp(v, 0.0, 100.0)) * 0.01);
                    }
                }
            } else if (isPhysical(source)) {
                AttributeInstance defense = entity.getAttribute(BabelAttributes.DEFENSE);
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
                event.setNewDamage(amount);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void handleElementalAttackDamage(LivingDamageEvent.Pre event) {
        LivingEntity target = event.getEntity();
        Entity sourceEntity = event.getSource().getEntity();
        LivingEntity attacker = sourceEntity instanceof Projectile projectile && projectile.getOwner() instanceof LivingEntity owner
                ? owner
                : sourceEntity instanceof LivingEntity living ? living : null;

        if (attacker == null) return;

        EPManager.ElementalAttackConfig attackConfig = EPManager.getElementalAttackConfig(attacker);
        AbstractEPCapability.EPType epType = attackConfig.type();
        double rate = attackConfig.rate();
        double injuryDamage = attackConfig.injuryDamage();
        EPManager.ElementalDefenseConfig defenseConfig = EPManager.getElementalDefenseConfig(target);
        if (epType == AbstractEPCapability.EPType.NERVOUS && defenseConfig.type() == epType) {
            rate *= defenseConfig.baseModifier() * defenseConfig.totalModifier();
            injuryDamage *= defenseConfig.baseModifier() * defenseConfig.totalModifier();
        }
        double elementalDamage = injuryDamage + event.getNewDamage() * rate;

        if (elementalDamage > 0 && !EPManager.hurtElemental(target, epType, attacker, Mth.floor(elementalDamage))) return;
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onWitherDamageCancel(LivingDamageEvent.Pre event) {
        LivingEntity entity = event.getEntity();
        DamageSource source = event.getSource();
        if (!source.is(DamageTypes.WITHER)) return;
        if (entity.level().isClientSide()) return;
        EPCapability ep = cachedEP(entity);
        AbstractEPCapability necrosis = ep.getEP(AbstractEPCapability.EPType.NECROSIS);
        if (necrosis == null || !necrosis.underBurst()) return;
        event.setNewDamage(0);
    }

    @SubscribeEvent
    public static void onPalsyingCancel(LivingIncomingDamageEvent event) {
        if (event.isCanceled()) return;
        LivingEntity target = event.getEntity();
        if (target.level().isClientSide()) return;
        Entity sourceEntity = event.getSource().getEntity();
        if (!(sourceEntity instanceof LivingEntity attacker)) return;
        if (!attacker.hasEffect(BabelMobEffects.PALSY)) return;
        PalsyMobEffect.onAttackBlocked(attacker, target);
        event.setCanceled(true);
    }

    public static final TagKey<DamageType> FORGE_MAGIC = TagKey.create(
            Registries.DAMAGE_TYPE, ResourceLocation.fromNamespaceAndPath("forge", "is_magic"));

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
