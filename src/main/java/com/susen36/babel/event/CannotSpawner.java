package com.susen36.babel.event;

import com.susen36.babel.BabelMod;
import com.susen36.babel.entity.CannotEntity;
import com.susen36.babel.init.BabelEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;

@Mod.EventBusSubscriber(modid = BabelMod.MODID)
public class CannotSpawner {
    private static final String SPAWN_DELAY_KEY = BabelMod.MODID + ":cannot_spawn_delay";
    private static final String SPAWN_CHANCE_KEY = BabelMod.MODID + ":cannot_spawn_chance";
    private static final int SPAWN_DELAY = 24000;
    private static final int MIN_SPAWN_CHANCE = 25;
    private static final int MAX_SPAWN_CHANCE = 75;
    private static final int SPAWN_CHANCE_INCREASE = 25;
    private static final int DESPAWN_DELAY = 48000;
    private static final int DEDUP_RADIUS = 128;
    private static final int SEARCH_RADIUS = 48;

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
                if (!player.isSpectator() && player.tickCount % 1200 == 0
                        && player.level().getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING)) {
                    CompoundTag data = player.getPersistentData();
                    if (!data.contains(SPAWN_DELAY_KEY, 99)) {
                        data.putInt(SPAWN_DELAY_KEY, SPAWN_DELAY);
                    }
                    if (!data.contains(SPAWN_CHANCE_KEY, 99)) {
                        data.putInt(SPAWN_CHANCE_KEY, MIN_SPAWN_CHANCE);
                    }
                    int spawnDelay = data.getInt(SPAWN_DELAY_KEY) - 1200;
                    data.putInt(SPAWN_DELAY_KEY, spawnDelay);
                    if (spawnDelay <= 0) {
                        data.putInt(SPAWN_DELAY_KEY, SPAWN_DELAY);
                        int chance = data.getInt(SPAWN_CHANCE_KEY);
                        data.putInt(SPAWN_CHANCE_KEY, Mth.clamp(chance + SPAWN_CHANCE_INCREASE, MIN_SPAWN_CHANCE, MAX_SPAWN_CHANCE));
                        if (player.getRandom().nextInt(100) <= chance && spawnCannot(player)) {
                            data.putInt(SPAWN_CHANCE_KEY, MIN_SPAWN_CHANCE);
                        }
                    }
                }
            }
        }
    }

    private static boolean spawnCannot(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        BlockPos anchor = findMeetingPoint(level, player.blockPosition());
        BlockPos spawnPos = findSpawnPositionNear(level, anchor, player);
        boolean spawned = false;
        if (player.getRandom().nextInt(10) == 0
                && !hasNearbyCannot(player)
                && spawnPos != null
                && hasEnoughSpace(level, spawnPos)
                && !level.getBiome(spawnPos).is(BiomeTags.WITHOUT_WANDERING_TRADER_SPAWNS)) {
            CannotEntity cannot = BabelEntities.CANNOT.get().spawn(level, spawnPos, MobSpawnType.EVENT);
            if (cannot != null) {
                cannot.setDespawnDelay(DESPAWN_DELAY);
                cannot.restrictTo(anchor, 16);
                spawned = true;
            }
        }
        return spawned;
    }

    private static boolean hasNearbyCannot(ServerPlayer player) {
        return player.level().getNearestEntity(CannotEntity.class, TargetingConditions.DEFAULT,
                player, player.getX(), player.getY(), player.getZ(),
                player.getBoundingBox().inflate(DEDUP_RADIUS)) != null;
    }

    private static BlockPos findMeetingPoint(ServerLevel level, BlockPos center) {
        PoiManager poiManager = level.getPoiManager();
        return poiManager.find(type -> type.is(PoiTypes.MEETING), pos -> true, center, SEARCH_RADIUS, PoiManager.Occupancy.ANY)
                .orElse(center);
    }

    @Nullable
    private static BlockPos findSpawnPositionNear(ServerLevel level, BlockPos center, ServerPlayer player) {
        BlockPos found = null;
        for (int i = 0; i < 10; ++i) {
            int x = center.getX() + player.getRandom().nextInt(SEARCH_RADIUS * 2) - SEARCH_RADIUS;
            int z = center.getZ() + player.getRandom().nextInt(SEARCH_RADIUS * 2) - SEARCH_RADIUS;
            BlockPos pos = new BlockPos(x, level.getHeight(Heightmap.Types.WORLD_SURFACE, x, z), z);
            if (NaturalSpawner.isSpawnPositionOk(SpawnPlacements.Type.ON_GROUND, level, pos, BabelEntities.CANNOT.get())) {
                found = pos;
                break;
            }
        }
        return found;
    }

    private static boolean hasEnoughSpace(ServerLevel level, BlockPos pos) {
        boolean hasSpace = true;
        for (BlockPos blockPos : BlockPos.betweenClosed(pos, pos.offset(1, 2, 1))) {
            if (!level.getBlockState(blockPos).getCollisionShape(level, blockPos).isEmpty()) {
                hasSpace = false;
                break;
            }
        }
        return hasSpace;
    }
}