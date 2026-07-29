package com.susen36.babel.elemental.base;

import com.susen36.babel.BabelMod;
import com.susen36.babel.elemental.BurnInjury;
import com.susen36.babel.elemental.CorrosionInjury;
import com.susen36.babel.elemental.NecrosisInjury;
import com.susen36.babel.elemental.NervousInjury;
import com.susen36.babel.init.BabelAttributes;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.util.INBTSerializable;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public abstract class AbstractEPCapability implements INBTSerializable<CompoundTag> {

    public enum EPType {
        NERVOUS("nervous", "sanity", NervousInjury::new),
        CORROSION("corrosion", "water", CorrosionInjury::new),
        BURN("burn", "burn", BurnInjury::new),
        NECROSIS("necrosis", "dark", NecrosisInjury::new),
        EMPTY("empty", "gun_mu", null)
        ;

        private final String nickName;
        private final String descriptionID;
        private final Function<LivingEntity, AbstractEPCapability> factory;

        EPType(String nickName, String descriptionID, Function<LivingEntity, AbstractEPCapability> factory) {
            this.nickName = nickName;
            this.descriptionID = "ep_type." + BabelMod.MODID + "." + descriptionID;
            this.factory = factory;
        }

        public String getNickName() {
            return nickName;
        }

        public boolean isEmpty() {
            return this == EMPTY;
        }

        public String getDescriptionID() {
            return descriptionID;
        }

        public Component description() {
            return Component.translatable(getDescriptionID());
        }

        public AbstractEPCapability create(LivingEntity entity) {
            if (factory == null) return null;
            return factory.apply(entity);
        }

        public static EPType byNickName(String nickName) {
            nickName = nickName.toLowerCase();
            for (EPType t : values()) {
                if (t.nickName.equals(nickName)) return t;
            }
            return EMPTY;
        }

        public String asString() {
            return this.name().toLowerCase();
        }
    }

    public record HurtResult(
            boolean failed,
            boolean shouldBurst,
            HurtReason reason,
            float damage,
            float burstDamage,
            @Nullable ElementalInjurySource<?> source
    ) {
        public static HurtResult fail(float damage, ElementalInjurySource<?> src) {
            return new HurtResult(true, false, HurtReason.FAILED, damage, 0.0F, src);
        }

        public static HurtResult success(boolean burst, HurtReason why, float damage, float burstDamage, ElementalInjurySource<?> src) {
            return new HurtResult(false, burst, why, damage, burstDamage, src);
        }
    }

    protected enum HurtReason {
        LOCKED,
        RESISTED,
        THRESHOLD,
        NORMAL,
        FAILED
    }

    protected final EPType type;
    protected final LivingEntity livingEntity;
    protected float value = 0.0F;
    protected int maxReviveTick = 200;
    protected int reviveTick = 0;
    protected int immunityTick = 0;

    protected AbstractEPCapability(EPType pType, LivingEntity living) {
        this.type = pType;
        this.livingEntity = living;
    }

    public abstract void doPlayerBurst();

    public abstract void doNonPlayerBurst();

    public abstract void burstTick();

    public void doBurst() {
        if (livingEntity instanceof Player) doPlayerBurst();
        else doNonPlayerBurst();
    }

    public EPType getType() {
        return type;
    }

    public LivingEntity getLiving() {
        return livingEntity;
    }

    public float getValue() {
        return value;
    }

    public int getReviveTick() {
        return reviveTick;
    }

    public int getMaxReviveTick() {
        return maxReviveTick;
    }

    public void setMaxReviveTick(int maxReviveT) {
        this.maxReviveTick = Math.max(1, maxReviveT);
    }

    public int lockTick() {
        return maxReviveTick;
    }

    public float getReviveProcess() {
        return (float) reviveTick / (float) maxReviveTick;
    }

    public float getInjuryProgress() {
        float threshold = BabelAttributes.getImpairmentThreshold(livingEntity);
        if (threshold <= 0.0F) return 1.0F;
        return 1.0F - Mth.clamp(value / threshold, 0.0F, 1.0F);
    }

    public HurtResult hurtAndBurst(ElementalInjurySource<?> source, float amount) {
        if (isImmune(source)) {
            return HurtResult.fail(amount, source);
        }
        AttributeInstance thresholdAttr = livingEntity.getAttribute(BabelAttributes.IMPAIRMENT_THRESHOLD);
        AttributeInstance resistanceAttr = livingEntity.getAttribute(BabelAttributes.IMPAIRMENT_RESISTANCE);
        float threshold = thresholdAttr == null ? 1000.0F : (float) thresholdAttr.getValue();
        float resistance = resistanceAttr == null ? 0.0F : (float) resistanceAttr.getValue();

        float effectiveAmount = Mth.clamp(amount * (1.0F - resistance * 0.01F), 0.0F, Float.MAX_VALUE);
        if (effectiveAmount <= 0.0F) {
            return HurtResult.success(false, HurtReason.RESISTED, 0.0F, 0.0F, source);
        }
        value += effectiveAmount;

        if (shouldBurst(source)) {
            float overkill = value - threshold;
            float burstDamage = Mth.clamp(overkill * 0.1F + 6.0F, 6.0F, threshold * 0.4F);
            reviveTick = maxReviveTick;
            setImmune(maxReviveTick);
            doBurst();
            return HurtResult.success(true, HurtReason.NORMAL, effectiveAmount, burstDamage, source);
        }
        return HurtResult.success(false, HurtReason.NORMAL, effectiveAmount, 0.0F, source);
    }

    public float hurt(ElementalInjurySource<?> source, float amount) {
        return hurtAndBurst(source, amount).damage();
    }

    public void heal(ElementalInjurySource<?> source, float amount) {
        if (underBurst()) return;
        if (amount <= 0.0F) return;
        value = Mth.clamp(value - amount, 0.0F, Float.MAX_VALUE);
    }

    public void onReviveTick() {
        float threshold = BabelAttributes.getImpairmentThreshold(livingEntity);
        value = threshold * (1.0F - getReviveProcess());
        reviveTick--;
        burstTick();
    }

    public void doBesideBurst(int lockTime) {
        if (underBurst()) return;
        if (immunityTick > 0) setImmune(Math.max(immunityTick, lockTime));
        value = 0.0F;
    }

    public void tick() {
        if (underBurst()) {
            onReviveTick();
        }
        if (immunityTick > 0) immunityTick--;
    }

    public boolean isImmune(ElementalInjurySource<?> source) {
        return immunityTick != 0 || underBurst();
    }

    public void setImmune(int immuneDuration) {
        this.immunityTick = Math.max(0, immuneDuration);
    }

    public void setPermanentImmunity() {
        this.immunityTick = -1;
    }

    public boolean shouldBurst(ElementalInjurySource<?> source) {
        float threshold = BabelAttributes.getImpairmentThreshold(livingEntity);
        return !isImmune(source) && value >= threshold;
    }

    public boolean underBurst() {
        return reviveTick > 0;
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        tag.putFloat(type.getNickName() + ".injury", value);
        tag.putInt(type.getNickName() + ".maxReviveTick", maxReviveTick);
        tag.putInt(type.getNickName() + ".leftReviveTick", reviveTick);
        tag.putInt(type.getNickName() + ".immunityTick", immunityTick);
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        String prefix = type.getNickName() + ".";
        if (tag.contains(prefix + "injury")) this.value = tag.getFloat(prefix + "injury");
        if (tag.contains(prefix + "maxReviveTick")) this.maxReviveTick = tag.getInt(prefix + "maxReviveTick");
        if (tag.contains(prefix + "leftReviveTick")) this.reviveTick = tag.getInt(prefix + "leftReviveTick");
        if (tag.contains(prefix + "immunityTick")) this.immunityTick = tag.getInt(prefix + "immunityTick");
    }

    @Deprecated
    public CompoundTag saveWithoutProvider() {
        return serializeNBT(livingEntity == null ? null : livingEntity.registryAccess());
    }

    @Deprecated
    public void loadWithoutProvider(CompoundTag tag) {
        deserializeNBT(livingEntity == null ? null : livingEntity.registryAccess(), tag);
    }
}