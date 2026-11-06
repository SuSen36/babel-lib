package com.susen36.babel.init;

import com.susen36.babel.BabelMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;

public final class BabelEntityTypeTags {
    private BabelEntityTypeTags() {
    }

    /** Boss。 */
    public static final TagKey<EntityType<?>> FORGE_BOSSES = TagKey.create(
            Registries.ENTITY_TYPE, new ResourceLocation("forge", "bosses"));

    /** 精英怪。 */
    public static final TagKey<EntityType<?>> ELITE = create("elite");

    /** 感染生物*/
    public static final TagKey<EntityType<?>> INFECTED_CREATURE = create("infected_creature");

    /** 海嗣*/
    public static final TagKey<EntityType<?>> SEABORN = create("seaborn");

    /** 源石造物 */
    public static final TagKey<EntityType<?>> ORIGINIUM_CREATION = create("originium_creation");

    /** 坍缩体*/
    public static final TagKey<EntityType<?>> COLLAPSAL = create("collapsal");

    /** 机械*/
    public static final TagKey<EntityType<?>> MACHINA = create("machina");

    /** 萨卡兹*/
    public static final TagKey<EntityType<?>> SARKAZ = create("sarkaz");

    /** 化物*/
    public static final TagKey<EntityType<?>> APPARITION = create("apparition");

    private static TagKey<EntityType<?>> create(String path) {
        return TagKey.create(Registries.ENTITY_TYPE,
                new ResourceLocation(BabelMod.MODID, path));
    }
}