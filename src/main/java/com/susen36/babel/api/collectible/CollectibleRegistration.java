package com.susen36.babel.api.collectible;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.NewRegistryEvent;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import static com.susen36.babel.BabelMod.MODID;

/** 在模组构造期间安装声明回调；只在 NewRegistryEvent 阶段查找实现。 */
public record CollectibleRegistration(BiFunction<String, IEventBus, CollectibleRegistrar> registrarFactory)
        implements IExtensionPoint<CollectibleRegistration> {

    public static void register(String modId, IEventBus modBus, Consumer<CollectibleRegistrar> declarations) {
        Objects.requireNonNull(modId, "modId");
        Objects.requireNonNull(declarations, "declarations");
        modBus.addListener((NewRegistryEvent event) -> {
            CollectibleRegistration registration = ModList.get().getModContainerById(MODID)
                    .flatMap(container -> container.getCustomExtension(CollectibleRegistration.class))
                    .orElseThrow(() -> new IllegalStateException("Babel collectible registration is not available for " + modId));
            declarations.accept(registration.registrarFactory.apply(modId, modBus));
        });
    }
}