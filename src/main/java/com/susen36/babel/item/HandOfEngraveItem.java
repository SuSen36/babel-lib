package com.susen36.babel.item;

import com.susen36.babel.BabelConfig;
import com.susen36.babel.collectible.CollectibleActivation;
import com.susen36.babel.collectible.CollectibleItem;
import com.susen36.babel.collectible.Collectibles;
import com.susen36.babel.collectible.CollectibleTiers;
import com.susen36.babel.init.BabelAttributes;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingDeathEvent;

public class HandOfEngraveItem extends CollectibleItem.CustomCollectibleItem {

    private static final TagKey<Item> FORGE_TRIDENTS = ItemTags.create(new ResourceLocation("forge:tools/tridents"));

    public HandOfEngraveItem() {
        super(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC), 25, false, CollectibleTiers.ADVANCED,
                new CollectibleItem.Levels(0, 99, 0),
                CollectibleActivation.builder()
                        .sound(SoundEvents.PLAYER_LEVELUP, 2F, 1F)
                        .particle(ParticleTypes.CLOUD, 72)
                        .showOverlay(true)
                        .build());
        addLayerAttributeModifier(Attributes.ATTACK_DAMAGE, 0.01, AttributeModifier.Operation.MULTIPLY_BASE);
        addLayerAttributeModifier(Attributes.MAX_HEALTH, 0.01, AttributeModifier.Operation.MULTIPLY_BASE);
        addLayerAttributeModifier(Attributes.ARMOR_TOUGHNESS, 0.01, AttributeModifier.Operation.MULTIPLY_BASE);
        addLayerAttributeModifier(BabelAttributes.MAGIC_RESISTANCE.get(), 0.25, AttributeModifier.Operation.ADDITION);
        addLayerAttributeModifier(Attributes.ARMOR, 0.01, AttributeModifier.Operation.MULTIPLY_BASE);
    }

    @Override
    public void onEffectStarted(ItemStack stack, Level level, Player player, int layer) {
        Collectibles.getLayerData(player).setLayer(this, 0);
    }

    @Override
    public void onPlayerKill(LivingDeathEvent event, Player player, Entity victim, int layer) {
        if (layer >= 0 && layer < 99
                && (victim instanceof Monster || victim instanceof Mob mob && mob.getTarget() == player)) {
            ItemStack mainHandItem = player.getMainHandItem();
            boolean validWeapon = mainHandItem.is(Items.TRIDENT) || mainHandItem.is(FORGE_TRIDENTS) || event.getSource().is(DamageTypes.TRIDENT)
                    || matchesConfigured(BabelConfig.handEngraveRecognizable,
                            BuiltInRegistries.ITEM.getKey(mainHandItem.getItem()).toString());
            if (validWeapon) {
                Collectibles.getLayerData(player).setLayer(this, layer + 1);
            }
        }
    }
}
