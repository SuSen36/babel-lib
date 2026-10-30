package com.susen36.babel.collectible;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
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
    private static final Map<UUID, Map<UUID, RangeHit>> RANGE_TRACKER = new HashMap<>();

    private record RangeHit(Attribute attribute, Set<UUID> entityIds) {
    }

    private CollectibleAttributes() {
    }

    public static void reconcile(Player player) {
        if (!player.level().isClientSide) {
            var used = Collectibles.getData(player);
            for (RegistryObject<Item> entry : Collectibles.Collectibles.values()) {
                Item item = entry.get();
                if (item instanceof CollectibleItem.CustomCollectibleItem collectible) {
                    for (CollectibleItem.CustomCollectibleItem.AttributeBonus bonus : collectible.getAttributeBonuses()) {
                        int stacks = used.isUsed(item) ? bonus.stacks().getStacks(player, collectible) : 0;
                        AttributeInstance instance = player.getAttribute(bonus.attribute());
                        if (instance != null) {
                            ModifierIdentity identity = modifierIdentity(item, bonus.attribute(), "");
                            AttributeModifier existing = instance.getModifier(identity.id());
                            if (stacks > 0) {
                                double expected = bonus.perStack() * stacks;
                                if (existing == null || existing.getAmount() != expected) {
                                    if (existing != null) {
                                        instance.removeModifier(identity.id());
                                    }
                                    instance.addTransientModifier(new AttributeModifier(identity.id(), identity.name(), expected, bonus.operation()));
                                }
                            } else if (existing != null) {
                                instance.removeModifier(identity.id());
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * 范围属性修饰符对账（每 20 tick）：为生效收藏品按声明范围补齐或更新修饰符；
     * 上一轮命中但已离开范围、或收藏品已失效的实体一律移除，保证修饰符自动过期。
     */
    public static void reconcileRange(Player player) {
        if (player.level() instanceof ServerLevel serverLevel) {
            Map<UUID, RangeHit> tracked = RANGE_TRACKER.computeIfAbsent(player.getUUID(), id -> new HashMap<>());
            var used = Collectibles.getData(player);
            Set<UUID> expected = new HashSet<>();
            for (RegistryObject<Item> entry : Collectibles.Collectibles.values()) {
                Item item = entry.get();
                if (used.isUsed(item) && item instanceof CollectibleItem.CustomCollectibleItem collectible) {
                    for (CollectibleItem.CustomCollectibleItem.RangeAttributeBonus bonus : collectible.getRangeAttributeBonuses()) {
                        ModifierIdentity identity = modifierIdentity(item, bonus.attribute(), bonus.idSuffix());
                        UUID modifierId = identity.id();
                        expected.add(modifierId);
                        List<? extends LivingEntity> entities = serverLevel.getEntitiesOfClass(bonus.entityType(),
                                new AABB(player.position(), player.position()).inflate(bonus.range()));
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
                                if (existing == null || existing.getAmount() != bonus.amount()) {
                                    if (existing != null) {
                                        instance.removeModifier(modifierId);
                                    }
                                    instance.addTransientModifier(new AttributeModifier(
                                            modifierId, identity.name(), bonus.amount(), bonus.operation()));
                                }
                            }
                        }
                        RangeHit previous = tracked.get(modifierId);
                        if (previous != null) {
                            for (UUID entityId : previous.entityIds()) {
                                if (!hitIds.contains(entityId)) {
                                    removeModifierAt(serverLevel, entityId, previous.attribute(), modifierId);
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
                        removeModifierAt(serverLevel, entityId, entry.getValue().attribute(), entry.getKey());
                    }
                    stale.remove();
                }
            }
        }
    }

    /** 玩家登出时移除仍存活实体上的遗留范围修饰符并清空对账记录；键的移除不依赖 level 类型，防止条目滞留。 */
    public static void clearRangeModifiers(Player player) {
        Map<UUID, RangeHit> tracked = RANGE_TRACKER.remove(player.getUUID());
        if (tracked != null && player.level() instanceof ServerLevel serverLevel) {
            for (Map.Entry<UUID, RangeHit> entry : tracked.entrySet()) {
                for (UUID entityId : entry.getValue().entityIds()) {
                    removeModifierAt(serverLevel, entityId, entry.getValue().attribute(), entry.getKey());
                }
            }
        }
    }

    private static void removeModifierAt(ServerLevel level, UUID entityId, Attribute attribute, UUID modifierId) {
        if (level.getEntity(entityId) instanceof LivingEntity living) {
            AttributeInstance instance = living.getAttribute(attribute);
            if (instance != null) {
                instance.removeModifier(modifierId);
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
            String name = Collectibles.getIdByItem(key.collectible()) + "/" + ForgeRegistries.ATTRIBUTES.getKey(key.attribute()).getPath() + (key.idSuffix().isEmpty() ? "" : "/" + key.idSuffix());
            return new ModifierIdentity(name, UUID.nameUUIDFromBytes(name.getBytes(StandardCharsets.UTF_8)));
        });
    }
}