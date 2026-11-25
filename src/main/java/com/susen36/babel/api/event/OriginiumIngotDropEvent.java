package com.susen36.babel.api.event;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraftforge.eventbus.api.Event;
import org.jetbrains.annotations.Nullable;

/**
 * 实体战利品结算掉落源石锭时触发，用于让其他模组调整掉落倍率。
 * <p>
 * 倍率作用于基础掉落数量 1，小数部分按概率进位：倍率 1.5 表示必定掉落 1 个、另有 50% 概率额外掉落 1 个。
 * 将倍率设为 0 可阻止本次掉落。boss 的固定掉落不经过本事件。
 */
public class OriginiumIngotDropEvent extends Event {

    private final LootContext context;
    private final @Nullable Entity killer;
    private float multiplier;

    public OriginiumIngotDropEvent(LootContext context, @Nullable Entity killer, float multiplier) {
        this.context = context;
        this.killer = killer;
        this.multiplier = Float.isFinite(multiplier) ? Mth.clamp(multiplier, 0F, Item.MAX_STACK_SIZE) : 0F;
    }

    public LootContext getContext() {
        return context;
    }

    /**
     * 击杀者，可能为 null（如环境伤害导致的死亡）。
     */
    public @Nullable Entity getKiller() {
        return killer;
    }

    public float getMultiplier() {
        return multiplier;
    }

    /** 有限值限制在 [0, 64]（一次至多一组源石锭）；NaN 和无穷值按 0 处理。 */
    public void setMultiplier(float multiplier) {
        this.multiplier = Float.isFinite(multiplier) ? Mth.clamp(multiplier, 0F, Item.MAX_STACK_SIZE) : 0F;
    }
}