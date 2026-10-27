package com.susen36.babel.item;

import com.susen36.babel.BabelConfig;
import com.susen36.babel.collectible.CollectibleActivation;
import com.susen36.babel.collectible.CollectibleItem;
import com.susen36.babel.collectible.CollectibleTiers;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingDamageEvent;

public class RoyalAllianceTreatyItem extends CollectibleItem.CustomCollectibleItem {

	private static final TagKey<EntityType<?>> NETHER_MOBS = TagKey.create(
			Registries.ENTITY_TYPE, new ResourceLocation("forge:nether_mobs"));
	private static final TagKey<EntityType<?>> FORGE_BOSSES = TagKey.create(
			Registries.ENTITY_TYPE, new ResourceLocation("forge:bosses"));

	public RoyalAllianceTreatyItem() {
		super(new Item.Properties().stacksTo(1).fireResistant().rarity(Rarity.EPIC), 25, false, CollectibleTiers.ADVANCED, new CollectibleItem.Levels(0, 1, 0),
				CollectibleActivation.builder()
						.sound(SoundEvents.PLAYER_LEVELUP, 2F, 1F)
						.particle(ParticleTypes.FLAME, 72)
						.showOverlay(true)
						.build());
	}

	@Override
	public void onMobHurt(LivingDamageEvent event, Player player, int layer) {
		Entity sourceEntity = event.getSource().getEntity();
		if (sourceEntity != null) {
			boolean valid = sourceEntity.getType().is(NETHER_MOBS);
			if (!valid) {
				String regName = BuiltInRegistries.ENTITY_TYPE.getKey(sourceEntity.getType()).toString();
				valid = matchesConfigured(BabelConfig.royalAllianceTreatyRecognizable, regName);
			}
			if (valid) {
				double amount = event.getAmount();
				event.setAmount(sourceEntity.getType().is(FORGE_BOSSES) ? (float) (amount * 0.5) : (float) (amount * 0.01));
				Level level = player.level();
				level.playSound(null, player.blockPosition(), SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 1,
						(float) Mth.nextDouble(RandomSource.create(), 0.8, 1.2));
				if (level instanceof ServerLevel serverLevel) {
					serverLevel.sendParticles(ParticleTypes.SMOKE, player.getX(), player.getY() + 1, player.getZ(), 16, 1, 1, 1, 0.1);
				}
			}
		}
	}
}