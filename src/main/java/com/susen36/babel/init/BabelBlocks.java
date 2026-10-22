package com.susen36.babel.init;

import com.susen36.babel.BabelMod;
import com.susen36.babel.block.AllayBlockBlock;
import com.susen36.babel.block.BatbedUpperBlock;
import com.susen36.babel.block.BerryCanBlock;
import com.susen36.babel.block.BlockBatbedBlock;
import com.susen36.babel.block.BlockCrownBlock;
import com.susen36.babel.block.BlockCrystalBlock;
import com.susen36.babel.block.BlockExtensionBlock;
import com.susen36.babel.block.BlockFateBlock;
import com.susen36.babel.block.BlockKettleBlock;
import com.susen36.babel.block.BlockSpearBlock;
import com.susen36.babel.block.GoldenChaliseBlock;
import com.susen36.babel.block.KingsArmorBlock;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class BabelBlocks {
    public static final DeferredRegister<Block> REGISTRY = DeferredRegister.create(ForgeRegistries.BLOCKS, BabelMod.MODID);

    public static final RegistryObject<? extends Block> BLOCK_KETTLE = REGISTRY.register("block_kettle", BlockKettleBlock::new);
    public static final RegistryObject<? extends Block> BLOCK_BATBED = REGISTRY.register("block_batbed", BlockBatbedBlock::new);
    public static final RegistryObject<? extends Block> BATBED_UPPER = REGISTRY.register("batbed_upper", BatbedUpperBlock::new);
    public static final RegistryObject<? extends Block> KINGS_ARMOR = REGISTRY.register("kings_armor", KingsArmorBlock::new);
    public static final RegistryObject<? extends Block> BLOCK_CROWN = REGISTRY.register("block_crown", BlockCrownBlock::new);
    public static final RegistryObject<? extends Block> BLOCK_SPEAR = REGISTRY.register("block_spear", BlockSpearBlock::new);
    public static final RegistryObject<? extends Block> BLOCK_EXTENSION = REGISTRY.register("block_extension", BlockExtensionBlock::new);
    public static final RegistryObject<? extends Block> BLOCK_CRYSTAL = REGISTRY.register("block_crystal", BlockCrystalBlock::new);
    public static final RegistryObject<? extends Block> BLOCK_FATE = REGISTRY.register("block_fate", BlockFateBlock::new);
    public static final RegistryObject<? extends Block> BERRY_CAN = REGISTRY.register("berry_can", BerryCanBlock::new);
    public static final RegistryObject<? extends Block> ALLAY_BLOCK = REGISTRY.register("allay_block", AllayBlockBlock::new);
    public static final RegistryObject<? extends Block> GOLDEN_CHALISE = REGISTRY.register("golden_chalise", GoldenChaliseBlock::new);

    private BabelBlocks() {
        throw new UnsupportedOperationException("Utility class");
    }
}
