package com.susen36.babel.item;

import com.susen36.babel.collectible.CollectibleActivation;
import com.susen36.babel.collectible.CollectibleItem;
import com.susen36.babel.collectible.CollectibleTiers;
import com.susen36.babel.collectible.Collectibles;
import com.susen36.babel.init.BabelCollectible;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class HandOfSpeedItem extends CollectibleItem.CustomCollectibleItem {

    public HandOfSpeedItem() {
        super(new Item.Properties().stacksTo(1), 25, false, CollectibleTiers.ADVANCED,
                new CollectibleItem.Levels(0, 1, 0),
                CollectibleActivation.builder()
                        .sound(SoundEvents.PLAYER_LEVELUP, 2F, 1F)
                        .particle(ParticleTypes.CLOUD, 72)
                        .showOverlay(true)
                        .build());
        addConditionalAttributeModifier(Attributes.ATTACK_SPEED, 1.8, AttributeModifier.Operation.ADDITION,
                (player, self) -> isAlone(player) ? 1 : 0);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int tickCount, int layer) {
        return tickCount % 10 == 0;
    }

    @Override
    public void applyEffectTick(Player player, int layer) {
        if (!player.level().isClientSide() && player.getMainHandItem().is(ItemTags.PICKAXES) && isAlone(player)) {
            MobEffectInstance haste = player.getEffect(MobEffects.DIG_SPEED);
            if (haste == null || haste.getAmplifier() < 2) {
                player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 20, 2));
            }
        }
    }

    private static boolean isAlone(Player player) {
        boolean alone = false;
        if (Collectibles.isUsed(player, BabelCollectible.HAND_OF_SPEED)) {
            AABB area = AABB.ofSize(new Vec3(player.getX(), player.getY(), player.getZ()), 8, 8, 8);
            alone = true;
            for (LivingEntity nearby : player.level().getEntitiesOfClass(LivingEntity.class, area)) {
                if (nearby != player && (nearby instanceof Player
                        || (nearby instanceof OwnableEntity ownable && ownable.getOwner() instanceof Player))) {
                    alone = false;
                    break;
                }
            }
        }
        return alone;
    }
}