package com.susen36.babel.item;

import com.susen36.babel.collectible.CollectibleActivation;
import com.susen36.babel.collectible.CollectibleItem;
import com.susen36.babel.collectible.CollectibleTiers;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;

public class EmergencyActiveAgentItem extends CollectibleItem.CustomCollectibleItem {

    public EmergencyActiveAgentItem() {
        super(new Item.Properties().stacksTo(1), 25, false, CollectibleTiers.RARE,
                new CollectibleItem.Levels(0, 1, 0),
                CollectibleActivation.builder()
                        .sound(SoundEvents.GENERIC_DRINK, 2F, 1F)
                        .particle(ParticleTypes.EFFECT, 72)
                        .showOverlay(true)
                        .build());
        // 每满 1% 生命值缺失叠 1 层，每层 +0.6% 攻击速度，30% 生命值时 100 层即 +60%
        addConditionalAttributeModifier(Attributes.ATTACK_SPEED, 0.006, AttributeModifier.Operation.MULTIPLY_BASE, (player, ignoredStacks) -> missingHealthStacks(player));
    }

    private static int missingHealthStacks(Player player) {
        float ratio = player.getHealth() / player.getMaxHealth();
        return Mth.clamp(Mth.floor((1.0F - ratio) * 100.0F * 100.0F / 70.0F), 0, 100);
    }
}