package com.susen36.babel.event;

import com.susen36.babel.BabelMod;
import com.susen36.babel.api.event.*;
import com.susen36.babel.collectible.Collectibles;
import com.susen36.babel.init.BabelCollectible;
import com.susen36.babel.item.*;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;

// 具体收藏品的功能实现事件
@Mod.EventBusSubscriber(modid = BabelMod.MODID)
public class CollectiblesEvents {

    @SubscribeEvent
    public static void onOriginiumIngotDrop(@NotNull OriginiumIngotDropEvent event) {
        if (event.getKiller() instanceof Player player && Collectibles.isUsed(player, BabelCollectible.SUPREME_RING)) {
            event.setMultiplier(event.getMultiplier() * 1.5F);
        }
    }

    @SubscribeEvent
    public static void onCollectibleDrop(@NotNull CollectibleDropEvent event) {
        if (event.getKiller() instanceof Player player && Collectibles.isUsed(player, BabelCollectible.FOUR_LEAF_CLOVER_FOSSIL)) {
            event.setChance(event.getChance() * 2.0F);
        }
    }

    @SubscribeEvent
    public static void onLostAndFound(LostAndFoundEvent.Post event) {
        Player player = event.getEntity();
        if (!player.level().isClientSide() && Collectibles.isUsed(player, BabelCollectible.CANNOT_MARK)) {
            player.giveExperienceLevels(1);
        }
    }

    @SubscribeEvent
    public static void onDodge(@NotNull DodgeEvent event) {
        if (event.getEntity() instanceof Player player && !player.level().isClientSide()) {
            if (Collectibles.isUsed(player, BabelCollectible.GLORIOUS_KAZIMIERZ)) {
                GloriousKazimierzItem.startBuff(player);
            }
            if (Collectibles.isUsed(player, BabelCollectible.THE_RETURN)) {
                TheReturnItem.startBuff(player);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLifePointConsume(@NotNull LifePointConsumeEvent.Pre event) {
        if (event.getEntity() instanceof Player player && !player.level().isClientSide()
                && Collectibles.isUsed(player, BabelCollectible.END_OF_TIMES)
                && !Collectibles.isUsedUp(player, BabelCollectible.END_OF_TIMES)) {
            event.requestRevival(player.getMaxHealth(), () -> EndOfTimesItem.onRevived(player));
        }
    }

    @SubscribeEvent
    public static void onLivingAddEffectEvent(@NotNull LivingAddEffectEvent event) {
        if (event.getEntity() instanceof Player player
                && event.getEffect().getEffect().getCategory() == MobEffectCategory.BENEFICIAL
                && event.getEffect().getDuration() > 1) {
            // 多收藏品来源按乘法叠加
            float multiplier = 1.0F;
            if (Collectibles.isUsed(player, BabelCollectible.DREAMING_ESSENCE)) {
                multiplier *= 1.5F;
            }
            if (Collectibles.isUsed(player, BabelCollectible.VANILLA_SODA)) {
                multiplier *= 1.2F;
            }
            event.scaleDuration(multiplier);
        }
    }

    @SubscribeEvent
    public static void onEnemyAddEffect(@NotNull LivingAddEffectEvent event) {
        MobEffectInstance effect = event.getEffect();
        if (!(event.getEntity() instanceof Player) && effect.getDuration() > 1
                && effect.getEffect().getCategory() != MobEffectCategory.BENEFICIAL
                && TherapyTapeItem.isInRange(event.getEntity())) {
            event.scaleDuration(2.5F);
        }
    }

    @SubscribeEvent
    public static void onFloatStateExpired(@NotNull MobEffectEvent.Expired event) {
        MobEffectInstance instance = event.getEffectInstance();
        if (instance != null) {
            AncientFrescoItem.punishOnExpire(event.getEntity(), instance.getEffect());
        }
    }

    @SubscribeEvent
    public static void onNorthwindMobAttack(@NotNull LivingHurtEvent event) {
        if (event.getSource().getEntity() instanceof LivingEntity attacker && !(attacker instanceof Player)
                && NorthwindConstructItem.isInRange(attacker)) {
            NorthwindConstructItem.onMobAttack(attacker);
        }
    }
}