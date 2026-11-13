package com.susen36.babel.item;

import com.susen36.babel.collectible.CollectibleActivation;
import com.susen36.babel.collectible.CollectibleItem;
import com.susen36.babel.collectible.CollectibleTiers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.NaturalSpawner;
import org.jetbrains.annotations.NotNull;

public class OperationFileItem extends CollectibleItem.CustomCollectibleItem {

    public OperationFileItem() {
        super(new Item.Properties().stacksTo(1), 25, false, CollectibleTiers.RARE,
                new CollectibleItem.Levels(0, 1, 0),
                CollectibleActivation.builder()
                        .sound(SoundEvents.IRON_GOLEM_REPAIR, 2F, 1F)
                        .particle(ParticleTypes.POOF, 30)
                        .build());
        addRangeAttributeModifier(IronGolem.class, Attributes.MAX_HEALTH,
                0.5D, AttributeModifier.Operation.MULTIPLY_BASE,
                OperationFileItem::playerCreated);
        addRangeAttributeModifier(IronGolem.class, Attributes.ATTACK_DAMAGE,
                0.5D, AttributeModifier.Operation.MULTIPLY_BASE,
                OperationFileItem::playerCreated);
    }

    private static boolean playerCreated(LivingEntity entity, Player player) {
        return entity instanceof IronGolem golem && golem.isPlayerCreated();
    }

    @Override
    public void onEffectStarted(@NotNull ItemStack stack, @NotNull Level level, @NotNull Player player) {
        if (level instanceof ServerLevel serverLevel) {
            IronGolem golem = EntityType.IRON_GOLEM.create(serverLevel);
            if (golem != null) {
                BlockPos anchor = player.blockPosition();
                BlockPos spawnPos = anchor;
                for (int i = 0; i < 10; i++) {
                    int x = anchor.getX() + serverLevel.getRandom().nextInt(11) - 5;
                    int z = anchor.getZ() + serverLevel.getRandom().nextInt(11) - 5;
                    BlockPos candidate = new BlockPos(x, anchor.getY(), z);
                    if (!candidate.equals(anchor) && NaturalSpawner.isSpawnPositionOk(SpawnPlacements.Type.ON_GROUND, serverLevel, candidate, EntityType.IRON_GOLEM)) {
                        spawnPos = candidate;
                        break;
                    }
                }
                golem.moveTo(spawnPos.getX() + 0.5D, spawnPos.getY(), spawnPos.getZ() + 0.5D,
                        serverLevel.getRandom().nextFloat() * 360.0F, 0.0F);
                golem.setPlayerCreated(true);
                serverLevel.addFreshEntity(golem);
            }
        }
    }
}