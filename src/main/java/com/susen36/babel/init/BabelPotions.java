package com.susen36.babel.init;

import com.susen36.babel.BabelMod;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class BabelPotions {
    public static final DeferredRegister<Potion> REGISTRY = DeferredRegister.create(ForgeRegistries.POTIONS, BabelMod.MODID);

    public static final RegistryObject<Potion> MAGIC_DODGE = REGISTRY.register("magic_dodge", () -> new Potion(new MobEffectInstance(BabelMobEffects.MAGIC_DODGE.get(), 3600, 0)));
    public static final RegistryObject<Potion> PHYSICAL_DODGE = REGISTRY.register("physical_dodge", () -> new Potion(new MobEffectInstance(BabelMobEffects.PHYSICAL_DODGE.get(), 3600, 0)));
    public static final RegistryObject<Potion> INVISIBLE = REGISTRY.register("invisible", () -> new Potion(new MobEffectInstance(BabelMobEffects.INVISIBLE.get(), 3600, 0)));
    public static final RegistryObject<Potion> LONG_INVISIBLE = REGISTRY.register("long_invisible", () -> new Potion("invisible", new MobEffectInstance(BabelMobEffects.INVISIBLE.get(), 9600, 0)));
    public static final RegistryObject<Potion> STRONG_INVISIBLE = REGISTRY.register("strong_invisible", () -> new Potion("invisible", new MobEffectInstance(BabelMobEffects.INVISIBLE.get(), 9600, 1)));

    private BabelPotions() {
        throw new UnsupportedOperationException("Utility class");
    }
}