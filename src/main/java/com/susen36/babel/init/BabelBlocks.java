package com.susen36.babel.init;

import com.susen36.babel.BabelMod;
import com.susen36.babel.block.*;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class BabelBlocks {
    public static final DeferredRegister<Block> REGISTRY = DeferredRegister.create(ForgeRegistries.BLOCKS, BabelMod.MODID);

    public static final RegistryObject<? extends Block> BLOCK_KETTLE = REGISTRY.register("block_kettle", BlockKettleBlock::new);
    public static final RegistryObject<? extends Block> BLOCK_BATBED = REGISTRY.register("block_batbed", BlockBatbedBlock::new);
    public static final RegistryObject<? extends Block> BATBED_UPPER = REGISTRY.register("batbed_upper", BatbedUpperBlock::new);
    public static final RegistryObject<? extends Block> KING_PEDESTAL = REGISTRY.register("king_pedestal", KingPedestalBlock::new);
    public static final RegistryObject<? extends Block> BERRY_CAN = REGISTRY.register("berry_can", BerryCanBlock::new);
    public static final RegistryObject<? extends Block> ALLAY_BLOCK = REGISTRY.register("allay_block", AllayBlockBlock::new);
    public static final RegistryObject<? extends Block> GOLDEN_CHALICE = REGISTRY.register("golden_chalice", GoldenChaliceBlock::new);

    private BabelBlocks() {
        throw new UnsupportedOperationException("Utility class");
    }
}