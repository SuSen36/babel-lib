package com.susen36.babel.capability.ep;

import com.mojang.datafixers.util.Pair;
import com.susen36.babel.BabelMod;
import com.susen36.babel.api.event.ElementEvent;
import com.susen36.babel.elemental.BurnInjury;
import com.susen36.babel.elemental.CorrosionInjury;
import com.susen36.babel.elemental.NecrosisInjury;
import com.susen36.babel.elemental.NervousInjury;
import com.susen36.babel.elemental.base.AbstractEPCapability;
import com.susen36.babel.elemental.base.ElementalInjurySource;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.common.util.INBTSerializable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class EPCapability implements INBTSerializable<CompoundTag> {
    private final LivingEntity entity;
    private final Map<AbstractEPCapability.EPType, AbstractEPCapability> EP_TYPES = new HashMap<>();

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(BabelMod.MODID, "ep_cap");

    public EPCapability(LivingEntity living) {
        this.entity = living;
        this.EP_TYPES.put(AbstractEPCapability.EPType.NERVOUS, new NervousInjury(living));
        this.EP_TYPES.put(AbstractEPCapability.EPType.CORROSION, new CorrosionInjury(living));
        this.EP_TYPES.put(AbstractEPCapability.EPType.BURN, new BurnInjury(living));
        this.EP_TYPES.put(AbstractEPCapability.EPType.NECROSIS, new NecrosisInjury(living));
    }

    public float getVale(AbstractEPCapability.EPType pType) {
        if (pType.isEmpty()) return -1.0F;
        AbstractEPCapability ep = EP_TYPES.get(pType);
        if (ep != null) return ep.getValue();
        return -1.0F;
    }

    public boolean hurt(AbstractEPCapability.EPType pType, ElementalInjurySource<?> pSource, float pAmount) {
        if (pType.isEmpty()) return false;
        AbstractEPCapability ep = EP_TYPES.get(pType);
        if (ep != null) {
            pAmount = ElementEvent.onElementalHurt(entity, pType, pSource, pAmount);
            if (pAmount <= 0.0F) return false;
            AbstractEPCapability.HurtResult result = ep.hurtAndBurst(pSource, pAmount);
            ElementEvent.afterElementalHurt(entity, pType, pSource, result);
            if (result.failed()) return false;
            if (result.shouldBurst()) {
                ElementEvent.onElementalBurst(entity, pType, pSource);
                doOnBurst(ep.lockTick());
            }
            return true;
        }
        return false;
    }

    public boolean hurt(AbstractEPCapability.EPType pType, float pAmount) {
        return hurt(pType, ElementalInjurySource.fromNothing(), pAmount);
    }

    public void heal(AbstractEPCapability.EPType pType, ElementalInjurySource<?> pSource, float pAmount) {
        if (pType.isEmpty()) return;
        AbstractEPCapability ep = EP_TYPES.get(pType);
        if (ep != null) {
            pAmount = ElementEvent.onElementalHeal(entity, pType, pSource, pAmount);
            if (pAmount > 0.0F) ep.heal(pSource, pAmount);
        }
    }

    public void heal(AbstractEPCapability.EPType pType, float pAmount) {
        heal(pType, ElementalInjurySource.fromNothing(), pAmount);
    }

    public void tick() {
        forEachEPType(AbstractEPCapability::tick);
    }

    public Pair<Float, AbstractEPCapability.EPType> getDisplayingProgress() {
        float leastProgress = 1.0F;
        AbstractEPCapability.EPType displayingType = AbstractEPCapability.EPType.NERVOUS;
        for (AbstractEPCapability ep : EP_TYPES.values()) {
            float p = (float) ep.getInjuryProgress();
            if (p < leastProgress) {
                leastProgress = p;
                displayingType = ep.getType();
            }
        }
        return Pair.of(leastProgress, displayingType);
    }

    private void doOnBurst(int lockTime) {
        forEachEPType(ep -> ep.doBesideBurst(lockTime));
    }

    public void forEachEPType(Consumer<AbstractEPCapability> operation) {
        for (AbstractEPCapability ep : EP_TYPES.values()) {
            operation.accept(ep);
        }
    }

    public LivingEntity getEntity() {
        return entity;
    }

    public AbstractEPCapability getEP(AbstractEPCapability.EPType type) {
        return EP_TYPES.get(type);
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        forEachEPType(ep -> tag.put("ep." + ep.getType().getNickName(), ep.serializeNBT(provider)));
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        forEachEPType(ep -> {
            String key = "ep." + ep.getType().getNickName();
            if (tag.contains(key)) {
                ep.deserializeNBT(provider, tag.getCompound(key));
            }
        });
    }

    @Deprecated
    public CompoundTag saveWithoutProvider() {
        return serializeNBT(entity == null ? null : entity.registryAccess());
    }

    @Deprecated
    public void loadWithoutProvider(CompoundTag tag) {
        deserializeNBT(entity == null ? null : entity.registryAccess(), tag);
    }
}