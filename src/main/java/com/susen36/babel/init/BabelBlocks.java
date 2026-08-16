package com.susen36.babel.init;

import com.susen36.babel.BabelMod;
import com.susen36.babel.block.BatbedUpperBlock;
import com.susen36.babel.block.BlockBatbedBlock;
import com.susen36.babel.block.BlockKettleBlock;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class BabelBlocks {
    public static final DeferredRegister<Block> REGISTRY = DeferredRegister.create(BuiltInRegistries.BLOCK, BabelMod.MODID);

    public static final DeferredHolder<Block, ? extends Block> BLOCK_KETTLE = REGISTRY.register("block_kettle", BlockKettleBlock::new);
    public static final DeferredHolder<Block, ? extends Block> BLOCK_BATBED = REGISTRY.register("block_batbed", BlockBatbedBlock::new);
    public static final DeferredHolder<Block, ? extends Block> BATBED_UPPER = REGISTRY.register("batbed_upper", BatbedUpperBlock::new);

    private BabelBlocks() {
        throw new UnsupportedOperationException("Utility class");
    }
}