package com.susen36.babel.api.elemental;

import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

/** 本次结算的来源视图。原始对象可能为空，具体类型由来源类别决定。 */
public interface ElementSource<T> {
    enum FactorType { DAMAGE, ENTITY_DIRECT, ENTITY_INDIRECT, BLOCK, ITEM, VOID, COMMAND, OTHER }

    FactorType factor();

    @Nullable T source();

    boolean isDirectEntity();

    /** 包括直接实体来源和间接实体来源。 */
    boolean isIndirectEntity();

    /** 仅直接实体来源返回攻击实体。 */
    @Nullable Entity getAttackerEntity();
}