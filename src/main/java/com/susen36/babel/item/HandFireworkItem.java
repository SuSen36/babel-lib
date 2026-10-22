package com.susen36.babel.item;

import com.susen36.babel.BabelConfig;
import com.susen36.babel.collectible.CollectibleActivation;
import com.susen36.babel.collectible.CollectibleItem;
import com.susen36.babel.collectible.CollectibleTiers;
import com.susen36.babel.init.BabelDamageTypes;
import com.susen36.babel.init.BabelItemTags;
import com.susen36.babel.util.ServerTasks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.Tags;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

import java.util.List;

public class HandFireworkItem extends CollectibleItem.CustomCollectibleItem {

    public HandFireworkItem() {
        super(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC), 25, false, CollectibleTiers.ADVANCED,
                new CollectibleItem.Levels(0, 1, 0),
                CollectibleActivation.builder()
                        .sound(SoundEvents.PLAYER_LEVELUP, 2F, 1F)
                        .particle(ParticleTypes.CLOUD, 72)
                        .showOverlay(true)
                        .build());
    }

    @Override
    public void onAttackHit(LivingHurtEvent event, Player player, LivingEntity target, int layer) {
        if (!event.isCanceled() && event.getSource().getDirectEntity() != player) {
            Level level = player.level();
            ItemStack mainHandItem = player.getMainHandItem();
            String registryName = BuiltInRegistries.ITEM.getKey(mainHandItem.getItem()).toString();
            if (isFireworkWeapon(mainHandItem, registryName) && player.getRandom().nextFloat() < 0.33F) {
                level.playSound(null, player.blockPosition(), SoundEvents.FIREWORK_ROCKET_LAUNCH, SoundSource.PLAYERS, 3.6F, 1);
                double amount = event.getAmount();
                Vec3 targetPosition = target.position();
                ServerTasks.schedule(10, () -> detonateFirework(level, target, targetPosition, player, amount));
            }
        }
    }

    private static boolean isFireworkWeapon(ItemStack stack, String registryName) {
        return stack.is(Tags.Items.TOOLS_BOWS) || stack.getItem() instanceof BowItem
                || stack.is(BabelItemTags.FIREWORK_WEAPONS)
                || matchesConfigured(BabelConfig.handFireworkRecognizable, registryName);
    }

    private static void detonateFirework(Level level, LivingEntity target, Vec3 targetPosition, Player player, double amount) {
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.FIREWORK, targetPosition.x, targetPosition.y, targetPosition.z, 85, 2, 2, 2, 0.22);
        }
        level.playSound(null, BlockPos.containing(targetPosition), SoundEvents.FIREWORK_ROCKET_TWINKLE, SoundSource.PLAYERS, 3.6F, 1);
        List<LivingEntity> nearbyEntities = level.getEntitiesOfClass(LivingEntity.class,
                new AABB(targetPosition, targetPosition).inflate(2.5),
                candidate -> candidate != player && (candidate.getType() == target.getType() || candidate instanceof Monster));
        for (LivingEntity candidate : nearbyEntities) {
            candidate.hurt(BabelDamageTypes.source(level, BabelDamageTypes.HAND_FIREWORK, player), (float) (amount * 3));
        }
    }
}
