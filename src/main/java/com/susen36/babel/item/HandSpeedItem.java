package com.susen36.babel.item;

import com.susen36.babel.collectible.CollectibleActivation;
import com.susen36.babel.collectible.CollectibleItem;
import com.susen36.babel.collectible.Collectibles;
import com.susen36.babel.collectible.CollectibleTiers;
import com.susen36.babel.init.BabelCollectible;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class HandSpeedItem extends CollectibleItem.CustomCollectibleItem {

    public HandSpeedItem() {
        super(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC), 25, false, CollectibleTiers.ADVANCED,
                new CollectibleItem.Levels(0, 1, 0),
                CollectibleActivation.builder()
                        .sound(SoundEvents.PLAYER_LEVELUP, 2F, 1F)
                        .particle(ParticleTypes.CLOUD, 72)
                        .showOverlay(true)
                        .build());
        addConditionalAttributeModifier(Attributes.ATTACK_SPEED, 0.6, AttributeModifier.Operation.ADDITION,
                (player, self) -> isMiningAlone(player) ? 1 : 0);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int tickCount, int layer) {
        return tickCount % 10 == 0;
    }

    @Override
    public void applyEffectTick(Player player, int layer) {
        if (!player.level().isClientSide() && isMiningAlone(player)) {
            MobEffectInstance haste = player.getEffect(MobEffects.DIG_SPEED);
            if (haste == null || haste.getAmplifier() < 2) {
                player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 20, 2));
            }
        }
    }

    private static boolean isMiningAlone(Player player) {
        boolean alone = false;
        if (Collectibles.getData(player).isUsed(BabelCollectible.HAND_SPEED.get())
                && player.getMainHandItem().is(ItemTags.PICKAXES)) {
            AABB area = AABB.ofSize(new Vec3(player.getX(), player.getY(), player.getZ()), 8, 8, 8);
            alone = true;
            for (LivingEntity nearby : player.level().getEntitiesOfClass(LivingEntity.class, area)) {
                if (nearby != player && (nearby instanceof Player || nearby instanceof Animal)) {
                    alone = false;
                    break;
                }
            }
        }
        return alone;
    }
}
