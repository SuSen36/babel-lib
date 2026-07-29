package com.susen36.babel.event;

import com.susen36.babel.BabelMod;
import com.susen36.babel.api.event.ResistDamageEvent;
import com.susen36.babel.capability.BabelCapability;
import com.susen36.babel.capability.ep.EPCapability;
import com.susen36.babel.elemental.base.AbstractEPCapability;
import com.susen36.babel.elemental.base.ElementalInjurySource;
import com.susen36.babel.init.BabelAttributes;
import com.susen36.babel.network.BabelNetwork;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import java.util.Map;
import java.util.WeakHashMap;

@EventBusSubscriber(modid = BabelMod.MODID)
public class BabelLivingEvents {

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
        ep.tick();
        if (living instanceof Player && living.tickCount % 40 == 0) {
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

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingDamagePreLowest(LivingDamageEvent.Pre event) {
        LivingEntity entity = event.getEntity();
        Entity sourceEntity = event.getSource().getEntity();
        if (sourceEntity instanceof LivingEntity attacker) {
            float amount = event.getNewDamage();
            if (amount <= 0) return;
            DamageSource damageSource = event.getSource();
            dealAdditionalInjury(entity, attacker, damageSource, BabelAttributes.NERVOUS_RATE, AbstractEPCapability.EPType.NERVOUS, amount);
            dealAdditionalInjury(entity, attacker, damageSource, BabelAttributes.CORROSION_RATE, AbstractEPCapability.EPType.CORROSION, amount);
            dealAdditionalInjury(entity, attacker, damageSource, BabelAttributes.BURN_RATE, AbstractEPCapability.EPType.BURN, amount);
            dealAdditionalInjury(entity, attacker, damageSource, BabelAttributes.NECROSIS_RATE, AbstractEPCapability.EPType.NECROSIS, amount);
        }
    }

    public static void dealAdditionalInjury(LivingEntity victim, LivingEntity attacker, DamageSource source,
                                             Holder<Attribute> attribute, AbstractEPCapability.EPType type, float amount) {
        AttributeInstance instance = attacker.getAttribute(attribute);
        if (instance != null) {
            float injuryAmount = (float) (amount * instance.getValue());
            if (injuryAmount > 0) {
                EPCapability ep = BabelCapability.getEP(victim);
                ep.hurt(type, ElementalInjurySource.fromDamageSource(source), injuryAmount);
            }
        }
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
