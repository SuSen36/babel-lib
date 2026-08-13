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
 * 参考 Forge 的 {@code RegisterEvent} 设计，在 {@link CollectibleBuilder#register(IEventBus)} 时
 * 于 mod 事件总线上同步触发。监听方在事件回调内通过 {@link #builder()} 调用
 * {@link CollectibleBuilder#registerCollectible(String, java.util.function.Supplier)} 完成注册。
 * <p>
 * 相比在静态初始化阶段直接绑定总线，该事件将总线绑定与事件触发延后到 {@code init} 阶段，
 * 使各 Mod 的收藏品注册字段可保持 {@code public static final} 并直接在静态初始化期注册，
 * 同时保留事件机制供第三方扩展注入收藏品。
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