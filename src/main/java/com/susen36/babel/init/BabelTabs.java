package com.susen36.babel.init;

import com.susen36.babel.BabelMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class BabelTabs {
    public static final DeferredRegister<CreativeModeTab> REGISTRY = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, BabelMod.MODID);

    public static final RegistryObject<CreativeModeTab> COLLECTIBLES_TAB =
            REGISTRY.register("babel_collectibles", () -> CreativeModeTab.builder()
                    .title(Component.translatable("item_group.babel.collectibles"))
                    .withTabsBefore(CreativeModeTabs.COMBAT)
                    .icon(() -> new ItemStack(BabelCollectible.KETTLE.get()))
                    .displayItems((parameters, output) -> {
                        for (var item : BabelCollectible.BABEL_COLLECTIBLES) {
                            output.accept(item.get());
                        }
                    }).build());

    public static final RegistryObject<CreativeModeTab> BABEL_TOWER_TAB =
            REGISTRY.register("babel_tower", () -> CreativeModeTab.builder()
                    .title(Component.translatable("item_group.babel.babel_tower"))
                    .withTabsBefore(CreativeModeTabs.COMBAT)
                    .icon(() -> new ItemStack(BabelItems.ORIGINIUM_INGOT.get()))
                    .displayItems((parameters, output) -> {
                        output.accept(BabelItems.ORIGINIUM_INGOT.get());
                        output.accept(BabelItems.CANNOT_SPAWN_EGG.get());
                    }).build());

    private BabelTabs() {
        throw new UnsupportedOperationException("Utility class");
    }
}
