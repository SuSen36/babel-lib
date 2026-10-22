package com.susen36.babel.item;

import com.susen36.babel.collectible.CollectibleActivation;
import com.susen36.babel.collectible.CollectibleItem;
import com.susen36.babel.collectible.CollectibleTiers;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;

public class UnripeYearningItem extends CollectibleItem.CustomCollectibleItem {
	public UnripeYearningItem() {
		super(new Item.Properties().stacksTo(1).fireResistant().rarity(Rarity.EPIC), 25, false, CollectibleTiers.ADVANCED, new CollectibleItem.Levels(0, 1, 0),
				CollectibleActivation.builder()
						.sound(SoundEvents.RESPAWN_ANCHOR_SET_SPAWN, 2F, 1F)
						.particle(ParticleTypes.DOLPHIN, 72)
						.showOverlay(true)
						.build());
		addConditionalAttributeModifier(Attributes.KNOCKBACK_RESISTANCE, 0.03, AttributeModifier.Operation.MULTIPLY_BASE,
				UnripeYearningItem::thoughtStacks);
		addConditionalAttributeModifier(Attributes.ARMOR, 0.03, AttributeModifier.Operation.MULTIPLY_BASE,
				UnripeYearningItem::thoughtStacks);
		addConditionalAttributeModifier(Attributes.MAX_HEALTH, 1.0, AttributeModifier.Operation.ADDITION,
				UnripeYearningItem::thoughtStacks);
	}

	private static int thoughtStacks(Player player, Item self) {
		int stacks = 0;
		if (!player.getItemBySlot(EquipmentSlot.CHEST).isEmpty()) {
			int amplifier = Mth.clamp(Mth.floor(player.experienceLevel * 0.25), 0, 64);
			if (amplifier >= 1) {
				stacks = amplifier + 1;
			}
		}
		return stacks;
	}

}
