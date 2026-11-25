package com.susen36.babel.collectible;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.Nullable;

/**
 * 收藏品 Capability 提供者：懒创建 {@link Collectibles} 数据并负责 NBT 持久化。
 * <p>
 * 调用方应通过 {@code Collectibles} 的静态方法读写，不要直接持有该类型。
 * 不注册失效监听：{@code PlayerEvent.Clone} 依赖 {@code reviveCaps()} 读取旧数据，
 * LazyOptional 一旦失效无法恢复。
 */
final class CollectibleCapability implements ICapabilitySerializable<CompoundTag> {
    private static final String STATES = "states";

    private final @Nullable ServerPlayer serverPlayer;
    private @Nullable Collectibles collectibles;

    CollectibleCapability(Player owner) {
        this.serverPlayer = owner instanceof ServerPlayer player ? player : null;
    }

    private Collectibles getOrCreate() {
        Collectibles data = collectibles;
        if (data == null) {
            data = new Collectibles(serverPlayer);
            collectibles = data;
        }
        return data;
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        if (cap == Collectibles.CAPABILITY) {
            return LazyOptional.of(this::getOrCreate).cast();
        }
        return LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag root = new CompoundTag();
        root.put(STATES, getOrCreate().serializeNBT());
        return root;
    }

    @Override
    public void deserializeNBT(CompoundTag root) {
        if (root.contains(STATES, Tag.TAG_LIST)) {
            ListTag states = root.getList(STATES, Tag.TAG_COMPOUND);
            getOrCreate().deserializeNBT(states);
        }
    }
}