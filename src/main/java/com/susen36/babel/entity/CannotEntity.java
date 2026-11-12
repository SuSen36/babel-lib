package com.susen36.babel.entity;

import com.susen36.babel.collectible.CollectibleLike;
import com.susen36.babel.collectible.CollectibleTiers;
import com.susen36.babel.collectible.Collectibles;
import com.susen36.babel.entity.robbery.Robberies;
import com.susen36.babel.entity.robbery.Robbery;
import com.susen36.babel.entity.robbery.RobberyCapability;
import com.susen36.babel.init.BabelCollectible;
import com.susen36.babel.init.BabelItems;
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
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.*;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nullable;
import java.util.*;

public class CannotEntity extends AbstractVillager {
    private static final int MIN_OFFER_COUNT = 7;
    private static final int PRICE_LOW_TIER = 8;
    private static final int PRICE_MID_TIER = 12;
    private static final int PRICE_HIGH_TIER = 16;
    private static final float LOST_AND_FOUND_CHANCE = 0.75F;
    private static final float DAMAGE_CAP_FRACTION = 0.25F;
    private static final int SKILL_ANIM_LENGTH_TICKS = 40;
    private static final byte EVENT_SKILL = (byte)90;

    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState skillAnimationState = new AnimationState();
    private int skillAnimationStart;
    private boolean skillPlaying;
    private int despawnDelay;
    private boolean hasLostAndFound;
    private final RobberyState robbery = new RobberyState();
    private static final int POOF_PARTICLE_COUNT = 20;
    private static final int LEAVE_MESSAGE_RANGE_SQR = Mth.square(64);

    public CannotEntity(EntityType<? extends CannotEntity> type, Level level) {
        super(type, level);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new TradeWithPlayerGoal(this));
        this.goalSelector.addGoal(1, new LookAtTradingPlayerGoal(this));
        this.goalSelector.addGoal(4, new MoveTowardsRestrictionGoal(this, 0.5D));
        this.goalSelector.addGoal(8, new WaterAvoidingRandomStrollGoal(this, 0.5D));
        this.goalSelector.addGoal(9, new InteractGoal(this, Player.class, 3.0F, 1.0F));
        this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, Mob.class, 8.0F));
    }

    @Override
    public void tick() {
        if (this.level().isClientSide()) {
            this.idleAnimationState.animateWhen(!this.walkAnimation.isMoving(), this.tickCount);
            if (this.skillPlaying && this.tickCount - this.skillAnimationStart >= SKILL_ANIM_LENGTH_TICKS) {
                this.skillAnimationState.stop();
                this.skillPlaying = false;
            }
        }
        super.tick();
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == EVENT_SKILL) {
            this.skillAnimationStart = this.tickCount;
            this.skillAnimationState.start(this.tickCount);
            this.skillPlaying = true;
        } else {
            super.handleEntityEvent(id);
        }
    }

    public void triggerSkillAnimation() {
        this.level().broadcastEntityEvent(this, EVENT_SKILL);
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        this.robbery.addBarPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        this.robbery.removeBarPlayer(player);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.level().isClientSide) {
            this.robbery.tick((ServerLevel) this.level(), this);
            if (this.despawnDelay > 0 && !this.isTrading() && !this.robbery.isActive()) {
                this.despawnDelay--;
                if (this.despawnDelay == 0) {
                    this.discard();
                }
            }
        }
    }

    public void setDespawnDelay(int delay) {
        this.despawnDelay = delay;
    }

    public int getDespawnDelay() {
        return this.despawnDelay;
    }

    public boolean hasLostAndFound() {
        return this.hasLostAndFound;
    }

    public void setHasLostAndFound(boolean hasLostAndFound) {
        this.hasLostAndFound = hasLostAndFound;
    }

    @Override
    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType, @Nullable SpawnGroupData groupData, @Nullable CompoundTag tag) {
        this.setHasLostAndFound(this.random.nextFloat() < LOST_AND_FOUND_CHANCE);
        return super.finalizeSpawn(level, difficulty, spawnType, groupData, tag);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("DespawnDelay", this.despawnDelay);
        tag.putBoolean("HasLostAndFound", this.hasLostAndFound);
        this.robbery.save(tag);
    }

    @Override
    @Nullable
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob other) {
        return null;
    }

    @Override
    public boolean showProgressBar() {
        return false;
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        InteractionResult result;
        if (this.robbery.isActive() || (!this.level().isClientSide && RobberyCapability.of(player).isRestricted(this.level().getGameTime()))) {
            result = InteractionResult.PASS;
        } else if (this.isAlive() && !this.isTrading() && !this.isBaby()) {
            if (hand == InteractionHand.MAIN_HAND) {
                player.awardStat(Stats.TALKED_TO_VILLAGER);
            }

            if (!this.getOffers().isEmpty() && !this.level().isClientSide) {
                this.updateSpecialPrices(player);
                this.setTradingPlayer(player);
                this.openTradingScreen(player, this.getDisplayName(), 1);
            }
            result = InteractionResult.sidedSuccess(this.level().isClientSide);
        } else {
            result = super.mobInteract(player, hand);
        }
        return result;
    }

    @Override
    public boolean isInvulnerable() {
        return this.robbery.isActive() || super.isInvulnerable();
    }

    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        return this.robbery.isActive() || super.isInvulnerableTo(source);
    }

    @Override
    public boolean canBeSeenAsEnemy() {
        return false;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean hurt = super.hurt(source, amount);
        if (hurt && this.level() instanceof ServerLevel serverLevel) {
            if (source.getEntity() instanceof Player player && RobberyCapability.of(player).isRestricted(this.level().getGameTime()) && this.isAlive()) {
                this.flee();
            } else {
                this.robbery.start(serverLevel, this, source);
            }
        }
        return hurt;
    }

    @Override
    protected void actuallyHurt(DamageSource source, float amount) {
        super.actuallyHurt(source, Math.min(amount, this.getMaxHealth() * DAMAGE_CAP_FRACTION));
    }

    @Override
    public void remove(Entity.RemovalReason reason) {
        if (this.robbery.isActive() && this.level() instanceof ServerLevel serverLevel) {
            this.robbery.stop(serverLevel);
        }
        super.remove(reason);
    }

    public void vanish() {
        if (!this.level().isClientSide) {
            ServerLevel level = (ServerLevel) this.level();
            this.robbery.stop(level);
            for (MerchantOffer offer : this.getOffers()) {
                if (!offer.isOutOfStock()) {
                    this.spawnAtLocation(offer.getResult().copy());
                }
            }
            for (int i = 0; i < POOF_PARTICLE_COUNT; ++i) {
                double dx = this.getRandom().nextGaussian() * 0.02D;
                double dy = this.getRandom().nextGaussian() * 0.02D;
                double dz = this.getRandom().nextGaussian() * 0.02D;
                level.sendParticles(ParticleTypes.POOF, this.getRandomX(1.0D), this.getRandomY(), this.getRandomZ(1.0D), 1, dx, dy, dz, 0.0D);
            }
            this.playSound(SoundEvents.WANDERING_TRADER_DISAPPEARED, 1.0F, 1.0F);
            this.discard();
        }
    }

    public void flee() {
        if (this.level() instanceof ServerLevel level) {
            for (ServerPlayer player : level.players()) {
                if (player.blockPosition().distSqr(this.blockPosition()) <= LEAVE_MESSAGE_RANGE_SQR) {
                    player.sendSystemMessage(Component.translatable("event.babel.robbery.left"));
                }
            }
            for (int i = 0; i < POOF_PARTICLE_COUNT; ++i) {
                double dx = this.getRandom().nextGaussian() * 0.02D;
                double dy = this.getRandom().nextGaussian() * 0.02D;
                double dz = this.getRandom().nextGaussian() * 0.02D;
                level.sendParticles(ParticleTypes.POOF, this.getRandomX(1.0D), this.getRandomY(), this.getRandomZ(1.0D), 1, dx, dy, dz, 0.0D);
            }
            this.playSound(SoundEvents.WANDERING_TRADER_DISAPPEARED, 1.0F, 1.0F);
            this.discard();
        }
    }

    @Override
    public void setTradingPlayer(@Nullable Player player) {
        boolean closing = this.getTradingPlayer() != null && player == null;
        super.setTradingPlayer(player);
        if (closing) {
            this.stopTrading();
        }
    }

    @Override
    protected void stopTrading() {
        super.stopTrading();
        this.resetSpecialPrices();
    }

    private void resetSpecialPrices() {
        for (MerchantOffer offer : this.getOffers()) {
            offer.resetSpecialPriceDiff();
        }
    }

    private void updateSpecialPrices(Player player) {
        if (Collectibles.isUsed(player, BabelCollectible.RUSTED_IRON_HAMMER)) {
            for (MerchantOffer offer : this.getOffers()) {
                offer.addToSpecialPriceDiff(-Math.max(offer.getBaseCostA().getCount() / 2, 1));
            }
        }
    }

    @Override
    public void openTradingScreen(Player player, Component name, int containerId) {
        if (!this.level().isClientSide) {
            NetworkHooks.openScreen((ServerPlayer) player, new SimpleMenuProvider((id, inventory, p) -> new CannotMenu(id, inventory, this, -1), name), buf -> {
                buf.writeVarInt(this.getId());
                buf.writeNbt(this.getOffers().createTag());
            });
        }
    }

    @Override
    protected void updateTrades() {
        List<RegistryObject<Item>> pool = new ArrayList<>(BabelCollectible.BABEL_COLLECTIBLES);
        pool.removeIf(entry -> !(entry.get() instanceof CollectibleLike collectible) || collectible.getTier() == CollectibleTiers.CURSED);

        MerchantOffers offers = this.getOffers();
        int offerCount = MIN_OFFER_COUNT + this.random.nextInt(2);
        while (offerCount > 0 && !pool.isEmpty()) {
            RegistryObject<Item> entry = pool.remove(this.random.nextInt(pool.size()));
            CollectibleLike collectible = (CollectibleLike) entry.get();
            offers.add(new MerchantOffer(new ItemStack(BabelItems.ORIGINIUM_INGOT.get(), priceForTier(collectible)), new ItemStack(entry.get()), 1, 0, 0.0F));
            offerCount--;
        }
    }

    private static int priceForTier(CollectibleLike collectible) {
        switch (collectible.getTier()) {
            case ADVANCED:
                return PRICE_HIGH_TIER;
            case RARE:
                return PRICE_MID_TIER;
            default:
                return PRICE_LOW_TIER;
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("DespawnDelay", 99)) {
            this.despawnDelay = tag.getInt("DespawnDelay");
        }
        if (tag.contains("HasLostAndFound", 99)) {
            this.hasLostAndFound = tag.getBoolean("HasLostAndFound");
        }
        this.robbery.load(tag);
        this.setInvulnerable(false);
        this.setAge(Math.max(0, this.getAge()));
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    @Override
    protected void rewardTradeXp(MerchantOffer offer) {
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 40.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.45D);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return this.isTrading() ? SoundEvents.WANDERING_TRADER_TRADE : SoundEvents.WANDERING_TRADER_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.WANDERING_TRADER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.WANDERING_TRADER_DEATH;
    }

    @Override
    protected SoundEvent getTradeUpdatedSound(boolean agreed) {
        return agreed ? SoundEvents.WANDERING_TRADER_YES : SoundEvents.WANDERING_TRADER_NO;
    }

    @Override
    public SoundEvent getNotifyTradeSound() {
        return SoundEvents.WANDERING_TRADER_YES;
    }

    private static final class RobberyState {
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

        public void addBarPlayer(ServerPlayer player) {
            if (this.active) {
                this.bossEvent.addPlayer(player);
            }
        }

        public void removeBarPlayer(ServerPlayer player) {
            this.bossEvent.removePlayer(player);
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
                for (ServerPlayer viewer : level.players()) {
                    if (viewer.blockPosition().distSqr(cannot.blockPosition()) <= BOSSBAR_VISIBLE_RANGE_SQR) {
                        this.bossEvent.addPlayer(viewer);
                    }
                }
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
                    this.updateBossbar(level, config);
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
            } else {
                this.nextSpawnAt = level.getGameTime() + SPAWN_RETRY_DELAY_TICKS;
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

        private void updateBossbar(ServerLevel level, Robbery config) {
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
                    this.configId = ResourceLocation.tryParse(tag.getString("RobberyConfigId"));
                    if (this.configId == null) {
                        this.configId = Robberies.DEFAULT_ID;
                    }
                } else {
                    this.configId = Robberies.DEFAULT_ID;
                }
                this.config = Robberies.get(this.configId);
                if (this.config != null) {
                    this.bossEvent.setName(this.config.getBossBarName());
                }
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
            if (this.active && this.config == null) {
                this.active = false;
                this.bossEvent.removeAllPlayers();
                this.bossEvent.setProgress(0.0F);
            }
        }
    }
}