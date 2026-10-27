package com.susen36.babel.item;

import com.susen36.babel.api.event.LifePointConsumeEvent;
import com.susen36.babel.collectible.CollectibleActivation;
import com.susen36.babel.collectible.CollectibleItem;
import com.susen36.babel.collectible.CollectibleTiers;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

import java.util.List;

public class RainbowJarItem extends CollectibleItem.CustomCollectibleItem {

    public RainbowJarItem() {
        super(new Item.Properties().stacksTo(1).rarity(Rarity.RARE), 25, false, CollectibleTiers.RARE,
                new CollectibleItem.Levels(0, 1, 0),
                CollectibleActivation.builder()
                        .sound(SoundEvents.AMETHYST_BLOCK_CHIME, 2F, 1F)
                        .particle(ParticleTypes.END_ROD, 72)
                        .showOverlay(true)
                        .build());
    }

    @Override
    public void onLifePointLoss(LifePointConsumeEvent event, Player player, int layer) {
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 300, 1));
        List<MobEffectInstance> harmful = player.getActiveEffects().stream()
                .filter(instance -> instance.getEffect().getCategory() == MobEffectCategory.HARMFUL)
                .toList();
        if (!harmful.isEmpty()) {
            player.removeEffect(harmful.get(player.getRandom().nextInt(harmful.size())).getEffect());
        }
    }
}