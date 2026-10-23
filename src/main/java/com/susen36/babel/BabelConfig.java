package com.susen36.babel;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.config.ModConfigEvent;

import java.util.List;

public class BabelConfig {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    public static final ForgeConfigSpec.BooleanValue EP_BAR_STYLE = BUILDER
            .comment("元素损伤显示采用和饥饿值相似的小图标，若关闭则为圆环")
            .define("ep_icon_style", false);

    public static final ForgeConfigSpec.BooleanValue BAN_ADVANCED_COLLECTIBLES = BUILDER
            .comment("禁用高级（ADVANCED）收藏品：无法激活")
            .define("ban_advanced_collectibles", false);

    public static final ForgeConfigSpec.DoubleValue COLLECTIBLE_LOOT_CHANCE = BUILDER
            .comment("收藏品刷新概率。在每次触发战利品表（如打开箱子）时以此概率额外随机加入一件收藏品")
            .defineInRange("collectible_loot_chance", 0.3, 0.0, 1.0);

    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> HAND_STRANGLE_RECOGNIZABLE = BUILDER
            .comment("其它能使收藏品扼喉之手生效的物品。最好是弩。用 * 号代表该命名空间下的所有物品，如 tacz:*, babel:oceanized_* 等，下同")
            .defineList("hand_strangle_recognizable", List.of("CrossbowMod2:item.woodenCrossbow"), entry -> true);

    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> HAND_FIREWORK_RECOGNIZABLE = BUILDER
            .comment("其它能使收藏品烟花之手生效的物品。最好是弓。")
            .defineList("hand_firework_recognizable", List.of("twilightforest:triple_bow"), entry -> true);

    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> HAND_ENGRAVE_RECOGNIZABLE = BUILDER
            .comment("其它能使收藏品刻勋之手生效的物品。最好是三叉戟。")
            .defineList("hand_engrave_recognizable", List.of("iceandfire:tide_trident"), entry -> true);

    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> CRIMSON_TREATY_RECOGNIZABLE = BUILDER
            .comment("其它能使收藏品绯红盟约生效的生物。最好来自下界。")
            .defineList("crimson_treaty_recognizable", List.of("cataclysm:ignis", "cataclysm:netherite_monstrosity"), entry -> true);

    public static final ForgeConfigSpec.DoubleValue EP_X_OFFSET = BUILDER
            .comment("元素损伤渲染x轴偏移。正数值为向右偏移")
            .defineInRange("ep_x_offset", 0.0, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);

    public static final ForgeConfigSpec.DoubleValue EP_Y_OFFSET = BUILDER
            .comment("元素损伤渲染y轴偏移。正数值为向下偏移")
            .defineInRange("ep_y_offset", 0.0, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);

    public static final ForgeConfigSpec.DoubleValue LIFE_POINT_INIT = BUILDER
            .comment("初始目标生命。")
            .defineInRange("initial_life_point", 6.0, 1.0, Double.MAX_VALUE);

    public static final ForgeConfigSpec.DoubleValue LIFE_POINT_LIMIT = BUILDER
            .comment("全局目标生命上限。")
            .defineInRange("life_point_global_limit", 32767.0, 1.0, Double.MAX_VALUE);

    public static final ForgeConfigSpec.DoubleValue SHIELD_LIMIT = BUILDER
            .comment("全局护盾值上限。")
            .defineInRange("player_shield_global_limit", 99999.0, 0.0, Double.MAX_VALUE);

    public static final ForgeConfigSpec.DoubleValue LIFE_X_OFFSET = BUILDER
            .comment("目标生命值渲染x轴偏移。正数值为向右偏移")
            .defineInRange("life_point_x_offset", 0.0, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);

    public static final ForgeConfigSpec.DoubleValue LIFE_Y_OFFSET = BUILDER
            .comment("目标生命值渲染y轴偏移。正数值为向下偏移")
            .defineInRange("life_point_y_offset", 0.0, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);

    public static final ForgeConfigSpec.DoubleValue SHIELD_X_OFFSET = BUILDER
            .comment("护盾值渲染x轴偏移，注意为相对目标生命UI的偏移")
            .defineInRange("shield_x_offset", 0.0, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);

    public static final ForgeConfigSpec.DoubleValue SHIELD_Y_OFFSET = BUILDER
            .comment("护盾值渲染y轴偏移，注意为相对目标生命UI的偏移")
            .defineInRange("shield_y_offset", 0.0, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);

    static final ForgeConfigSpec SPEC = BUILDER.build();
    public static boolean epBarStyle;
    public static boolean banAdvancedCollectibles;
    public static double collectibleLootChance;
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
    public static List<? extends String> handStrangleRecognizable;
    public static List<? extends String> handFireworkRecognizable;
    public static List<? extends String> handEngraveRecognizable;
    public static List<? extends String> crimsonTreatyRecognizable;

    @SubscribeEvent
    public static void onLoad(final ModConfigEvent.Loading event) {
        epBarStyle = EP_BAR_STYLE.get();
        banAdvancedCollectibles = BAN_ADVANCED_COLLECTIBLES.get();
        collectibleLootChance = COLLECTIBLE_LOOT_CHANCE.get();
        epXOffset = EP_X_OFFSET.get();
        epYOffset = EP_Y_OFFSET.get();
        lifePointInit = LIFE_POINT_INIT.get();
        lifePointLimit = LIFE_POINT_LIMIT.get();
        shieldLimit = SHIELD_LIMIT.get();
        lifeXOffset = LIFE_X_OFFSET.get();
        lifeYOffset = LIFE_Y_OFFSET.get();
        shieldXOffset = SHIELD_X_OFFSET.get();
        shieldYOffset = SHIELD_Y_OFFSET.get();
        handStrangleRecognizable = HAND_STRANGLE_RECOGNIZABLE.get();
        handFireworkRecognizable = HAND_FIREWORK_RECOGNIZABLE.get();
        handEngraveRecognizable = HAND_ENGRAVE_RECOGNIZABLE.get();
        crimsonTreatyRecognizable = CRIMSON_TREATY_RECOGNIZABLE.get();
    }

    public static void onReloading(final ModConfigEvent.Reloading event) {
        epBarStyle = EP_BAR_STYLE.get();
        banAdvancedCollectibles = BAN_ADVANCED_COLLECTIBLES.get();
        collectibleLootChance = COLLECTIBLE_LOOT_CHANCE.get();
        epXOffset = EP_X_OFFSET.get();
        epYOffset = EP_Y_OFFSET.get();
        lifePointInit = LIFE_POINT_INIT.get();
        lifePointLimit = LIFE_POINT_LIMIT.get();
        shieldLimit = SHIELD_LIMIT.get();
        lifeXOffset = LIFE_X_OFFSET.get();
        lifeYOffset = LIFE_Y_OFFSET.get();
        shieldXOffset = SHIELD_X_OFFSET.get();
        shieldYOffset = SHIELD_Y_OFFSET.get();
        handStrangleRecognizable = HAND_STRANGLE_RECOGNIZABLE.get();
        handFireworkRecognizable = HAND_FIREWORK_RECOGNIZABLE.get();
        handEngraveRecognizable = HAND_ENGRAVE_RECOGNIZABLE.get();
        crimsonTreatyRecognizable = CRIMSON_TREATY_RECOGNIZABLE.get();
    }
}