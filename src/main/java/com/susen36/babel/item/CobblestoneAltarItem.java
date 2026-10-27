package com.susen36.babel.item;

import com.susen36.babel.collectible.CollectibleActivation;
import com.susen36.babel.collectible.CollectibleItem;
import com.susen36.babel.collectible.CollectibleTiers;
import com.susen36.babel.collectible.Collectibles;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingDeathEvent;

public class CobblestoneAltarItem extends CollectibleItem.CustomCollectibleItem {

    public CobblestoneAltarItem() {
        super(new Item.Properties().stacksTo(1).rarity(Rarity.COMMON), 25, false, CollectibleTiers.NORMAL,
                new CollectibleItem.Levels(0, 10, 0),
                CollectibleActivation.builder()
                        .sound(SoundEvents.PLAYER_LEVELUP, 2F, 1F)
                        .particle(ParticleTypes.HAPPY_VILLAGER, 72)
                        .showOverlay(true)
                        .build());
        addLayerAttributeModifier(Attributes.ATTACK_DAMAGE, 0.05, AttributeModifier.Operation.MULTIPLY_BASE);
        addLayerAttributeModifier(Attributes.ARMOR, 0.05, AttributeModifier.Operation.MULTIPLY_BASE);
    }

    @Override
    public void onEffectStarted(ItemStack stack, Level level, Player player, int layer) {
        Collectibles.getLayerData(player).setLayer(this, 0);
    }

    @Override
    public void onPlayerKill(LivingDeathEvent event, Player player, Entity victim, int layer) {
        if (layer >= 0 && layer < 10
                && (victim instanceof Monster || (victim instanceof Mob mob ? mob.getTarget() : null) == player)
                && Math.random() < 0.075) {
            Collectibles.addLayerAndSync(player, this, 1);
            if (victim.level() instanceof ServerLevel level) {
                level.sendParticles(ParticleTypes.WAX_ON, victim.getX(), victim.getY(), victim.getZ(), 48, 0.7, 1.5, 0.7, 0.2);
            }
        }
    }
}