package com.susen36.babel.api.event;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.Cancelable;

import javax.annotation.Nullable;
import java.util.Objects;

/**
 * 目标生命系统在玩家死亡时扣除护盾/目标生命的事件回调。
 * <p>
 * babel 负责核心扣除与阻挡死亡逻辑，依赖模组通过该事件处理模组专属效果。
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

    /** 扣除前的提案。取消会跳过整个 Babel 保护流程，不等于阻止原死亡事件。 */
    @Cancelable
    public static class Pre extends LivingEvent {
        private final DamageSource source;
        private Type type;
        private float amount;
        @Nullable
        private Revival revival;

        public Pre(Player player, DamageSource source) {
            this(player, source, Type.FAIL);
        }

        public Pre(Player player, DamageSource source, Type type) {
            super(player);
            this.source = source;
            if (type == Type.REVIVED) {
                throw new IllegalArgumentException("REVIVED is a result, not a resource");
            }
            this.type = Objects.requireNonNull(type);
            this.amount = type == Type.FAIL ? 0F : 1F;
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

        /** SHIELD/LIVES 请求有限正数；FAIL/0 表示跳过资源，尝试备用复活。 */
        public final void setConsumption(Type type, float amount) {
            Objects.requireNonNull(type);
            if (type == Type.REVIVED || !Float.isFinite(amount) || (type == Type.FAIL ? amount != 0F : amount <= 0F)) {
                throw new IllegalArgumentException("Invalid life point consumption: " + type + "/" + amount);
            }
            this.type = type;
            this.amount = amount;
        }

        /** 登记备用复活；首个请求获选，不立即回血或执行回调。 */
        public boolean requestRevival(float health, Runnable onRevived) {
            boolean accepted = revival == null;
            if (accepted) {
                revival = new Revival(health, onRevived);
            }
            return accepted;
        }

        @Nullable
        public Revival getRevival() {
            return revival;
        }
    }

    public record Revival(float health, Runnable onRevived) {
        public Revival {
            if (!Float.isFinite(health) || health <= 0F) {
                throw new IllegalArgumentException("Revival health must be finite and positive");
            }
            Objects.requireNonNull(onRevived);
        }
    }

    /** 只读结果：资源扣除并同步、备用复活完成，或 Babel 未能阻挡死亡。 */
    public static class Post extends LifePointConsumeEvent {
        public Post(Player player, DamageSource source, Type type, float amount) {
            super(player, source, type, amount);
        }
    }

    public enum Type {
        SHIELD,
        LIVES,
        /** Babel 未阻止死亡；其他 Forge 死亡监听器仍可处理。 */
        FAIL,
        /** 备用复活已执行，没有扣除资源。 */
        REVIVED
    }
}