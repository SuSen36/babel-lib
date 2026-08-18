package com.susen36.babel.init;

import com.susen36.babel.BabelMod;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Objects;

public class BabelItems {
    public static final DeferredRegister<Item> REGISTRY = DeferredRegister.create(BuiltInRegistries.ITEM, BabelMod.MODID);

    public static final DeferredHolder<Item, ? extends Item> ORIGINIUM_INGOT = REGISTRY.register("originium_ingot", () -> new Item(new Item.Properties().stacksTo(64).rarity(Rarity.COMMON)));
    public static final DeferredHolder<Item, ? extends Item> BLOCK_KETTLE = block(BabelBlocks.BLOCK_KETTLE);
    public static final DeferredHolder<Item, ? extends Item> BLOCK_BATBED = block(BabelBlocks.BLOCK_BATBED);
    public static final DeferredHolder<Item, ? extends Item> BATBED_UPPER = block(BabelBlocks.BATBED_UPPER);

    private static DeferredHolder<Item, ? extends Item> block(DeferredHolder<Block, ? extends Block> block) {
        return REGISTRY.register(Objects.requireNonNull(block.getId()).getPath(), () -> new BlockItem(block.get(), new Item.Properties()));
    }

    private BabelItems() {
        throw new UnsupportedOperationException("Utility class");
    }
}