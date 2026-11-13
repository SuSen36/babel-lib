package com.susen36.babel.item;

import com.susen36.babel.collectible.CollectibleActivation;
import com.susen36.babel.collectible.CollectibleItem;
import com.susen36.babel.collectible.CollectibleTiers;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.entity.npc.VillagerType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.NaturalSpawner;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SecretHrLetterItem extends CollectibleItem.CustomCollectibleItem {

    public SecretHrLetterItem() {
        super(new Item.Properties().stacksTo(1), 25, false, CollectibleTiers.RARE,
                new CollectibleItem.Levels(0, 1, 0),
                CollectibleActivation.builder()
                        .sound(SoundEvents.VILLAGER_YES, 2F, 1F)
                        .particle(ParticleTypes.HAPPY_VILLAGER, 30)
                        .build());
    }

    @Override
    public void onEffectStarted(@NotNull ItemStack stack, @NotNull Level level, @NotNull Player player) {
        if (level instanceof ServerLevel serverLevel && !serverLevel.isClientSide()) {
            Villager villager = EntityType.VILLAGER.create(serverLevel);
            if (villager != null) {
                BlockPos anchor = player.blockPosition();
                BlockPos spawnPos = anchor;
                for (int i = 0; i < 10; i++) {
                    int x = anchor.getX() + serverLevel.getRandom().nextInt(11) - 5;
                    int z = anchor.getZ() + serverLevel.getRandom().nextInt(11) - 5;
                    BlockPos candidate = new BlockPos(x, anchor.getY(), z);
                    if (!candidate.equals(anchor) && NaturalSpawner.isSpawnPositionOk(SpawnPlacements.Type.ON_GROUND, serverLevel, candidate, EntityType.VILLAGER)) {
                        spawnPos = candidate;
                        break;
                    }
                }
                villager.moveTo(spawnPos.getX() + 0.5D, spawnPos.getY(), spawnPos.getZ() + 0.5D, serverLevel.getRandom().nextFloat() * 360.0F, 0.0F);
                List<VillagerProfession> professions = BuiltInRegistries.VILLAGER_PROFESSION.stream()
                        .filter(profession -> profession != VillagerProfession.NONE)
                        .toList();
                VillagerProfession profession = professions.get(villager.getRandom().nextInt(professions.size()));
                villager.setVillagerData(villager.getVillagerData()
                        .setType(VillagerType.byBiome(serverLevel.getBiome(villager.blockPosition())))
                        .setProfession(profession));
                // 先在 1 级触发懒加载生成首批交易，再升到 5 级补齐后续各档交易，保证交易按等级顺序排列
                villager.getOffers();
                villager.setVillagerData(villager.getVillagerData().setLevel(5));
                Int2ObjectMap<VillagerTrades.ItemListing[]> trades = VillagerTrades.TRADES.get(profession);
                if (trades != null) {
                    for (int tradeLevel = 2; tradeLevel <= 5; tradeLevel++) {
                        VillagerTrades.ItemListing[] listings = trades.get(tradeLevel);
                        if (listings != null) {
                            ArrayList<VillagerTrades.ItemListing> remaining = new ArrayList<>(Arrays.asList(listings));
                            int added = 0;
                            while (added < 2 && !remaining.isEmpty()) {
                                MerchantOffer offer = remaining.remove(villager.getRandom().nextInt(remaining.size())).getOffer(villager, villager.getRandom());
                                if (offer != null) {
                                    villager.getOffers().add(offer);
                                    added++;
                                }
                            }
                        }
                    }
                }
                serverLevel.addFreshEntity(villager);
            }
        }
    }
}