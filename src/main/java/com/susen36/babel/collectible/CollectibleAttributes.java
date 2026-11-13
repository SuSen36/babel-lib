package com.susen36.babel.collectible;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class CollectibleAttributes {

    // 范围修饰符对账记录：玩家 UUID -> (修饰符 ID -> 上轮命中实体)；修饰符均为 transient 不入档，卸载/死亡由存档特性兜底
    private static final Map<UUID, Map<UUID, RangeHit>> RANGE_TRACKER = new ConcurrentHashMap<>();

    private record RangeHit(Attribute attribute, Set<UUID> entityIds) {
    }

    private CollectibleAttributes() {
    }

    public static void reconcile(Player player) {
        if (!player.level().isClientSide) {
            var used = Collectibles.getData(player);
            boolean healthChanged = false;
            for (RegistryObject<Item> entry : Collectibles.REGISTRY.values()) {
                Item item = entry.get();
                if (item instanceof CollectibleItem.CustomCollectibleItem collectible) {
                    for (CollectibleItem.CustomCollectibleItem.AttributeBonus bonus : collectible.getAttributeBonuses()) {
                        int stacks = 0;
                        if (used != null) {
                            stacks = used.isUsed(item) ? bonus.stacks().getStacks(player, collectible) : 0;
                        }
                        AttributeInstance instance = player.getAttribute(bonus.attribute());
                        if (instance != null) {
                            ModifierIdentity identity = modifierIdentity(item, bonus.attribute(), "");
                            AttributeModifier existing = instance.getModifier(identity.id());
                            if (stacks > 0) {
                                double expected = bonus.perStack() * stacks;
                                if (existing == null || !Mth.equal(existing.getAmount(), expected)) {
                                    if (existing != null) {
                                        instance.removeModifier(identity.id());
                                    }
                                    instance.addTransientModifier(new AttributeModifier(identity.id(), identity.name(), expected, bonus.operation()));
                                    healthChanged |= bonus.attribute() == Attributes.MAX_HEALTH;
                                }
                            } else if (existing != null) {
                                instance.removeModifier(identity.id());
                                healthChanged |= bonus.attribute() == Attributes.MAX_HEALTH;
                            }
                        }
                    }
                }
            }
            if (healthChanged) {
                syncHealth(player);
            }
        }
    }

    /**
     * 范围属性修饰符对账（每 20 tick）：为生效收藏品按声明范围补齐或更新修饰符；
     * 上一轮命中但已离开范围、或收藏品已失效的实体一律移除，保证修饰符自动过期。
     */
    public static void reconcileRange(Player player) {
        if (player.level() instanceof ServerLevel serverLevel) {
            Map<UUID, RangeHit> tracked = RANGE_TRACKER.computeIfAbsent(player.getUUID(), id -> new ConcurrentHashMap<>());
            var used = Collectibles.getData(player);
            Set<UUID> expected = new HashSet<>();
            Set<LivingEntity> healthTargets = new HashSet<>();
            for (RegistryObject<Item> entry : Collectibles.REGISTRY.values()) {
                Item item = entry.get();
                if (used != null && used.isUsed(item) && item instanceof CollectibleItem.CustomCollectibleItem collectible) {
                    for (CollectibleItem.CustomCollectibleItem.RangeAttributeBonus bonus : collectible.getRangeAttributeBonuses()) {
                        ModifierIdentity identity = modifierIdentity(item, bonus.attribute(), bonus.idSuffix());
                        UUID modifierId = identity.id();
                        expected.add(modifierId);
                        List<? extends LivingEntity> entities = serverLevel.getEntitiesOfClass(bonus.entityType(),
                                new AABB(player.position(), player.position()).inflate(bonus.range()), LivingEntity::isAlive);
                        Set<UUID> hitIds = new HashSet<>();
                        for (LivingEntity living : entities) {
                            // 持有者自身与未通过归属过滤的实体不参与范围修饰符对账
                            if (living == player || (bonus.ownerFilter() != null && !bonus.ownerFilter().test(living, player))) {
                                continue;
                            }
                            AttributeInstance instance = living.getAttribute(bonus.attribute());
                            if (instance != null) {
                                hitIds.add(living.getUUID());
                                AttributeModifier existing = instance.getModifier(modifierId);
                                if (existing == null || !Mth.equal(existing.getAmount(), bonus.amount())) {
                                    double oldMax = instance.getValue();
                                    instance.removeModifier(modifierId);
                                    instance.addTransientModifier(new AttributeModifier(
                                            modifierId, identity.name(), bonus.amount(), bonus.operation()));
                                    syncHealth(living, bonus.attribute(), oldMax);
                                }
                                if (bonus.attribute() == Attributes.MAX_HEALTH) {
                                    healthTargets.add(living);
                                }
                            }
                        }
                        RangeHit previous = tracked.get(modifierId);
                        if (previous != null) {
                            for (UUID entityId : previous.entityIds()) {
                                if (!hitIds.contains(entityId)) {
                                    removeModifierAt(serverLevel, entityId, previous.attribute(), modifierId, healthTargets);
                                }
                            }
                        }
                        tracked.put(modifierId, new RangeHit(bonus.attribute(), hitIds));
                    }
                }
            }
            Iterator<Map.Entry<UUID, RangeHit>> stale = tracked.entrySet().iterator();
            while (stale.hasNext()) {
                Map.Entry<UUID, RangeHit> entry = stale.next();
                if (!expected.contains(entry.getKey())) {
                    for (UUID entityId : entry.getValue().entityIds()) {
                        removeModifierAt(serverLevel, entityId, entry.getValue().attribute(), entry.getKey(), healthTargets);
                    }
                    stale.remove();
                }
            }
            for (LivingEntity living : healthTargets) {
                syncHealth(living);
            }
        }
    }

    /** 玩家登出时移除仍存活实体上的遗留范围修饰符并清空对账记录；键的移除不依赖 level 类型，防止条目滞留。 */
    public static void clearRangeModifiers(Player player) {
        Map<UUID, RangeHit> tracked = RANGE_TRACKER.remove(player.getUUID());
        if (tracked != null && player.level() instanceof ServerLevel serverLevel) {
            Set<LivingEntity> healthTargets = new HashSet<>();
            for (Map.Entry<UUID, RangeHit> entry : tracked.entrySet()) {
                for (UUID entityId : entry.getValue().entityIds()) {
                    removeModifierAt(serverLevel, entityId, entry.getValue().attribute(), entry.getKey(), healthTargets);
                }
            }
            for (LivingEntity living : healthTargets) {
                syncHealth(living);
            }
        }
    }

    private static void removeModifierAt(ServerLevel level, UUID entityId, Attribute attribute, UUID modifierId, Set<LivingEntity> healthTargets) {
        if (level.getEntity(entityId) instanceof LivingEntity living) {
            AttributeInstance instance = living.getAttribute(attribute);
            if (instance != null) {
                double oldMax = instance.getValue();
                instance.removeModifier(modifierId);
                syncHealth(living, attribute, oldMax);
                if (attribute == Attributes.MAX_HEALTH) {
                    healthTargets.add(living);
                }
            }
        }
    }

    /** 生命上限被修饰符改变后，按比例同步当前生命；旧上限异常时按满血处理，避免除零产生 Infinity/NaN */
    private static void syncHealth(LivingEntity living, Attribute attribute, double oldMax) {
        AttributeInstance instance = living.getAttribute(Attributes.MAX_HEALTH);
        if (attribute == Attributes.MAX_HEALTH && instance != null && !Mth.equal(instance.getValue(), oldMax)) {
            double newMax = instance.getValue();
            float ratio = oldMax > 0.0D ? (float) (living.getHealth() / oldMax) : 1.0F;
            living.getEntityData().set(LivingEntity.DATA_HEALTH_ID,
                    (float) newMax * Mth.clamp(ratio, 0.0F, 1.0F));
        }
    }

    /**
     * 整轮修饰符增删完成后再钳制，避免中间上限造成额外扣血。
     */
    private static void syncHealth(LivingEntity living) {
        AttributeInstance instance = living.getAttribute(Attributes.MAX_HEALTH);
        if (instance != null) {
            float newMax = (float) instance.getValue();
            float health = living.getHealth();
            if (health > newMax) {
                living.getEntityData().set(LivingEntity.DATA_HEALTH_ID, newMax);
            }
        }
    }

    // 注册表冻结后修饰符 name 与 UUID 恒定不变；同属性多条 bonus 靠 idSuffix 区分
    private static final Map<ModifierKey, ModifierIdentity> MODIFIER_IDENTITIES = new ConcurrentHashMap<>();

    private record ModifierKey(Item collectible, Attribute attribute, String idSuffix) {
    }

    private record ModifierIdentity(String name, UUID id) {
    }

    private static ModifierIdentity modifierIdentity(Item collectible, Attribute attribute, String idSuffix) {
        return MODIFIER_IDENTITIES.computeIfAbsent(new ModifierKey(collectible, attribute, idSuffix), key -> {
            // Item 实例必然已在物品注册表，注册键即稳定唯一标识，无需校验收藏品注册状态
            String name = ForgeRegistries.ITEMS.getKey(key.collectible())
                    + "/" + ForgeRegistries.ATTRIBUTES.getKey(key.attribute()).getPath()
                    + (key.idSuffix().isEmpty() ? "" : "/" + key.idSuffix());
            return new ModifierIdentity(name, UUID.nameUUIDFromBytes(name.getBytes(StandardCharsets.UTF_8)));
        });
    }
}