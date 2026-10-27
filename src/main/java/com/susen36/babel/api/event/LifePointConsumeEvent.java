package com.susen36.babel.api.event;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.Cancelable;

/**
 * 目标生命系统在玩家死亡时扣除护盾/目标生命的事件回调。
 * <p>
 * babel 负责核心扣除与阻挡死亡逻辑,依赖模组通过该事件处理模组专属效果。
 */
public class LifePointConsumeEvent extends LivingEvent {

    private final DamageSource source;
    private final Type type;
    private final float amount;

    public LifePointConsumeEvent(Player player, DamageSource source, Type type, float amount) {
        super(player);
        this.source = source;
        this.type = type;
        this.amount = amount;
    }

    public DamageSource getSource() {
        return source;
    }

    public Type getType() {
        return type;
    }

    public float getAmount() {
        return amount;
    }

    /**
     * 扣除前触发的前置事件，可取消。模组用于跳过豁免类伤害（如绕过保护标签）的目标生命扣除。
     * <p>
     * 此时尚未决定扣除哪一类资源，因此不携带扣除信息。
     */
    @Cancelable
    public static class Pre extends LivingEvent {
        private final DamageSource source;

        public Pre(Player player, DamageSource source) {
            super(player);
            this.source = source;
        }

        public DamageSource getSource() {
            return source;
        }
    }

    /**
     * 扣除并阻挡死亡后触发的事件，用于播放音效、粒子、药水、成就等模组专属效果。
     */
    public static class Post extends LifePointConsumeEvent {
        public Post(Player player, DamageSource source, Type type, float amount) {
            super(player, source, type, amount);
        }
    }

    public static boolean firePre(Player player, DamageSource source) {
        Pre event = new Pre(player, source);
        MinecraftForge.EVENT_BUS.post(event);
        return event.isCanceled();
    }

    public static void firePost(Player player, DamageSource source, Type type, float amount) {
        Post event = new Post(player, source, type, amount);
        MinecraftForge.EVENT_BUS.post(event);
    }

    /** 实际被扣除的资源类型。 */
    public enum Type {
        SHIELD,
        LIVES
    }
}