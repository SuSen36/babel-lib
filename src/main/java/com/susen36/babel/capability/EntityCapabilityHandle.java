package com.susen36.babel.capability;

import com.google.common.base.Suppliers;
import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Supplier;

/** 数据归实体所有；卸载只作废对外句柄，Forge reviveCaps 后重新查询可访问同一份数据。 */
public final class EntityCapabilityHandle<T> implements ICapabilityProvider {
    private final Capability<T> type;
    private final Supplier<T> data;
    @Nullable
    private LazyOptional<T> handle;

    public EntityCapabilityHandle(Capability<T> type, Supplier<T> factory) {
        this.type = type;
        this.data = Suppliers.memoize(factory::get);
    }

    public T get() {
        return data.get();
    }

    @Nonnull
    @Override
    public <R> LazyOptional<R> getCapability(@Nonnull Capability<R> cap, @Nullable Direction side) {
        if (cap == type) {
            if (handle == null) {
                handle = LazyOptional.of(data::get);
            }
            return handle.cast();
        }
        return LazyOptional.empty();
    }

    public void invalidate() {
        LazyOptional<T> previous = handle;
        if (previous != null) {
            handle = null;
            previous.invalidate();
        }
    }
}