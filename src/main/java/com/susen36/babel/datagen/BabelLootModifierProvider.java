package com.susen36.babel.datagen;

import com.susen36.babel.BabelMod;
import com.susen36.babel.init.BabelLootModifiers;
import com.susen36.babel.loot.CollectibleLootModifier;
import com.susen36.babel.loot.OriginiumIngotLootModifier;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.data.GlobalLootModifierProvider;

public final class BabelLootModifierProvider extends GlobalLootModifierProvider {
    public BabelLootModifierProvider(PackOutput output) {
        super(output, BabelMod.MODID);
    }

    @Override
    protected void start() {
        // 两种修改器均处理箱子和实体表，不按历史 ID 将其中一个来源误过滤。
        add(BabelLootModifiers.COLLECTIBLES_IN_CHESTS.getId().getPath(), new CollectibleLootModifier(new LootItemCondition[0]));
        add(BabelLootModifiers.ORIGINIUM_INGOT_FROM_ENTITIES.getId().getPath(), new OriginiumIngotLootModifier(new LootItemCondition[0]));
    }
}