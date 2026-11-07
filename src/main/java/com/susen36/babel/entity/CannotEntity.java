package com.susen36.babel.entity;

import com.susen36.babel.collectible.CollectibleLike;
import com.susen36.babel.collectible.CollectibleTiers;
import com.susen36.babel.collectible.Collectibles;
import com.susen36.babel.init.BabelCollectible;
import com.susen36.babel.init.BabelItems;
import com.susen36.babel.menu.CannotMenu;
import com.susen36.babel.robbery.RobberyCapability;
import com.susen36.babel.robbery.RobberyState;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.damagesource.DamageSource;
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
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

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

    public CannotEntity(EntityType<? extends CannotEntity> type, Level level) {
        super(type, level);
    }


    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new TradeWithPlayerGoal(this));
        this.goalSelector.addGoal(1, new LookAtTradingPlayerGoal(this));
        this.goalSelector.addGoal(4, new MoveTowardsRestrictionGoal(this, 0.5D));
        this.goalSelector.addGoal(8, new WaterAvoidingRandomStrollGoal(this, 0.5D));
        this.goalSelector.addGoal(9, new InteractGoal(this, Player.class, 3.0F, 1.0F));
        this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, Mob.class, 8.0F));
    }

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

    public void aiStep() {
        super.aiStep();
        if (!this.level().isClientSide) {
            this.robbery.tick((ServerLevel) this.level(), this);
            this.maybeDespawn();
        }
    }

    private void maybeDespawn() {
        if (this.despawnDelay > 0 && !this.isTrading() && !this.robbery.isActive() && --this.despawnDelay == 0) {
            this.discard();
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

    @Nullable
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob other) {
        return null;
    }

    public boolean showProgressBar() {
        return false;
    }

    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        InteractionResult result;
        if (this.robbery.isActive() || !this.level().isClientSide && RobberyCapability.of(player).isRestricted(this.level().getGameTime())) {
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

    public boolean hurt(DamageSource source, float amount) {
        boolean hurt = super.hurt(source, amount);
        if (hurt && !this.level().isClientSide) {
            this.robbery.start((ServerLevel) this.level(), this, source);
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
            this.playSound(SoundEvents.WANDERING_TRADER_DEATH, 1.0F, 1.0F);
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

    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    protected void rewardTradeXp(MerchantOffer offer) {
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 40.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.45D);
    }

    protected SoundEvent getAmbientSound() {
        return this.isTrading() ? SoundEvents.WANDERING_TRADER_TRADE : SoundEvents.WANDERING_TRADER_AMBIENT;
    }

    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.WANDERING_TRADER_HURT;
    }

    protected SoundEvent getDeathSound() {
        return SoundEvents.WANDERING_TRADER_DEATH;
    }

    protected SoundEvent getTradeUpdatedSound(boolean agreed) {
        return agreed ? SoundEvents.WANDERING_TRADER_YES : SoundEvents.WANDERING_TRADER_NO;
    }

    public SoundEvent getNotifyTradeSound() {
        return SoundEvents.WANDERING_TRADER_YES;
    }
}