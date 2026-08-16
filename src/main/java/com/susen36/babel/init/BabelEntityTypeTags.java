package com.susen36.babel.init;

import com.susen36.babel.BabelMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;

/**
 * babel 库提供的实体类型标签常量。
 * <p>
 * 这些标签只负责“表达某实体属于哪一类”，供 {@code entity.getType().is(...)} 判断；
 * 标签数据由依赖模组（如 caerula_arbor）通过 datagen 填充。
 */
public final class BabelEntityTypeTags {
    private BabelEntityTypeTags() {
    }

    /** 精英生物标签，默认空，由依赖模组填充。 */
    public static final TagKey<EntityType<?>> ELITE = create("elite");

    /** forge 生态通用的首领生物标签。 */
    public static final TagKey<EntityType<?>> FORGE_BOSSES = TagKey.create(
            Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath("forge", "bosses"));

    private static TagKey<EntityType<?>> create(String path) {
        return TagKey.create(Registries.ENTITY_TYPE,
                ResourceLocation.fromNamespaceAndPath(BabelMod.MODID, path));
    }
}
