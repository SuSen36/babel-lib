package com.susen36.babel.item;

import com.susen36.babel.collectible.CollectibleActivation;
import com.susen36.babel.collectible.CollectibleItem;
import com.susen36.babel.collectible.CollectibleTiers;
import com.susen36.babel.difficulty.NDifficulty;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraftforge.event.entity.living.LivingHealEvent;

public class ForestNightWhispersItem extends CollectibleItem.CustomCollectibleItem {

    public ForestNightWhispersItem() {
        super(new Item.Properties().stacksTo(1), 25, false, CollectibleTiers.RARE,
                new CollectibleItem.Levels(0, 1, 0),
                CollectibleActivation.builder()
                        .sound(SoundEvents.PLAYER_LEVELUP, 2F, 1F)
                        .particle(ParticleTypes.WAX_OFF, 72)
                        .showOverlay(true)
                        .build());
        addConditionalAttributeModifier(Attributes.ARMOR, 0.01, AttributeModifier.Operation.MULTIPLY_BASE,
                (player, self) -> switch (NDifficulty.difficultyLevel(player.level()).tier()) {
                    case EASY -> 6;
                    case NORMAL -> 7;
                    case HARD -> 8;
                    case EXPERT, PERILOUS -> 10;
                });
    }

    @Override
    public void onOwnerHeal(LivingHealEvent event, Player player, int layer) {
        event.setAmount(event.getAmount() + 1.0F);
    }
}