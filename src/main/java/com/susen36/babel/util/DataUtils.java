package com.susen36.babel.util;

import com.susen36.babel.init.BabelEntityTypeTags;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.LivingEntity;

public class DataUtils {

    public static final TagKey<DamageType> FORGE_MAGIC = TagKey.create(
            Registries.DAMAGE_TYPE, new ResourceLocation("forge", "is_magic"));

    private DataUtils() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static boolean isBoss(LivingEntity entity) {
        return entity.getType().is(BabelEntityTypeTags.FORGE_BOSSES);
    }

    public static boolean isMagic(DamageSource source) {
        return isNotReal(source) && !source.is(DamageTypeTags.BYPASSES_ENCHANTMENTS) &&
                (source.is(FORGE_MAGIC) || source.is(DamageTypeTags.BYPASSES_ARMOR) || source.is(DamageTypeTags.WITCH_RESISTANT_TO));
    }

    public static boolean isNotReal(DamageSource source) {
        return !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY) && !source.is(DamageTypeTags.BYPASSES_EFFECTS);
    }

    public static boolean isPhysical(DamageSource source) {
        return isNotReal(source) && !isMagic(source) && !source.is(DamageTypeTags.BYPASSES_RESISTANCE);
    }
}
