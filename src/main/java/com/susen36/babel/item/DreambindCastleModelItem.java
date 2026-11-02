package com.susen36.babel.item;

import com.susen36.babel.collectible.CollectibleActivation;
import com.susen36.babel.collectible.CollectibleItem;
import com.susen36.babel.collectible.CollectibleTiers;
import com.susen36.babel.collectible.Collectibles;
import com.susen36.babel.init.BabelCollectible;
import com.susen36.babel.init.BabelMobEffects;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraftforge.event.entity.living.LivingDamageEvent;

public class DreambindCastleModelItem extends CollectibleItem.CustomCollectibleItem {

    public DreambindCastleModelItem() {
        super(new Item.Properties().stacksTo(1), 25, false, CollectibleTiers.ADVANCED,
                new CollectibleItem.Levels(0, 1, 0),
                CollectibleActivation.builder()
                        .sound(SoundEvents.AMETHYST_BLOCK_RESONATE, 2F, 1F)
                        .particle(ParticleTypes.END_ROD, 72)
                        .showOverlay(true)
                        .build());
    }

    public static boolean isUsed(Player player) {
        return Collectibles.getData(player).isUsed(BabelCollectible.DREAMBIND_CASTLE_MODEL.get());
    }

    @Override
    public void onDealDamage(LivingDamageEvent event, Player player, LivingEntity target, int layer) {
        if (target.hasEffect(BabelMobEffects.SLEEP.get())) {
            event.setAmount(event.getAmount() * 1.75F);
        }
    }
}