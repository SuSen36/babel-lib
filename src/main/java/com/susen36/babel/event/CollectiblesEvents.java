package com.susen36.babel.event;

import com.susen36.babel.BabelMod;
import com.susen36.babel.collectible.CollectibleAttributes;
import com.susen36.babel.collectible.CollectibleItem;
import com.susen36.babel.collectible.Collectibles;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;

@Mod.EventBusSubscriber(modid = BabelMod.MODID)
public class CollectiblesEvents {

    @FunctionalInterface
    private interface UsedCollectibleHook {
        void invoke(CollectibleItem.CustomCollectibleItem collectible, Player player, int layer);
    }

    // 必须按注册顺序遍历：攻击/击杀钩子共用玩家随机数，遍历顺序变化会改变掷骰结果
    private static void forEachUsedCollectible(Player player, UsedCollectibleHook hook) {
        Collectibles usedData = Collectibles.getData(player);
        Collectibles.Layer layerData = Collectibles.getLayerData(player);
        for (RegistryObject<Item> holder : Collectibles.Collectibles.values()) {
            if (usedData.isUsed(holder.get()) && holder.get() instanceof CollectibleItem.CustomCollectibleItem collectible) {
                hook.invoke(collectible, player, layerData.getLayer(holder.get()));
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        Player player = event.player;
        if (event.phase == TickEvent.Phase.END && !player.isRemoved()) {
            if (player.tickCount % 20 == 0) {
                Collectibles.expireUsedCollectibles(player);
            }
            if (player.tickCount % 10 == 0) {
                CollectibleAttributes.reconcile(player);
            }
            forEachUsedCollectible(player, (collectible, owner, layer) -> {
                if (collectible.shouldApplyEffectTickThisTick(owner.tickCount, layer)) {
                    collectible.applyEffectTick(owner, layer);
                }
            });
            if (player.tickCount % 20 == 0) {
                applyMonsterRangeEffects(player);
            }
        }
    }

    private static void applyMonsterRangeEffects(Player player) {
        if (!player.level().isClientSide()) {
            Collectibles usedData = Collectibles.getData(player);
            Collectibles.Layer layerData = Collectibles.getLayerData(player);
            List<Monster> monsters = null;
            for (RegistryObject<Item> holder : Collectibles.Collectibles.values()) {
                if (usedData.isUsed(holder.get()) && holder.get() instanceof CollectibleItem.CustomCollectibleItem collectible
                        && collectible.hasMonsterRangeEffect()) {
                    if (monsters == null) {
                        monsters = player.level().getEntitiesOfClass(Monster.class,
                                new AABB(player.position(), player.position())
                                        .inflate(CollectibleItem.CustomCollectibleItem.MONSTER_RANGE));
                    }
                    int layer = layerData.getLayer(holder.get());
                    for (Monster monster : monsters) {
                        collectible.onMonsterInRange(monster, player, layer);
                    }
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingIncoming(LivingHurtEvent event) {
        if (event.getSource().getEntity() instanceof Player player) {
            LivingEntity target = event.getEntity();
            forEachUsedCollectible(player, (collectible, owner, layer) ->
                    collectible.onAttackHit(event, owner, target, layer));
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onLivingDamagePre(LivingDamageEvent event) {
        DamageSource source = event.getSource();
        if (source.getEntity() instanceof Player player && source.is(DamageTypes.PLAYER_ATTACK)) {
            LivingEntity target = event.getEntity();
            forEachUsedCollectible(player, (collectible, owner, layer) ->
                    collectible.onDealMeleeDamage(event, owner, target, layer));
        }
        if (source.getEntity() instanceof Player player) {
            LivingEntity target = event.getEntity();
            forEachUsedCollectible(player, (collectible, owner, layer) ->
                    collectible.onDealDamage(event, owner, target, layer));
        }
        if (event.getEntity() instanceof Player player) {
            forEachUsedCollectible(player, (collectible, owner, layer) ->
                    collectible.onMobHurt(event, owner, layer));
        }
    }

    @SubscribeEvent
    public static void onLivingHeal(LivingHealEvent event) {
        if (event.getEntity() instanceof Player player) {
            forEachUsedCollectible(player, (collectible, owner, layer) ->
                    collectible.onOwnerHeal(event, owner, layer));
        }
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (event.getSource().getEntity() instanceof Player player) {
            Entity victim = event.getEntity();
            forEachUsedCollectible(player, (collectible, owner, layer) ->
                    collectible.onPlayerKill(event, owner, victim, layer));
        }
    }

    @SubscribeEvent
    public static void onCriticalHit(CriticalHitEvent event) {
        Player player = event.getEntity();
        forEachUsedCollectible(player, (collectible, owner, layer) ->
                collectible.onCriticalHit(event, owner, layer));
    }

    @SubscribeEvent
    public static void onUseItemFinish(LivingEntityUseItemEvent.Finish event) {
        if (event.getEntity() instanceof Player player) {
            forEachUsedCollectible(player, (collectible, owner, layer) ->
                    collectible.onUseItemEnd(event, owner, layer));
        }
    }

    @SubscribeEvent
    public static void onUseItemStop(LivingEntityUseItemEvent.Stop event) {
        if (event.getEntity() instanceof Player player) {
            forEachUsedCollectible(player, (collectible, owner, layer) ->
                    collectible.onUseItemEnd(event, owner, layer));
        }
    }

    @SubscribeEvent
    public static void onLivingExpDrop(LivingExperienceDropEvent event) {
        Player player = event.getAttackingPlayer();
        if (player != null) {
            forEachUsedCollectible(player, (collectible, owner, layer) ->
                    collectible.onExperienceDrop(event, owner, layer));
        }
    }
}
