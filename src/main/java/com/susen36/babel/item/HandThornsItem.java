package com.susen36.babel.item;

import com.susen36.babel.BabelMod;
import com.susen36.babel.collectible.CollectibleActivation;
import com.susen36.babel.collectible.CollectibleItem;
import com.susen36.babel.collectible.CollectibleTiers;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingDamageEvent;

public class HandThornsItem extends CollectibleItem.CustomCollectibleItem {

    private static final TagKey<DamageType> IGNORES_THORN_REBOUND = TagKey.create(
            Registries.DAMAGE_TYPE, new ResourceLocation(BabelMod.MODID, "ignores_thorn_rebound"));

    public HandThornsItem() {
        super(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC), 25, false, CollectibleTiers.ADVANCED,
                new CollectibleItem.Levels(0, 1, 0),
                CollectibleActivation.builder()
                        .sound(SoundEvents.PLAYER_LEVELUP, 2F, 1F)
                        .particle(ParticleTypes.CLOUD, 72)
                        .showOverlay(true)
                        .build());
    }

    @Override
    public void onMobHurt(LivingDamageEvent event, Player player, int layer) {
        Entity sourceEntity = event.getSource().getEntity();
        if (sourceEntity != null
                && player.tickCount - player.getLastHurtByMobTimestamp() >= 5
                && !player.isShiftKeyDown() && sourceEntity.isAlive() && player.isAlive()
                && !(sourceEntity instanceof Player)
                && !event.getSource().is(DamageTypes.THORNS)
                && !event.getSource().is(IGNORES_THORN_REBOUND)) {
            if (!(sourceEntity instanceof TamableAnimal tamed && tamed.isOwnedBy(player))) {
                Level level = player.level();
                if (level instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.SMOKE, sourceEntity.getX(), sourceEntity.getY(), sourceEntity.getZ(), 72, 0.85, 1, 0.85, 0.2);
                }
                sourceEntity.hurt(level.damageSources().thorns(player), player.getArmorValue());
            }
        }
    }

}
