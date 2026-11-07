package com.susen36.babel.robbery;

import com.susen36.babel.entity.CannotEntity;
import com.susen36.babel.menu.CannotMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.*;

public class RobberyState {
    private static final int MAX_SPAWN_ATTEMPTS = 3;
    private static final int SPAWN_RETRY_DELAY_TICKS = 20;
    private static final int SPAWN_HEIGHT_RANGE = 8;
    private static final float START_HEALTH_FRACTION = 0.75F;
    private static final int POST_RAID_TICKS = 40;
    private static final long RAID_TIMEOUT_TICKS = 48000L;
    private static final int MAX_TRACK_DISTANCE_SQR = Mth.square(64);
    private static final int BOSSBAR_VISIBLE_RANGE_SQR = Mth.square(64);
    private static final int SMOKE_PARTICLE_COUNT = 20;
    private static final int FLAME_PARTICLE_COUNT = 10;

    private final ServerBossEvent bossEvent = new ServerBossEvent(Component.empty(), BossEvent.BossBarColor.RED, BossEvent.BossBarOverlay.NOTCHED_10);
    private boolean active;
    @Nullable
    private Robbery config;
    @Nullable
    private ResourceLocation configId;
    private final Set<UUID> spawnedMobs = new HashSet<>();
    private int wavesSpawned;
    private int waveSpawnCount;
    private int waveCooldownTicks;
    private int postRaidTicks;
    private float waveTotalHealth;
    private long nextSpawnAt;
    private long nextSupportAt;
    private long ticksActive;

    public boolean isActive() {
        return this.active;
    }

    public void start(ServerLevel level, CannotEntity cannot, DamageSource source) {
        Map.Entry<ResourceLocation, Robbery> picked = Robberies.getRandom(level.getRandom());
        Robbery config = picked != null ? picked.getValue() : Robberies.get(Robberies.DEFAULT_ID);
        ResourceLocation configId = picked != null ? picked.getKey() : Robberies.DEFAULT_ID;
        if (level.getDifficulty() != Difficulty.PEACEFUL && !this.active
                && source.getEntity() instanceof Player player
                && !RobberyCapability.of(player).isRestricted(level.getGameTime())
                && cannot.getHealth() <= cannot.getMaxHealth() * START_HEALTH_FRACTION) {
            this.config = config;
            this.configId = configId;
            this.active = true;
            this.wavesSpawned = 0;
            this.waveSpawnCount = 0;
            this.waveCooldownTicks = config.getWaveIntervalTicks();
            this.postRaidTicks = 0;
            this.waveTotalHealth = 0.0F;
            this.nextSpawnAt = 0L;
            this.nextSupportAt = level.getGameTime() + config.getSupportIntervalTicks();
            this.ticksActive = 0L;
            this.bossEvent.setName(config.getBossBarName());
            this.bossEvent.setProgress(0.0F);
            RobberyCapability.of(player).startRestriction(level.getGameTime());
            if (player instanceof ServerPlayer serverPlayer && serverPlayer.containerMenu instanceof CannotMenu) {
                serverPlayer.closeContainer();
            }
            player.sendSystemMessage(Component.translatable("event.babel.robbery.started"));
        }
    }

    public void tick(ServerLevel level, CannotEntity cannot) {
        if (this.active) {
            this.ticksActive++;
            if (level.getDifficulty() == Difficulty.PEACEFUL || this.ticksActive >= RAID_TIMEOUT_TICKS) {
                this.stop(level);
            } else if (this.config != null) {
                Robbery config = this.config;
                this.spawnedMobs.removeIf(uuid -> this.removeFromRaid(level, cannot, uuid));
                if (this.wavesSpawned >= config.getWaveCount() && this.spawnedMobs.isEmpty()) {
                    if (this.postRaidTicks < POST_RAID_TICKS) {
                        this.postRaidTicks++;
                    } else {
                        cannot.vanish();
                    }
                } else if (this.waveCooldownTicks > 0) {
                    this.waveCooldownTicks--;
                } else if (this.waveSpawnCount == 0 && this.spawnedMobs.isEmpty() && this.wavesSpawned < config.getWaveCount()) {
                    this.startWave(level, cannot, config);
                } else if (this.waveSpawnCount >= config.getMobsPerWave() && this.spawnedMobs.isEmpty()) {
                    this.waveSpawnCount = 0;
                    this.waveCooldownTicks = config.getWaveIntervalTicks();
                } else if (this.waveSpawnCount < config.getMobsPerWave()
                        && this.spawnedMobs.size() < config.getSimultaneousMobs()
                        && level.getGameTime() >= this.nextSpawnAt) {
                    if (this.spawnMob(level, cannot, config, this.wavesSpawned)) {
                        this.nextSpawnAt = level.getGameTime() + config.getTicksBetweenSpawn();
                    } else {
                        this.nextSpawnAt = level.getGameTime() + SPAWN_RETRY_DELAY_TICKS;
                    }
                }
                if (level.getGameTime() >= this.nextSupportAt && !this.spawnedMobs.isEmpty()) {
                    this.applySupport(level, cannot, config);
                    this.nextSupportAt = level.getGameTime() + config.getSupportIntervalTicks();
                }
                this.updateBossbar(level, cannot, config);
            }
        }
    }

    private void startWave(ServerLevel level, CannotEntity cannot, Robbery config) {
        this.waveTotalHealth = 0.0F;
        this.waveSpawnCount = 0;
        int opening = Math.min(config.getInitialSpawnCount(), config.getMobsPerWave());
        boolean spawned = false;
        for (int i = 0; i < opening; ++i) {
            spawned |= this.spawnMob(level, cannot, config, this.wavesSpawned + 1);
        }
        if (spawned) {
            this.wavesSpawned++;
            this.nextSpawnAt = level.getGameTime() + config.getTicksBetweenSpawn();
            level.playSound(null, cannot.blockPosition(), SoundEvents.RAID_HORN.get(), SoundSource.HOSTILE, 1.0F, 1.0F);
        }
    }

    private boolean spawnMob(ServerLevel level, CannotEntity cannot, Robbery config, int wave) {
        Robbery.SpawnEntry entry = this.pickWeightedEntry(config.getSpawnPotentials(), wave, level.getRandom());
        boolean spawned = false;
        if (entry != null) {
            RandomSource random = level.getRandom();
            int attempt = 0;
            while (attempt < MAX_SPAWN_ATTEMPTS && !spawned) {
                float angle = random.nextFloat() * ((float) Math.PI * 2F);
                int x = cannot.getBlockX() + Mth.floor(Mth.cos(angle) * config.getSpawnRange());
                int z = cannot.getBlockZ() + Mth.floor(Mth.sin(angle) * config.getSpawnRange());
                int baseY = cannot.getBlockY();
                int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
                if (Mth.abs(y - baseY) > SPAWN_HEIGHT_RANGE) {
                    y = baseY;
                    while (y > baseY - SPAWN_HEIGHT_RANGE && level.getBlockState(new BlockPos(x, y - 1, z)).isAir()) {
                        y--;
                    }
                }
                spawned = this.spawnAt(level, entry, x + 0.5D, y, z + 0.5D, new BlockPos(x, y, z));
                attempt++;
            }
        }
        return spawned;
    }

    private boolean spawnAt(ServerLevel level, Robbery.SpawnEntry entry, double x, double y, double z, BlockPos pos) {
        boolean spawned = false;
        if (level.noCollision(entry.type().getAABB(x, y, z))
                && SpawnPlacements.checkSpawnRules(entry.type(), level, MobSpawnType.SPAWNER, pos, level.getRandom())) {
            CompoundTag entityTag = entry.tag() != null ? entry.tag().copy() : new CompoundTag();
            entityTag.putString("id", ForgeRegistries.ENTITY_TYPES.getKey(entry.type()).toString());
            Entity entity = EntityType.loadEntityRecursive(entityTag, level, created -> {
                created.moveTo(x, y, z, level.getRandom().nextFloat() * 360.0F, 0.0F);
                return created;
            });
            if (entity != null) {
                if (entry.tag() == null && entity instanceof Mob mob) {
                    mob.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), MobSpawnType.SPAWNER, null, null);
                }
                spawned = level.tryAddFreshEntityWithPassengers(entity);
                if (spawned) {
                    this.spawnedMobs.add(entity.getUUID());
                    this.waveSpawnCount++;
                    if (entity instanceof LivingEntity living) {
                        this.waveTotalHealth += living.getHealth();
                    }
                    level.sendParticles(ParticleTypes.SMOKE, x, y + 0.5D, z, SMOKE_PARTICLE_COUNT, 0.4D, 0.5D, 0.4D, 0.01D);
                    level.sendParticles(ParticleTypes.FLAME, x, y + 0.5D, z, FLAME_PARTICLE_COUNT, 0.4D, 0.5D, 0.4D, 0.02D);
                }
            }
        }
        return spawned;
    }

    private void applySupport(ServerLevel level, CannotEntity cannot, Robbery config) {
        double radiusSqr = Mth.square(config.getSupportRadius());
        List<Robbery.SupportEffect> effects = config.getSupportGroups()
                .get(cannot.getRandom().nextInt(config.getSupportGroups().size())).effects();
        boolean supported = false;
        for (UUID uuid : this.spawnedMobs) {
            Entity entity = level.getEntity(uuid);
            if (entity instanceof Mob mob && mob.isAlive()
                    && mob.blockPosition().distSqr(cannot.blockPosition()) <= radiusSqr) {
                supported = true;
                for (Robbery.SupportEffect support : effects) {
                    mob.addEffect(new MobEffectInstance(support.effect(), support.duration(), support.amplifier()));
                }
                if (config.getSupportHealPerPulse() > 0.0F) {
                    mob.heal(config.getSupportHealPerPulse());
                    level.sendParticles(ParticleTypes.HEART, mob.getX(), mob.getY(1.0D), mob.getZ(), 2, 0.3D, 0.5D, 0.3D, 0.0D);
                }
            }
        }
        if (supported) {
            cannot.triggerSkillAnimation();
        }
    }

    private boolean removeFromRaid(ServerLevel level, CannotEntity cannot, UUID uuid) {
        Entity entity = level.getEntity(uuid);
        boolean dead = entity == null || !entity.isAlive();
        boolean far = !dead && entity.blockPosition().distSqr(cannot.blockPosition()) > MAX_TRACK_DISTANCE_SQR;
        if (far && entity instanceof LivingEntity living) {
            this.waveTotalHealth -= living.getHealth();
        }
        return dead || far;
    }

    private void updateBossbar(ServerLevel level, CannotEntity cannot, Robbery config) {
        if (this.wavesSpawned >= config.getWaveCount() && this.spawnedMobs.isEmpty()) {
            this.bossEvent.setProgress(0.0F);
            this.bossEvent.setName(config.getBossBarName());
            this.bossEvent.removeAllPlayers();
        } else {
            if (this.waveCooldownTicks > 0) {
                int interval = config.getWaveIntervalTicks();
                this.bossEvent.setProgress(Mth.clamp((float) (interval - this.waveCooldownTicks) / (float) interval, 0.0F, 1.0F));
            } else {
                float health = 0.0F;
                for (UUID uuid : this.spawnedMobs) {
                    if (level.getEntity(uuid) instanceof LivingEntity living) {
                        health += living.getHealth();
                    }
                }
                this.bossEvent.setProgress(this.waveTotalHealth > 0.0F ? Mth.clamp(health / this.waveTotalHealth, 0.0F, 1.0F) : 0.0F);
            }
            this.bossEvent.setName(this.spawnedMobs.isEmpty()
                    ? config.getBossBarName()
                    : config.getBossBarName().copy()
                            .append(" - ")
                            .append(Component.translatable("event.babel.robbery.wave", this.wavesSpawned, config.getWaveCount())));
            for (ServerPlayer player : level.players()) {
                if (player.blockPosition().distSqr(cannot.blockPosition()) <= BOSSBAR_VISIBLE_RANGE_SQR) {
                    this.bossEvent.addPlayer(player);
                } else {
                    this.bossEvent.removePlayer(player);
                }
            }
        }
    }

    @Nullable
    private Robbery.SpawnEntry pickWeightedEntry(List<Robbery.SpawnEntry> potentials, int currentWave, RandomSource random) {
        Robbery.SpawnEntry entry = null;
        int totalWeight = 0;
        for (Robbery.SpawnEntry candidate : potentials) {
            if (candidate.minWave() <= currentWave) {
                totalWeight += candidate.weight();
            }
        }
        if (totalWeight > 0) {
            int roll = random.nextInt(totalWeight);
            for (Robbery.SpawnEntry candidate : potentials) {
                if (candidate.minWave() > currentWave) {
                    continue;
                }
                roll -= candidate.weight();
                if (roll < 0) {
                    entry = candidate;
                    break;
                }
            }
        }
        return entry;
    }

    public void stop(ServerLevel level) {
        if (this.active) {
            this.active = false;
            this.bossEvent.removeAllPlayers();
            this.bossEvent.setProgress(0.0F);
            for (UUID uuid : this.spawnedMobs) {
                Entity entity = level.getEntity(uuid);
                if (entity != null) {
                    entity.discard();
                }
            }
            this.spawnedMobs.clear();
            this.wavesSpawned = 0;
            this.waveSpawnCount = 0;
            this.waveCooldownTicks = 0;
            this.postRaidTicks = 0;
            this.waveTotalHealth = 0.0F;
            this.nextSpawnAt = 0L;
            this.ticksActive = 0L;
            this.config = null;
            this.configId = null;
        }
    }

    public void save(CompoundTag tag) {
        tag.putBoolean("RobberyActive", this.active);
        if (this.active) {
            if (this.configId != null) {
                tag.putString("RobberyConfigId", this.configId.toString());
            }
            ListTag mobs = new ListTag();
            for (UUID uuid : this.spawnedMobs) {
                mobs.add(NbtUtils.createUUID(uuid));
            }
            tag.put("RobberyMobs", mobs);
            tag.putInt("RobberyWavesSpawned", this.wavesSpawned);
            tag.putInt("RobberyWaveSpawned", this.waveSpawnCount);
            tag.putInt("RobberyWaveCooldown", this.waveCooldownTicks);
            tag.putInt("RobberyPostRaidTicks", this.postRaidTicks);
            tag.putFloat("RobberyWaveTotalHealth", this.waveTotalHealth);
            tag.putLong("RobberyNextSpawnAt", this.nextSpawnAt);
            tag.putLong("RobberyNextSupportAt", this.nextSupportAt);
            tag.putLong("RobberyTicksActive", this.ticksActive);
        }
    }

    public void load(CompoundTag tag) {
        this.active = tag.getBoolean("RobberyActive");
        this.spawnedMobs.clear();
        this.wavesSpawned = 0;
        this.waveSpawnCount = 0;
        this.waveCooldownTicks = 0;
        this.postRaidTicks = 0;
        this.waveTotalHealth = 0.0F;
        this.nextSpawnAt = 0L;
        this.nextSupportAt = 0L;
        this.ticksActive = 0L;
        if (this.active && tag.contains("RobberyMobs", 9)) {
            if (tag.contains("RobberyConfigId", 8)) {
                this.configId = new ResourceLocation(tag.getString("RobberyConfigId"));
            } else {
                this.configId = Robberies.DEFAULT_ID;
            }
            this.config = Robberies.get(this.configId);
            this.bossEvent.setName(this.config.getBossBarName());
            ListTag mobs = tag.getList("RobberyMobs", 11);
            for (Tag mobTag : mobs) {
                this.spawnedMobs.add(NbtUtils.loadUUID(mobTag));
            }
            if (tag.contains("RobberyWavesSpawned", 99)) {
                this.wavesSpawned = tag.getInt("RobberyWavesSpawned");
            }
            if (tag.contains("RobberyWaveSpawned", 99)) {
                this.waveSpawnCount = tag.getInt("RobberyWaveSpawned");
            }
            if (tag.contains("RobberyWaveCooldown", 99)) {
                this.waveCooldownTicks = tag.getInt("RobberyWaveCooldown");
            }
            if (tag.contains("RobberyPostRaidTicks", 99)) {
                this.postRaidTicks = tag.getInt("RobberyPostRaidTicks");
            }
            if (tag.contains("RobberyWaveTotalHealth", 99)) {
                this.waveTotalHealth = tag.getFloat("RobberyWaveTotalHealth");
            }
            if (tag.contains("RobberyNextSpawnAt", 99)) {
                this.nextSpawnAt = tag.getLong("RobberyNextSpawnAt");
            }
            if (tag.contains("RobberyNextSupportAt", 99)) {
                this.nextSupportAt = tag.getLong("RobberyNextSupportAt");
            }
            if (tag.contains("RobberyTicksActive", 99)) {
                this.ticksActive = tag.getLong("RobberyTicksActive");
            }
        }
    }
}