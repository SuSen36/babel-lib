package com.susen36.babel.item;

import com.susen36.babel.BabelMod;
import com.susen36.babel.api.event.LifePointConsumeEvent;
import com.susen36.babel.collectible.CollectibleActivation;
import com.susen36.babel.collectible.CollectibleItem;
import com.susen36.babel.collectible.CollectibleTiers;
import com.susen36.babel.collectible.Collectibles;
import com.susen36.babel.init.BabelCollectible;
import com.susen36.babel.network.BabelNetwork;
import com.susen36.babel.util.LifePointUtils;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * “时光之末”：目标生命耗尽后真实死亡时原地满血复活并使收藏品失效；
 * 被目标生命/护盾阻断的死亡计入累计损失，累计达到阈值时回复失去的全部，否则回复固定量。
 */
public class EndOfTimesItem extends CollectibleItem.CustomCollectibleItem {

    // 计数存入 PERSISTED_NBT_TAG 下的 babel/end_of_times 命名空间，随玩家死亡克隆保留，重新激活时清零
    private static final String LOSS_COUNTER_TAG = "end_of_times";
    private static final String LIVES_LOST_TAG = "lives_lost";
    private static final String SHIELD_LOST_TAG = "shield_lost";

    private static final int RESTORE_ALL_LOSS_THRESHOLD = 10;
    private static final int PARTIAL_RESTORE_LIVES = 1;
    private static final int PARTIAL_RESTORE_SHIELD = 9;

    public EndOfTimesItem() {
        super(new Item.Properties().stacksTo(1), 25, false, CollectibleTiers.ADVANCED,
                new CollectibleItem.Levels(0, 1, 0),
                CollectibleActivation.builder()
                        .sound(SoundEvents.TOTEM_USE, 2F, 1F)
                        .particle(ParticleTypes.TOTEM_OF_UNDYING, 72)
                        .showOverlay(true)
                        .build());
    }

    @Override
    public void onEffectStarted(ItemStack stack, Level level, Player player, int layer) {
        resetLossCounter(player);
    }

    @Override
    public void onLifePointLoss(LifePointConsumeEvent event, Player player, int layer) {
        int lostLives = getLostLives(player);
        int lostShields = getLostShields(player);
        if (event.getType() == LifePointConsumeEvent.Type.LIVES) {
            lostLives++;
        } else {
            lostShields++;
        }
        saveLossCounter(player, lostLives, lostShields);
    }

    public static int getLostLives(Player player) {
        return getLossCounter(player).getInt(LIVES_LOST_TAG);
    }

    public static int getLostShields(Player player) {
        return getLossCounter(player).getInt(SHIELD_LOST_TAG);
    }

    private static CompoundTag getLossCounter(Player player) {
        return player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG)
                .getCompound(BabelMod.MODID).getCompound(LOSS_COUNTER_TAG);
    }

    private static void saveLossCounter(Player player, int lostLives, int lostShields) {
        CompoundTag counter = new CompoundTag();
        counter.putInt(LIVES_LOST_TAG, lostLives);
        counter.putInt(SHIELD_LOST_TAG, lostShields);
        CompoundTag persisted = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
        CompoundTag mod = persisted.getCompound(BabelMod.MODID);
        mod.put(LOSS_COUNTER_TAG, counter);
        persisted.put(BabelMod.MODID, mod);
        player.getPersistentData().put(Player.PERSISTED_NBT_TAG, persisted);
    }

    public static void resetLossCounter(Player player) {
        saveLossCounter(player, 0, 0);
    }

    public static void revive(Player player) {
        int lostLives = getLostLives(player);
        int lostShields = getLostShields(player);
        if (lostLives + lostShields >= RESTORE_ALL_LOSS_THRESHOLD) {
            LifePointUtils.addLifePoint(player, lostLives);
            LifePointUtils.addShieldPoint(player, lostShields);
        } else {
            LifePointUtils.addLifePoint(player, PARTIAL_RESTORE_LIVES);
            LifePointUtils.addShieldPoint(player, PARTIAL_RESTORE_SHIELD);
        }
        player.setHealth(player.getMaxHealth());
        resetLossCounter(player);
        double x = player.getX();
        double y = player.getY();
        double z = player.getZ();
        player.level().playSound(null, x, y, z, SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 1.0F, 1.0F);
        if (player.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, x, y + 0.95, z, 72, 0.75, 0.55, 0.75, 0.2);
        }
        if (player instanceof ServerPlayer serverPlayer) {
            BabelNetwork.syncCollectibleUse(serverPlayer, new ItemStack(BabelCollectible.END_OF_TIMES.get()));
        }
        Collectibles.deactivateAndSync(player, BabelCollectible.END_OF_TIMES.get());
    }
}
