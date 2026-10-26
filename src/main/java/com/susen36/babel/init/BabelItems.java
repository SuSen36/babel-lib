package com.susen36.babel.init;

import com.susen36.babel.BabelMod;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.Objects;

public class BabelItems {
    public static final DeferredRegister<Item> REGISTRY = DeferredRegister.create(ForgeRegistries.ITEMS, BabelMod.MODID);

    public static final RegistryObject<? extends Item> ORIGINIUM_INGOT = REGISTRY.register("originium_ingot", () -> new Item(new Item.Properties().stacksTo(64).rarity(Rarity.COMMON)));
    public static final RegistryObject<ForgeSpawnEggItem> CANNOT_SPAWN_EGG = REGISTRY.register("cannot_spawn_egg", () -> new ForgeSpawnEggItem(BabelEntities.CANNOT, 0x2B2B2B, 0x8A8A8A, new Item.Properties()));
    public static final RegistryObject<? extends Item> BLOCK_KETTLE = block(BabelBlocks.BLOCK_KETTLE);
    public static final RegistryObject<? extends Item> BLOCK_BATBED = block(BabelBlocks.BLOCK_BATBED);
    public static final RegistryObject<? extends Item> BATBED_UPPER = block(BabelBlocks.BATBED_UPPER);
    public static final RegistryObject<? extends Item> KING_PEDESTAL = block(BabelBlocks.KING_PEDESTAL);
    public static final RegistryObject<? extends Item> BERRY_CAN = block(BabelBlocks.BERRY_CAN);
    public static final RegistryObject<? extends Item> ALLAY_BLOCK = block(BabelBlocks.ALLAY_BLOCK);

    private static RegistryObject<? extends Item> block(RegistryObject<? extends Block> block) {
        return REGISTRY.register(Objects.requireNonNull(block.getId()).getPath(), () -> new BlockItem(block.get(), new Item.Properties()));
    }

    private BabelItems() {
        throw new UnsupportedOperationException("Utility class");
    }
}
