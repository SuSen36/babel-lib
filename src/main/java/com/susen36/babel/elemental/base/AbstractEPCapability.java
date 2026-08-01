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
        NERVOUS("sanity", "sanity", NervousInjury::new),
        CORROSION("water", "water", CorrosionInjury::new),
        BURN("fire", "fire", BurnInjury::new),
        NECROSIS("dark", "dark", NecrosisInjury::new),
        EMPTY("empty", "gun_mu", null);

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
            int damage,
            int burstDamage,
            @Nullable ElementalInjurySource<?> source
    ) {
        public static HurtResult fail(int damage, ElementalInjurySource<?> src) {
            return new HurtResult(true, false, HurtReason.FAILED, damage, 0, src);
        }

        public static HurtResult success(boolean burst, HurtReason why, int damage, int burstDamage, ElementalInjurySource<?> src) {
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
    protected int value;
    protected int maxReviveTick = 200;
    protected int reviveTick = 0;
    protected int immunityTick = 0;

    protected AbstractEPCapability(EPType pType, LivingEntity living) {
        this.type = pType;
        this.livingEntity = living;
        this.value = getMaxValue();
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

    public int getValue() {
        return value;
    }

    public int getMaxValue() {
        float threshold = BabelAttributes.getMaxElementalValue(livingEntity);
        if (threshold <= 0.0F) return 1000;
        return Mth.floor(threshold);
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
        float threshold = BabelAttributes.getMaxElementalValue(livingEntity);
        if (threshold <= 0.0F) return 0.0F;
        return Mth.clamp(value / threshold, 0.0F, 1.0F);
    }

    public HurtResult hurtAndBurst(ElementalInjurySource<?> source, int amount) {
        if (isImmune(source)) {
            return HurtResult.fail(amount, source);
        }
        AttributeInstance thresholdAttr = livingEntity.getAttribute(BabelAttributes.MAX_ELEMENTAL_VALUE);
        AttributeInstance resistanceAttr = livingEntity.getAttribute(BabelAttributes.IMPAIRMENT_RESISTANCE);
        float threshold = thresholdAttr == null ? 1000.0F : (float) thresholdAttr.getValue();
        float resistance = resistanceAttr == null ? 0.0F : (float) resistanceAttr.getValue();

        float effectiveAmountF = Mth.clamp((float) amount * (1.0F - resistance * 0.01F), 0.0F, Float.MAX_VALUE);
        int effectiveAmount = Mth.floor(effectiveAmountF);
        if (effectiveAmount <= 0) {
            return HurtResult.success(false, HurtReason.RESISTED, 0, 0, source);
        }
        int previousValue = value;
        value = Mth.clamp(value - effectiveAmount, 0, getMaxValue());

        if (shouldBurst(source)) {
            float overkill = (float) effectiveAmount - previousValue;
            int burstDamage = Mth.floor(Mth.clamp(overkill * 0.1F + 6.0F, 6.0F, threshold * 0.4F));
            reviveTick = maxReviveTick;
            setImmune(maxReviveTick);
            return HurtResult.success(true, HurtReason.NORMAL, effectiveAmount, burstDamage, source);
        }
        return HurtResult.success(false, HurtReason.NORMAL, effectiveAmount, 0, source);
    }

    public int hurt(ElementalInjurySource<?> source, int amount) {
        return hurtAndBurst(source, amount).damage();
    }

    public int hurt(int amount) {
        return hurt(ElementalInjurySource.fromNothing(), amount);
    }

    public void heal(int amount) {
        if (underBurst() || immunityTick != 0) return;
        if (amount <= 0) return;
        value = Mth.clamp(value + amount, 0, getMaxValue());
    }

    public void onReviveTick() {
        int maxValue = getMaxValue();
        reviveTick--;
        value = Mth.floor(maxValue * (maxReviveTick - reviveTick) / (float) maxReviveTick);
        burstTick();
    }

    public void doBesideBurst(int lockTime) {
        if (underBurst()) return;
        setImmune(Math.max(immunityTick, lockTime));
    }

    public void restoreValue() {
        value = getMaxValue();
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
        return !isImmune(source) && value <= 0;
    }

    public boolean underBurst() {
        return reviveTick > 0;
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        tag.putInt(type.getNickName() + ".injury", value);
        tag.putInt(type.getNickName() + ".maxReviveTick", maxReviveTick);
        tag.putInt(type.getNickName() + ".leftReviveTick", reviveTick);
        tag.putInt(type.getNickName() + ".immunityTick", immunityTick);
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        String prefix = type.getNickName() + ".";
        if (tag.contains(prefix + "injury")) this.value = Mth.clamp(tag.getInt(prefix + "injury"), 0, getMaxValue());
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