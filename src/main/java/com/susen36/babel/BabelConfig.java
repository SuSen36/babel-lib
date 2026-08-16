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
            .comment("元素损伤显示采用和饥饿值相似的小图标，若关闭则为圆环")
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

    public static final ModConfigSpec.DoubleValue LIFE_POINT_INIT = BUILDER
            .comment("初始目标生命。")
            .defineInRange("initial_life_point", 6.0, 1.0, Double.MAX_VALUE);

    public static final ModConfigSpec.DoubleValue LIFE_POINT_LIMIT = BUILDER
            .comment("全局目标生命上限。")
            .defineInRange("life_point_global_limit", 32767.0, 1.0, Double.MAX_VALUE);

    public static final ModConfigSpec.DoubleValue SHIELD_LIMIT = BUILDER
            .comment("全局护盾值上限。")
            .defineInRange("player_shield_global_limit", 99999.0, 0.0, Double.MAX_VALUE);

    public static final ModConfigSpec.DoubleValue LIFE_X_OFFSET = BUILDER
            .comment("目标生命值渲染x轴偏移。正数值为向右偏移")
            .defineInRange("life_point_x_offset", 0.0, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);

    public static final ModConfigSpec.DoubleValue LIFE_Y_OFFSET = BUILDER
            .comment("目标生命值渲染y轴偏移。正数值为向下偏移")
            .defineInRange("life_point_y_offset", 0.0, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);

    public static final ModConfigSpec.DoubleValue SHIELD_X_OFFSET = BUILDER
            .comment("护盾值渲染x轴偏移，注意为相对目标生命UI的偏移")
            .defineInRange("shield_x_offset", 0.0, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);

    public static final ModConfigSpec.DoubleValue SHIELD_Y_OFFSET = BUILDER
            .comment("护盾值渲染y轴偏移，注意为相对目标生命UI的偏移")
            .defineInRange("shield_y_offset", 0.0, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);

    static final ModConfigSpec SPEC = BUILDER.build();
    public static int magicNumber;
    public static String magicNumberIntroduction;
    public static boolean epBarStyle;
    public static boolean banAdvancedCollectibles;
    public static double epXOffset;
    public static double epYOffset;
    public static double lifePointInit;
    public static double lifePointLimit;
    public static double shieldLimit;
    public static double lifeXOffset;
    public static double lifeYOffset;
    public static double shieldXOffset;
    public static double shieldYOffset;
    public static List<? extends String> surgingWavesEntry;

    @SubscribeEvent
    public static void onLoad(final ModConfigEvent.Loading event) {
        magicNumber = MAGIC_NUMBER.get();
        magicNumberIntroduction = MAGIC_NUMBER_INTRODUCTION.get();
        epBarStyle = EP_BAR_STYLE.get();
        banAdvancedCollectibles = BAN_ADVANCED_COLLECTIBLES.get();
        epXOffset = EP_X_OFFSET.get();
        epYOffset = EP_Y_OFFSET.get();
        lifePointInit = LIFE_POINT_INIT.get();
        lifePointLimit = LIFE_POINT_LIMIT.get();
        shieldLimit = SHIELD_LIMIT.get();
        lifeXOffset = LIFE_X_OFFSET.get();
        lifeYOffset = LIFE_Y_OFFSET.get();
        shieldXOffset = SHIELD_X_OFFSET.get();
        shieldYOffset = SHIELD_Y_OFFSET.get();
    }

    public static void onReloading(final ModConfigEvent.Reloading event) {
        magicNumber = MAGIC_NUMBER.get();
        magicNumberIntroduction = MAGIC_NUMBER_INTRODUCTION.get();
        epBarStyle = EP_BAR_STYLE.get();
        banAdvancedCollectibles = BAN_ADVANCED_COLLECTIBLES.get();
        epXOffset = EP_X_OFFSET.get();
        epYOffset = EP_Y_OFFSET.get();
        lifePointInit = LIFE_POINT_INIT.get();
        lifePointLimit = LIFE_POINT_LIMIT.get();
        shieldLimit = SHIELD_LIMIT.get();
        lifeXOffset = LIFE_X_OFFSET.get();
        lifeYOffset = LIFE_Y_OFFSET.get();
        shieldXOffset = SHIELD_X_OFFSET.get();
        shieldYOffset = SHIELD_Y_OFFSET.get();
    }
}