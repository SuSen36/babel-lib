package com.susen36.babel.collectible;

import com.susen36.babel.BabelMod;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
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

    private static final String USED = "used";
    private static final String LAYER = "layer";
    private static final String COOLDOWN = "cooldown";

    private final LazyOptional<CollectibleCapability> self = LazyOptional.of(() -> this);
    private final Collectibles used;
    private final Collectibles.Layer layer;
    private final Collectibles.Cooldown cooldown;

    public CollectibleCapability(Player owner) {
        this.used = new Collectibles();
        this.layer = new Collectibles.Layer();
        this.cooldown = new Collectibles.Cooldown(owner instanceof ServerPlayer serverPlayer ? serverPlayer : null);
    }

    public Collectibles getUsed() {
        return used;
    }

    public Collectibles.Layer getLayer() {
        return layer;
    }

    public Collectibles.Cooldown getCooldown() {
        return cooldown;
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

    public void invalidate() {
        self.invalidate();
    }

    public static CollectibleCapability of(Player player) {
        return player.getCapability(CAPABILITY).orElseThrow(() ->
                new IllegalStateException("collectible capability is not attached to " + player));
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag root = new CompoundTag();
        root.put(USED, used.serializeNBT());
        root.put(LAYER, layer.serializeNBT());
        root.put(COOLDOWN, cooldown.serializeNBT());
        return root;
    }

    @Override
    public void deserializeNBT(CompoundTag root) {
        if (root.contains(USED)) {
            used.deserializeNBT(root.getList(USED, Tag.TAG_STRING));
        }
        if (root.contains(LAYER)) {
            layer.deserializeNBT(root.getCompound(LAYER));
        }
        if (root.contains(COOLDOWN)) {
            cooldown.deserializeNBT(root.getCompound(COOLDOWN));
        }
    }
}