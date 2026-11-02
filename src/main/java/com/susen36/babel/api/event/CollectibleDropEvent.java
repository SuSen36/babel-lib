package com.susen36.babel.api.event;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Event;

/**
 * 实体死亡结算掉落收藏品时触发，用于让其他模组调整掉落概率。
 * <p>
 * 概率设为 0 可阻止本次掉落。boss 死亡时 {@link #isGuaranteed()} 为 true，
 * 本次掉落固定从高级（ADVANCED）藏品中选取；其他模组可通过 setChance 降为 0 来阻止。
 */
public class CollectibleDropEvent extends Event {

    private final LootContext context;
    private final Entity victim;
    private final Entity killer;
    private final boolean guaranteed;
    private float chance;

    public CollectibleDropEvent(LootContext context, boolean guaranteed, float chance) {
        this.context = context;
        this.victim = context.getParamOrNull(LootContextParams.THIS_ENTITY);
        this.killer = context.getParamOrNull(LootContextParams.KILLER_ENTITY);
        this.guaranteed = guaranteed;
        this.chance = chance;
    }

    public LootContext getContext() {
        return context;
    }

    /**
     * 死亡的实体，可能为 null。
     */
    public Entity getVictim() {
        return victim;
    }

    /**
     * 击杀者，可能为 null（如环境伤害导致的死亡）。
     */
    public Entity getKiller() {
        return killer;
    }

    /**
     * 是否为 boss 必掉（本次掉落固定为高级藏品）。
     */
    public boolean isGuaranteed() {
        return guaranteed;
    }

    public float getChance() {
        return chance;
    }

    public void setChance(float chance) {
        this.chance = chance;
    }

    /**
     * 触发事件并返回最终概率。
     */
    public static float fire(LootContext context, boolean guaranteed, float chance) {
        CollectibleDropEvent event = new CollectibleDropEvent(context, guaranteed, chance);
        MinecraftForge.EVENT_BUS.post(event);
        return event.getChance();
    }
}
