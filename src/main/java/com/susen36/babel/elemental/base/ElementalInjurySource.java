package com.susen36.babel.elemental.base;

import com.susen36.babel.api.elemental.ElementSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

// 元素损伤来源描述：来源类别加可选原始对象，供事件订阅方区分直接攻击、投射物、方块、物品与指令等来源。
public record ElementalInjurySource<T>(FactorType factor, @Nullable T source) implements ElementSource<T> {

    public static <T> ElementalInjurySource<T> from(LivingEntity direct, @Nullable LivingEntity indirect) {
        return new ElementalInjurySource<>(FactorType.ENTITY_DIRECT, (T) direct);
    }

    public static <T> ElementalInjurySource<T> fromEntityIndirect(LivingEntity indirect) {
        return new ElementalInjurySource<>(FactorType.ENTITY_INDIRECT, (T) indirect);
    }

    public static <T> ElementalInjurySource<T> fromBlock(BlockState state) {
        return new ElementalInjurySource<>(FactorType.BLOCK, (T) state);
    }

    public static <T> ElementalInjurySource<T> fromItem(ItemStack stack) {
        return new ElementalInjurySource<>(FactorType.ITEM, (T) stack);
    }

    public static <T> ElementalInjurySource<T> fromVoid() {
        return new ElementalInjurySource<>(FactorType.VOID, null);
    }

    public static <T> ElementalInjurySource<T> fromNothing() {
        return new ElementalInjurySource<>(FactorType.OTHER, null);
    }

    public static ElementalInjurySource<DamageSource> fromDamageSource(DamageSource source) {
        return new ElementalInjurySource<>(FactorType.DAMAGE, source);
    }

    public static <T> ElementalInjurySource<T> fromOther(T value) {
        return new ElementalInjurySource<>(FactorType.OTHER, value);
    }

    public static <T> ElementalInjurySource<T> fromCommand(CommandSourceStack source) {
        return new ElementalInjurySource<>(FactorType.COMMAND, (T) source);
    }

    @Override
    public boolean isDirectEntity() {
        return this.factor == FactorType.ENTITY_DIRECT;
    }

    @Override
    public boolean isIndirectEntity() {
        return this.factor == FactorType.ENTITY_INDIRECT || this.factor == FactorType.ENTITY_DIRECT;
    }

    @Override
    public @Nullable Entity getAttackerEntity() {
        if (this.factor == FactorType.ENTITY_DIRECT) return (Entity) this.source;
        return null;
    }
}