package com.susen36.babel.api.event;

import com.susen36.babel.collectible.CollectibleBuilder;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.IModBusEvent;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

/**
 * 收藏品注册事件：在收藏品注册表就绪后触发，供各 Mod（含本模组与第三方）注入收藏品。
 * <p>
 * 参考 Forge 的 {@code RegisterEvent} 设计，在 {@link CollectibleBuilder#register(IEventBus)} )} 时
 * 于 mod 事件总线上同步触发。监听方在事件回调内通过 {@link #builder()} 调用
 * {@link CollectibleBuilder#registerCollectible(String, Supplier)} 完成注册。
 * <p>
 * 相比在静态初始化阶段直接创建 builder 并绑定总线，该事件将注册延迟到 mod 总线可用之后，
 * 从而规避静态初始化顺序冲突（DeferredRegister 需要 IEventBus，而静态字段初始化时尚未获得）。
 */
public class CollectibleRegisterEvent extends Event implements IModBusEvent {

    private final CollectibleBuilder builder;

    public CollectibleRegisterEvent(@NotNull CollectibleBuilder builder) {
        this.builder = builder;
    }

    /** 供注册使用的收藏品构建器。 */
    public @NotNull CollectibleBuilder builder() {
        return builder;
    }
}