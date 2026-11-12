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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class TopographicalMapItem extends CollectibleItem.CustomCollectibleItem {

    public TopographicalMapItem() {
        super(new Item.Properties().stacksTo(1), 25, false, CollectibleTiers.NORMAL, new CollectibleItem.Levels(0, 1, 0),
                CollectibleActivation.builder()
                        .sound(SoundEvents.PLAYER_LEVELUP, 2F, 1F)
                        .particle(ParticleTypes.HAPPY_VILLAGER, 72)
                        .showOverlay(true)
                        .build());
        addConditionalAttributeModifier(Attributes.ARMOR, 0.01, AttributeModifier.Operation.MULTIPLY_BASE,
                (player, self) -> switch (NDifficulty.difficultyLevel(player.level()).tier()) {
                    case EASY -> 4;
                    case NORMAL -> 5;
                    case HARD -> 6;
                    case EXPERT, PERILOUS -> 8;
                });
    }

    @Override
    public void onEffectStarted(ItemStack stack, Level level, Player player, int layer) {
        if (!level.isClientSide()) {
            player.giveExperienceLevels(1);
        }
    }
}