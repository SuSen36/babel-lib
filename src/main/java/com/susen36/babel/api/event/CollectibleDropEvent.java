package com.susen36.babel.api.event;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraftforge.eventbus.api.Event;
import org.jetbrains.annotations.Nullable;

/**
 * 实体死亡结算掉落收藏品时触发，用于让其他模组调整掉落概率。
 * <p>
 * 概率设为 0 可阻止本次掉落。boss 死亡时 {@link #isGuaranteed()} 为 true，
 * 本次掉落固定从高级（ADVANCED）藏品中选取；其他模组可通过 setChance 降为 0 来阻止。
 */
public class CollectibleDropEvent extends Event {

    private final LootContext context;
    private final @Nullable Entity victim;
    private final @Nullable Entity killer;
    private final boolean guaranteed;
    private float chance;

    public CollectibleDropEvent(LootContext context, @Nullable Entity victim, @Nullable Entity killer, boolean guaranteed, float chance) {
        this.context = context;
        this.victim = victim;
        this.killer = killer;
        this.guaranteed = guaranteed;
        this.chance = Float.isFinite(chance) ? Mth.clamp(chance, 0F, 1F) : 0F;
    }

    public LootContext getContext() {
        return context;
    }

    /**
     * 死亡的实体，可能为 null。
     */
    public @Nullable Entity getVictim() {
        return victim;
    }

    /**
     * 击杀者，可能为 null（如环境伤害导致的死亡）。
     */
    public @Nullable Entity getKiller() {
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

    /** 有限值限制在 [0, 1]；NaN 和无穷值按 0 处理。 */
    public void setChance(float chance) {
        this.chance = Float.isFinite(chance) ? Mth.clamp(chance, 0F, 1F) : 0F;
    }
}