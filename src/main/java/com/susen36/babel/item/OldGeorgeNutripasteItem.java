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

public class OldGeorgeNutripasteItem extends CollectibleItem.CustomCollectibleItem {

    public OldGeorgeNutripasteItem() {
        super(new Item.Properties().stacksTo(1), 25, false, CollectibleTiers.RARE,
                new CollectibleItem.Levels(0, 1, 0),
                CollectibleActivation.builder()
                        .sound(SoundEvents.GENERIC_DRINK, 2F, 1F)
                        .particle(ParticleTypes.EFFECT, 72)
                        .showOverlay(true)
                        .build());
        // 每满 1% 生命值叠 1 层，每层 +0.3% 攻击力，满生命 100 层即 +30%
        addConditionalAttributeModifier(Attributes.ATTACK_DAMAGE, 0.003, AttributeModifier.Operation.MULTIPLY_BASE, (player, ignoredStacks) -> healthStacks(player));
    }

    private static int healthStacks(Player player) {
        float ratio = player.getHealth() / player.getMaxHealth();
        return Mth.clamp(Mth.floor(ratio * 100.0F), 0, 100);
    }
}