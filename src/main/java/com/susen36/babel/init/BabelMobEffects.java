package com.susen36.babel.init;

import com.susen36.babel.BabelMod;
import com.susen36.babel.effect.StunMobEffect;
import com.susen36.babel.effect.EssenceResistanceMobEffect;
import com.susen36.babel.effect.FeeblenessMobEffect;
import com.susen36.babel.effect.FrozenMobEffect;
import com.susen36.babel.effect.LessArmorMobEffect;
import com.susen36.babel.effect.PalsyMobEffect;
import com.susen36.babel.effect.MassLossMobEffect;
import com.susen36.babel.effect.MuteMobEffect;
import com.susen36.babel.effect.MagicDodgeMobEffect;
import com.susen36.babel.effect.PhysicalDodgeMobEffect;
import com.susen36.babel.effect.AddReachMobEffect;
import com.susen36.babel.effect.UntameConfirmMobEffect;
import com.susen36.babel.effect.WeakMagicResistanceMobEffect;
import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class BabelMobEffects {
    public static final DeferredRegister<MobEffect> REGISTRY = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, BabelMod.MODID);

    public static final RegistryObject<LessArmorMobEffect> LESS_ARMOR = REGISTRY.register("less_armor", LessArmorMobEffect::new);
    public static final RegistryObject<MobEffect> STUN = REGISTRY.register("stun", StunMobEffect::new);
    public static final RegistryObject<FrozenMobEffect> FROZEN = REGISTRY.register("frozen", FrozenMobEffect::new);
    public static final RegistryObject<MobEffect> ESSENCE_RESISTANCE = REGISTRY.register("essence_resistance", EssenceResistanceMobEffect::new);
    public static final RegistryObject<PalsyMobEffect> PALSY = REGISTRY.register("palsy", PalsyMobEffect::new);
    public static final RegistryObject<FeeblenessMobEffect> FEEBLENESS = REGISTRY.register("feebleness", FeeblenessMobEffect::new);
    public static final RegistryObject<MassLossMobEffect> MASS_LOSS = REGISTRY.register("mass_loss", MassLossMobEffect::new);
    public static final RegistryObject<AddReachMobEffect> ADD_REACH = REGISTRY.register("add_reach", AddReachMobEffect::new);
    public static final RegistryObject<WeakMagicResistanceMobEffect> WEAK_MAGIC_RESISTANCE = REGISTRY.register("weak_magic_resistance", WeakMagicResistanceMobEffect::new);
    public static final RegistryObject<MuteMobEffect> MUTE = REGISTRY.register("mute", MuteMobEffect::new);
    public static final RegistryObject<MagicDodgeMobEffect> MAGIC_DODGE = REGISTRY.register("magic_dodge", MagicDodgeMobEffect::new);
    public static final RegistryObject<PhysicalDodgeMobEffect> PHYSICAL_DODGE = REGISTRY.register("physical_dodge", PhysicalDodgeMobEffect::new);
    public static final RegistryObject<MobEffect> UNTAME_CONFIRM = REGISTRY.register("untame_confirm", UntameConfirmMobEffect::new);

    private BabelMobEffects() {
        throw new UnsupportedOperationException("Utility class");
    }
}
