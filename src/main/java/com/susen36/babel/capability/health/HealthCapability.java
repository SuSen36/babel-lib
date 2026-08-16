package com.susen36.babel.capability.health;

import com.susen36.babel.BabelConfig;
import com.susen36.babel.BabelMod;
import com.susen36.babel.network.receive.HealthSyncMessage;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.network.PacketDistributor;

public class HealthCapability implements INBTSerializable<CompoundTag> {

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(BabelMod.MODID, "health_cap");

    private final LivingEntity entity;
    private double lives;
    private double maxLives;
    private double shield;

    public HealthCapability(LivingEntity living) {
        this.entity = living;
        this.lives = BabelConfig.lifePointInit;
        this.maxLives = BabelConfig.lifePointInit;
        this.shield = 0;
    }

    public LivingEntity getEntity() {
        return entity;
    }

    // ========== Lives ==========

    public double getLives() {
        return lives;
    }

    public void setLives(double value) {
        double clamped = Math.max(1, Math.min(value, maxLives));
        if (this.lives != clamped) {
            this.lives = clamped;
        }
    }

    // ========== Max Lives ==========

    public double getMaxLives() {
        return maxLives;
    }

    public void setMaxLives(double value) {
        double clamped = Math.max(0, Math.min(value, BabelConfig.lifePointLimit));
        if (this.maxLives != clamped) {
            this.maxLives = clamped;
        }
        if (this.lives > this.maxLives) {
            this.lives = this.maxLives;
        }
    }

    // ========== Shield ==========

    public double getShield() {
        return shield;
    }

    public void setShield(double value) {
        double clamped = Math.max(0, Math.min(value, BabelConfig.shieldLimit));
        if (this.shield != clamped) {
            this.shield = clamped;
        }
    }

    // ========== Sync ==========

    public void sync() {
        if (entity != null && !entity.level().isClientSide()) {
            PacketDistributor.sendToPlayersTrackingEntityAndSelf(entity, new HealthSyncMessage(this));
        }
    }

    // ========== NBT ==========

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        tag.putDouble("lives", lives);
        tag.putDouble("maxLives", maxLives);
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
        if (nbt.contains("maxLives", Tag.TAG_ANY_NUMERIC)) {
            this.maxLives = nbt.getDouble("maxLives");
        }
        if (this.maxLives < 1.0D) {
            this.maxLives = 1.0D;
        } else if (this.maxLives > BabelConfig.lifePointLimit) {
            this.maxLives = BabelConfig.lifePointLimit;
        }
        if (this.lives > this.maxLives) {
            this.lives = this.maxLives;
        }
        if (nbt.contains("shield", Tag.TAG_ANY_NUMERIC)) {
            this.shield = nbt.getDouble("shield");
        }
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        readNBT(tag);
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