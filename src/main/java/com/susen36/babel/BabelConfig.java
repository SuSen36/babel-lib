package com.susen36.babel;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class BabelConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    private static final ModConfigSpec.BooleanValue LOG_DIRT_BLOCK = BUILDER
            .comment("Whether to log the dirt block on common setup")
            .define("logDirtBlock", false);

    private static final ModConfigSpec.IntValue MAGIC_NUMBER = BUILDER
            .comment("A magic number")
            .defineInRange("magicNumber", 42, 0, Integer.MAX_VALUE);

    public static final ModConfigSpec.ConfigValue<String> MAGIC_NUMBER_INTRODUCTION = BUILDER
            .comment("What you want the introduction message to be for the magic number")
            .define("magicNumberIntroduction", "Babel magic number: ");

    private static final ModConfigSpec.ConfigValue<List<? extends String>> ITEM_STRINGS = BUILDER
            .comment("A list of items to log on common setup.")
            .defineListAllowEmpty("items", List.of("minecraft:iron_ingot"), BabelConfig::validateItemName);

    public static final ModConfigSpec.BooleanValue EP_BAR_STYLE = BUILDER
            .comment("元素损伤显示采用条状，若关闭则为圆环（仅神经损伤有效）。")
            .define("ep_bar_style", false);

    public static final ModConfigSpec.DoubleValue EP_X_OFFSET = BUILDER
            .comment("元素损伤渲染x轴偏移。正数值为向右偏移")
            .defineInRange("ep_x_offset", 0.0, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);

    public static final ModConfigSpec.DoubleValue EP_Y_OFFSET = BUILDER
            .comment("元素损伤渲染y轴偏移。正数值为向下偏移")
            .defineInRange("ep_y_offset", 0.0, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);

    static final ModConfigSpec SPEC = BUILDER.build();

    public static boolean logDirtBlock;
    public static int magicNumber;
    public static String magicNumberIntroduction;
    public static Set<Item> items;
    public static boolean epBarStyle;
    public static double epXOffset;
    public static double epYOffset;

    private static boolean validateItemName(final Object obj) {
        return obj instanceof final String itemName
                && BuiltInRegistries.ITEM.containsKey(ResourceLocation.tryBuild(itemName.indexOf(':') >= 0
                        ? itemName.substring(0, itemName.indexOf(':'))
                        : "minecraft", itemName.contains(":") ? itemName.substring(itemName.indexOf(':') + 1) : itemName));
    }

    public static void onLoad(final ModConfigEvent.Loading event) {
        logDirtBlock = LOG_DIRT_BLOCK.get();
        magicNumber = MAGIC_NUMBER.get();
        magicNumberIntroduction = MAGIC_NUMBER_INTRODUCTION.get();
        items = ITEM_STRINGS.get().stream()
                .<Item>map(itemName -> BuiltInRegistries.ITEM.get(ResourceLocation.tryParse(itemName)))
                .collect(Collectors.toSet());
        epBarStyle = EP_BAR_STYLE.get();
        epXOffset = EP_X_OFFSET.get();
        epYOffset = EP_Y_OFFSET.get();
    }

    public static void onReloading(final ModConfigEvent.Reloading event) {
        logDirtBlock = LOG_DIRT_BLOCK.get();
        magicNumber = MAGIC_NUMBER.get();
        magicNumberIntroduction = MAGIC_NUMBER_INTRODUCTION.get();
        epBarStyle = EP_BAR_STYLE.get();
        epXOffset = EP_X_OFFSET.get();
        epYOffset = EP_Y_OFFSET.get();
    }
}