package com.susen36.babel.robbery;

import com.susen36.babel.BabelMod;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class RobberyCapability implements ICapabilitySerializable<CompoundTag> {
    public static final Capability<RobberyCapability> CAPABILITY = CapabilityManager.get(new CapabilityToken<RobberyCapability>() {});
    public static final ResourceLocation KEY = new ResourceLocation(BabelMod.MODID, "robbery_cap");
    private static final String RESTRICTED_UNTIL_TICK = "RestrictedUntilTick";
    private static final long RESTRICTION_DURATION_TICKS = 3L * 60L * 60L * 20L;

    private final LazyOptional<RobberyCapability> self = LazyOptional.of(() -> this);
    private long restrictedUntilTick;

    public static RobberyCapability of(Player player) {
        return player.getCapability(CAPABILITY).orElseThrow(() ->
                new IllegalStateException("robbery capability is not attached to " + player));
    }

    public boolean isRestricted(long currentTick) {
        return currentTick < this.restrictedUntilTick;
    }

    public void startRestriction(long currentTick) {
        this.restrictedUntilTick = currentTick + RESTRICTION_DURATION_TICKS;
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        return cap == CAPABILITY ? this.self.cast() : LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putLong(RESTRICTED_UNTIL_TICK, this.restrictedUntilTick);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        if (tag.contains(RESTRICTED_UNTIL_TICK, Tag.TAG_LONG)) {
            this.restrictedUntilTick = tag.getLong(RESTRICTED_UNTIL_TICK);
        }
    }
}