package com.susen36.babel.item;

import com.susen36.babel.collectible.CollectibleActivation;
import com.susen36.babel.collectible.CollectibleItem;
import com.susen36.babel.collectible.CollectibleTiers;
import com.susen36.babel.util.LifePointUtils;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;

public class LakebedAegisItem extends CollectibleItem.CustomCollectibleItem {

    public LakebedAegisItem() {
        super(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON), 25, false, CollectibleTiers.RARE,
                new CollectibleItem.Levels(0, 1, 0),
                CollectibleActivation.builder()
                        .sound(SoundEvents.AMETHYST_BLOCK_RESONATE, 2F, 1F)
                        .particle(ParticleTypes.NAUTILUS, 72)
                        .showOverlay(true)
                        .build());
        addConditionalAttributeModifier(Attributes.MAX_HEALTH, 0.60D, AttributeModifier.Operation.MULTIPLY_BASE,
                (player, self) -> LifePointUtils.getShieldPoint(player) > 0 ? 1 : 0);
    }

    @Override
    public void onEffectStarted(ItemStack stack, Level level, Player player, int layer) {
        LifePointUtils.setShieldPoint(player, LifePointUtils.getShieldPoint(player) + 1);
    }
}
