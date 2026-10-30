package com.susen36.babel.api.event;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Event;

/**
 * 实体战利品结算掉落源石锭时触发，用于让其他模组调整掉落倍率。
 * <p>
 * 倍率作用于基础掉落数量 1，小数部分按概率进位：倍率 1.5 表示必定掉落 1 个、另有 50% 概率额外掉落 1 个。
 * 将倍率设为 0 可阻止本次掉落。
 */
public class OriginiumIngotDropEvent extends Event {

    private final LootContext context;
    private final Entity killer;
    private float multiplier;

    public OriginiumIngotDropEvent(LootContext context, float multiplier) {
        this.context = context;
        this.killer = context.getParamOrNull(LootContextParams.KILLER_ENTITY);
        this.multiplier = multiplier;
    }

    public LootContext getContext() {
        return context;
    }

    /**
     * 击杀者，可能为 null（如环境伤害导致的死亡）。
     */
    public Entity getKiller() {
        return killer;
    }

    public float getMultiplier() {
        return multiplier;
    }

    public void setMultiplier(float multiplier) {
        this.multiplier = multiplier;
    }

    /**
     * 触发事件并返回最终倍率。
     */
    public static float fire(LootContext context, float multiplier) {
        OriginiumIngotDropEvent event = new OriginiumIngotDropEvent(context, multiplier);
        MinecraftForge.EVENT_BUS.post(event);
        return event.getMultiplier();
    }
}
