package com.susen36.babel.collectible;

import com.susen36.babel.BabelMod;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

public class CollectibleCapability implements ICapabilitySerializable<CompoundTag> {
    public static final Capability<CollectibleCapability> CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {});
    private static final ResourceLocation KEY = new ResourceLocation(BabelMod.MODID, "collectible_cap");

    private static final String STATES = "states";

    private final LazyOptional<CollectibleCapability> self = LazyOptional.of(() -> this);
    private final Collectibles collectibles;

    public CollectibleCapability(Player owner) {
        this.collectibles = new Collectibles(owner instanceof ServerPlayer serverPlayer ? serverPlayer : null);
    }

    public Collectibles getCollectibles() {
        return collectibles;
    }

    public Collectibles.Layer getLayer() {
        return collectibles.layer;
    }

    public Collectibles.Cooldown getCooldown() {
        return collectibles.cooldown;
    }

    public static ResourceLocation getKey() {
        return KEY;
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        if (cap == CAPABILITY) {
            return self.cast();
        }
        return LazyOptional.empty();
    }

    public static CollectibleCapability of(Player player) {
        return player.getCapability(CAPABILITY).orElseThrow(() ->
                new IllegalStateException("collectible capability is not attached to " + player));
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag root = new CompoundTag();
        root.put(STATES, collectibles.serializeNBT());
        return root;
    }

    @Override
    public void deserializeNBT(CompoundTag root) {
        if (root.contains(STATES)) {
            ListTag states = root.getList(STATES, Tag.TAG_COMPOUND);
            collectibles.deserializeNBT(states);
        }
    }
}