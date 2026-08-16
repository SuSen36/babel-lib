package com.susen36.babel.init;

import com.susen36.babel.BabelMod;
import com.susen36.babel.effect.StunMobEffect;
import com.susen36.babel.effect.EssenceResistanceMobEffect;
import com.susen36.babel.effect.FeeblenessMobEffect;
import com.susen36.babel.effect.LessArmorMobEffect;
import com.susen36.babel.effect.PalsyMobEffect;
import com.susen36.babel.effect.MassLossMobEffect;
import com.susen36.babel.effect.AddReachMobEffect;
import com.susen36.babel.effect.WeakMagicResistanceMobEffect;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class BabelMobEffects {
    public static final DeferredRegister<MobEffect> REGISTRY = DeferredRegister.create(BuiltInRegistries.MOB_EFFECT, BabelMod.MODID);

    public static final DeferredHolder<MobEffect, LessArmorMobEffect> LESS_ARMOR = REGISTRY.register("less_armor", LessArmorMobEffect::new);
    public static final DeferredHolder<MobEffect, MobEffect> STUN = REGISTRY.register("stun", StunMobEffect::new);
    public static final DeferredHolder<MobEffect, MobEffect> ESSENCE_RESISTANCE = REGISTRY.register("essence_resistance", EssenceResistanceMobEffect::new);
    public static final DeferredHolder<MobEffect, MobEffect> PALSY = REGISTRY.register("palsy", PalsyMobEffect::new);
    public static final DeferredHolder<MobEffect, FeeblenessMobEffect> FEEBLENESS = REGISTRY.register("feebleness", FeeblenessMobEffect::new);
    public static final DeferredHolder<MobEffect, MassLossMobEffect> MASS_LOSS = REGISTRY.register("mass_loss", MassLossMobEffect::new);
    public static final DeferredHolder<MobEffect, AddReachMobEffect> ADD_REACH = REGISTRY.register("add_reach", AddReachMobEffect::new);
    public static final DeferredHolder<MobEffect, WeakMagicResistanceMobEffect> WEAK_MAGIC_RESISTANCE = REGISTRY.register("weak_magic_resistance", WeakMagicResistanceMobEffect::new);

    private BabelMobEffects() {
        throw new UnsupportedOperationException("Utility class");
    }
}