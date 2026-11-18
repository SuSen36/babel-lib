package com.susen36.babel.capability.health;

import com.susen36.babel.BabelConfig;
import com.susen36.babel.BabelMod;
import com.susen36.babel.init.BabelAttributes;
import com.susen36.babel.network.BabelNetwork;
import com.susen36.babel.network.receive.LivesSyncMessage;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraftforge.common.util.INBTSerializable;

import javax.annotation.Nullable;

// 目标生命与护盾的存储与同步：上限取自 MAX_LIFE_POINT 属性并受全局上限配置限制。
// 修改数值后需调用 sync 才会同步到客户端。
public class HealthCapability implements INBTSerializable<CompoundTag> {

    public static final ResourceLocation ID = new ResourceLocation(BabelMod.MODID, "health_cap");

    @Nullable
    private final LivingEntity entity;
    private double lives;
    private double shield;

    public HealthCapability(@Nullable LivingEntity living) {
        this.entity = living;
        this.lives = BabelConfig.lifePointInit;
        this.shield = 0;
    }

    @Nullable
    public LivingEntity getEntity() {
        return entity;
    }

    // ========== Lives ==========

    public double getLives() {
        return lives;
    }

    // 设置当前值，夹取到 [1, getMaxLives()]
    public void setLives(double value) {
        double clamped = Math.max(1, Math.min(value, getMaxLives()));
        if (this.lives != clamped) {
            this.lives = clamped;
        }
    }

    // ========== Max Lives ==========

    // 返回目标生命上限，取自 MAX_LIFE_POINT 属性并受全局上限配置限制；属性缺失时返回初始值
    public double getMaxLives() {
        AttributeInstance instance = getAttributeInstance();
        if (instance != null) {
            return Math.max(1.0D, Math.min(instance.getValue(), BabelConfig.lifePointLimit));
        }
        return BabelConfig.lifePointInit;
    }

    // 设置上限值，写入属性基础值前夹取到 [1, 全局上限]
    public void setMaxLives(double value) {
        AttributeInstance instance = getAttributeInstance();
        if (instance != null) {
            instance.setBaseValue(Math.max(1.0D, Math.min(value, BabelConfig.lifePointLimit)));
        }
    }

    @Nullable
    private AttributeInstance getAttributeInstance() {
        return entity == null ? null : entity.getAttribute(BabelAttributes.MAX_LIFE_POINT.get());
    }

    // ========== Shield ==========

    public double getShield() {
        return shield;
    }

    // 设置护盾值，夹取到 [0, 全局护盾上限]
    public void setShield(double value) {
        double clamped = Math.max(0, Math.min(value, BabelConfig.shieldLimit));
        if (this.shield != clamped) {
            this.shield = clamped;
        }
    }

    // ========== Sync ==========

    // 向追踪该实体的客户端与自身发送同步包，仅服务端调用有效
    public void sync() {
        if (entity != null && !entity.level().isClientSide) {
            BabelNetwork.sendToTrackingAndSelf(entity, new LivesSyncMessage(entity.getId(), serializeNBT()));
        }
    }

    // ========== NBT ==========

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putDouble("lives", lives);
        tag.putDouble("shield", shield);
        return tag;
    }

    public void readNBT(Tag tag) {
        CompoundTag nbt = (CompoundTag) tag;
        if (nbt.contains("lives", Tag.TAG_ANY_NUMERIC)) {
            this.lives = nbt.getDouble("lives");
        }
        if (this.lives < 1.0D) {
            this.lives = 1.0D;
        }
        // 兼容旧存档：旧版本 maxLives 存在 capability NBT 中，加载时迁移到属性基值
        if (nbt.contains("maxLives", Tag.TAG_ANY_NUMERIC)) {
            setMaxLives(nbt.getDouble("maxLives"));
        }
        if (this.lives > getMaxLives()) {
            this.lives = getMaxLives();
        }
        if (nbt.contains("shield", Tag.TAG_ANY_NUMERIC)) {
            this.shield = nbt.getDouble("shield");
        }
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        readNBT(tag);
    }
}