package com.susen36.babel.init;

import com.susen36.babel.BabelMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;

public interface BabelDamageTags {
    TagKey<DamageType> BYPASS_MISS = create("bypass_miss");
    TagKey<DamageType> SKIP_SOURCE_CHECK = create("skip_source_check");

    private static TagKey<DamageType> create(String name) {
        return TagKey.create(Registries.DAMAGE_TYPE, new ResourceLocation(BabelMod.MODID, name));
    }
}