package com.susen36.babel.event;

import com.susen36.babel.api.elemental.ElementSource;
import com.susen36.babel.api.elemental.ElementType;
import com.susen36.babel.api.elemental.HurtResult;
import com.susen36.babel.api.event.*;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraftforge.common.MinecraftForge;

import javax.annotation.Nullable;

/** 负责构造并同步发布 Babel 事件，读取监听方最终给出的结果。 */
public final class BabelEventFactory {
    private BabelEventFactory() {
    }

    public static float collectibleDropChance(LootContext context, boolean guaranteed, float chance) {
        CollectibleDropEvent event = new CollectibleDropEvent(context,
                context.getParamOrNull(LootContextParams.THIS_ENTITY), context.getParamOrNull(LootContextParams.KILLER_ENTITY),
                guaranteed, chance);
        MinecraftForge.EVENT_BUS.post(event);
        return event.getChance();
    }

    public static void onUsed(Player player, Item item) {
        MinecraftForge.EVENT_BUS.post(new CollectibleEvent.Used(player, item));
    }

    public static void onRemoved(Player player, Item item) {
        MinecraftForge.EVENT_BUS.post(new CollectibleEvent.Removed(player, item));
    }

    public static void onLayerChanged(Player player, Item item, int newLayer) {
        MinecraftForge.EVENT_BUS.post(new CollectibleEvent.LayerChanged(player, item, newLayer));
    }

    public static void postDodge(LivingEntity entity, DamageSource source) {
        MinecraftForge.EVENT_BUS.post(new DodgeEvent(entity, source));
    }

    public static ElementEvent.Hurt onElementalHurt(LivingEntity entity, ElementType type, ElementSource<?> source, int amount, ElementEvent.HurtType hurtType, @Nullable LivingEntity attacker) {
        ElementEvent.Hurt event = new ElementEvent.Hurt(entity, type, source, amount, hurtType, attacker);
        MinecraftForge.EVENT_BUS.post(event);
        return event;
    }

    public static void afterElementalHurt(LivingEntity entity, ElementType type, ElementSource<?> source, HurtResult result) {
        MinecraftForge.EVENT_BUS.post(new ElementEvent.PostHurt(entity, type, source, result));
    }

    public static ElementEvent.Heal onElementalHeal(LivingEntity entity, ElementType type, ElementSource<?> source, int amount) {
        ElementEvent.Heal event = new ElementEvent.Heal(entity, type, source, amount);
        MinecraftForge.EVENT_BUS.post(event);
        return event;
    }

    public static void onElementalBurst(LivingEntity entity, ElementType type, ElementSource<?> source) {
        onElementalBurst(entity, type, source, 0);
    }

    public static float onElementalBurst(LivingEntity entity, ElementType type, ElementSource<?> source, int baseDamage) {
        ElementEvent.Burst event = new ElementEvent.Burst(entity, type, source, baseDamage);
        MinecraftForge.EVENT_BUS.post(event);
        return event.getDamageMultiplier();
    }

    public static LifePointConsumeEvent.Pre beforeLifePointConsume(Player player, DamageSource source, LifePointConsumeEvent.Type type) {
        LifePointConsumeEvent.Pre event = new LifePointConsumeEvent.Pre(player, source, type);
        MinecraftForge.EVENT_BUS.post(event);
        return event;
    }

    public static void afterLifePointConsume(Player player, DamageSource source, LifePointConsumeEvent.Type type, float amount) {
        MinecraftForge.EVENT_BUS.post(new LifePointConsumeEvent.Post(player, source, type, amount));
    }

    public static MobEffectInstance onAddEffect(LivingEntity entity, MobEffectInstance effect) {
        LivingAddEffectEvent event = new LivingAddEffectEvent(entity, effect);
        MinecraftForge.EVENT_BUS.post(event);
        return event.getEffect();
    }

    @Nullable
    public static Item beforeLostAndFound(Player player, @Nullable AbstractVillager trader, Item lost, Item gained) {
        LostAndFoundEvent.Pre event = new LostAndFoundEvent.Pre(player, trader, lost, gained);
        MinecraftForge.EVENT_BUS.post(event);
        Item result = null;
        if (!event.isCanceled()) {
            result = event.getGained();
        }
        return result;
    }

    public static void afterLostAndFound(Player player, @Nullable AbstractVillager trader, Item lost, Item gained) {
        MinecraftForge.EVENT_BUS.post(new LostAndFoundEvent.Post(player, trader, lost, gained));
    }

    public static float originiumDropMultiplier(LootContext context, float multiplier) {
        OriginiumIngotDropEvent event = new OriginiumIngotDropEvent(context, context.getParamOrNull(LootContextParams.KILLER_ENTITY), multiplier);
        MinecraftForge.EVENT_BUS.post(event);
        return event.getMultiplier();
    }

    public static double modifyEffectiveDefenseValue(LivingEntity entity, DamageSource source, float amount, double value) {
        ResistDamageEvent.Defense event = new ResistDamageEvent.Defense(entity, source, amount, value);
        MinecraftForge.EVENT_BUS.post(event);
        return event.getAttrValue();
    }

    public static double modifyEffectiveResistanceValue(LivingEntity entity, DamageSource source, float amount, double value) {
        ResistDamageEvent.MagicResistance event = new ResistDamageEvent.MagicResistance(entity, source, amount, value);
        MinecraftForge.EVENT_BUS.post(event);
        return event.getAttrValue();
    }
}