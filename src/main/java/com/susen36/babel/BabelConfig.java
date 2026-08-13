package com.susen36.babel;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

public class BabelConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    private static final ModConfigSpec.IntValue MAGIC_NUMBER = BUILDER
            .comment("A magic number")
            .defineInRange("magicNumber", 42, 0, Integer.MAX_VALUE);

    public static final ModConfigSpec.ConfigValue<String> MAGIC_NUMBER_INTRODUCTION = BUILDER
            .comment("What you want the introduction message to be for the magic number")
            .define("magicNumberIntroduction", "Babel magic number: ");

    public static final ModConfigSpec.BooleanValue EP_BAR_STYLE = BUILDER
            .comment("元素损伤显示采和饥饿值相似的小图标，若关闭则为圆环")
            .define("ep_icon_style", false);

    public static final ModConfigSpec.BooleanValue BAN_ADVANCED_COLLECTIBLES = BUILDER
            .comment("禁用高级（ADVANCED）收藏品：无法激活")
            .define("ban_advanced_collectibles", false);

    public static final ModConfigSpec.DoubleValue EP_X_OFFSET = BUILDER
            .comment("元素损伤渲染x轴偏移。正数值为向右偏移")
            .defineInRange("ep_x_offset", 0.0, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);

    public static final ModConfigSpec.DoubleValue EP_Y_OFFSET = BUILDER
            .comment("元素损伤渲染y轴偏移。正数值为向下偏移")
            .defineInRange("ep_y_offset", 0.0, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);


    static final ModConfigSpec SPEC = BUILDER.build();
    public static int magicNumber;
    public static String magicNumberIntroduction;
    public static boolean epBarStyle;
    public static boolean banAdvancedCollectibles;
    public static double epXOffset;
    public static double epYOffset;
    public static List<? extends String> surgingWavesEntry;

    @SubscribeEvent
    public static void onLoad(final ModConfigEvent.Loading event) {
        magicNumber = MAGIC_NUMBER.get();
        magicNumberIntroduction = MAGIC_NUMBER_INTRODUCTION.get();
        epBarStyle = EP_BAR_STYLE.get();
        banAdvancedCollectibles = BAN_ADVANCED_COLLECTIBLES.get();
        epXOffset = EP_X_OFFSET.get();
        epYOffset = EP_Y_OFFSET.get();
    }

    public static void onReloading(final ModConfigEvent.Reloading event) {
        magicNumber = MAGIC_NUMBER.get();
        magicNumberIntroduction = MAGIC_NUMBER_INTRODUCTION.get();
        epBarStyle = EP_BAR_STYLE.get();
        banAdvancedCollectibles = BAN_ADVANCED_COLLECTIBLES.get();
        epXOffset = EP_X_OFFSET.get();
        epYOffset = EP_Y_OFFSET.get();
    }
}