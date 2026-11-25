package com.susen36.babel.api.event;

import com.susen36.babel.api.elemental.ElementSource;
import com.susen36.babel.api.elemental.ElementType;
import com.susen36.babel.api.elemental.HurtResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.Cancelable;

import javax.annotation.Nullable;

/**
 * 元素损伤事件族：由 babel 在损伤结算、恢复与爆条时发布到 Forge 事件总线。
 * <p>
 * 子事件按执行阶段排列：{@link Hurt}（结算前，可取消或改数值）、{@link PostHurt}（结算后，只读）、
 * {@link Heal}（恢复前，可取消或改数值）、{@link Burst}（损伤攒满爆条时，可调伤害倍率）。
 */
public class ElementEvent extends LivingEvent {
    private final ElementType type;
    private final ElementSource<?> source;

    public ElementEvent(LivingEntity entity, ElementType type, ElementSource<?> source) {
        super(entity);
        this.type = type;
        this.source = source;
    }

    public ElementType getType() {
        return type;
    }

    /**
     * 本次损伤的来源，携带类型与数值计算所需的上下文。
     */
    public ElementSource<?> getSource() {
        return source;
    }

    public enum HurtType {
        BLOCK, ENTITY, POTION, FOOD, DEFAULT
    }

    /**
     * 元素损伤结算前触发，可取消或通过 {@link #setAmount(int)} 修改本次损伤值。
     */
    @Cancelable
    public static class Hurt extends ElementEvent {
        private int amount;
        private final HurtType hurtType;
        private final @Nullable LivingEntity attacker;

        public Hurt(LivingEntity entity, ElementType type, ElementSource<?> source, int amount) {
            this(entity, type, source, amount, HurtType.DEFAULT, null);
        }

        public Hurt(LivingEntity entity, ElementType type, ElementSource<?> source, int amount, HurtType hurtType, @Nullable LivingEntity attacker) {
            super(entity, type, source);
            this.amount = Math.max(0, amount);
            this.hurtType = hurtType;
            this.attacker = attacker;
        }

        public int getAmount() {
            return amount;
        }

        /** 负数按 0 处理；置零不等于取消事件。 */
        public void setAmount(int amount) {
            this.amount = Math.max(0, amount);
        }

        public HurtType getHurtType() {
            return hurtType;
        }

        public @Nullable LivingEntity getAttacker() {
            return attacker;
        }
    }

    /**
     * 元素损伤结算完成后触发，携带实际生效的损伤结果，只读。
     */
    public static class PostHurt extends ElementEvent {
        private final HurtResult result;

        public PostHurt(LivingEntity entity, ElementType type, ElementSource<?> source, HurtResult result) {
            super(entity, type, source);
            this.result = result;
        }

        public HurtResult getHurtResult() {
            return result;
        }
    }

    /**
     * 元素损伤恢复前触发，可取消或通过 {@link #setAmount(int)} 修改恢复量。
     */
    @Cancelable
    public static class Heal extends ElementEvent {
        private int amount;

        public Heal(LivingEntity entity, ElementType type, ElementSource<?> source, int amount) {
            super(entity, type, source);
            this.amount = Math.max(0, amount);
        }

        public int getAmount() {
            return amount;
        }

        /** 负数按 0 处理；置零不等于取消事件。 */
        public void setAmount(int amount) {
            this.amount = Math.max(0, amount);
        }
    }

    /**
     * 爆条结算前发布。倍率为 0 表示免除直接与周期爆条伤害，不取消状态效果。
     */
    public static class Burst extends ElementEvent {
        private final int baseDamage;
        private float damageMultiplier = 1F;

        public Burst(LivingEntity entity, ElementType type, ElementSource<?> source) {
            this(entity, type, source, 0);
        }

        public Burst(LivingEntity entity, ElementType type, ElementSource<?> source, int baseDamage) {
            super(entity, type, source);
            this.baseDamage = baseDamage;
        }

        public int getBaseDamage() {
            return baseDamage;
        }

        public float getDamageMultiplier() {
            return damageMultiplier;
        }

        public void setDamageMultiplier(float damageMultiplier) {
            if (!Float.isFinite(damageMultiplier) || damageMultiplier < 0F) {
                throw new IllegalArgumentException("Burst damage multiplier must be finite and non-negative");
            }
            this.damageMultiplier = damageMultiplier;
        }
    }
}