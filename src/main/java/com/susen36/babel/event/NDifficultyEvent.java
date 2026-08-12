package com.susen36.babel.event;

import com.susen36.babel.BabelMod;
import com.susen36.babel.difficulty.NDifficulty;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.LevelAccessor;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

/**
 * 难度（探索等级）通用实体应用逻辑。
 * <p>
 * 仅复制 N18 相关通用部分：实体生成时按 {@link NDifficulty#multiplier} 提升
 * 生命、攻击、盔甲值与盔甲韧性，并保持当前血量百分比。不包含任何海嗣专属依赖。
 */
@EventBusSubscriber(modid = BabelMod.MODID)
public class NDifficultyEvent {

    /** 强制应用标签：加入该标签的实体即使不属于默认敌对目标也会应用难度倍率。 */
    public static final TagKey<EntityType<?>> APPLIES_DIFFICULTY = TagKey.create(
            Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(BabelMod.MODID, "applies_difficulty"));

    /** 绕过标签：加入该标签的实体即使属于默认敌对目标也不会应用难度倍率。 */
    public static final TagKey<EntityType<?>> BYPASSES_DIFFICULTY = TagKey.create(
            Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(BabelMod.MODID, "bypasses_difficulty"));

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof LivingEntity living)) return;
        if (!shouldApply(entity)) return;

        LevelAccessor world = event.getLevel();
        double n = NDifficulty.multiplier(world);
        if (n <= 1.0) return;

        double percentage = living.getHealth() / living.getMaxHealth();

        AttributeInstance maxHealth = living.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealth != null) {
            maxHealth.setBaseValue(maxHealth.getBaseValue() * n);
        }
        AttributeInstance armor = living.getAttribute(Attributes.ARMOR);
        if (armor != null) {
            armor.setBaseValue(armor.getBaseValue() * n);
        }
        AttributeInstance armorToughness = living.getAttribute(Attributes.ARMOR_TOUGHNESS);
        if (armorToughness != null) {
            armorToughness.setBaseValue(armorToughness.getBaseValue() * n);
        }
        AttributeInstance attackDamage = living.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attackDamage != null) {
            attackDamage.setBaseValue(attackDamage.getBaseValue() * n);
        }

        AttributeInstance newMaxHealth = living.getAttribute(Attributes.MAX_HEALTH);
        if (newMaxHealth != null) {
            living.setHealth((float) (newMaxHealth.getValue() * percentage));
        }
    }

    private static boolean shouldApply(Entity entity) {
        EntityType<?> type = entity.getType();
        if (type.is(BYPASSES_DIFFICULTY)) return false;
        if (type.is(APPLIES_DIFFICULTY)) return true;
        return entity instanceof Monster;
    }
}