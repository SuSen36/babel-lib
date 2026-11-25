package com.susen36.babel.init;

import com.susen36.babel.BabelMod;
import com.susen36.babel.effect.*;
import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.MissingMappingsEvent;
import net.minecraftforge.registries.RegistryObject;

@Mod.EventBusSubscriber(modid = BabelMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class BabelMobEffects {
    public static final DeferredRegister<MobEffect> REGISTRY = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, BabelMod.MODID);

    public static final RegistryObject<LessArmorMobEffect> LESS_ARMOR = REGISTRY.register("less_armor", LessArmorMobEffect::new);
    public static final RegistryObject<StunMobEffect> STUN = REGISTRY.register("stun", StunMobEffect::new);
    public static final RegistryObject<SleepingMobEffect> SLEEP = REGISTRY.register("sleep", SleepingMobEffect::new);
    public static final RegistryObject<BindingMobEffect> BIND = REGISTRY.register("bind", BindingMobEffect::new);
    public static final RegistryObject<InvisibleMobEffect> INVISIBLE = REGISTRY.register("invisible", InvisibleMobEffect::new);
    public static final RegistryObject<FrozenMobEffect> FROZEN = REGISTRY.register("frozen", FrozenMobEffect::new);
    public static final RegistryObject<EssenceResistanceMobEffect> ESSENCE_RESISTANCE = REGISTRY.register("essence_resistance", EssenceResistanceMobEffect::new);
    public static final RegistryObject<PalsyMobEffect> PALSY = REGISTRY.register("palsy", PalsyMobEffect::new);
    public static final RegistryObject<FeeblenessMobEffect> FEEBLENESS = REGISTRY.register("feebleness", FeeblenessMobEffect::new);
    public static final RegistryObject<MassLossMobEffect> MASS_LOSS = REGISTRY.register("weightless", MassLossMobEffect::new);
    public static final RegistryObject<WeakMagicResistanceMobEffect> WEAK_MAGIC_RESISTANCE = REGISTRY.register("weak_magic_resistance", WeakMagicResistanceMobEffect::new);
    public static final RegistryObject<MagicResistanceMobEffect> MAGIC_RESISTANCE = REGISTRY.register("magic_resistance", MagicResistanceMobEffect::new);
    public static final RegistryObject<SilencedMobEffect> SILENCED = REGISTRY.register("silenced", SilencedMobEffect::new);
    public static final RegistryObject<MagicDodgeMobEffect> MAGIC_DODGE = REGISTRY.register("magic_dodge", MagicDodgeMobEffect::new);
    public static final RegistryObject<PhysicalDodgeMobEffect> PHYSICAL_DODGE = REGISTRY.register("physical_dodge", PhysicalDodgeMobEffect::new);
    public static final RegistryObject<FearMobEffect> FEAR = REGISTRY.register("fear", FearMobEffect::new);
    public static final RegistryObject<AlacrityMobEffect> ALACRITY = REGISTRY.register("alacrity", AlacrityMobEffect::new);

    @SubscribeEvent
    public static void onMissingMappings(MissingMappingsEvent event) {
        for (MissingMappingsEvent.Mapping<MobEffect> mapping : event.getMappings(ForgeRegistries.Keys.MOB_EFFECTS, BabelMod.MODID)) {
            if (mapping.getKey().getPath().equals("mass_loss")) {
                mapping.remap(MASS_LOSS.get());
            }
        }
    }

    private BabelMobEffects() {
    }
}