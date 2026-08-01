package com.susen36.babel.init;

import com.susen36.babel.BabelMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelReader;

import javax.annotation.Nullable;

public class BabelDamageTypes {
    public static final ResourceKey<DamageType> SANITY_BREAK = create("sanity_break");
    public static final ResourceKey<DamageType> CORROSION_BREAK = create("corrosion_break");
    public static final ResourceKey<DamageType> BURN_BREAK = create("burn_break");
    public static final ResourceKey<DamageType> ELEMENT_BREAK = create("element_break");

    private BabelDamageTypes() {
        throw new UnsupportedOperationException("Utility class");
    }

    private static ResourceKey<DamageType> create(String name) {
        return ResourceKey.create(Registries.DAMAGE_TYPE, ResourceLocation.fromNamespaceAndPath(BabelMod.MODID, name));
    }

    public static DamageSource source(LevelReader level, ResourceKey<DamageType> damageType) {
        return new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(damageType));
    }

    public static DamageSource source(LevelReader level, ResourceKey<DamageType> damageType, @Nullable Entity directEntity) {
        return new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(damageType), directEntity);
    }

    public static DamageSource source(LevelReader level, ResourceKey<DamageType> damageType, @Nullable Entity directEntity, @Nullable Entity causingEntity) {
        return new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(damageType), directEntity, causingEntity);
    }
}
