package com.susen36.babel.item;

import com.susen36.babel.collectible.CollectibleActivation;
import com.susen36.babel.collectible.CollectibleItem;
import com.susen36.babel.collectible.CollectibleTiers;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

public class LegendChitinItem extends CollectibleItem.CustomCollectibleItem {

    public LegendChitinItem() {
        super(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON), 25, false, CollectibleTiers.NORMAL,
                new CollectibleItem.Levels(0, 1, 0),
                CollectibleActivation.builder()
                        .sound(SoundEvents.PLAYER_LEVELUP, 2F, 1F)
                        .particle(ParticleTypes.HAPPY_VILLAGER, 72)
                        .showOverlay(true)
                        .build());
        addAttributeModifier(Attributes.ATTACK_DAMAGE, 1.0, AttributeModifier.Operation.MULTIPLY_TOTAL);
        addAttributeModifier(Attributes.MAX_HEALTH, 1.0, AttributeModifier.Operation.MULTIPLY_TOTAL);
    }

    @Override
    public void onAttackHit(LivingHurtEvent event, Player player, LivingEntity target, int layer) {
        if (player.getRandom().nextFloat() < 2.0F) {
            Level level = player.level();
            float maxHealth = player.getMaxHealth();
            double healthPercent = maxHealth <= 0.0f ? 0.0 : Mth.clamp(player.getHealth() / (double) maxHealth, 0.0, 1.0);
            if (healthPercent > 0) {
                player.setHealth((float) (player.getMaxHealth() * healthPercent));
            }
            if (level.isClientSide()) {
                Vec3 targetPosition = target.position();
                level.playLocalSound(targetPosition.x, targetPosition.y, targetPosition.z,
                        SoundEvents.BEACON_ACTIVATE, SoundSource.NEUTRAL, 3.2F, 1, false);
            }
        }
    }
}
