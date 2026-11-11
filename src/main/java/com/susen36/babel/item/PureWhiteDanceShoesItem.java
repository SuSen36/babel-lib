package com.susen36.babel.item;

import com.susen36.babel.collectible.CollectibleActivation;
import com.susen36.babel.collectible.CollectibleItem;
import com.susen36.babel.collectible.CollectibleTiers;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.CombatRules;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraftforge.event.entity.living.LivingDamageEvent;

public class PureWhiteDanceShoesItem extends CollectibleItem.CustomCollectibleItem {

    private static final float FALL_PROTECTION_CAP = 20.0F;

    public PureWhiteDanceShoesItem() {
        super(new Item.Properties().stacksTo(1), 25, false, CollectibleTiers.RARE,
                new CollectibleItem.Levels(0, 1, 0),
                CollectibleActivation.builder()
                        .sound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, 2F, 1F)
                        .particle(ParticleTypes.END_ROD, 72)
                        .showOverlay(true)
                        .build());
        addAttributeModifier(Attributes.MOVEMENT_SPEED, 0.08, AttributeModifier.Operation.MULTIPLY_TOTAL);
    }

    @Override
    public void onMobHurt(LivingDamageEvent event, Player player, int layer) {
        DamageSource source = event.getSource();
        if (source.is(DamageTypeTags.IS_FALL)) {
            event.setAmount(CombatRules.getDamageAfterMagicAbsorb(event.getAmount(), FALL_PROTECTION_CAP));
        }
    }
}