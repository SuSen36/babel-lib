package com.susen36.babel.collectible;

import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public final class CollectibleAttributes {

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
                            AttributeModifier existing = instance.getModifier(modifierId(item, bonus.attribute()));
                            if (stacks > 0) {
                                double expected = bonus.perStack() * stacks;
                                if (existing == null || existing.getAmount() != expected) {
                                    if (existing != null) {
                                        instance.removeModifier(modifierId(item, bonus.attribute()));
                                    }
                                    instance.addTransientModifier(new AttributeModifier(modifierId(item, bonus.attribute()), modifierName(item, bonus.attribute()), expected, bonus.operation()));
                                }
                            } else if (existing != null) {
                                instance.removeModifier(modifierId(item, bonus.attribute()));
                            }
                        }
                    }
                }
            }
        }
    }

    // TODO ID 仅按「收藏品 + 属性」区分，同一物品声明两条同属性 bonus 会生成相同 ID，reconcile 时后一条覆盖前一条；需要并存时须为 ID 增加序号维度
    private static String modifierName(Item collectible, Attribute attribute) {
        return Collectibles.getIdByItem(collectible) + "/" + ForgeRegistries.ATTRIBUTES.getKey(attribute).getPath();
    }

    private static UUID modifierId(Item collectible, Attribute attribute) {
        return UUID.nameUUIDFromBytes(modifierName(collectible, attribute).getBytes(StandardCharsets.UTF_8));
    }
}