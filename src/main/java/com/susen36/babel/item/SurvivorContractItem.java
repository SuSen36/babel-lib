package com.susen36.babel.item;

import com.susen36.babel.collectible.CollectibleActivation;
import com.susen36.babel.collectible.CollectibleItem;
import com.susen36.babel.collectible.CollectibleTiers;
import com.susen36.babel.collectible.Collectibles;
import com.susen36.babel.init.BabelAttributes;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
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

public class SurvivorContractItem extends CollectibleItem.CustomCollectibleItem {

    private static final TagKey<EntityType<?>> FORGE_BOSSES = TagKey.create(
            Registries.ENTITY_TYPE, new ResourceLocation("forge:bosses"));

    public SurvivorContractItem() {
        super(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC), 25, false, CollectibleTiers.ADVANCED,
                new CollectibleItem.Levels(0, 32, 0),
                CollectibleActivation.builder()
                        .sound(SoundEvents.BEACON_ACTIVATE, 3.2F, 1F)
                        .particle(ParticleTypes.GLOW, 72)
                        .showOverlay(true)
                        .build());
        addLayerAttributeModifier(Attributes.ATTACK_DAMAGE, 0.2, AttributeModifier.Operation.MULTIPLY_BASE);
        addLayerAttributeModifier(Attributes.ARMOR, 1.0, AttributeModifier.Operation.ADDITION);
        addLayerAttributeModifier(BabelAttributes.DEFENSE.get(), 0.25, AttributeModifier.Operation.ADDITION);
    }

    @Override
    public void onEffectStarted(ItemStack stack, Level level, Player player, int layer) {
        Collectibles.getLayerData(player).setLayer(this, 0);
    }

    @Override
    public void onPlayerKill(LivingDeathEvent event, Player player, Entity victim, int layer) {
        if (layer >= 0 && layer < 32
                && (victim instanceof Monster || (victim instanceof Mob mob ? mob.getTarget() : null) == player)
                && (Math.random() < 0.35 || victim.getType().is(FORGE_BOSSES))) {
            if (player != null) {
                Collectibles.addLayerAndSync(player, this, 1);
            }
            if (victim.level() instanceof ServerLevel level) {
                level.sendParticles(ParticleTypes.WAX_ON, victim.getX(), victim.getY(), victim.getZ(), 48, 0.7, 1.5, 0.7, 0.2);
            }
        }
    }
}