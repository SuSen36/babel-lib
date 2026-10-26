package com.susen36.babel.datagen.tags;

import com.susen36.babel.BabelMod;
import com.susen36.babel.init.BabelDamageTypes;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

/**
 * {@code #babel:element_break} 归拢元素损伤五类爆发伤害，作为其父级通用属性（如绕抗性）的挂载点；
 * sanity 绕效果、burn 算魔法等子类型专属特性在原版/forge tag 中单独追加，不通过父级 tag 扩散。
 */
public class DamageTypeTagsProvider extends TagsProvider<DamageType> {

    private static final TagKey<DamageType> ELEMENT_BREAK = babelDamageTypeTag("element_break");
    private static final TagKey<DamageType> BYPASSES_RESISTANCE = vanillaDamageTypeTag("bypasses_resistance");
    private static final TagKey<DamageType> BYPASSES_EFFECTS = vanillaDamageTypeTag("bypasses_effects");
    private static final TagKey<DamageType> FORGE_IS_MAGIC = TagKey.create(
            Registries.DAMAGE_TYPE, new ResourceLocation("forge", "is_magic"));

    public DamageTypeTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider,
                                  @Nullable ExistingFileHelper existingFileHelper) {
        super(output, Registries.DAMAGE_TYPE, lookupProvider, BabelMod.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(ELEMENT_BREAK)
                .add(BabelDamageTypes.ELEMENT_BREAK, BabelDamageTypes.SANITY_BREAK, BabelDamageTypes.CORROSION_BREAK,
                        BabelDamageTypes.BURN_BREAK, BabelDamageTypes.FRENZY_BREAK);

        tag(BYPASSES_RESISTANCE)
                .addTag(ELEMENT_BREAK)
                .add(BabelDamageTypes.TRUE_DAMAGE);

        tag(BYPASSES_EFFECTS)
                .add(BabelDamageTypes.SANITY_BREAK);

        tag(FORGE_IS_MAGIC)
                .add(BabelDamageTypes.BURN_BREAK);
    }

    private static TagKey<DamageType> babelDamageTypeTag(String path) {
        return TagKey.create(Registries.DAMAGE_TYPE, new ResourceLocation(BabelMod.MODID, path));
    }

    private static TagKey<DamageType> vanillaDamageTypeTag(String path) {
        return TagKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("minecraft", path));
    }
}