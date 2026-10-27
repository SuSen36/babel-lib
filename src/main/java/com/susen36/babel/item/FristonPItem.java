package com.susen36.babel.item;

import com.susen36.babel.collectible.CollectibleActivation;
import com.susen36.babel.collectible.CollectibleItem;
import com.susen36.babel.collectible.CollectibleTiers;
import com.susen36.babel.collectible.Collectibles;
import com.susen36.babel.init.BabelEntityTypeTags;
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

public class FristonPItem extends CollectibleItem.CustomCollectibleItem {

    private static final int MAX_LAYERS = 15;
    private static final double STACK_CHANCE = 0.15;
    private static final double ATTACK_SPEED_PER_LAYER = 0.1D;

    public FristonPItem() {
        super(new Item.Properties().stacksTo(1).rarity(Rarity.RARE), 25, false, CollectibleTiers.RARE,
                new CollectibleItem.Levels(0, MAX_LAYERS, 0),
                CollectibleActivation.builder()
                        .sound(SoundEvents.PLAYER_LEVELUP, 2F, 1F)
                        .particle(ParticleTypes.HAPPY_VILLAGER, 72)
                        .showOverlay(true)
                        .build());
        addLayerAttributeModifier(Attributes.ATTACK_SPEED, ATTACK_SPEED_PER_LAYER, AttributeModifier.Operation.ADDITION);
    }

    @Override
    public void onEffectStarted(ItemStack stack, Level level, Player player, int layer) {
        Collectibles.getLayerData(player).setLayer(this, 0);
    }

    @Override
    public void onPlayerKill(LivingDeathEvent event, Player player, Entity victim, int layer) {
        if (layer >= 0 && layer < MAX_LAYERS) {
            boolean owned = victim instanceof Monster || (victim instanceof Mob mob ? mob.getTarget() : null) == player;
            boolean emergency = victim.getType().is(BabelEntityTypeTags.ELITE) || victim.getType().is(BabelEntityTypeTags.FORGE_BOSSES);
            if (owned && (emergency || Math.random() < STACK_CHANCE)) {
                Collectibles.addLayerAndSync(player, this, 1);
                if (victim.level() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.WAX_ON, victim.getX(), victim.getY(), victim.getZ(), 48, 0.7, 1.5, 0.7, 0.2);
                }
            }
        }
    }
}