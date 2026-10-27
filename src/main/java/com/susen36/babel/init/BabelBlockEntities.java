package com.susen36.babel.init;

import com.susen36.babel.BabelMod;
import com.susen36.babel.block.GoldenChaliseBlockEntity;
import com.susen36.babel.block.KingPedestalBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class BabelBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> REGISTRY = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, BabelMod.MODID);

    public static final RegistryObject<BlockEntityType<GoldenChaliseBlockEntity>> GOLDEN_CHALISE =
            REGISTRY.register("golden_chalise", () -> BlockEntityType.Builder.of(GoldenChaliseBlockEntity::new, BabelBlocks.GOLDEN_CHALISE.get()).build(null));

    public static final RegistryObject<BlockEntityType<KingPedestalBlockEntity>> KING_PEDESTAL =
            REGISTRY.register("king_pedestal", () -> BlockEntityType.Builder.of(KingPedestalBlockEntity::new, BabelBlocks.KING_PEDESTAL.get()).build(null));

    private BabelBlockEntities() {
        throw new UnsupportedOperationException("Utility class");
    }
}