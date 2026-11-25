package com.susen36.babel.collectible;

import com.susen36.babel.capability.EntityCapabilityHandle;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

/**
 * 收藏品 Capability 提供者：懒创建 {@link Collectibles} 数据并负责 NBT 持久化。
 * <p>
 * 调用方应通过 {@code Collectibles} 的静态方法读写，不要直接持有该类型。
 */
final class CollectibleCapability implements ICapabilitySerializable<CompoundTag> {
    private static final String STATES = "states";

    private final EntityCapabilityHandle<Collectibles> data;

    CollectibleCapability(Player owner) {
        ServerPlayer serverPlayer = owner instanceof ServerPlayer player ? player : null;
        this.data = new EntityCapabilityHandle<>(Collectibles.CAPABILITY, () -> new Collectibles(serverPlayer));
    }

    void invalidate() {
        data.invalidate();
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        return data.getCapability(cap, side);
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag root = new CompoundTag();
        root.put(STATES, data.get().serializeNBT());
        return root;
    }

    @Override
    public void deserializeNBT(CompoundTag root) {
        if (root.contains(STATES, Tag.TAG_LIST)) {
            ListTag states = root.getList(STATES, Tag.TAG_COMPOUND);
            data.get().deserializeNBT(states);
        }
    }
}