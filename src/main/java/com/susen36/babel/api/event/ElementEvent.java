package com.susen36.babel.api.event;

import com.susen36.babel.elemental.base.AbstractEPCapability;
import com.susen36.babel.elemental.base.ElementalInjurySource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.Cancelable;

import javax.annotation.Nullable;

/**
 * 元素损伤事件族：由 babel 在损伤结算、恢复与爆条时发布到 Forge 事件总线。
 * <p>
 * 子事件按执行阶段排列：{@link Hurt}（结算前，可取消或改数值）、{@link PostHurt}（结算后，只读）、
 * {@link Heal}（恢复前，可取消或改数值）、{@link Burst}（损伤攒满爆条时，只读）。
 */
public class ElementEvent extends LivingEvent {
    private final AbstractEPCapability.EPType type;
    private final ElementalInjurySource<?> source;

    public ElementEvent(LivingEntity entity, AbstractEPCapability.EPType type, ElementalInjurySource<?> source) {
        super(entity);
        this.type = type;
        this.source = source;
    }

    public AbstractEPCapability.EPType getType() {
        return type;
    }

    /**
     * 本次损伤的来源，携带类型与数值计算所需的上下文。
     */
    public ElementalInjurySource<?> getSource() {
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

        public Hurt(LivingEntity entity, AbstractEPCapability.EPType type, ElementalInjurySource<?> source, int amount) {
            this(entity, type, source, amount, HurtType.DEFAULT, null);
        }

        public Hurt(LivingEntity entity, AbstractEPCapability.EPType type, ElementalInjurySource<?> source, int amount, HurtType hurtType, @Nullable LivingEntity attacker) {
            super(entity, type, source);
            this.amount = amount;
            this.hurtType = hurtType;
            this.attacker = attacker;
        }

        public int getAmount() {
            return amount;
        }

        public void setAmount(int amount) {
            this.amount = amount;
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
        private final AbstractEPCapability.HurtResult result;

        public PostHurt(LivingEntity entity, AbstractEPCapability.EPType type, ElementalInjurySource<?> source, AbstractEPCapability.HurtResult result) {
            super(entity, type, source);
            this.result = result;
        }

        public AbstractEPCapability.HurtResult getHurtResult() {
            return result;
        }
    }

    /**
     * 元素损伤恢复前触发，可取消或通过 {@link #setAmount(int)} 修改恢复量。
     */
    @Cancelable
    public static class Heal extends ElementEvent {
        private int amount;

        public Heal(LivingEntity entity, AbstractEPCapability.EPType type, ElementalInjurySource<?> source, int amount) {
            super(entity, type, source);
            this.amount = amount;
        }

        public int getAmount() {
            return amount;
        }

        public void setAmount(int amount) {
            this.amount = amount;
        }
    }

    /**
     * 损伤攒满触发爆条时发布，只读；对应的负面效果与元素伤害由 babel 在此之后结算。
     */
    public static class Burst extends ElementEvent {
        public Burst(LivingEntity entity, AbstractEPCapability.EPType type, ElementalInjurySource<?> source) {
            super(entity, type, source);
        }
    }

    public static int onElementalHurt(LivingEntity entity, AbstractEPCapability.EPType type, ElementalInjurySource<?> source, int amount) {
        return onElementalHurt(entity, type, source, amount, HurtType.DEFAULT, null);
    }

    public static int onElementalHurt(LivingEntity entity, AbstractEPCapability.EPType type, ElementalInjurySource<?> source, int amount, HurtType hurtType, @Nullable LivingEntity attacker) {
        Hurt event = new Hurt(entity, type, source, amount, hurtType, attacker);
        MinecraftForge.EVENT_BUS.post(event);
        if (event.isCanceled()) return 0;
        return event.getAmount();
    }

    public static void afterElementalHurt(LivingEntity entity, AbstractEPCapability.EPType type, ElementalInjurySource<?> source, AbstractEPCapability.HurtResult result) {
        MinecraftForge.EVENT_BUS.post(new PostHurt(entity, type, source, result));
    }

    public static int onElementalHeal(LivingEntity entity, AbstractEPCapability.EPType type, ElementalInjurySource<?> source, int amount) {
        Heal event = new Heal(entity, type, source, amount);
        MinecraftForge.EVENT_BUS.post(event);
        if (event.isCanceled()) return 0;
        return event.getAmount();
    }

    public static void onElementalBurst(LivingEntity entity, AbstractEPCapability.EPType type, ElementalInjurySource<?> source) {
        MinecraftForge.EVENT_BUS.post(new Burst(entity, type, source));
    }
}