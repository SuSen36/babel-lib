package com.susen36.babel;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.List;

public class BabelConfig {
    private static final ForgeConfigSpec.Builder COMMON = new ForgeConfigSpec.Builder();
    private static final ForgeConfigSpec.Builder CLIENT = new ForgeConfigSpec.Builder();

    public static final ForgeConfigSpec.BooleanValue EP_BAR_STYLE = CLIENT
            .comment("元素损伤显示采用和饥饿值相似的小图标，若关闭则为圆环")
            .define("ep_icon_style", false);

    public static final ForgeConfigSpec.BooleanValue BAN_ADVANCED_COLLECTIBLES = COMMON
            .comment("禁用高级（ADVANCED）收藏品：无法激活")
            .define("ban_advanced_collectibles", false);

    public static final ForgeConfigSpec.DoubleValue COLLECTIBLE_LOOT_CHANCE = COMMON
            .comment("收藏品刷新概率。在每次触发战利品表（如打开箱子）时以此概率额外随机加入一件收藏品")
            .defineInRange("collectible_loot_chance", 0.3, 0.0, 1.0);

    public static final ForgeConfigSpec.LongValue COLLECTIBLE_DURATION_TICKS = COMMON
            .comment("收藏品激活后的持续时长（tick），-1 表示永久有效。修改也适用于已激活的收藏品")
            .defineInRange("collectible_duration_ticks", 864000L, -1L, Long.MAX_VALUE);

    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> HAND_STRANGLE_RECOGNIZABLE = COMMON
            .comment("其它能使收藏品扼喉之手生效的物品。最好是弩。用 * 号代表该命名空间下的所有物品，如 tacz:*, babel:oceanized_* 等，下同")
            .defineListAllowEmpty("hand_strangle_recognizable", List.of(), BabelConfig::isValidRegistryRule);

    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> HAND_FIREWORK_RECOGNIZABLE = COMMON
            .comment("其它能使收藏品烟花之手生效的物品。最好是弓。")
            .defineListAllowEmpty("hand_firework_recognizable", List.of("twilightforest:triple_bow"), BabelConfig::isValidRegistryRule);

    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> HAND_ENGRAVE_RECOGNIZABLE = COMMON
            .comment("其它能使收藏品刻勋之手生效的物品。最好是三叉戟。")
            .defineListAllowEmpty("hand_engrave_recognizable", List.of("iceandfire:tide_trident"), BabelConfig::isValidRegistryRule);

    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> ROYAL_ALLIANCE_TREATY_RECOGNIZABLE = COMMON
            .comment("其它能使收藏品王庭盟约生效的生物。最好是萨卡兹或来自下界。")
            .defineListAllowEmpty("royal_alliance_treaty_recognizable", List.of("cataclysm:ignis", "cataclysm:netherite_monstrosity"), BabelConfig::isValidRegistryRule);

    public static final ForgeConfigSpec.DoubleValue EP_X_OFFSET = CLIENT
            .comment("元素损伤渲染x轴偏移。正数值为向右偏移")
            .defineInRange("ep_x_offset", 0.0, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);

    public static final ForgeConfigSpec.DoubleValue EP_Y_OFFSET = CLIENT
            .comment("元素损伤渲染y轴偏移。正数值为向下偏移")
            .defineInRange("ep_y_offset", 0.0, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);

    public static final ForgeConfigSpec.DoubleValue LIFE_POINT_INIT = COMMON
            .comment("初始目标生命。")
            .defineInRange("initial_life_point", 6.0, 1.0, Double.MAX_VALUE);

    public static final ForgeConfigSpec.DoubleValue LIFE_POINT_LIMIT = COMMON
            .comment("全局目标生命上限。")
            .defineInRange("life_point_global_limit", 32767.0, 1.0, Double.MAX_VALUE);

    public static final ForgeConfigSpec.DoubleValue SHIELD_LIMIT = COMMON
            .comment("全局护盾值上限。")
            .defineInRange("player_shield_global_limit", 99999.0, 0.0, Double.MAX_VALUE);

    public static final ForgeConfigSpec.DoubleValue LIFE_X_OFFSET = CLIENT
            .comment("目标生命值渲染x轴偏移。正数值为向右偏移")
            .defineInRange("life_point_x_offset", 0.0, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);

    public static final ForgeConfigSpec.DoubleValue LIFE_Y_OFFSET = CLIENT
            .comment("目标生命值渲染y轴偏移。正数值为向下偏移")
            .defineInRange("life_point_y_offset", 0.0, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);

    public static final ForgeConfigSpec.DoubleValue SHIELD_X_OFFSET = CLIENT
            .comment("护盾值渲染x轴偏移，注意为相对目标生命UI的偏移")
            .defineInRange("shield_x_offset", 0.0, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);

    public static final ForgeConfigSpec.DoubleValue SHIELD_Y_OFFSET = CLIENT
            .comment("护盾值渲染y轴偏移，注意为相对目标生命UI的偏移")
            .defineInRange("shield_y_offset", 0.0, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);

    public static final ForgeConfigSpec.BooleanValue WITHER_ELEMENTAL_DAMAGE = COMMON
            .comment("凋灵的攻击造成的凋零药水效果替换为凋亡坏死元素损伤")
            .define("wither_elemental_damage", true);

    public static final ForgeConfigSpec.BooleanValue COLD_TO_FROZEN = COMMON
            .comment("两段式寒冷转化：首次冻结伤害仅标记寒冷（原版冻结计时），寒冷期间再次受到冻结伤害时转为冰冻药水效果，时长取寒冷剩余时间。关闭后冻结伤害保持原版行为")
            .define("cold_to_frozen", true);

    public static final ForgeConfigSpec.LongValue ROBBERY_RESTRICTION_DURATION_TICKS = COMMON
            .comment("触发坎诺特劫掠后的限制时长，期间坎诺特不再理睬该玩家")
            .defineInRange("robbery_restriction_duration_ticks", 252000L, 0L, Long.MAX_VALUE);

    static final ForgeConfigSpec COMMON_SPEC = COMMON.build();
    static final ForgeConfigSpec CLIENT_SPEC = CLIENT.build();

    public static boolean isValidRegistryRule(Object entry) {
        boolean valid = false;
        if (entry instanceof String rule) {
            int separator = rule.indexOf(':');
            if (rule.equals("*")) {
                valid = true;
            } else if (separator > 0 && separator < rule.length() - 1) {
                int end = rule.endsWith("*") ? rule.length() - 1 : rule.length();
                valid = ResourceLocation.isValidNamespace(rule.substring(0, separator))
                        && ResourceLocation.isValidPath(rule.substring(separator + 1, end));
            }
        }
        return valid;
    }
}