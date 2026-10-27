package com.susen36.babel.init;

import com.susen36.babel.BabelMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public final class BabelItemTags {
    private BabelItemTags() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static final TagKey<Item> FIREWORK_WEAPONS = create("firework_weapons");
    public static final TagKey<Item> KING_RELICS = create("king_relics");
    public static final TagKey<Item> ARCHFIEND_RELICS = create("archfiend_relics");

    private static TagKey<Item> create(String path) {
        return TagKey.create(Registries.ITEM,
                new ResourceLocation(BabelMod.MODID, path));
    }
}