package com.susen36.babel.init;

import com.susen36.babel.BabelMod;
import com.susen36.babel.menu.CannotMenu;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.network.IContainerFactory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class BabelMenus {
    public static final DeferredRegister<MenuType<?>> REGISTRY = DeferredRegister.create(ForgeRegistries.MENU_TYPES, BabelMod.MODID);

    public static final RegistryObject<MenuType<CannotMenu>> CANNOT = REGISTRY.register("cannot",
            () -> new MenuType<>((IContainerFactory<CannotMenu>) (windowId, inventory, buffer) -> new CannotMenu(windowId, inventory, buffer), FeatureFlags.VANILLA_SET));

    private BabelMenus() {
        throw new UnsupportedOperationException("Utility class");
    }
}