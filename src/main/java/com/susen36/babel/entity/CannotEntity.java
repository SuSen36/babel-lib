package com.susen36.babel.entity;

import com.susen36.babel.collectible.CollectibleLike;
import com.susen36.babel.collectible.CollectibleTiers;
import com.susen36.babel.init.BabelCollectible;
import com.susen36.babel.init.BabelItems;
import com.susen36.babel.menu.CannotMenu;
import com.susen36.babel.network.BabelNetwork;
import com.susen36.babel.network.receive.MerchantOffersMessage;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class CannotEntity extends AbstractVillager {
    private static final int MIN_OFFER_COUNT = 7;
    private static final int MAX_TRADE_USES = 12;
    private static final int PRICE_LOW_TIER = 8;
    private static final int PRICE_MID_TIER = 12;
    private static final int PRICE_HIGH_TIER = 16;

    public final AnimationState idleAnimationState = new AnimationState();
    private int despawnDelay;

    public CannotEntity(EntityType<? extends CannotEntity> type, Level level) {
        super(type, level);
        this.setInvulnerable(true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MOVEMENT_SPEED, 0.45D);
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
        }
        super.tick();
    }

    public void aiStep() {
        super.aiStep();
        if (!this.level().isClientSide) {
            this.maybeDespawn();
        }
    }

    private void maybeDespawn() {
        if (this.despawnDelay > 0 && !this.isTrading() && --this.despawnDelay == 0) {
            this.discard();
        }
    }

    public void setDespawnDelay(int delay) {
        this.despawnDelay = delay;
    }

    public int getDespawnDelay() {
        return this.despawnDelay;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("DespawnDelay", this.despawnDelay);
    }

    @Nullable
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob other) {
        return null;
    }

    public boolean showProgressBar() {
        return false;
    }

    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (this.isAlive() && !this.isTrading() && !this.isBaby()) {
            if (hand == InteractionHand.MAIN_HAND) {
                player.awardStat(Stats.TALKED_TO_VILLAGER);
            }

            if (this.getOffers().isEmpty()) {
                return InteractionResult.sidedSuccess(this.level().isClientSide);
            } else {
                if (!this.level().isClientSide) {
                    this.setTradingPlayer(player);
                    this.openTradingScreen(player, this.getDisplayName(), 1);
                }

                return InteractionResult.sidedSuccess(this.level().isClientSide);
            }
        } else {
            return super.mobInteract(player, hand);
        }
    }

    @Override
    public void openTradingScreen(Player player, Component name, int containerId) {
        if (!this.level().isClientSide) {
            NetworkHooks.openScreen((ServerPlayer) player, new SimpleMenuProvider((id, inventory, p) -> new CannotMenu(id, inventory, this, -1), name), buf -> buf.writeVarInt(this.getId()));
            AbstractContainerMenu menu = player.containerMenu;
            if (menu != null && !this.getOffers().isEmpty()) {
                BabelNetwork.sendTo(new MerchantOffersMessage(menu.containerId, this.getOffers().createTag()), (ServerPlayer) player);
            }
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
            offers.add(new MerchantOffer(new ItemStack(BabelItems.ORIGINIUM_INGOT.get(), priceForTier(collectible)), new ItemStack(entry.get()), MAX_TRADE_USES, 0, 0.0F));
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
        this.setAge(Math.max(0, this.getAge()));
    }

    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    protected void rewardTradeXp(MerchantOffer offer) {
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
