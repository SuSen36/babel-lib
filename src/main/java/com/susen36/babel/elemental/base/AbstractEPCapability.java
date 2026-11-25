package com.susen36.babel.elemental.base;

import com.susen36.babel.BabelMod;
import com.susen36.babel.api.elemental.ElementType;
import com.susen36.babel.api.elemental.HurtResult;
import com.susen36.babel.capability.BabelCapability;
import com.susen36.babel.capability.ep.EPCapability;
import com.susen36.babel.elemental.*;
import com.susen36.babel.init.BabelAttributes;
import com.susen36.babel.init.BabelDamageTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.util.INBTSerializable;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.function.Function;

// 单类元素损伤的抽象实现：维护当前剩余值、上限、爆条与免疫状态及 NBT 序列化，
// 子类分别实现玩家与非玩家两条爆条结算。EPType 定义五类损伤与空占位类型。
public abstract class AbstractEPCapability implements INBTSerializable<CompoundTag> {

    public enum EPType {
        NERVOUS(ElementType.NERVOUS, "nervous", "nervous", "sanity", NervousInjury::new),
        CORROSION(ElementType.CORROSION, "corrosion", "corrosion", "water", CorrosionInjury::new),
        BURN(ElementType.BURN, "burn", "burn", "fire", BurnInjury::new),
        NECROSIS(ElementType.NECROSIS, "necrosis", "necrosis", "dark", NecrosisInjury::new),
        FRENZY(ElementType.FRENZY, "frenzy", "frenzy", "anger", FrenzyInjury::new),
        EMPTY(ElementType.EMPTY, "empty", "empty", "empty", null);

        private final ElementType apiType;
        private final String nickName;
        private final String descriptionID;
        private final String textureName;
        private final @Nullable Function<LivingEntity, AbstractEPCapability> factory;

        EPType(ElementType apiType, String nickName, String descriptionID, String textureName, @Nullable Function<LivingEntity, AbstractEPCapability> factory) {
            this.apiType = apiType;
            this.nickName = nickName;
            this.descriptionID = "ep_type." + BabelMod.MODID + "." + descriptionID;
            this.textureName = textureName;
            this.factory = factory;
        }

        public ElementType apiType() {
            return apiType;
        }

        public static EPType fromApi(ElementType type) {
            return switch (type) {
                case NERVOUS -> NERVOUS;
                case CORROSION -> CORROSION;
                case BURN -> BURN;
                case NECROSIS -> NECROSIS;
                case FRENZY -> FRENZY;
                case EMPTY -> EMPTY;
            };
        }

        public String getNickName() {
            return nickName;
        }

        public String getTextureName() {
            return textureName;
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

        public @Nullable AbstractEPCapability create(LivingEntity entity) {
            if (factory == null) return null;
            return factory.apply(entity);
        }

        public static EPType byNickName(String nickName) {
            nickName = nickName.toLowerCase(Locale.ROOT);
            for (EPType t : values()) {
                if (t.nickName.equals(nickName)) return t;
            }
            return EMPTY;
        }

        public String asString() {
            return this.name().toLowerCase(Locale.ROOT);
        }
    }

    private static final int REFERENCE_BURST_DAMAGE = 6;
    private float burstDamageScale = 1.0F;
    protected final EPType type;
    protected final LivingEntity livingEntity;
    protected int value;
    protected int maxReviveTick = 200;
    protected int reviveTick = 0;
    protected int immunityTick = 0;
    protected boolean dirty = true;

    protected AbstractEPCapability(EPType pType, LivingEntity living) {
        this.type = pType;
        this.livingEntity = living;
        this.value = getMaxValue();
    }

    public abstract void doPlayerBurst();

    public abstract void doNonPlayerBurst();

    public abstract void doPlayerBurstTick();

    public abstract void doNonPlayerBurstTick();

    public final void burstTick(){
        if (livingEntity instanceof Player) {
            doPlayerBurstTick();
        }
        else doNonPlayerBurstTick();
    }

    public final void doBurst() {
        doBurst(REFERENCE_BURST_DAMAGE, 1.0F);
    }

    public final void doBurst(int baseDamage, float damageMultiplier) {
        if (!Float.isFinite(damageMultiplier) || damageMultiplier < 0.0F) {
            throw new IllegalArgumentException("Burst damage multiplier must be finite and non-negative");
        }
        // 基数 6 对齐历史档位；限制溢出的自然增幅，不限制监听器有意设置的倍率。
        float naturalScale = Mth.clamp(1.0F + (baseDamage - (float) REFERENCE_BURST_DAMAGE) * 0.02F, 0.8F, 1.2F);
        burstDamageScale = (float) Math.min((double) naturalScale * damageMultiplier, Float.MAX_VALUE);
        dirty = true;
        EPCapability ep = BabelCapability.getEP(livingEntity);
        if (ep != null) {
            ep.setUnderBreak(maxReviveTick);
        }
        if (livingEntity instanceof Player) {
            doPlayerBurst();
        }
        else doNonPlayerBurst();
    }

    protected final void applyBurstDamage(float referenceDamage) {
        float damage = (float) Math.min((double) referenceDamage * burstDamageScale, Float.MAX_VALUE);
        if (damage > 0.0F && !livingEntity.level().isClientSide) {
            livingEntity.hurt(BabelDamageTypes.source(livingEntity.level(), BabelDamageTypes.ELEMENT_BREAK), damage);
        }
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

    public boolean consumeDirty() {
        boolean wasDirty = dirty;
        dirty = false;
        return wasDirty;
    }

    public boolean isDirty() {
        return dirty;
    }

    public int getMaxValue() {
        float threshold = BabelAttributes.getMaxElementalValue(livingEntity);
        if (threshold <= 0.0F) return 50;
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
        this.dirty = true;
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
            return new HurtResult(true, false, HurtResult.Reason.FAILED, amount, 0, source);
        }
        AttributeInstance thresholdAttr = livingEntity.getAttribute(BabelAttributes.MAX_ELEMENTAL_VALUE.get());
        AttributeInstance resistanceAttr = livingEntity.getAttribute(BabelAttributes.ELEMENTAL_RESISTANCE.get());
        float threshold = thresholdAttr == null ? 50.0F : (float) thresholdAttr.getValue();
        float resistance = resistanceAttr == null ? 0.0F : (float) resistanceAttr.getValue();

        float effectiveAmountF = Mth.clamp((float) amount * (1.0F - resistance * 0.01F), 0.0F, Float.MAX_VALUE);
        int effectiveAmount = Mth.floor(effectiveAmountF);
        if (effectiveAmount <= 0) {
            return new HurtResult(false, false, HurtResult.Reason.RESISTED, 0, 0, source);
        }
        int previousValue = value;
        value = Mth.clamp(value - effectiveAmount, 0, getMaxValue());
        if (value != previousValue) {
            this.dirty = true;
        }

        if (shouldBurst(source)) {
            float overkill = (float) effectiveAmount - previousValue;
            int burstDamage = Mth.floor(Mth.clamp(overkill * 0.1F + REFERENCE_BURST_DAMAGE, REFERENCE_BURST_DAMAGE, threshold * 0.4F));
            reviveTick = maxReviveTick;
            setImmune(maxReviveTick);
            return new HurtResult(false, true, HurtResult.Reason.NORMAL, effectiveAmount, burstDamage, source);
        }
        return new HurtResult(false, false, HurtResult.Reason.NORMAL, effectiveAmount, 0, source);
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
        int previous = value;
        value = Mth.clamp(value + amount, 0, getMaxValue());
        if (value != previous) {
            this.dirty = true;
        }
    }

    public void onReviveTick() {
        int maxValue = getMaxValue();
        reviveTick--;
        int elapsed = Mth.clamp(maxReviveTick - reviveTick, 0, maxReviveTick);
        value = (int) ((long) maxValue * elapsed / maxReviveTick);
        this.dirty = true;
        burstTick();
    }

    public void doBesideBurst(int lockTime) {
        if (!underBurst() && immunityTick != -1) {
            setImmune(Math.max(immunityTick, lockTime));
        }
    }

    public void restoreValue() {
        value = getMaxValue();
        this.dirty = true;
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
        int previous = this.immunityTick;
        this.immunityTick = Math.max(0, immuneDuration);
        if (this.immunityTick != previous) {
            this.dirty = true;
        }
    }

    public void setPermanentImmunity() {
        this.immunityTick = -1;
        this.dirty = true;
    }

    public boolean shouldBurst(ElementalInjurySource<?> source) {
        return !isImmune(source) && value <= 0;
    }

    public boolean underBurst() {
        return reviveTick > 0;
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putInt(type.getNickName() + ".injury", value);
        tag.putInt(type.getNickName() + ".maxReviveTick", maxReviveTick);
        tag.putInt(type.getNickName() + ".leftReviveTick", reviveTick);
        tag.putInt(type.getNickName() + ".immunityTick", immunityTick);
        if (underBurst() && burstDamageScale != 1.0F) {
            tag.putFloat("burstDamageScale", burstDamageScale);
        }
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        float scale = tag.contains("burstDamageScale", Tag.TAG_FLOAT) ? tag.getFloat("burstDamageScale") : 1.0F;
        burstDamageScale = Float.isFinite(scale) && scale >= 0.0F ? scale : 1.0F;
        String prefix = type.getNickName() + ".";
        if (tag.contains(prefix + "injury")) this.value = Mth.clamp(tag.getInt(prefix + "injury"), 0, getMaxValue());
        if (tag.contains(prefix + "maxReviveTick")) this.maxReviveTick = Math.max(1, tag.getInt(prefix + "maxReviveTick"));
        if (tag.contains(prefix + "leftReviveTick")) this.reviveTick = tag.getInt(prefix + "leftReviveTick");
        this.reviveTick = Mth.clamp(this.reviveTick, 0, this.maxReviveTick);
        if (tag.contains(prefix + "immunityTick")) this.immunityTick = tag.getInt(prefix + "immunityTick");
    }
}