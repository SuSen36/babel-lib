package com.susen36.babel.init;

import com.mojang.serialization.Codec;
import com.susen36.babel.BabelMod;
import com.susen36.babel.loot.CollectibleLootModifier;
import com.susen36.babel.loot.OriginiumIngotLootModifier;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class BabelLootModifiers {
    public static final DeferredRegister<Codec<? extends IGlobalLootModifier>> REGISTRY =
            DeferredRegister.create(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, BabelMod.MODID);
    public static final RegistryObject<Codec<CollectibleLootModifier>> COLLECTIBLES_IN_CHESTS =
            REGISTRY.register("collectibles_in_chests", CollectibleLootModifier.CODEC);
    public static final RegistryObject<Codec<OriginiumIngotLootModifier>> ORIGINIUM_INGOT_FROM_ENTITIES =
            REGISTRY.register("originium_ingot_from_entities", OriginiumIngotLootModifier.CODEC);

    private BabelLootModifiers() {
    }
}