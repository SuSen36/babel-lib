package com.susen36.babel.datagen.tags;

import com.susen36.babel.BabelMod;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

/**
 * {@code babel:applies_difficulty} 默认纳入末影龙与 {@code forge:bosses}；
 * {@code babel:bypasses_difficulty} 保持为空，交由依赖模组自行添加。
 */
public class EntityTypeTagsProvider extends TagsProvider<EntityType<?>> {

    private static final TagKey<EntityType<?>> APPLIES_DIFFICULTY = babelEntityTypeTag("applies_difficulty");
    private static final TagKey<EntityType<?>> BYPASSES_DIFFICULTY = babelEntityTypeTag("bypasses_difficulty");
    private static final TagKey<EntityType<?>> FORGE_BOSSES = TagKey.create(
            Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath("forge", "bosses"));

    public EntityTypeTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider,
                                  @Nullable ExistingFileHelper existingFileHelper) {
        super(output, Registries.ENTITY_TYPE, lookupProvider, BabelMod.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(APPLIES_DIFFICULTY)
                .add(ResourceKey.create(Registries.ENTITY_TYPE, EntityType.getKey(EntityType.ENDER_DRAGON)))
                .addTag(FORGE_BOSSES);

        tag(BYPASSES_DIFFICULTY);
    }

    private static TagKey<EntityType<?>> babelEntityTypeTag(String path) {
        return TagKey.create(Registries.ENTITY_TYPE,
                ResourceLocation.fromNamespaceAndPath(BabelMod.MODID, path));
    }
}