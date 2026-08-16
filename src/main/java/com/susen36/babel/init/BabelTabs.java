package com.susen36.babel.init;

import com.susen36.babel.BabelMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredRegister;

public class BabelTabs {
    public static final DeferredRegister<CreativeModeTab> REGISTRY = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, BabelMod.MODID);

    static {
        REGISTRY.register("babel_collectibles", () -> CreativeModeTab.builder()
                .title(Component.translatable("item_group.babel.collectibles"))
                .withTabsBefore(CreativeModeTabs.COMBAT)
                .icon(() -> new ItemStack(BabelCollectible.KETTLE))
                .displayItems((parameters, output) -> {
                    for (var item : BabelCollectible.BABEL_COLLECTIBLES) {
                        output.accept(item.value());
                    }
                }).build());
    }

    private BabelTabs() {
        throw new UnsupportedOperationException("Utility class");
    }
}