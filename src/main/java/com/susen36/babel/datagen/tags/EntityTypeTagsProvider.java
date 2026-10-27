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
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * {@code babel:applies_difficulty} 默认纳入末影龙与 {@code forge:bosses}；
 * {@code babel:elite} 默认纳入原版精锐敌怪；{@code babel:bypasses_difficulty} 保持为空，交由依赖模组自行添加。
 */
public class EntityTypeTagsProvider extends TagsProvider<EntityType<?>> {

    private static final TagKey<EntityType<?>> APPLIES_DIFFICULTY = babelEntityTypeTag("applies_difficulty");
    private static final TagKey<EntityType<?>> BYPASSES_DIFFICULTY = babelEntityTypeTag("bypasses_difficulty");
    private static final TagKey<EntityType<?>> ELITE = babelEntityTypeTag("elite");
    private static final TagKey<EntityType<?>> FORGE_BOSSES = TagKey.create(
            Registries.ENTITY_TYPE, new ResourceLocation("forge", "bosses"));

    public EntityTypeTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider,
                                  @Nullable ExistingFileHelper existingFileHelper) {
        super(output, Registries.ENTITY_TYPE, lookupProvider, BabelMod.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(FORGE_BOSSES);

        tag(APPLIES_DIFFICULTY)
                .add(ResourceKey.create(Registries.ENTITY_TYPE, EntityType.getKey(EntityType.ENDER_DRAGON)))
                .addTag(FORGE_BOSSES);

        tag(ELITE)
                .add(entityTypeKey(EntityType.BLAZE), entityTypeKey(EntityType.ELDER_GUARDIAN),
                        entityTypeKey(EntityType.ENDERMAN), entityTypeKey(EntityType.EVOKER), entityTypeKey(EntityType.GHAST),
                        entityTypeKey(EntityType.PIGLIN_BRUTE), entityTypeKey(EntityType.RAVAGER), entityTypeKey(EntityType.SHULKER),
                        entityTypeKey(EntityType.VINDICATOR), entityTypeKey(EntityType.WARDEN),
                        entityTypeKey(EntityType.WITHER_SKELETON));

        tag(BYPASSES_DIFFICULTY);
    }

    private static ResourceKey<EntityType<?>> entityTypeKey(EntityType<?> type) {
        return ResourceKey.create(Registries.ENTITY_TYPE, Objects.requireNonNull(EntityType.getKey(type)));
    }

    private static TagKey<EntityType<?>> babelEntityTypeTag(String path) {
        return TagKey.create(Registries.ENTITY_TYPE,
                new ResourceLocation(BabelMod.MODID, path));
    }
}