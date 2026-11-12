package com.susen36.babel.collectible;

import com.susen36.babel.BabelMod;
import com.susen36.babel.api.event.ElementEvent;
import com.susen36.babel.api.event.LifePointConsumeEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.RegistryObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// 遍历已使用收藏品，把游戏事件转发给 CollectibleItem 声明的各钩子
@Mod.EventBusSubscriber(modid = BabelMod.MODID)
public class CollectibleItemEvents {

    @FunctionalInterface
    private interface UsedCollectibleHook {
        void invoke(CollectibleItem.CustomCollectibleItem collectible, Player player, int layer);
    }

    // 必须按注册顺序遍历：攻击/击杀钩子共用玩家随机数，遍历顺序变化会改变掷骰结果
    // 玩家死亡瞬间 Capability 可能已被注销（removed=KILLED），此时静默跳过
    private static void forEachUsedCollectible(Player player, UsedCollectibleHook hook) {
        Collectibles usedData = Collectibles.getData(player);
        if (usedData != null) {
            Collectibles.Layer layerData = Collectibles.getLayerData(player);
            for (RegistryObject<Item> holder : Collectibles.Collectibles.values()) {
                if (usedData.isUsed(holder.get()) && holder.get() instanceof CollectibleItem.CustomCollectibleItem collectible) {
                    hook.invoke(collectible, player, layerData.getLayer(holder.get()));
                }
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
                CollectibleAttributes.reconcileRange(player);
                applyRangeEffects(player);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        CollectibleAttributes.clearRangeModifiers(event.getEntity());
    }

    private static void applyRangeEffects(Player player) {
        if (!player.level().isClientSide()) {
            Collectibles usedData = Collectibles.getData(player);
            Collectibles.Layer layerData = Collectibles.getLayerData(player);
            Map<RangeKey, List<? extends LivingEntity>> rangedEntities = new HashMap<>();
            for (RegistryObject<Item> holder : Collectibles.Collectibles.values()) {
                if (usedData.isUsed(holder.get()) && holder.get() instanceof CollectibleItem.CustomCollectibleItem collectible) {
                    int layer = layerData.getLayer(holder.get());
                    for (CollectibleItem.CustomCollectibleItem.RangeBinding<?> binding : collectible.getRangeEffects()) {
                        dispatchRange(binding, rangedEntities, player, layer);
                    }
                }
            }
        }
    }

    private record RangeKey(Class<?> entityType, double range) {
    }

    private static <T extends LivingEntity> void dispatchRange(
            CollectibleItem.CustomCollectibleItem.RangeBinding<T> binding,
            Map<RangeKey, List<? extends LivingEntity>> rangedEntities,
            Player player, int layer) {
        RangeKey key = new RangeKey(binding.entityType(), binding.range());
        List<? extends LivingEntity> entities = rangedEntities.get(key);
        if (entities == null) {
            entities = player.level().getEntitiesOfClass(binding.entityType(),
                    new AABB(player.position(), player.position()).inflate(binding.range()));
            rangedEntities.put(key, entities);
        }
        for (LivingEntity living : entities) {
            if (living != player && (binding.ownerFilter() == null || binding.ownerFilter().test(living, player))) {
                binding.effect().apply(binding.entityType().cast(living), player, layer);
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

    /** 仅在损伤结算成功（未 failed）时分发；损伤爆发造成的 HP 伤害另走 LivingDamageEvent，不在此重复。 */
    @SubscribeEvent
    public static void onElementalHurtPost(ElementEvent.PostHurt event) {
        if (event.getEntity() instanceof Player player && !event.getHurtResult().failed()) {
            forEachUsedCollectible(player, (collectible, owner, layer) ->
                    collectible.onElementalHurt(event, owner, layer));
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
    public static void onLifePointLoss(LifePointConsumeEvent event) {
        // FAIL 类型代表真实死亡而非失去资源，不进入收藏品计件钩子
        if (event.getEntity() instanceof Player player && event.getType() != LifePointConsumeEvent.Type.FAIL) {
            forEachUsedCollectible(player, (collectible, owner, layer) ->
                    collectible.onLifePointLoss(event, owner, layer));
        }
    }

    // LOW 优先级保证在 LifePointEvents 的 HIGHEST 扣减之后分发，仅目标生命系统未拦截的真死会到达此处
    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onOwnerDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof Player player && !event.isCanceled()) {
            forEachUsedCollectible(player, (collectible, owner, layer) ->
                    collectible.onOwnerDeath(event, owner, layer));
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