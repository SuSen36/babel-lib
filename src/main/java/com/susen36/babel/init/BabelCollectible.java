package com.susen36.babel.init;

import com.susen36.babel.BabelMod;
import com.susen36.babel.collectible.CollectibleBuilder;
import com.susen36.babel.item.BatBedItem;
import com.susen36.babel.item.KettleItem;
import com.susen36.babel.item.OmniKeyItem;
import com.susen36.babel.item.PiglinDiaryItem;
import com.susen36.babel.item.RainbowCandyItem;
import com.susen36.babel.item.RedstoneIrisFlowerItem;
import com.susen36.babel.item.VoyageOfGoldItem;
import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;

import java.util.ArrayList;

public class BabelCollectible {
    public static CollectibleBuilder REGISTRY;

    public static Holder<Item> REDSTONE_IRIS_FLOWER;
    public static Holder<Item> UTIL_OMNIKEY;
    public static Holder<Item> VOYAGE_OF_GOLD;
    public static Holder<Item> PIGLIN_DIARY;
    public static Holder<Item> KETTLE;
    public static Holder<Item> BAT_BED;
    public static Holder<Item> RAINBOW_CANDY;

    public static final ArrayList<Holder<Item>> BABEL_COLLECTIBLES = new ArrayList<>();

    public static void register(IEventBus bus) {
        REGISTRY = CollectibleBuilder.create(BabelMod.MODID, bus);
        BABEL_COLLECTIBLES.add(REDSTONE_IRIS_FLOWER = REGISTRY.registerCollectible("redstone_iris_flower", RedstoneIrisFlowerItem::new));
        BABEL_COLLECTIBLES.add(UTIL_OMNIKEY = REGISTRY.registerCollectible("util_omnikey", OmniKeyItem::new));
        BABEL_COLLECTIBLES.add(VOYAGE_OF_GOLD = REGISTRY.registerCollectible("voyage_of_gold", VoyageOfGoldItem::new));
        BABEL_COLLECTIBLES.add(PIGLIN_DIARY = REGISTRY.registerCollectible("piglin_diary", PiglinDiaryItem::new));
        BABEL_COLLECTIBLES.add(KETTLE = REGISTRY.registerCollectible("kettle", KettleItem::new));
        BABEL_COLLECTIBLES.add(BAT_BED = REGISTRY.registerCollectible("bat_bed", BatBedItem::new));
        BABEL_COLLECTIBLES.add(RAINBOW_CANDY = REGISTRY.registerCollectible("rainbow_candy", RainbowCandyItem::new));
    }

    private BabelCollectible() {
        throw new UnsupportedOperationException("Utility class");
    }
}