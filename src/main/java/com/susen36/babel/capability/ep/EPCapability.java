package com.susen36.babel.capability.ep;

import com.mojang.datafixers.util.Pair;
import com.susen36.babel.BabelMod;
import com.susen36.babel.api.event.ElementEvent;
import com.susen36.babel.elemental.BurnInjury;
import com.susen36.babel.elemental.CorrosionInjury;
import com.susen36.babel.elemental.FrenzyInjury;
import com.susen36.babel.elemental.NecrosisInjury;
import com.susen36.babel.elemental.NervousInjury;
import com.susen36.babel.elemental.base.AbstractEPCapability;
import com.susen36.babel.elemental.base.ElementalInjurySource;
import com.susen36.babel.network.receive.EPSyncMessage;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class EPCapability implements INBTSerializable<CompoundTag> {
    private final LivingEntity entity;
    private final Map<AbstractEPCapability.EPType, AbstractEPCapability> EP_TYPES = new HashMap<>();
    private int underBreakTick;
    private boolean underBreakDirty;

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(BabelMod.MODID, "ep_cap");

    public EPCapability(LivingEntity living) {
        this.entity = living;
        this.EP_TYPES.put(AbstractEPCapability.EPType.NERVOUS, new NervousInjury(living));
        this.EP_TYPES.put(AbstractEPCapability.EPType.CORROSION, new CorrosionInjury(living));
        this.EP_TYPES.put(AbstractEPCapability.EPType.BURN, new BurnInjury(living));
        this.EP_TYPES.put(AbstractEPCapability.EPType.NECROSIS, new NecrosisInjury(living));
        this.EP_TYPES.put(AbstractEPCapability.EPType.FRENZY, new FrenzyInjury(living));
    }

    private boolean isEntityProtected() {
        if (entity instanceof Player player) {
            if (player.isCreative() || player.isSpectator()) {
                return true;
            }
        }
        return entity != null && entity.isInvulnerable();
    }

    public void setUnderBreak(int ticks) {
        if (isEntityProtected()) return;
        if (ticks < 0) ticks = 0;
        if (this.underBreakTick != ticks) {
            this.underBreakTick = ticks;
            this.underBreakDirty = true;
            if (entity != null && !entity.level().isClientSide()) {
                this.syncDirty();
            }
        }
    }

    public boolean isUnderBreak() {
        return underBreakTick > 0;
    }

    public int getValue(AbstractEPCapability.EPType pType) {
        if (pType.isEmpty()) return -1;
        AbstractEPCapability ep = EP_TYPES.get(pType);
        if (ep != null) return ep.getValue();
        return -1;
    }

    public boolean hurt(AbstractEPCapability.EPType pType, ElementalInjurySource<?> pSource, int pAmount) {
        return hurt(pType, pSource, pAmount, ElementEvent.HurtType.DEFAULT, null);
    }

    public boolean hurt(AbstractEPCapability.EPType pType, ElementalInjurySource<?> pSource, int pAmount, ElementEvent.HurtType hurtType, LivingEntity attacker) {
        if (pType.isEmpty()) return false;
        if (isEntityProtected()) return false;
        AbstractEPCapability ep = EP_TYPES.get(pType);
        if (ep != null) {
            pAmount = ElementEvent.onElementalHurt(entity, pType, pSource, pAmount, hurtType, attacker);
            if (pAmount <= 0) return false;
            AbstractEPCapability.HurtResult result = ep.hurtAndBurst(pSource, pAmount);
            ElementEvent.afterElementalHurt(entity, pType, pSource, result);
            if (!result.failed()) {
                if (result.shouldBurst()) {
                    ElementEvent.onElementalBurst(entity, pType, pSource);
                    doOnBurst();
                    ep.doBurst();
                }
                if (!entity.level().isClientSide()) {
                    this.syncDirty();
                }
                return true;
            }
        }
        return false;
    }

    public boolean hurt(AbstractEPCapability.EPType pType, int pAmount) {
        return hurt(pType, ElementalInjurySource.fromNothing(), pAmount);
    }

    public boolean hurt(AbstractEPCapability.EPType pType, int pAmount, ElementEvent.HurtType hurtType, LivingEntity attacker) {
        return hurt(pType, ElementalInjurySource.fromNothing(), pAmount, hurtType, attacker);
    }

    public void heal(AbstractEPCapability.EPType pType, int pAmount) {
        if (pType.isEmpty()) return;
        AbstractEPCapability ep = EP_TYPES.get(pType);
        if (ep != null) {
            pAmount = ElementEvent.onElementalHeal(entity, pType, ElementalInjurySource.fromNothing(), pAmount);
            if (pAmount > 0) {
                ep.heal(pAmount);
                if (!entity.level().isClientSide()) {
                    this.syncDirty();
                }
            }
        }
    }

    public boolean tick() {
        boolean burstJustEnded = false;
        for (AbstractEPCapability ep : EP_TYPES.values()) {
            boolean wasUnderBurst = ep.underBurst();
            ep.tick();
            if (wasUnderBurst && !ep.underBurst()) {
                burstJustEnded = true;
            }
        }
        if (burstJustEnded) {
            forEachEPType(AbstractEPCapability::restoreValue);
        }
        if (underBreakTick > 0) {
            underBreakTick--;
            underBreakDirty = true;
        }
        return burstJustEnded;
    }

    public AbstractEPCapability getCurrentElement() {
        int leastValue = Integer.MAX_VALUE;
        AbstractEPCapability currentElement = null;
        for (AbstractEPCapability.EPType type : AbstractEPCapability.EPType.values()) {
            if (type.isEmpty()) continue;
            AbstractEPCapability ep = EP_TYPES.get(type);
            if (ep == null || ep.getValue() >= leastValue) continue;
            leastValue = ep.getValue();
            currentElement = ep;
        }
        return currentElement;
    }

    public Pair<Float, AbstractEPCapability.EPType> getDisplayingProgress() {
        AbstractEPCapability currentElement = getCurrentElement();
        return currentElement == null
                ? Pair.of(1.0F, AbstractEPCapability.EPType.NERVOUS)
                : Pair.of(currentElement.getInjuryProgress(), currentElement.getType());
    }

    private void doOnBurst() {
        forEachEPType(ep -> ep.doBesideBurst(ep.lockTick()));
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
        tag.putInt("underBreakTick", underBreakTick);
        return tag;
    }

    public CompoundTag serializeSyncNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        forEachEPType(ep -> {
            if (ep.consumeDirty()) {
                tag.put("ep." + ep.getType().getNickName(), ep.serializeNBT(provider));
            }
        });
        if (underBreakDirty) {
            underBreakDirty = false;
            tag.putInt("underBreakTick", underBreakTick);
        }
        return tag;
    }

    public void sync() {
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(entity, new EPSyncMessage(this));
    }

    public void syncDirty() {
        CompoundTag nbt = serializeSyncNBT(entity.registryAccess());
        if (!nbt.isEmpty()) {
            PacketDistributor.sendToPlayersTrackingEntityAndSelf(entity, new EPSyncMessage(entity.getId(), nbt));
        }
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        forEachEPType(ep -> {
            String key = "ep." + ep.getType().getNickName();
            if (tag.contains(key)) {
                ep.deserializeNBT(provider, tag.getCompound(key));
            }
        });
        if (tag.contains("underBreakTick")) {
            underBreakTick = tag.getInt("underBreakTick");
        }
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