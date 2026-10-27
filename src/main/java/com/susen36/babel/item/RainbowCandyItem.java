package com.susen36.babel.item;

import com.susen36.babel.collectible.CollectibleActivation;
import com.susen36.babel.collectible.CollectibleItem;
import com.susen36.babel.collectible.CollectibleTiers;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class RainbowCandyItem extends CollectibleItem.CustomCollectibleItem {
    public RainbowCandyItem() {
        super(new Item.Properties().stacksTo(64).rarity(Rarity.EPIC).food(new FoodProperties.Builder().nutrition(6).saturationMod(0.5f).alwaysEat().build()), 25, false, CollectibleTiers.NORMAL, new CollectibleItem.Levels(0, 1, 0),
                CollectibleActivation.builder()
                        .sound(SoundEvents.PLAYER_LEVELUP, 2F, 1F)
                        .particle(ParticleTypes.HAPPY_VILLAGER, 72)
                        .showOverlay(true)
                        .build());
    }

    @Override
    protected InteractionResultHolder<ItemStack> onSneakUse(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        double entityReach = player.getEntityReach();
        double blockReach = player.getBlockReach();
        double pickRange = Math.max(entityReach, blockReach);
        Vec3 eye = player.getEyePosition(1.0F);
        Vec3 sight = eye.add(player.getViewVector(1.0F).scale(pickRange));
        HitResult blockHit = player.pick(pickRange, 1.0F, false);
        double limitSqr = pickRange * pickRange;
        if (blockHit.getType() != HitResult.Type.MISS && blockHit.getLocation().distanceToSqr(eye) <= blockReach * blockReach) {
            limitSqr = blockHit.getLocation().distanceToSqr(eye);
        }
        EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(player, eye, sight,
                player.getBoundingBox().expandTowards(sight.subtract(eye)).inflate(1.0D),
                candidate -> candidate != player && !candidate.isSpectator() && candidate.isPickable(), limitSqr);
        InteractionResultHolder<ItemStack> result = InteractionResultHolder.pass(itemstack);
        if (entityHit != null && entityHit.getEntity() instanceof Sheep sheep) {
            if (!level.isClientSide()) {
                sheep.setCustomName(Component.literal("jeb_"));
            }
            if (!player.getAbilities().instabuild) {
                itemstack.shrink(1);
            }
            result = InteractionResultHolder.sidedSuccess(itemstack, level.isClientSide());
        }
        return result;
    }

    @Override
    public void onEffectStarted(ItemStack stack, Level level, Player player, int layer) {
        if (!level.isClientSide()) {
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 280, 1));
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 280, 1));
            player.addEffect(new MobEffectInstance(MobEffects.HEALTH_BOOST, 560, 1));
        }
    }
}