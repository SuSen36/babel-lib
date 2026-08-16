package com.susen36.babel.api.event;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingEvent;

/**
 * 目标生命系统在玩家死亡时扣除护盾/目标生命的事件回调。
 * <p>
 * babel 负责核心扣除与阻挡死亡逻辑,依赖模组通过该事件处理模组专属效果。
 */
public class HealthConsumeEvent extends LivingEvent {
    private final DamageSource source;
    private final boolean blocked;
    private final boolean shieldConsumed;
    private final boolean livesConsumed;

    protected HealthConsumeEvent(Player player, DamageSource source, boolean blocked, boolean shieldConsumed, boolean livesConsumed) {
        super(player);
        this.source = source;
        this.blocked = blocked;
        this.shieldConsumed = shieldConsumed;
        this.livesConsumed = livesConsumed;
    }

    public DamageSource getSource() {
        return source;
    }

    public boolean isBlocked() {
        return blocked;
    }

    public boolean isShieldConsumed() {
        return shieldConsumed;
    }

    public boolean isLivesConsumed() {
        return livesConsumed;
    }

    /**
     * 扣除前触发的前置事件，可取消。模组用于跳过豁免类伤害（如绕过保护标签）的目标生命扣除。
     */
    public static class Pre extends HealthConsumeEvent implements ICancellableEvent {
        public Pre(Player player, DamageSource source) {
            super(player, source, false, false, false);
        }
    }

    /**
     * 扣除并阻挡死亡后触发的事件，用于播放音效、粒子、药水、成就等模组专属效果。
     */
    public static class Post extends HealthConsumeEvent {
        public Post(Player player, DamageSource source, boolean blocked, boolean shieldConsumed, boolean livesConsumed) {
            super(player, source, blocked, shieldConsumed, livesConsumed);
        }
    }

    public static boolean firePre(Player player, DamageSource source) {
        Pre event = new Pre(player, source);
        NeoForge.EVENT_BUS.post(event);
        return event.isCanceled();
    }

    public static void firePost(Player player, DamageSource source, boolean blocked, boolean shieldConsumed, boolean livesConsumed) {
        Post event = new Post(player, source, blocked, shieldConsumed, livesConsumed);
        NeoForge.EVENT_BUS.post(event);
    }
}