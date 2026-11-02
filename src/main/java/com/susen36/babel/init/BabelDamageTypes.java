package com.susen36.babel.init;

import com.susen36.babel.BabelMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelReader;

import javax.annotation.Nullable;

public class BabelDamageTypes {
    public static final ResourceKey<DamageType> ELEMENT_BREAK = create("element_break");
    public static final ResourceKey<DamageType> SANITY_BREAK = create("sanity_break");
    public static final ResourceKey<DamageType> CORROSION_BREAK = create("corrosion_break");
    public static final ResourceKey<DamageType> BURN_BREAK = create("burn_break");
    public static final ResourceKey<DamageType> FRENZY_BREAK = create("frenzy_break");
    public static final ResourceKey<DamageType> TRUE_DAMAGE = create("true_damage");
    public static final ResourceKey<DamageType> DREAMBIND_STRIKE = create("dreambind_strike");
    /** 伤害类型加入此 tag 即可命中沉睡单位 */
    public static final TagKey<DamageType> CAN_HURT_SLEEPING = TagKey.create(
            Registries.DAMAGE_TYPE, new ResourceLocation(BabelMod.MODID, "can_hurt_sleeping"));

    private BabelDamageTypes() {
        throw new UnsupportedOperationException("Utility class");
    }

    private static ResourceKey<DamageType> create(String name) {
        return ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation(BabelMod.MODID, name));
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