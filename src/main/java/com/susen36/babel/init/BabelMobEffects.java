package com.susen36.babel.init;

import com.susen36.babel.BabelMod;
import com.susen36.babel.effect.DizzyMobEffect;
import com.susen36.babel.effect.EssenceResistanceMobEffect;
import com.susen36.babel.effect.LessArmorMobEffect;
import com.susen36.babel.effect.NumbMobEffect;
import com.susen36.babel.effect.UnderBreakMobEffect;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class BabelMobEffects {
    public static final DeferredRegister<MobEffect> REGISTRY = DeferredRegister.create(BuiltInRegistries.MOB_EFFECT, BabelMod.MODID);

    public static final DeferredHolder<MobEffect, MobEffect> UNDER_BREAK = REGISTRY.register("under_break", UnderBreakMobEffect::new);
    public static final DeferredHolder<MobEffect, LessArmorMobEffect> LESS_ARMOR = REGISTRY.register("less_armor", LessArmorMobEffect::new);
    public static final DeferredHolder<MobEffect, MobEffect> DIZZY = REGISTRY.register("dizzy", DizzyMobEffect::new);
    public static final DeferredHolder<MobEffect, MobEffect> ESSENCE_RESISTANCE = REGISTRY.register("essence_resistance", EssenceResistanceMobEffect::new);
    public static final DeferredHolder<MobEffect, MobEffect> NUMB = REGISTRY.register("numb", NumbMobEffect::new);

    private BabelMobEffects() {
        throw new UnsupportedOperationException("Utility class");
    }
}
