package com.susen36.babel.item;

import com.susen36.babel.api.event.LifePointConsumeEvent;
import com.susen36.babel.collectible.CollectibleActivation;
import com.susen36.babel.collectible.CollectibleItem;
import com.susen36.babel.collectible.CollectibleTiers;
import com.susen36.babel.init.BabelAttributes;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

public class MedicineSticksItem extends CollectibleItem.CustomCollectibleItem {

    private static final int SHIELD_LAYERS = 2;

    public MedicineSticksItem() {
        super(new Item.Properties().stacksTo(1), 25, false, CollectibleTiers.RARE, new CollectibleItem.Levels(0, 1, 0),
                CollectibleActivation.builder()
                        .sound(SoundEvents.GENERIC_EAT, 2F, 1F)
                        .particle(ParticleTypes.EFFECT, 72)
                        .showOverlay(true)
                        .build());
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.EAT;
    }

    @Override
    public void onEffectStarted(ItemStack stack, Level level, Player player) {
        grantShield(player);
    }

    @Override
    public void onLifePointLoss(LifePointConsumeEvent event, Player player, int layer) {
        grantShield(player);
    }

    private void grantShield(Player player) {
        AttributeInstance instance = player.getAttribute(BabelAttributes.SHIELD.get());
        if (instance != null) {
            instance.setBaseValue(instance.getBaseValue() + SHIELD_LAYERS);
        }
    }
}